package cz.jaro.rozvrh.suplovani

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Zmeny(
    val nepritomneTridy: Map<Trida, Map<Hodina, String>>,
    val nepritomniUcitele: Map<JmenoVyucujiciho, Map<Hodina, String>>,
    val mistnostiMimoProvoz: Map<Mistnost, Map<Hodina, String>>,
    val zmenyVRozvrzichUcitelu: Map<JmenoVyucujiciho, List<ZmenaVyucujiciho>>,
    val zmenyVRozvrzichTrid: Map<Trida, List<ZmenaTridy>>,
    val pedagogickyDohledUTrid: Map<Trida, List<Pair<List<Hodina>, List<JmenoVyucujiciho>>>>,
    val poznamky: List<String>
)

typealias Hodina = Int
typealias Vyucujici = String
typealias JmenoVyucujiciho = String
typealias Mistnost = String
typealias Skupina = String
typealias Trida = String
typealias TridaSkupina = String
typealias Predmet = String

@Serializable
sealed interface ZmenaTridy {
    @Serializable
    @SerialName("Absence")
    data class Absence(
        val hodiny: List<Hodina>,
        val typ: String,
        val skupina: Skupina?,
    ) : ZmenaTridy

    @Serializable
    @SerialName("PresunZ")
    data class PresunZ(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        @Serializable(with = LocalDateSerializer::class)
        val naDatum: LocalDate?,
        val naHodinu: Hodina,
    ) : ZmenaTridy

    @Serializable
    @SerialName("PresunNa")
    data class PresunNa(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        val vyucujici: Vyucujici,
        val mistnost: Mistnost,
        @Serializable(with = LocalDateSerializer::class)
        val zDatumu: LocalDate?,
        val zHodiny: Hodina,
    ) : ZmenaTridy

    @Serializable
    @SerialName("Odpada")
    data class Odpada(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        val misto: Vyucujici,
    ) : ZmenaTridy

    @Serializable
    @SerialName("Spoji")
    data class Spoji(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        val mistnost: Mistnost,
        val vyucujici: Vyucujici,
        val misto: Vyucujici,
    ) : ZmenaTridy

    @Serializable
    @SerialName("Zmena")
    data class Zmena(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        val mistnost: Mistnost,
        val vyucujici: Vyucujici,
    ) : ZmenaTridy

    @Serializable
    @SerialName("Supluje")
    data class Supluje(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        val mistnost: Mistnost,
        val vyucujici: Vyucujici,
        val misto: Vyucujici,
    ) : ZmenaTridy

    @Serializable
    @SerialName("Navic")
    data class Navic(
        val hodina: Hodina,
        val predmet: Predmet,
        val skupina: Skupina?,
        val mistnost: Mistnost,
        val vyucujici: Vyucujici,
    ) : ZmenaTridy
}

data class UniverzalniZmenaTridy(
    val hodiny: List<Hodina>,
    val typ: String,
    val skupina: Skupina,
    val predmet: Predmet,
    val vyucujici: Vyucujici,
    val mistnost: Mistnost,
    val poznamka: String,
)

