package cz.jaro.rozvrh.rozvrh

sealed class TypRozvrhu(
    val nazev: String,
    val seznam: List<String>,
    val seznamOdkazu: List<String>,
) {
    object Trida : TypRozvrhu(
        nazev = "Třída",
        seznam = Seznamy.tridy,
        seznamOdkazu = Seznamy.tridyOdkazy,
    )

    object Mistnost : TypRozvrhu(
        nazev = "Místnost",
        seznam = Seznamy.mistnosti,
        seznamOdkazu = emptyList() /*Seznamy.mistnostiOdkazy*/,
    )

    object Vyucujici : TypRozvrhu(
        nazev = "Vyučující",
        seznam = Seznamy.vyucujici,
        seznamOdkazu = emptyList() /*Seznamy.vyucujiciOdkazy*/,
    )
}
