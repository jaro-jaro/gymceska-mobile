package cz.jaro.rozvrh

import java.time.LocalDateTime

sealed interface ZdrojDat

data object Online : ZdrojDat

data class Offline(
    val ziskano: LocalDateTime,
) : ZdrojDat

data class OfflineRuzneCasti(
    val nejstarsi: LocalDateTime,
) : ZdrojDat