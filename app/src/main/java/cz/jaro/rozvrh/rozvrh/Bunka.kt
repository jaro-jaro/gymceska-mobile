package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.jaro.rozvrh.ResponsiveText
import kotlinx.serialization.Serializable

typealias Tyden = List<Den>
typealias Den = List<Hodina>
typealias Hodina = List<Bunka>

@Serializable
data class Bunka(
    val ucebna: String,
    val predmet: String,
    val ucitel: String,
    val tridaSkupina: String = "",
    val zbarvit: Boolean = false,
) {
    companion object {

        val prazdna = Bunka(
            ucebna = "",
            predmet = "",
            ucitel = "",
            tridaSkupina = "",
            zbarvit = false
        )
    }

    @Composable
    fun Compose(
        aspectRatio: Float,
        tridy: List<Vjec.TridaVjec>,
        mistnosti: List<Vjec.MistnostVjec>,
        vyucujici: List<Vjec.VyucujiciVjec>,
        kliklNaNeco: (vjec: Vjec) -> Unit,
    ) = Box(
        modifier = Modifier
            .aspectRatio(aspectRatio)
            .border(1.dp, MaterialTheme.colorScheme.secondary)
            .size(zakladniVelikostBunky, zakladniVelikostBunky / aspectRatio)
            .background(if (zbarvit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .matchParentSize(),
            verticalAlignment = Alignment.Top,
        ) {
            if (ucebna.isNotBlank()) Box(
                Modifier,
                contentAlignment = Alignment.TopStart,
            ) {
                ResponsiveText(
                    text = ucebna,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .clickable {
                            if (ucebna.isEmpty()) return@clickable
                            val vjec = mistnosti
                                .find { ucebna == it.zkratka } ?: return@clickable
                            kliklNaNeco(vjec)
                        },
                    color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground,
                )
            }
            if (tridaSkupina.isNotBlank()) Box(
                Modifier
                    .weight(1F),
                contentAlignment = Alignment.TopEnd,
            ) {
                ResponsiveText(
                    text = tridaSkupina,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .clickable {
                            if (tridaSkupina.isEmpty()) return@clickable
                            val vjec = tridy.find {
                                tridaSkupina
                                    .split(" ")
                                    .first() == it.zkratka
                            } ?: return@clickable
                            kliklNaNeco(vjec)
                        },
                    color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        @Composable
        fun Predmet() = ResponsiveText(
            text = predmet,
            modifier = Modifier
                .padding(all = 8.dp),
            color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
        )

        @Composable
        fun Ucitel() = ResponsiveText(
            text = ucitel,
            modifier = Modifier
                .padding(all = 8.dp)
                .clickable {
                    if (ucitel.isEmpty()) return@clickable
                    val vjec = vyucujici.find {
                        ucitel
                            .split(",")
                            .first() == it.zkratka
                    } ?: return@clickable
                    kliklNaNeco(vjec)
                },
            color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground,
        )

        val divnyRozlozeni = aspectRatio > 1F

        if (divnyRozlozeni) Row(
            Modifier
                .matchParentSize(),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (predmet.isNotBlank()) Box(
                Modifier,
                contentAlignment = Alignment.BottomStart,
            ) {
                Predmet()
            }
            if (ucitel.isNotBlank()) Box(
                Modifier
                    .weight(1F),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Ucitel()
            }
        }

        if (!divnyRozlozeni) Box(
            Modifier
                .matchParentSize(),
            contentAlignment = Alignment.Center,
        ) {
            Predmet()
        }
        if (!divnyRozlozeni) Box(
            Modifier
                .matchParentSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Ucitel()
        }
    }
}

val zakladniVelikostBunky = 128.dp

fun Boolean.toInt() = if (this) 1 else 0