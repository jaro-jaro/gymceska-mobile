package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.Tyden
import cz.jaro.rozvrh.suplovani.Zmeny
import java.time.LocalDate

sealed interface Result {
    data object TridaNeexistuje : Result

    data object ZadnaData : Result

    data object Error : Result

    data class Uspech(
        val rozvrh: Tyden,
        val zdroj: ZdrojDat,
    ) : Result
}

sealed interface Result2 {
    data object ZadnaData : Result2

    data object Error : Result2

    data class Uspech(
        val datum: LocalDate,
        val zmeny: Zmeny,
        val zdroj: ZdrojDat,
    ) : Result2
}