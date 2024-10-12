package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.Offline
import cz.jaro.rozvrh.OfflineRuzneCasti
import cz.jaro.rozvrh.Online
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Result
import cz.jaro.rozvrh.Uspech
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.jsoup.nodes.Document

object TvorbaRozvrhu {

    private val dny = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne", "Rden", "Pi")
    fun vytvoritTabulku(
        vjec: Vjec,
        doc: Document,
    ): Tyden = listOf(
        listOf(
            listOf(
                Bunka(
                    ucebna = "",
                    predmet = vjec.zkratka,
                    ucitel = "",
                    tridaSkupina = ""
                )
            )
        ) + doc
            .getElementsByClass("bk-timetable-body").first()!!
            .getElementById("main")!!
            .getElementsByClass("bk-timetable-hours").first()!!
            .getElementsByClass("bk-hour-wrapper")
            .take(10)
            .map { hodina ->
                val num = hodina.getElementsByClass("num").first()!!
                val hour = hodina.getElementsByClass("hour").first()!!

                listOf(
                    Bunka(
                        ucebna = "",
                        predmet = num.text(),
                        ucitel = hour.text(),
                        tridaSkupina = ""
                    )
                )
            }
    ) + doc
        .getElementsByClass("bk-timetable-body").first()!!
        .getElementById("main")!!
        .getElementsByClass("bk-timetable-row")
        .mapIndexed { i, timeTableRow ->
            listOf(
                listOf(
                    Bunka(
                        ucebna = "",
                        predmet = dny[i],
                        ucitel = "",
                        tridaSkupina = ""
                    )
                )
            ) + timeTableRow
                .getElementsByClass("bk-cell-wrapper").first()!!
                .getElementsByClass("bk-timetable-cell")
                .take(10)
                .map { timetableCell ->
                    timetableCell.getElementsByClass("day-item").first()
                        ?.getElementsByClass("day-item-hover")
                        ?.flatMap { dayItemHover ->
                            val bunka = dayItemHover.getElementsByClass("day-flex").first()?.let { dayFlex ->
                                Bunka(
                                    ucebna = dayFlex
                                        .getElementsByClass("top").first()!!
                                        .getElementsByClass("right").first()
                                        ?.text()
                                        ?: "",
                                    predmet = dayFlex
                                        .getElementsByClass("middle").first()!!
                                        .text(),
                                    ucitel = dayFlex
                                        .getElementsByClass("bottom").first()!!
                                        .text(),
                                    tridaSkupina = dayFlex
                                        .getElementsByClass("top").first()!!
                                        .getElementsByClass("left").first()
                                        ?.text()
                                        ?: "",
                                    typ = when {
                                        dayItemHover.hasClass("pink") -> TypBunky.Suplovani
                                        dayItemHover.hasClass("green") -> TypBunky.Trid
                                        else -> TypBunky.Normalni
                                    }
                                )
                            } ?: Bunka.empty

                            if (dayItemHover.hasClass("hasAbsent")) listOf(
                                bunka,
                                Bunka(
                                    ucebna = "",
                                    predmet = "Absc",
                                    ucitel = "",
                                    tridaSkupina = "",
                                    typ = TypBunky.Trid
                                )
                            )
                            else listOf(bunka)
                        }
                        ?.ifEmpty {
                            listOf(Bunka.empty)
                        }
                        ?: timetableCell.getElementsByClass("day-item-volno").first()
                            ?.getElementsByClass("day-off")?.first()
                            ?.let {
                                listOf(
                                    Bunka(
                                        ucebna = "",
                                        predmet = it.text(),
                                        ucitel = "",
                                        tridaSkupina = "",
                                        typ = TypBunky.Volno
                                    )
                                )
                            }
                        ?: listOf(Bunka.empty)
                }
        }

    suspend fun vytvoritRozvrhPodleJinych(
        vjec: Vjec,
        stalost: Stalost,
        repo: Repository,
    ): Result = withContext(Dispatchers.IO) {
        require(vjec is Vjec.MistnostVjec || vjec is Vjec.VyucujiciVjec)

        val seznamNazvu = repo.tridy.value.drop(1)

        val novaTabulka = MutableList(6) { MutableList(11) { mutableListOf<Bunka>() } }

        val nejstarsi = seznamNazvu.fold(null as LocalDateTime?) { zatimNejstarsi, trida ->

            val result = repo.ziskatRozvrh(trida, stalost)

            if (result !is Uspech) return@withContext result

            result.rozvrh.forEachIndexed trida@{ i, den ->
                den.forEachIndexed den@{ j, hodina ->
                    hodina.forEach hodina@{ bunka ->
                        if (i == 0 || j == 0) {
                            if (novaTabulka[i][j].isEmpty()) novaTabulka[i][j] += bunka
                            return@hodina
                        }
                        if (bunka.ucitel.isEmpty() || bunka.predmet.isEmpty()) {
                            return@hodina
                        }
                        val zajimavaVec = when (vjec) {
                            is Vjec.VyucujiciVjec -> bunka.ucitel.split(",").first()
                            is Vjec.MistnostVjec -> bunka.ucebna
                            else -> throw IllegalArgumentException()
                        }
                        if (zajimavaVec == vjec.zkratka) {
                            novaTabulka[i][j] += bunka.copy(tridaSkupina = "${trida.zkratka} ${bunka.tridaSkupina}".trim()).let {
                                when (vjec) {
                                    is Vjec.VyucujiciVjec -> it.copy(ucitel = "")
                                    is Vjec.MistnostVjec -> it.copy(ucebna = "")
                                    else -> throw IllegalArgumentException()
                                }
                            }
                        }
                    }
                }
            }

            if (result.zdroj !is Offline) zatimNejstarsi
            else if (zatimNejstarsi == null || result.zdroj.ziskano < zatimNejstarsi) result.zdroj.ziskano
            else zatimNejstarsi
        }
        novaTabulka.forEachIndexed { i, den ->
            if (den.getOrNull(1)?.singleOrNull()?.typ == TypBunky.Volno) return@forEachIndexed
            den.forEachIndexed { j, hodina ->
                hodina.ifEmpty {
                    novaTabulka[i][j] += Bunka.empty
                }
            }
        }
        novaTabulka[0][0][0] = novaTabulka[0][0][0].copy(predmet = vjec.zkratka)
        if (nejstarsi == null) Uspech(novaTabulka, Online)
        else Uspech(novaTabulka, OfflineRuzneCasti(nejstarsi))
    }

