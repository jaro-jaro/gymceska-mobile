package cz.jaro.rozvrh.suplovani

import android.content.Context
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.KonsumerDsl
import com.gitlab.mvysny.konsumexml.anyName
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import cz.jaro.rozvrh.Offline
import cz.jaro.rozvrh.Online
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Repository.Companion.isOnline
import cz.jaro.rozvrh.Result2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import org.koin.core.annotation.Single
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Single(createdAtStart = true)
class SuplovaniRepository(
    private val ctx: Context,
    private val suplovaniApi: SuplovaniApi,
    private val preferences: DataStore<Preferences>
) {
    private val scope = MainScope()

    private val keys = MutableStateFlow(null as Keys?)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val headers = mapOf(
        "origin" to "https://gymceska.bakalari.cz",
        "referer" to "https://gymceska.bakalari.cz/next/zmeny.aspx",
        "Accept-Language" to "en-US,en",
    )

    private suspend fun getResponseForToday(): Response<ResponseBody>? = withContext(Dispatchers.IO) {
        val response = try {
            suplovaniApi.zmenyGet(
                headers = headers
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
            return@withContext null
        }
        println(response.code())

        response
    }

    private suspend fun getResponseForOtherDate(
        datum: LocalDate,
    ) = withContext(Dispatchers.IO) {
        val keys = keys.getOrPut { getResponseForToday()?.processKeys() } ?: return@withContext null

        val call = try {
            suplovaniApi.zmeny(
                headers = headers,
                eventValidation = keys.eventValidation,
                viewState = keys.viewState,
                dateEditState = """{"rawValue":"${json.encodeToString(LocalDateSerializer(), datum)}"}""",
                dateEdit = "",
                filterDropDownVI = 0,
                filterDropDown = ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
            return@withContext null
        }
        val response = call.awaitResponse()

        println(response.code())

        response
    }

    suspend fun suplovani(datum: LocalDate): Result2 {
        // todo nejde do budoucna moc
        if (ctx.isOnline() && !pouzitOfflineSuplovani(datum)) try {
            val zmeny = (
                    if (datum == LocalDate.now()) getResponseForToday() else getResponseForOtherDate(datum)
                    )?.processResponse() ?: throw IOException()

            preferences.edit {
                it[Repository.Keys.suplovani(datum)] = Json.encodeToString(zmeny)
                it[Repository.Keys.suplovaniPosledni(datum)] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()
            }

            return Result2.Uspech(datum, zmeny, Online)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val kdy = preferences.data.first()[Repository.Keys.suplovaniPosledni(datum)]?.let { LocalDateTime.parse(it) }
            ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                }
                return Result2.ZadnaData
            }

        val zmeny = preferences.data.first()[Repository.Keys.suplovani(datum)]?.let { Json.decodeFromString<Zmeny>(it) }
            ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                }
                return Result2.ZadnaData
            }

        return try {
            Result2.Uspech(datum, zmeny, Offline(kdy))
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
            Result2.Error
        }
    }

    private suspend fun pouzitOfflineSuplovani(datum: LocalDate): Boolean {
        val limit = 2
        val posledni = preferences.data.first()[Repository.Keys.suplovaniPosledni(datum)]?.let { LocalDateTime.parse(it) } ?: return false
        val staryHodin = posledni.until(LocalDateTime.now(), ChronoUnit.HOURS)
        return staryHodin < limit
    }
}

private fun Response<ResponseBody>.processKeys() = this
    .takeIf { code() == 200 }
    ?.body()
    ?.string()
    ?.let { html ->
        Keys(
            eventValidation = Regex("^[\\d\\D]*id=\"__EVENTVALIDATION\" value=\"([^\"]*)\"[\\d\\D]*$")
                .matchEntire(html)?.groupValues?.get(1)/*?.encode()*/ ?: return null,
            viewState = Regex("^[\\d\\D]*id=\"__VIEWSTATE\" value=\"([^\"]*)\"[\\d\\D]*$")
                .matchEntire(html)?.groupValues?.get(1)/*?.encode()*/ ?: return null,
        )
    }

