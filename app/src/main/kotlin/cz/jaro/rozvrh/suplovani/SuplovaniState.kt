package cz.jaro.rozvrh.suplovani

import cz.jaro.rozvrh.Result2
import java.time.LocalDate

sealed interface SuplovaniState {

    data object Nacitani : SuplovaniState

    data class DatumNevybran(
        val podporovanaData: List<LocalDate>,
    ) : SuplovaniState

    data class NacitaniDat(
        val datum: LocalDate,
        val podporovanaData: List<LocalDate>,
    ) : SuplovaniState

    data class Chyba(
        val datum: LocalDate,
        val podporovanaData: List<LocalDate>,
        val chyba: Result2,
    ) : SuplovaniState

    data class Data(
        val datum: LocalDate,
        val suplovani: Result2.Uspech,
        val podporovanaData: List<LocalDate>,
    ) : SuplovaniState
}