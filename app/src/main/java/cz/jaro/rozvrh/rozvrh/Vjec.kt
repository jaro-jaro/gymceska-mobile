package cz.jaro.rozvrh.rozvrh

import kotlinx.serialization.Serializable

@Serializable
sealed interface Vjec {
    val jmeno: String
    val zkratka: String

    @Serializable
    data class TridaVjec(
        override val jmeno: String,
        val odkaz: String? = null,
    ) : Vjec {
        override val zkratka: String get() = jmeno
    }

    @Serializable
    data class MistnostVjec(
        override val jmeno: String,
        val napoveda: String? = null,
    ) : Vjec {
        override val zkratka: String get() = jmeno
    }

    @Serializable
    data class VyucujiciVjec(
        override val jmeno: String,
        override val zkratka: String,
    ) : Vjec

    sealed interface Indexed : Vjec {
        val index: Int
    }

    @Serializable
    data class DenVjec(
        override val jmeno: String,
        override val zkratka: String,
        override val index: Int,
    ) : Indexed

    @Serializable
    data class HodinaVjec(
        override val jmeno: String,
        override val zkratka: String,
        override val index: Int,
    ) : Indexed
}
