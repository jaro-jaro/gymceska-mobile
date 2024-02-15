package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.Offline
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.nastaveni.nula
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import java.time.LocalDateTime

object TvorbaRozvrhu {

    private val dny = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne", "Rden", "Pi")
    fun vytvoritTabulku(
        vjec: Vjec,
        doc: Document,
        mujRozvrh: Boolean = false,
        mojeSkupiny: Set<String> = emptySet()
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
                        ?.filterNot {
                            mujRozvrh && it.tridaSkupina.isNotBlank() && it.tridaSkupina !in mojeSkupiny
                        }
                        ?.ifEmpty {
                            listOf(Bunka.prazdna)
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

    suspend fun vytvoritRozvrhPodleJinych(
        vjec: Vjec,
        stalost: Stalost,
        repo: Repository,
    ): Pair<Tyden, String?> = withContext(Dispatchers.IO) {
        require(vjec is Vjec.MistnostVjec || vjec is Vjec.VyucujiciVjec)

        val seznamNazvu = repo.tridy.value.drop(1)

        val novaTabulka = MutableList(6) { MutableList(11) { mutableListOf<Bunka>() } }

        val nejstarsi = seznamNazvu.fold(LocalDateTime.MAX) { zatimNejstarsi, trida ->

            val result = repo.ziskatDocument(trida, stalost)

            if (result !is Uspech) return@withContext emptyList<Den>() to ""

            val rozvrhTridy = vytvoritTabulku(vjec, result.document)

            rozvrhTridy.forEachIndexed trida@{ i, den ->
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
            else if (result.zdroj.ziskano < zatimNejstarsi!!) result.zdroj.ziskano
            else zatimNejstarsi
        }
        novaTabulka.forEachIndexed { i, den ->
            den.forEachIndexed { j, hodina ->
                hodina.ifEmpty {
                    novaTabulka[i][j] += Bunka.prazdna
                }
            }
        }
        if (nejstarsi == LocalDateTime.MAX) novaTabulka to null
        else novaTabulka to "Nejstarší část tohoto rozvrhu pochází z ${nejstarsi.dayOfMonth}. ${nejstarsi.monthValue}. ${nejstarsi.hour}:${nejstarsi.minute.nula()}."
    }

    suspend fun vytvoritSpecialniRozvrh(
        vjec: Vjec,
        stalost: Stalost,
        repo: Repository,
    ): Pair<Tyden, String?> = withContext(Dispatchers.IO) {
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

        val nejstarsi = seznamNazvu.fold(LocalDateTime.MAX) { zatimNejstarsi, trida ->

            val result = repo.ziskatDocument(trida, stalost)

            if (result !is Uspech) return@withContext emptyList<Den>() to ""

            val rozvrhTridy = vytvoritTabulku(vjec, result.document)

            if (vjec is Vjec.DenVjec) {
                novaTabulka[seznamNazvu.indexOf(trida) + 1][0] = mutableListOf(Bunka.prazdna.copy(predmet = trida.zkratka))
                rozvrhTridy[vjec.index].forEachIndexed den@{ j, hodina ->
                    novaTabulka[0][j] = rozvrhTridy[0][j].toMutableList()
                    hodina.forEach hodina@{ bunka ->
                        if (j == 0) return@hodina

                        novaTabulka[seznamNazvu.indexOf(trida) + 1][j] += bunka
                    }
                }
            }

            if (vjec is Vjec.HodinaVjec) {
                novaTabulka[0][seznamNazvu.indexOf(trida) + 1] = mutableListOf(Bunka.prazdna.copy(predmet = trida.zkratka))
                rozvrhTridy.forEachIndexed trida@{ i, den ->
                    novaTabulka[i][0] = rozvrhTridy[i][0].toMutableList()
                    den[vjec.index].forEach hodina@{ bunka ->
                        if (i == 0) return@hodina

                        novaTabulka[i][seznamNazvu.indexOf(trida) + 1] += bunka
                    }
                }
            }

            novaTabulka[0][0] = rozvrhTridy[0][0].toMutableList()

            if (result.zdroj !is Offline) zatimNejstarsi
            else if (result.zdroj.ziskano < zatimNejstarsi!!) result.zdroj.ziskano
            else zatimNejstarsi
        }
        novaTabulka.forEachIndexed { i, den ->
            den.forEachIndexed { j, hodina ->
                hodina.ifEmpty {
                    novaTabulka[i][j] += Bunka.prazdna
                }
            }
        }
        if (nejstarsi == LocalDateTime.MAX) novaTabulka to null
        else novaTabulka to "Nejstarší část tohoto rozvrhu pochází z ${nejstarsi.dayOfMonth}. ${nejstarsi.monthValue}. ${nejstarsi.hour}:${nejstarsi.minute.nula()}."
    }
}
