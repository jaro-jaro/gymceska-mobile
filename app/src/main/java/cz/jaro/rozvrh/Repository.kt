package cz.jaro.rozvrh

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import cz.jaro.rozvrh.rozvrh.oznameni.OznameniState
import org.jsoup.nodes.Document

interface Repository {

    var mojeTrida: String
    var indexMojiTridy: Int
    var mojeSkupiny: List<String>

    var darkMode: Boolean

    var oznameni: OznameniState

    suspend fun ziskatDocument(trida: String, stalost: String): Document?
    suspend fun ziskatDocument(stalost: String): Document?

    fun isOnline(): Boolean

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

    var poprve: Boolean
}
