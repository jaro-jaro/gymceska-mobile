package cz.jaro.rozvrh.ukoly

sealed interface UkolyState {

    object Nacitani : UkolyState

    data class Nacteno(
        val ukoly: List<JednoduchyUkol>,
    ) : UkolyState
}