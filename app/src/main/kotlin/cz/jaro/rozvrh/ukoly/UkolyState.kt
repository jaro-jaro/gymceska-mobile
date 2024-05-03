package cz.jaro.rozvrh.ukoly

sealed interface UkolyState {

    data object Nacitani : UkolyState

    data class Nacteno(
        val ukoly: List<JednoduchyUkol>,
    ) : UkolyState
}