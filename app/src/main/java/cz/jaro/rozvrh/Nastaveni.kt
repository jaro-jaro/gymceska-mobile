package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.Vjec
import kotlinx.serialization.Serializable

@Serializable
data class Nastaveni(
    val darkMode: Boolean = true,
    val darkModePodleSystemu: Boolean = true,
    val mojeTrida: Vjec.TridaVjec,
    val mojeSkupiny: Set<String> = emptySet(),
    val dynamicColors: Boolean = true,
    val prepnoutRozvrhWidget: PrepnoutRozvrhWidget = PrepnoutRozvrhWidget.OPulnoci,
)
