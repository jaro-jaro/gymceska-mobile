package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document

object TvorbaRozvrhu {

    private val dny = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne", "Rden", "Pi")
    fun vytvoritTabulku(doc: Document): Tyden = listOf(
        listOf(
            listOf(
                Bunka(
                    ucebna = "",
                    predmet = "",
                    vyucujici = "",
                    tridaSkupina = ""
                )
            )
        ) + doc
            .getElementsByClass("bk-timetable-body").first()!!
            .getElementById("main")!!
            .getElementsByClass("bk-timetable-hours").first()!!
            .getElementsByClass("bk-hour-wrapper")
            .map { hodina ->
                val num = hodina.getElementsByClass("num").first()!!
                val hour = hodina.getElementsByClass("hour").first()!!

                listOf(
                    Bunka(
                        ucebna = "",
                        predmet = num.text(),
                        vyucujici = hour.text(),
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
                        vyucujici = "",
                        tridaSkupina = ""
                    )
                )
            ) + timeTableRow
                .getElementsByClass("bk-cell-wrapper").first()!!
                .getElementsByClass("bk-timetable-cell")
                .map { timetableCell ->
                    timetableCell.getElementsByClass("day-item").first()
                        ?.getElementsByClass("day-item-hover")
                        ?.map { dayItemHover ->
                            dayItemHover.getElementsByClass("day-flex").first()?.let { dayFlex ->
                                Bunka(
                                    ucebna = dayFlex
                                        .getElementsByClass("top").first()!!
                                        .getElementsByClass("right").first()
                                        ?.text()
                                        ?: "",
                                    predmet = dayFlex
                                        .getElementsByClass("middle").first()!!
                                        .text(),
                                    vyucujici = dayFlex
                                        .getElementsByClass("bottom").first()!!
                                        .text(),
                                    tridaSkupina = dayFlex
                                        .getElementsByClass("top").first()!!
                                        .getElementsByClass("left").first()
                                        ?.text()
                                        ?: "",
                                    zbarvit = dayItemHover.hasClass("pink") || dayItemHover.hasClass("green")
                                )
                            } ?: Bunka.prazdna
                        }
                        ?: timetableCell.getElementsByClass("day-item-volno").first()
                            ?.getElementsByClass("day-off")?.first()
                            ?.let {
                                listOf(
                                    Bunka(
                                        ucebna = "",
                                        predmet = it.text(),
                                        vyucujici = "",
                                        tridaSkupina = "",
                                        zbarvit = true
                                    )
                                )
                            }
                        ?: listOf(Bunka.prazdna)
                }
        }

    suspend fun vytvoritRozvrhPodleJinych(
        vjec: Vjec,
        stalost: Stalost,
        repo: Repository,
    ): Tyden = withContext(Dispatchers.IO) {
        if (vjec is Vjec.TridaVjec) return@withContext emptyList()

        val seznamNazvu = Vjec.tridy.drop(1)

        val novaTabulka = MutableList(6) { MutableList(17) { mutableListOf<Bunka>() } }

        seznamNazvu.forEach forE@{ trida ->
            println(trida)

            val doc = repo.ziskatDocument(trida, stalost) ?: return@withContext emptyList()

            val rozvrhTridy = vytvoritTabulku(doc)

            rozvrhTridy.forEachIndexed { i, den ->
                den.forEachIndexed { j, hodina ->
                    hodina.forEach { bunka ->
                        if (bunka.vyucujici.isEmpty() || bunka.predmet.isEmpty()) {
                            return@forEach
                        }
                        if (i == 0 || j == 0) {
                            novaTabulka[i][j] += bunka
                            return@forEach
                        }
                        println(bunka)
                        val zajimavaVec = when (vjec) {
                            is Vjec.VyucujiciVjec -> bunka.vyucujici.split(",").first()
                            is Vjec.MistnostVjec -> bunka.ucebna
                            else -> throw IllegalArgumentException()
                        }
                        if (zajimavaVec == vjec.zkratka) {
                            novaTabulka[i][j] += bunka.copy(tridaSkupina = "${trida.zkratka} ${bunka.tridaSkupina}".trim())
                            return@forEach
                        }
                    }
                }
            }
        }
        novaTabulka.forEachIndexed { i, den ->
            den.forEachIndexed { j, hodina ->
                hodina.ifEmpty {
                    novaTabulka[i][j] += Bunka.prazdna
                }
            }
        }
        println(novaTabulka)
        novaTabulka
    }
}
