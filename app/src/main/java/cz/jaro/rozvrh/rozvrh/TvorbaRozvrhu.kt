package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document

object TvorbaRozvrhu {
    fun vytvoritTabulku(doc: Document): List<List<List<Bunka>>> {

        val tabulka = mutableListOf<MutableList<MutableList<Bunka>>>()

        val htmlTabulka = doc.getElementsByClass("bk-timetable-body")[0].getElementById("main")!!

        for (radek in htmlTabulka.getElementsByClass("d-flex flex-row bk-timetable-row")) {
            tabulka.add(mutableListOf())
            val opravdickejRadek = radek.getElementsByClass("bk-cell-wrapper")[0]

            for (hodina in opravdickejRadek.getElementsByClass("bk-timetable-cell")) {
                tabulka.last().add(mutableListOf())
                val opravdickaHodina = hodina.getElementsByClass("day-item")

                if (opravdickaHodina.isEmpty()) continue

                for (bunka in opravdickaHodina[0].getElementsByAttributeValueContaining(
                    "class",
                    "day-item"
                )) {
                    if (bunka == opravdickaHodina[0]) continue


                    if (bunka.getElementsByClass("day-flex").isEmpty()) {
                        continue
                    }

                    val opravdickaBunka = bunka.getElementsByClass("day-flex")[0]

                    val nahore = opravdickaBunka.getElementsByClass("top clearfix")[0]

                    val skupina =
                        if (nahore.getElementsByClass("left roll-vertical top-hide").isNotEmpty()) {
                            val nahoreLevo = nahore.getElementsByClass("left roll-vertical top-hide")[0]
                            nahoreLevo.getElementsByTag("div")[0].text()
                        } else ""

                    val mistnost = if (nahore.getElementsByClass("right top-hide").isNotEmpty()) {
                        val nahoreVpravo = nahore.getElementsByClass("right top-hide")[0]
                        nahoreVpravo.getElementsByClass("first")[0].text()
                    } else ""

                    val predmet = opravdickaBunka.getElementsByClass("middle")[0].text()

                    val spodek = opravdickaBunka.getElementsByClass("bottom")[0]
                    val vyucujici = spodek.getElementsByTag("div")[0].text()

                    if (bunka.className() in listOf("day-item-hover multi", "day-item-hover")) {
                        tabulka.last().last().add(Bunka(mistnost, predmet, vyucujici, skupina))
                    } else {
                        tabulka.last().last().add(Bunka(mistnost, predmet, vyucujici, skupina, true))
                    }
                }
            }
        }

        tabulka.add(0, mutableListOf())

        for (hodina in htmlTabulka.getElementsByClass("bk-timetable-hours")[0].getElementsByClass("bk-hour-wrapper")) {

            val num = hodina.getElementsByClass("num")[0]
            val hour = hodina.getElementsByClass("hour")[0]

            tabulka[0].add(mutableListOf(Bunka("", num.text(), hour.text())))
        }

        val dny = listOf("", "Po", "Út", "St", "Čt", "Pá", "So", "Ne", "Rden", "Pi")

        for ((i, den) in tabulka.withIndex()) {
            den.add(
                0, mutableListOf(
                    Bunka("", dny[i], "")
                )
            )
        }

        tabulka.forEachIndexed { i, radek ->
            radek.forEachIndexed { j, hodina ->
                if (hodina.isEmpty())
                    tabulka[i][j].add(Bunka.prazdna)
            }
        }

        return tabulka
    }

    suspend fun vytvoritRozvrhPodleJinych(
        typRozvrhu: TypRozvrhu,
        rozvrh: String,
        stalost: String,
        repo: Repository
    ): List<List<List<Bunka>>> = withContext(Dispatchers.IO) {
        if (typRozvrhu == TypRozvrhu.Trida) return@withContext emptyList()

        val seznamNazvu = Seznamy.tridy.drop(1)

        val novaTabulka = MutableList(6) { MutableList(17) { mutableListOf<Bunka>() } }

        seznamNazvu.forEach forE@{ nazev ->
            println(nazev)

            val doc = repo.ziskatDocument(nazev, stalost) ?: return@withContext emptyList()

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
                        val zajimavaVec = when (typRozvrhu) {
                            TypRozvrhu.Vyucujici -> Seznamy.vyucujici[Seznamy.vyucujiciZkratky.indexOf(bunka.vyucujici.split(",").first()) + 1]
                            TypRozvrhu.Mistnost -> bunka.ucebna
                            TypRozvrhu.Trida -> throw IllegalArgumentException()
                        }.also { println(it) }
                        if (zajimavaVec == rozvrh) {
                            novaTabulka[i][j] += bunka.copy(trida_skupina = "$nazev ${bunka.trida_skupina}".trim())
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
