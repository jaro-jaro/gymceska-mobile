package cz.jaro.rozvrh.rozvrh

import org.jsoup.nodes.Document

object TvorbaRozvrhu {

    private val dny = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne", "Rden", "Pi")
    fun vytvoritTabulku(doc: Document): Tyden = listOf(
        listOf(
            listOf(
                Bunka(
                    ucebna = "",
                    predmet = "",
                    ucitel = "",
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
                                    ucitel = dayFlex
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
                                        ucitel = "",
                                        tridaSkupina = "",
                                        zbarvit = true
                                    )
                                )
                            }
                        ?: listOf(Bunka.prazdna)
                }
        }
}
