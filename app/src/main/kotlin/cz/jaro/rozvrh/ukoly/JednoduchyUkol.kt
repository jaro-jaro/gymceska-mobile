package cz.jaro.rozvrh.ukoly

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class JednoduchyUkol(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val text: String,
    val stav: StavUkolu,
)
