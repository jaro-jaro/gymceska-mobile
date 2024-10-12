package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.ukoly.today

enum class Stalost(
    val nazev: String,
    val odkaz: String,
    val kdy: String,
) {
    TentoTyden(
        "Tento týden",
        "Actual",
        "tento týden",
    ),
    PristiTyden(
        "Příští týden",
        "Next",
        "příští týden",
    ),
    Staly(
        "Stálý",
        "Permanent",
        "vždy",
    );

    companion object
}

fun Stalost.Companion.dnesniEntries() = denniEntries(today().dayOfWeek.value)

fun Stalost.Companion.denniEntries(den: Int) = buildList {
    if (den in 1..5) add(Stalost.TentoTyden)
    add(Stalost.PristiTyden)
    add(Stalost.Staly)
}