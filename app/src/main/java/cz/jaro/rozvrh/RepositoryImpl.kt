package cz.jaro.rozvrh

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.content.edit
import cz.jaro.rozvrh.rozvrh.OznameniState
import cz.jaro.rozvrh.rozvrh.Seznamy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RepositoryImpl(val ctx: Context) : Repository {
    val sharedPref = ctx.getSharedPreferences("hm", Context.MODE_PRIVATE)

    override var mojeTrida: String
        get() = sharedPref.getString("sva_trida", "5.E")!!
        set(value) {
            sharedPref.edit { putString("sva_trida", value) }
        }

    override var indexMojiTridy: Int
        get() = sharedPref.getInt("sva_trida_index", 13)
        set(value) {
            sharedPref.edit { putInt("sva_trida_index", value) }
        }

    override var mojeSkupiny: List<String>
        get() = sharedPref.getStringSet("sve_skupiny", setOf())!!.toList()
        set(value) {
            sharedPref.edit { putStringSet("sve_skupiny", value.toSet()) }
        }

    override var darkMode: Boolean
        get() = sharedPref.getBoolean("DM", false)
        set(value) {
            sharedPref.edit { putBoolean("DM", value) }
        }

    override var oznameni: OznameniState
        get() = Json.decodeFromString(sharedPref.getString("oznameniRozvrh", "{}") ?: "{}")
        set(value) {
            sharedPref.edit { putString("oznameniRozvrh", Json.encodeToString(value)) }
        }


    override suspend fun ziskatDocument(trida: String, stalost: String): Document? = withContext(Dispatchers.IO) {
        if (isOnline()) {
            val odkaz = Seznamy.tridyOdkazy[Seznamy.tridy.indexOf(trida) - 1]
            Jsoup.connect(odkaz.replace("###", stalost)).get().also { doc ->
                sharedPref.edit {
                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                    putString("rozvrh_${trida}_$stalost", doc.toString())
                    putString("rozvrh_${trida}_${stalost}_datum", formatter.format(Date()))
                }
            }
        } else {

            val html = sharedPref.getString("rozvrh_${trida}_$stalost", null)
                ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                    }
                    null
                }

            html?.let { Jsoup.parse(it) }
        }
    }

    override suspend fun ziskatDocument(stalost: String): Document? = withContext(Dispatchers.IO) {
        if (isOnline()) {
            val odkaz = Seznamy.tridyOdkazy[indexMojiTridy - 1]
            return@withContext Jsoup.connect(odkaz.replace("###", stalost)).get().also { doc ->
                sharedPref.edit {
                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                    putString("rozvrh_${mojeTrida}_$stalost", doc.toString())
                    putString("rozvrh_${mojeTrida}_${stalost}_datum", formatter.format(Date()))
                }
            }
        } else {

            val html = sharedPref.getString("rozvrh_${mojeTrida}_$stalost", null)
                ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                    }
                    null
                }

            return@withContext html?.let { Jsoup.parse(it) }
        }
    }

    override fun isOnline(): Boolean {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
