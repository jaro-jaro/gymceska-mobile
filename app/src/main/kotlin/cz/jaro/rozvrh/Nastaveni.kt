package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.ui.theme.Theme
import kotlinx.serialization.Serializable

@Serializable
data class Nastaveni(
    val darkMode: Boolean = true,
    val darkModePodleSystemu: Boolean = true,
    val tema: Theme = Theme.Blue,
    val mojeTrida: Vjec.TridaVjec,
    val mojeSkupiny: Set<String> = emptySet(),
    val dynamicColors: Boolean = true,
    val prepnoutRozvrhWidget: PrepnoutRozvrhWidget = PrepnoutRozvrhWidget.OPulnoci,
    val defaultMujRozvrh: Boolean = false,
    val stahovatHned: Boolean = false,
)
