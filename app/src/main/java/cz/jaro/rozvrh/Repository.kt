package cz.jaro.rozvrh

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.content.edit
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu
import cz.jaro.rozvrh.rozvrh.Vjec
import kotlinx.coroutines.Dispatchers
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
    private val ctx: Context
) {
    private val sharedPref: SharedPreferences = ctx.getSharedPreferences("hm", Context.MODE_PRIVATE)

    var nastaveni: Nastaveni
        get() = Json.decodeFromString(sharedPref.getString("nastaveni", "{}") ?: "{}")
        set(value) {
            sharedPref.edit { putString("nastaveni", Json.encodeToString(value)) }
        }

    fun stahnoutVse(update: (String) -> Unit) {
        if (!isOnline()) return

        for (trida in Vjec.tridy) {
            for (stalost in Stalost.values()) {

                update("Stahování: ${trida.jmeno} – ${stalost.nazev}")

                val doc = Jsoup.connect(trida.odkaz?.replace("###", stalost.odkaz) ?: return).get()

                sharedPref.edit {
                    putString(
                        "rozvrh_${trida.jmeno}_${stalost.nazev}",
                        doc.toString()
                    )

                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                    putString(
                        "rozvrh_${trida.jmeno}_${stalost.nazev}_datum",
                        formatter.format(Date())
                    )
                }
            }
        }
    }

    suspend fun ziskatSkupiny(trida: Vjec.TridaVjec): Sequence<String> {
        val doc = ziskatDocument(trida, Stalost.Staly) ?: exitProcess(-1)

        return TvorbaRozvrhu.vytvoritTabulku(doc)
            .asSequence()
            .flatten()
            .filter { it.size > 1 }
            .flatten()
            .map { it.trida_skupina }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    suspend fun ziskatDocument(trida: Vjec.TridaVjec, stalost: Stalost): Document? = withContext(Dispatchers.IO) {
        if (trida.odkaz == null) return@withContext null
        if (isOnline()) {
            Jsoup.connect(trida.odkaz.replace("###", stalost.odkaz)).get().also { doc ->
                sharedPref.edit {
                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                    putString("rozvrh_${trida.jmeno}_${stalost.nazev}", doc.toString())
                    putString("rozvrh_${trida.jmeno}_${stalost.nazev}_datum", formatter.format(Date()))
                }
            }
        } else {

            val html = sharedPref.getString("rozvrh_${trida.jmeno}_${stalost.nazev}", null)
                ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                    }
                    null
                }

            html?.let { Jsoup.parse(it) }
        }
    }

    suspend fun ziskatDocument(stalost: Stalost): Document? = ziskatDocument(nastaveni.mojeTrida, stalost)

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

}
