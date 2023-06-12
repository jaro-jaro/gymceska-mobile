package cz.jaro.rozvrh.rozvrh

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
    ),
}