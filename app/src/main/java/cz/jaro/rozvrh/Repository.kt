package cz.jaro.rozvrh

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.ukoly.Ukol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.koin.core.annotation.Single
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

@Single
class Repository(
    private val ctx: Context,
) {
    private val preferences = PreferenceDataStoreFactory.create(migrations = listOf(SharedPreferencesMigration({
        ctx.getSharedPreferences("hm", Context.MODE_PRIVATE)!!
    }))) {
        ctx.preferencesDataStoreFile("Gymceska_JARO_datastore")
    }

    object Keys {
        val NASTAVENI = stringPreferencesKey("nastaveni")
        fun rozvrh(trida: Vjec.TridaVjec, stalost: Stalost) = stringPreferencesKey("rozvrh_${trida.jmeno}_${stalost.nazev}")
        fun rozvrhDatum(trida: Vjec.TridaVjec, stalost: Stalost) = stringPreferencesKey("rozvrh_${trida.jmeno}_${stalost.nazev}_datum")
        val SKRTLE_UKOLY = stringSetPreferencesKey("skrtle_ukoly")
    }

    val nastaveni = preferences.data.map { it[Keys.NASTAVENI]?.let { it1 -> Json.decodeFromString<Nastaveni>(it1) } ?: Nastaveni() }
    suspend fun zmenitNastaveni(edit: (Nastaveni) -> Nastaveni) {
        preferences.edit {
            it[Keys.NASTAVENI] = Json.encodeToString(edit(it[Keys.NASTAVENI]?.let { it1 -> Json.decodeFromString<Nastaveni>(it1) } ?: Nastaveni()))
        }
    }

    suspend fun stahnoutVse(update: (String) -> Unit, finish: () -> Unit) {
        if (!isOnline()) return
        withContext(Dispatchers.IO) {
            Vjec.tridy.drop(1).forEach { trida ->
                Stalost.values().forEach { stalost ->
                    update("Stahování:\n${trida.jmeno} – ${stalost.nazev}")

                    val doc = Jsoup.connect(trida.odkaz?.replace("###", stalost.odkaz) ?: run {
                        update("Něco se nepovedlo :(")
                        return@withContext
                    }).get()

                    preferences.edit {
                        it[Keys.rozvrh(trida, stalost)] = doc.toString()
                        val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))
                        it[Keys.rozvrhDatum(trida, stalost)] = formatter.format(Date())
                    }
                }
            }
            finish()
        }
    }

    suspend fun ziskatSkupiny(trida: Vjec.TridaVjec): Sequence<String> {
        val doc = ziskatDocument(trida, Stalost.Staly) ?: exitProcess(-1)

        return TvorbaRozvrhu.vytvoritTabulku(doc)
            .asSequence()
            .flatten()
            .filter { it.size > 1 }
            .flatten()
            .map { it.tridaSkupina }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    suspend fun ziskatDocument(trida: Vjec.TridaVjec, stalost: Stalost): Document? = withContext(Dispatchers.IO) {
        if (trida.odkaz == null) return@withContext null
        if (isOnline()) {
            Jsoup.connect(trida.odkaz.replace("###", stalost.odkaz)).get().also { doc ->
                preferences.edit {
                    it[Keys.rozvrh(trida, stalost)] = doc.toString()
                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))
                    it[Keys.rozvrhDatum(trida, stalost)] = formatter.format(Date())
                }
            }
        } else {

            val html = preferences.data.first()[Keys.rozvrh(trida, stalost)]
                ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                    }
                    null
                }

            html?.let { Jsoup.parse(it) }
        }
    }

    suspend fun ziskatDocument(stalost: Stalost): Document? = ziskatDocument(nastaveni.first().mojeTrida, stalost)

    fun isOnline(): Boolean = ctx.isOnline()

    companion object {
        fun Context.isOnline(): Boolean {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false

            return capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            )
        }
    }

    val skrtleUkoly = preferences.data.map { it[Keys.SKRTLE_UKOLY] ?: emptySet() }

    suspend fun upravitSkrtleUkoly(edit: (Set<String>) -> Set<String>) {
        preferences.edit {
            it[Keys.SKRTLE_UKOLY] = edit(it[Keys.SKRTLE_UKOLY] ?: emptySet())
        }
    }

    private val firebase = Firebase
    private val database = firebase.database("https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/")

    private val _ukoly = MutableStateFlow(null as List<Ukol>?)
    val ukoly = _ukoly.asStateFlow()

    private val ukolyRef = database.getReference("ukoly5")

    init {
        ukolyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ukoly = snapshot.getValue(object : GenericTypeIndicator<List<Map<String, String>>?>() {})
                _ukoly.value = ukoly?.mapNotNull {
                    Ukol(
                        datum = it["datum"] ?: return@mapNotNull null,
                        nazev = it["nazev"] ?: return@mapNotNull null,
                        predmet = it["predmet"] ?: return@mapNotNull null
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })
    }
}
