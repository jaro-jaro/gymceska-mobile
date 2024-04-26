package cz.jaro.rozvrh.rozvrh

import java.time.LocalDate

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

fun Stalost.Companion.dnesniEntries() = denniEntries(LocalDate.now().dayOfWeek.value)

fun Stalost.Companion.denniEntries(den: Int) = buildList {
    if (den in 1..5) add(Stalost.TentoTyden)
    add(Stalost.PristiTyden)
    add(Stalost.Staly)
}