private fun Response<ResponseBody>.processResponse() = this
    .takeIf { code() == 200 }
    ?.body()
    ?.string()
    ?.ifBlank { null }
    ?.substringAfter("<body>")
    ?.substringBefore("</body>")
    ?.replace("&hellip;", "...")
    ?.replace("&nbsp;", " ")
    ?.replace("<<", "((")
    ?.replace(">>>", ">))")
    ?.replace(">>", "))")
    ?.replace("\\s+".toRegex(), " ")
    ?.replace("<br />", "\n")
    ?.konsumeXml()
    ?.konsumeZmeny()

val dobreHodiny: (Hodina) -> Boolean = { it in (0..9) }

@JvmName("pouzeDobreHodinyTridy")
fun List<ZmenaTridy>.pouzeDobreHodiny() = mapNotNull {
    when (
        val pouzeKdyzDobre = it.takeUnless { it.hodiny.none(dobreHodiny) }
    ) {
        is ZmenaTridy.Absence -> pouzeKdyzDobre.copy(hodiny = pouzeKdyzDobre.hodiny.filter(dobreHodiny))
        else -> pouzeKdyzDobre
    }
}

@JvmName("pouzeDobreHodinyVyucujiciho")
fun List<ZmenaVyucujiciho>.pouzeDobreHodiny() = mapNotNull {
    when (it) {
        is ZmenaVyucujiciho.Absence -> it.copy(hodiny = it.hodiny.filter(dobreHodiny))
        else -> it.takeUnless { it.hodiny.none(dobreHodiny) }
    }
}

private fun Konsumer.konsumeZmeny() = child("form") {
    val result = children(anyName) {
        if (localName != "div" || "changes-content" !in classList) {
            skipContents()
            return@children null
        }

        ignoreChild("div")
        ignoreChild("div")
        ignoreChild("h1")

        Zmeny(
            nepritomneTridy = childOrNull("table") { konsumeNepritomneTridy() }.alsoIfNull { ignoreChild("div") }
                .orEmpty().mapValues { it.value.filterKeys(dobreHodiny) },
            nepritomniUcitele = childOrNull("table") { konsumeNepritomniUcitele() }.alsoIfNull { ignoreChild("div") }
                .orEmpty().mapValues { it.value.filterKeys(dobreHodiny) },
            mistnostiMimoProvoz = childOrNull("table") { konsumeMistnostiMimoProvoz() }.alsoIfNull { ignoreChild("div") }
                .orEmpty().mapValues { it.value.filterKeys(dobreHodiny) },
            zmenyVRozvrzichUcitelu = childOrNull("table") { konsumeZmenyVRozvrzichUcitelu() }.alsoIfNull { ignoreChild("div") }
                .orEmpty().mapValues { it.value.pouzeDobreHodiny() },
            zmenyVRozvrzichTrid = childOrNull("table") { konsumeZmenyVRozvrzichTrid() }.alsoIfNull { ignoreChild("div") }
                .orEmpty().mapValues { it.value.pouzeDobreHodiny() },
            pedagogickyDohledUTrid = childOrNull("table") { konsumePedagogickyDohledUTrid() }.alsoIfNull { ignoreChild("div") }
                .orEmpty().mapValues { p ->
                    p.value.map { it.first.filter(dobreHodiny) to it.second }.filter { it.first.isNotEmpty() }
                },
            poznamky = childTextOrNull("p")?.split("\n").orEmpty(),
        )
    }.firstNotNullOf { it }

    result
}

private fun Konsumer.konsumeNepritomneTridy() = run {
    child("tr") {
        require(childText("th") == "Nepřítomné třídy")
        ignoreChildren("th")
    }
    children("tr") {
        childText("td") to childrenIndexed("td") { i ->
            i to text()
        }.toMap()
    }.toMap()
}

