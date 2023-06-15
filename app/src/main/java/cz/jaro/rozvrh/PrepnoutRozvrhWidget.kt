package cz.jaro.rozvrh

import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
sealed interface PrepnoutRozvrhWidget {
    @Serializable
    object OPulnoci : PrepnoutRozvrhWidget

    @Serializable
    data class PoKonciVyucovani(
        val poHodin: Int,
    ) : PrepnoutRozvrhWidget

    @Serializable
    data class VCas(
        val hodin: Int,
        val minut: Int,
    ) : PrepnoutRozvrhWidget {
        fun toLocalTime() = LocalTime.of(hodin, minut)
    }
}
