package cz.jaro.rozvrh.rozvrh

enum class TypRozvrhu(
    val nazev: String,
    val vjeci: List<Vjec>,
) {
    Trida(
        nazev = "Třída",
        vjeci = Vjec.TridaVjec.tridy,
    ),
    Mistnost(
        nazev = "Místnost",
        vjeci = Vjec.MistnostVjec.mistnosti,
    ),
    Vyucujici(
        nazev = "Vyučující",
        vjeci = Vjec.VyucujiciVjec.mistnosti,
    )

}
