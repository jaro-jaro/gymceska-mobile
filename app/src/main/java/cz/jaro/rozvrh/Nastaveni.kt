package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.Vjec

data class Nastaveni(
    val darkMode: Boolean = true,
    val darkModePodleSystemu: Boolean = true,
    val mojeTrida: Vjec.TridaVjec = Vjec.TridaVjec.E1,
    val mojeSkupiny: Set<String> = emptySet(),
    val dynamicColors: Boolean = true,
)