private fun Konsumer.konsumeNepritomniUcitele() = run {
    child("tr") {
        require(childText("th") == "Nepřítomní učitelé")
        ignoreChildren("th")
    }
    children("tr") {
        childText("td") to childrenIndexed("td") { i ->
            i to text().replace("))", ">>").replace("((", "<<")
        }.toMap()
    }.toMap()
}

private fun Konsumer.konsumeMistnostiMimoProvoz() = run {
    child("tr") {
        require(childText("th") == "Místnosti mimo provoz")
        ignoreChildren("th")
    }
    children("tr") {
        childText("td") to childrenIndexed("td") { i ->
            i to text()
        }.toMap()
    }.toMap()
}

private fun Konsumer.konsumeZmenyVRozvrzichUcitelu() = run {
    child("tr") {
        require(childText("th") == "Změny v rozvrzích učitelů")
    }
    children("tr") {
        childText("td") to child("td") {
            child("table") {
                children("tr") {
                    val ch = childrenText("td")
                    if (ch.size == 1)
                        ch.first().zpracovatAbsenciVyucujiciho()
                    else when {
                        "přesun ))" in ch[1] -> ZmenaVyucujiciho.PresunZ(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            naDatum = ch[5].zpracovatDatum(),
                            naHodinu = ch[5].zpracovatHodinu(),
                        )

                        "přesun ((" in ch[1] -> ZmenaVyucujiciho.PresunNa(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            mistnost = ch[4],
                            zDatumu = ch[5].zpracovatDatum(),
                            zHodiny = ch[5].zpracovatHodinu(),
                        )

                        "odpadá" in ch[1] -> ZmenaVyucujiciho.Odpada(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            poznamka = ch[5],
                        )

                        "spojeno" in ch[1] -> ZmenaVyucujiciho.Spojeno(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            mistnost = ch[4],
                            misto = ch[2].substringAfter("(").substringBefore(")")
                        )

                        "změna" in ch[1] -> if (ch[3].last() == '+') ZmenaVyucujiciho.ZmenaPlus(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3].dropLast(1),
                        ) else ZmenaVyucujiciho.Zmena(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            mistnost = ch[4],
                        )

                        "supl." in ch[1] -> ZmenaVyucujiciho.Supluje(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            mistnost = ch[4],
                            misto = ch[2].substringAfter("(").substringBefore(")")
                        )

                        "navíc" in ch[1] -> ZmenaVyucujiciho.Navic(
                            hodina = ch[0].toInt(),
                            predmet = ch[2],
                            tridaSkupina = ch[3],
                            mistnost = ch[4],
                        )

                        else -> throw IllegalStateException()
                    }
                }
            }
        }
    }.toMap()
}

private fun Konsumer.konsumeZmenyVRozvrzichTrid() = run {
    child("tr") {
        require(childText("th") == "Změny v rozvrzích tříd")
    }
    children("tr") {
        childText("td") to child("td") {
            child("table") {
                children("tr") {
                    val ch = childrenText("td")
                    if (ch.size == 1)
                        ch.first().zpracovatAbsenciTridy()
                    else when {
                        "přesun ))" in ch[4] -> ZmenaTridy.PresunZ(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            naDatum = ch[6].zpracovatDatum(),
                            naHodinu = ch[6].zpracovatHodinu(),
                        )

                        "přesun ((" in ch[4] -> ZmenaTridy.PresunNa(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            mistnost = ch[3],
                            vyucujici = ch[5],
                            zDatumu = ch[6].zpracovatDatum(),
                            zHodiny = ch[6].zpracovatHodinu(),
                        )

                        "odpadá" in ch[4] -> ZmenaTridy.Odpada(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            misto = ch[6].substringAfter("(").substringBefore(")"),
                        )

                        "spojí" in ch[4] -> ZmenaTridy.Spoji(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            mistnost = ch[3],
                            vyucujici = ch[5],
                            misto = ch[6].substringAfter("(").substringBefore(")"),
                        )

                        "změna" in ch[4] -> ZmenaTridy.Zmena(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            mistnost = ch[3],
                            vyucujici = ch[5],
                        )

                        "supluje" in ch[4] -> ZmenaTridy.Supluje(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            mistnost = ch[3],
                            vyucujici = ch[5],
                            misto = ch[6].substringAfter("(").substringBefore(")"),
                        )

                        "navíc" in ch[4] -> ZmenaTridy.Navic(
                            hodina = ch[0].toInt(),
                            predmet = ch[1],
                            skupina = ch[2],
                            mistnost = ch[3],
                            vyucujici = ch[5],
                        )

                        else -> throw IllegalStateException()
                    }
                }.sortedBy {
                    it.hodiny.first()
                }
            }
        }
    }.toMap()
}

