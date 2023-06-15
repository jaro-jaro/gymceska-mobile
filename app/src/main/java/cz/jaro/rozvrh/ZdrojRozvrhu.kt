package cz.jaro.rozvrh

import java.time.LocalDateTime

sealed interface ZdrojRozvrhu

object Online : ZdrojRozvrhu

data class Offline(
    val ziskano: LocalDateTime,
) : ZdrojRozvrhu