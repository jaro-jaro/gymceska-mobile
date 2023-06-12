package cz.jaro.rozvrh.ukoly

import java.util.UUID

data class JednoduchyUkol(
    val id: UUID,
    val text: String,
    val stav: StavUkolu,
)
