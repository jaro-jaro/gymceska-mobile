package cz.jaro.rozvrh

import java.time.LocalDateTime

sealed interface ZdrojRozvrhu

data object Online : ZdrojRozvrhu

data class Offline(
    val ziskano: LocalDateTime,
) : ZdrojRozvrhu

data class OfflineRuzneCasti(
    val nejstarsi: LocalDateTime,
) : ZdrojRozvrhu