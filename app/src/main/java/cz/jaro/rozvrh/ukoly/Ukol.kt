package cz.jaro.rozvrh.ukoly

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Ukol(
    val datum: String,
    val nazev: String,
    val predmet: String,
    @Serializable(with = UUIDSerializer::class) val id: UUID,
) {
    fun asString() = "$datum - $predmet - $nazev"
    fun zjednusit(stav: StavUkolu) = JednoduchyUkol(id = id, text = asString(), stav = stav)

    companion object {
        fun new() = Ukol("0. 0.", "", "", UUID.randomUUID())
    }
}
