package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.Vjec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val routes =
    listOf(Route.Rozvrh::class, Route.Ukoly::class, Route.SpravceUkolu::class, Route.Nastaveni::class)

@Serializable
@SerialName("Route")
sealed interface Route {

    @Serializable
    @SerialName("rozvrh")
    data class Rozvrh(
        val vjec: Vjec? = null,
        val stalost: Stalost? = null,
        val mujRozvrh: Boolean? = null,
        val horScroll: Int? = null,
        val verScroll: Int? = null,
    ) : Route

    @Serializable
    @SerialName("ukoly")
    data object Ukoly : Route

    @Serializable
    @SerialName("spravce-ukolu")
    data object SpravceUkolu : Route

    @Serializable
    @SerialName("nastaveni")
    data object Nastaveni : Route
}