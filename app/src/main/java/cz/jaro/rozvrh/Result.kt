package cz.jaro.rozvrh

import org.jsoup.nodes.Document

sealed interface Result

object TridaNeexistuje : Result

object ZadnaData : Result

data class Uspech(
    val document: Document,
    val zdroj: ZdrojRozvrhu,
) : Result