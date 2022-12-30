package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.OznameniState
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
}
