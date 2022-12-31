package cz.jaro.rozvrh.rozvrh.oznameni

import kotlinx.serialization.Serializable

@Serializable
data class OznameniState(
    val denniOznameni: Boolean = false,
    val denniOznameniNaZitrek: Boolean = true,
    val denniOznameniCas: String = "16:00",
    val hodinovyOznameni: Boolean = false,
    val hodinovyOznameniPodle: TypHodinovyhoOznameni = TypHodinovyhoOznameni.PoMinulyHodine,
    val casString: String = "-5",
) {
    val cas: Int get() = casString.toIntOrNull() ?: 0

    enum class TypHodinovyhoOznameni {
        PoMinulyHodine, PredDalsiHodinou
    }
}