    suspend fun vytvoritSpecialniRozvrh(
        vjec: Vjec,
        stalost: Stalost,
        repo: Repository,
    ): Result = withContext(Dispatchers.IO) {
        require(vjec is Vjec.DenVjec || vjec is Vjec.HodinaVjec)

        val seznamNazvu = repo.tridy.value.drop(1)

        val vyska = when (vjec) {
            is Vjec.DenVjec -> seznamNazvu.count()
            is Vjec.HodinaVjec -> 5
            else -> throw IllegalArgumentException()
        }
        val sirka = when (vjec) {
            is Vjec.DenVjec -> 10
            is Vjec.HodinaVjec -> seznamNazvu.count()
            else -> throw IllegalArgumentException()
        }

        val novaTabulka = MutableList(vyska + 1) { MutableList(sirka + 1) { mutableListOf<Bunka>() } }

        val nejstarsi = seznamNazvu.fold(null as LocalDateTime?) { zatimNejstarsi, trida ->

            val result = repo.ziskatRozvrh(trida, stalost)

            if (result !is Uspech) return@withContext result

            val rozvrhTridy = result.rozvrh

            if (vjec is Vjec.DenVjec) {
                novaTabulka[seznamNazvu.indexOf(trida) + 1][0] = mutableListOf(Bunka.empty.copy(predmet = trida.zkratka))
                rozvrhTridy[vjec.index].forEachIndexed den@{ j, hodina ->
                    novaTabulka[0][j] = rozvrhTridy[0][j].toMutableList()
                    hodina.forEach hodina@{ bunka ->
                        if (j == 0) return@hodina

                        novaTabulka[seznamNazvu.indexOf(trida) + 1][j] += bunka
                    }
                }
            }

            if (vjec is Vjec.HodinaVjec) {
                novaTabulka[0][seznamNazvu.indexOf(trida) + 1] = mutableListOf(Bunka.empty.copy(predmet = trida.zkratka))
                rozvrhTridy.forEachIndexed trida@{ i, den ->
                    novaTabulka[i][0] = rozvrhTridy[i][0].toMutableList()
                    den.drop(1).singleOrGet(vjec.index - 1).forEach hodina@{ bunka ->
                        if (i == 0) return@hodina

                        novaTabulka[i][seznamNazvu.indexOf(trida) + 1] += bunka
                    }
                }
            }

            novaTabulka[0][0] = rozvrhTridy[0][0].toMutableList()

            if (result.zdroj !is Offline) zatimNejstarsi
            else if (zatimNejstarsi == null || result.zdroj.ziskano < zatimNejstarsi) result.zdroj.ziskano
            else zatimNejstarsi
        }
        novaTabulka.forEachIndexed { i, den ->
            if (den.getOrNull(1)?.singleOrNull()?.typ == TypBunky.Volno) return@forEachIndexed
            den.forEachIndexed { j, hodina ->
                hodina.ifEmpty {
                    novaTabulka[i][j] += Bunka.empty
                }
            }
        }
        novaTabulka[0][0][0] = novaTabulka[0][0][0].copy(predmet = vjec.zkratka)
        if (nejstarsi == null) Uspech(novaTabulka, Online)
        else Uspech(novaTabulka, OfflineRuzneCasti(nejstarsi))
    }
}

//private fun <E> MutableList<E>.takeInPlace(n: Int) = retainAll(take(n))

private fun <E> List<E>.singleOrGet(index: Int) = singleOrNull() ?: get(index)

fun Result.upravitTabulku(edit: (Tyden) -> Tyden) = when (this) {
    is Uspech -> copy(rozvrh = edit(rozvrh))
    else -> this
}

fun Tyden.filtrovatTabulku(
    mujRozvrh: Boolean = false,
    mojeSkupiny: Set<String> = emptySet(),
) = map { den ->
    den.filtrovatDen(mujRozvrh, mojeSkupiny)
}

fun Den.filtrovatDen(
    mujRozvrh: Boolean = false,
    mojeSkupiny: Set<String> = emptySet(),
) = map { hodina ->
    hodina.filtrovatHodinu(mujRozvrh, mojeSkupiny)
}

fun Hodina.filtrovatHodinu(
    mujRozvrh: Boolean = false,
    mojeSkupiny: Set<String> = emptySet(),
): Hodina {
    return if (!mujRozvrh) this
    else filter {
        it.tridaSkupina.isBlank() || it.tridaSkupina in mojeSkupiny
    }.map { mojeBunka ->
        val spojene = filter { bunka ->
            mojeBunka.ucitel == bunka.ucitel && mojeBunka.ucebna == bunka.ucebna && mojeBunka.predmet == bunka.predmet
        }
        mojeBunka.copy(
            tridaSkupina = spojene.map { it.tridaSkupina }.distinct().joinToString(", ")
        )
    }.ifEmpty { listOf(Bunka.empty) }
}