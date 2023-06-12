package cz.jaro.rozvrh.ukoly

import java.util.UUID

data class Ukol(
    val datum: String,
    val nazev: String,
    val predmet: String,
    val id: UUID,
) {
    fun asString() = "$datum - $predmet - $nazev"
    fun zjednusit(stav: StavUkolu) = JednoduchyUkol(id = id, text = asString(), stav = stav)

    companion object {
        fun new() = Ukol("0. 0.", "", "", UUID.randomUUID())
    }
}
