package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.Tyden

sealed interface Result

data object TridaNeexistuje : Result

data object ZadnaData : Result

data object Error : Result

data class Uspech(
    val rozvrh: Tyden,
    val zdroj: ZdrojRozvrhu,
) : Result