@Serializable
sealed interface ZmenaVyucujiciho {
    @Serializable
    @SerialName("Absence")
    data class Absence(
        val hodiny: List<Hodina>,
        val typ: String,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("PresunZ")
    data class PresunZ(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        @Serializable(with = LocalDateSerializer::class)
        val naDatum: LocalDate?,
        val naHodinu: Hodina,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("PresunNa")
    data class PresunNa(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        val mistnost: Mistnost,
        @Serializable(with = LocalDateSerializer::class)
        val zDatumu: LocalDate?,
        val zHodiny: Hodina,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("Odpada")
    data class Odpada(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        val poznamka: String?,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("Spojeno")
    data class Spojeno(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        val mistnost: Mistnost,
        val misto: Vyucujici,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("Zmena")
    data class Zmena(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        val mistnost: Mistnost,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("ZmenaPlus")
    data class ZmenaPlus(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("Supluje")
    data class Supluje(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        val mistnost: Mistnost,
        val misto: Vyucujici,
    ) : ZmenaVyucujiciho

    @Serializable
    @SerialName("Navic")
    data class Navic(
        val hodina: Hodina,
        val predmet: Predmet,
        val tridaSkupina: TridaSkupina,
        val mistnost: Mistnost,
    ) : ZmenaVyucujiciho
}

data class UniverzalniZmenaVyucujiciho(
    val hodiny: List<Hodina>,
    val typ: String,
    val tridaSkupina: TridaSkupina,
    val predmet: Predmet,
    val mistnost: Mistnost,
    val poznamka: String,
)

fun ZmenaTridy.toUniverzalniZmenaTridy() = when (this) {
    is ZmenaTridy.Absence -> UniverzalniZmenaTridy(hodiny, "!", skupinaHezky, "", "", "", typ)
    is ZmenaTridy.PresunZ -> UniverzalniZmenaTridy(hodiny, ">>", skupinaHezky, predmet, "", "", ">> $naHezky")
    is ZmenaTridy.PresunNa -> UniverzalniZmenaTridy(hodiny, "<<", skupinaHezky, predmet, vyucujici, mistnost, "<< $zHezky")
    is ZmenaTridy.Odpada -> UniverzalniZmenaTridy(hodiny, "x", skupinaHezky, predmet, "", "", "($misto)")
    is ZmenaTridy.Spoji -> UniverzalniZmenaTridy(hodiny, "><", skupinaHezky, predmet, vyucujici, mistnost, "($misto)")
    is ZmenaTridy.Zmena -> UniverzalniZmenaTridy(hodiny, "~", skupinaHezky, predmet, vyucujici, mistnost, "")
    is ZmenaTridy.Supluje -> UniverzalniZmenaTridy(hodiny, "x>", skupinaHezky, predmet, vyucujici, mistnost, "($misto)")
    is ZmenaTridy.Navic -> UniverzalniZmenaTridy(hodiny, "+", skupinaHezky, predmet, vyucujici, mistnost, "")
}

fun ZmenaVyucujiciho.toUniverzalniZmenaVyucujiciho() = when (this) {
    is ZmenaVyucujiciho.Absence -> UniverzalniZmenaVyucujiciho(hodiny, "!", tridaSkupina, "", "", typ)
    is ZmenaVyucujiciho.PresunZ -> UniverzalniZmenaVyucujiciho(hodiny, ">>", tridaSkupina, predmet, "", ">> $naHezky")
    is ZmenaVyucujiciho.PresunNa -> UniverzalniZmenaVyucujiciho(hodiny, "<<", tridaSkupina, predmet, mistnost, "<< $zHezky")
    is ZmenaVyucujiciho.Odpada -> UniverzalniZmenaVyucujiciho(hodiny, "x", tridaSkupina, predmet, "", poznamka.orEmpty())
    is ZmenaVyucujiciho.Spojeno -> UniverzalniZmenaVyucujiciho(hodiny, "><", tridaSkupina, predmet, mistnost, "($misto)")
    is ZmenaVyucujiciho.Zmena -> UniverzalniZmenaVyucujiciho(hodiny, "~", tridaSkupina, predmet, mistnost, "")
    is ZmenaVyucujiciho.ZmenaPlus -> UniverzalniZmenaVyucujiciho(hodiny, "x!", tridaSkupina, predmet, "", "")
    is ZmenaVyucujiciho.Supluje -> UniverzalniZmenaVyucujiciho(hodiny, "x>", tridaSkupina, predmet, mistnost, "($misto)")
    is ZmenaVyucujiciho.Navic -> UniverzalniZmenaVyucujiciho(hodiny, "+", tridaSkupina, predmet, mistnost, "")
}

val ZmenaTridy.hodiny
    get() = when (this) {
        is ZmenaTridy.PresunZ -> listOf(hodina)
        is ZmenaTridy.PresunNa -> listOf(hodina)
        is ZmenaTridy.Odpada -> listOf(hodina)
        is ZmenaTridy.Spoji -> listOf(hodina)
        is ZmenaTridy.Zmena -> listOf(hodina)
        is ZmenaTridy.Supluje -> listOf(hodina)
        is ZmenaTridy.Navic -> listOf(hodina)
        is ZmenaTridy.Absence -> hodiny
    }

val ZmenaVyucujiciho.hodiny
    get() = when (this) {
        is ZmenaVyucujiciho.PresunZ -> listOf(hodina)
        is ZmenaVyucujiciho.PresunNa -> listOf(hodina)
        is ZmenaVyucujiciho.Odpada -> listOf(hodina)
        is ZmenaVyucujiciho.Spojeno -> listOf(hodina)
        is ZmenaVyucujiciho.Zmena -> listOf(hodina)
        is ZmenaVyucujiciho.ZmenaPlus -> listOf(hodina)
        is ZmenaVyucujiciho.Supluje -> listOf(hodina)
        is ZmenaVyucujiciho.Navic -> listOf(hodina)
        is ZmenaVyucujiciho.Absence -> hodiny
    }

val ZmenaTridy.skupina
    get() = when (this) {
        is ZmenaTridy.PresunZ -> skupina
        is ZmenaTridy.PresunNa -> skupina
        is ZmenaTridy.Odpada -> skupina
        is ZmenaTridy.Spoji -> skupina
        is ZmenaTridy.Zmena -> skupina
        is ZmenaTridy.Supluje -> skupina
        is ZmenaTridy.Navic -> skupina
        is ZmenaTridy.Absence -> skupina
    }

val ZmenaVyucujiciho.tridaSkupina
    get() = when (this) {
        is ZmenaVyucujiciho.PresunZ -> tridaSkupina
        is ZmenaVyucujiciho.PresunNa -> tridaSkupina
        is ZmenaVyucujiciho.Odpada -> tridaSkupina
        is ZmenaVyucujiciho.Spojeno -> tridaSkupina
        is ZmenaVyucujiciho.Zmena -> tridaSkupina
        is ZmenaVyucujiciho.ZmenaPlus -> tridaSkupina
        is ZmenaVyucujiciho.Supluje -> tridaSkupina
        is ZmenaVyucujiciho.Navic -> tridaSkupina
        is ZmenaVyucujiciho.Absence -> ""
    }

fun Collection<Hodina>.hodinyHezky() = sorted().detectProgressions().joinToString()

fun List<Int>.detectProgressions(): List<String> {
    if (isEmpty()) return emptyList()

    var last: Int = first()
    var start: Int? = null
    val result = mutableListOf<String>()
    drop(1).forEach {
        if (start == null) {
            if (it - last == 1) start = last
            else result += "$last"
        } else if (it - last != 1) {
            result += "$start-$last"
            start = null
        }
        last = it
    }
    result += if (start != null) "$start-$last" else "$last"
    return result
}

val ZmenaTridy.PresunNa.zHezky get() = (if (zDatumu == null) "" else "(${zDatumu.dayOfMonth}. ${zDatumu.monthValue}.) ") + "$zHodiny. hod."
val ZmenaTridy.PresunZ.naHezky get() = (if (naDatum == null) "" else "(${naDatum.dayOfMonth}. ${naDatum.monthValue}.) ") + "$naHodinu. hod."
val ZmenaVyucujiciho.PresunNa.zHezky get() = (if (zDatumu == null) "" else "(${zDatumu.dayOfMonth}. ${zDatumu.monthValue}.) ") + "$zHodiny. hod."
val ZmenaVyucujiciho.PresunZ.naHezky get() = (if (naDatum == null) "" else "(${naDatum.dayOfMonth}. ${naDatum.monthValue}.) ") + "$naHodinu. hod."

val ZmenaTridy.skupinaHezky get() = if (skupina.isNullOrBlank()) "" else "$skupina"
