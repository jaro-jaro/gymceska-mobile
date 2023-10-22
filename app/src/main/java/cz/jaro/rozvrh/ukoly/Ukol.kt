package cz.jaro.rozvrh.ukoly

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Ukol(
    val datum: String,
    val nazev: String,
    val predmet: String,
    @Serializable(with = UUIDSerializer::class) val id: UUID,
)

const val PRVNI_MESIC_VE_SKOLNIM_ROCE = 8

/**
 * Předpokládáme, že každý měsíc má 31 dní => některá čísla proto nelze dosáhnout a tudíž se s výsledkem nedá provádět aritmetika; Pro řazení je to ale dostačující
 */
fun Ukol.ciselnaHodnotaDatumu(): Int {
    val datum = datum.replace(" ", "").split(".")
    val den = datum.getOrNull(0)?.toIntOrNull() ?: return 0
    val mesic = datum.getOrNull(1)?.toIntOrNull() ?: return 0

    val posledniMesicVeSkolnimRoce = PRVNI_MESIC_VE_SKOLNIM_ROCE - 1
    val mesicuVNovemSkRoceDoNovehoRoku = 12 - posledniMesicVeSkolnimRoce
    val mesicOdNuly = mesic - 1
    val mesicSkolnihoRokuOdNuly = mesicOdNuly + mesicuVNovemSkRoceDoNovehoRoku
    val denOdNuly = den - 1
    val denSkolnihoRokuOdNuly = denOdNuly + 31 * mesicSkolnihoRokuOdNuly
    val dniVRoce = 12 * 31
    return denSkolnihoRokuOdNuly % dniVRoce
}

fun Ukol.zjednusit(stav: StavUkolu) = JednoduchyUkol(id = id, text = asString(), stav = stav)

operator fun Ukol.Companion.invoke() = Ukol("0. 0.", "", "", UUID.randomUUID())

fun Ukol.asString() = if (predmet.isNotBlank()) "$datum - $predmet - $nazev" else "$datum - $nazev"
