package cz.jaro.rozvrh.ukoly

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Ukol(
    val datum: String,
    val nazev: String,
    val predmet: String,
    val skupina: String,
    val id: @Serializable(with = MyUuidSerializer::class) Uuid,
)

const val PRVNI_MESIC_VE_SKOLNIM_ROCE = 8

fun dateFromUkol(ukol: Ukol) = dateFromString(ukol.datum)

fun dateFromString(dateString: String): LocalDate? {
    val today = today()
    if (dateString == "0. 0.") return today

    val datum = dateString.replace(" ", "").split(".")
    val den = datum.getOrNull(0)?.toIntOrNull() ?: return null
    val mesic = datum.getOrNull(1)?.toIntOrNull() ?: return null

    val tentoMesic = today.monthNumber
    val tentoRok = today.year

    val jePredVanoci = tentoMesic >= PRVNI_MESIC_VE_SKOLNIM_ROCE
    val ukolJePredVanoci = mesic >= PRVNI_MESIC_VE_SKOLNIM_ROCE

    val rok = when {
        jePredVanoci == ukolJePredVanoci -> tentoRok
        ukolJePredVanoci -> tentoRok - 1
        else -> tentoRok + 1
    }

    return LocalDate(rok, mesic, den)
}

@OptIn(ExperimentalUuidApi::class)
fun Ukol.zjednusit(stav: StavUkolu) = JednoduchyUkol(id = id, text = asString(), stav = stav)

@OptIn(ExperimentalUuidApi::class)
operator fun Ukol.Companion.invoke() = Ukol("0. 0.", "", "", "", Uuid.random())

fun Ukol.asString() = when {
    predmet.isNotBlank() && skupina.isNotBlank() -> "$datum – $predmet $skupina – $nazev"
    predmet.isNotBlank() && skupina.isBlank() -> "$datum – $predmet – $nazev"
    predmet.isBlank() && skupina.isNotBlank() -> "$datum – $skupina – $nazev"
    else -> "$datum – $nazev"
}

fun today() = now().date
fun time() = now().time
fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