private fun Konsumer.konsumePedagogickyDohledUTrid() = run {
    child("tr") {
        require(childText("th") == "Pedagogický dohled u třídy:")
    }
    children("tr") {
        childText("td") to child("td") {
            child("table") {
                children("tr") {
                    childText("td") { str ->
                        Pair(
                            first = str.substringBefore("les.").removePrefix("Hodina:").trim().replace(".", "").replace(" ", "").let {
                                if ("-" in it) {
                                    val int = it.split("-")
                                    int.first().toInt()..int.last().toInt()
                                } else listOf(it.toInt())
                            }.toList(),
                            second = str.substringAfter("Učitel:").split(",").map {
                                it.trim()
                            }
                        )
                    }
                }
            }
        }
    }.toMap()
}


private fun String.zpracovatDatum() =
    trim().removePrefix("na").removePrefix("z").removeSuffix("hod").trim().removeSuffix(".").let { trimmed ->
        if (" " in trimmed) trimmed.split(" ")[0].split(".").let { LocalDate.of(0, it[1].toInt(), it[0].toInt()) }
        else null
    }

private fun String.zpracovatHodinu() =
    trim().removePrefix("na").removePrefix("z").removeSuffix("hod").trim().removeSuffix(".").let { trimmed ->
        if (" " in trimmed) trimmed.split(" ")[1].toInt()
        else trimmed.toInt()
    }

private fun String.zpracovatAbsenci() = Triple(
    third = if (!startsWith("skup")) null else split(":").first().removePrefix("skup").trim(),
    first = split(":").last().split("les.").dropLast(1).map {
        it.trim().replace(".", "").replace(",", "").replace(" ", "")
    }.flatMap {
        if ("-" in it.trim()) {
            val int = it.split("-")
            int.first().toInt()..int.last().toInt()
        } else
            listOf(it.toInt())
    }.sorted(),
    second = split("les.").last().trim()
)

private fun String.zpracovatAbsenciVyucujiciho() = zpracovatAbsenci().let { ZmenaVyucujiciho.Absence(it.first, it.second) }
private fun String.zpracovatAbsenciTridy() = zpracovatAbsenci().let { ZmenaTridy.Absence(it.first, it.second, it.third) }


private fun <T> T?.alsoIfNull(block: () -> Unit): T? = this ?: also { block() }

@KonsumerDsl
private fun Konsumer.ignoreChild(name: String) = child(name) { skipContents() }

@KonsumerDsl
private fun Konsumer.ignoreChildren(name: String) = children(name) { skipContents() }

@KonsumerDsl
private fun <T : Any?> Konsumer.childrenIndexed(name: String, block: Konsumer.(index: Int) -> T): List<T> {
    var i = 0
    return children(name) { block(i++) }
}

private val Konsumer.classList get() = attributes.getValue("class").split(" ")

inline fun <T> MutableStateFlow<T?>.getOrPut(default: () -> T) = value ?: default().also { value = it }