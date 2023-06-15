package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
        bunekVHodine: Int,
        maxBunekDne: Int,
        tridy: List<Vjec.TridaVjec>,
        mistnosti: List<Vjec.MistnostVjec>,
        vyucujici: List<Vjec.VyucujiciVjec>,
        kliklNaNeco: (vjec: Vjec) -> Unit,
    ) = Box(
        modifier = Modifier
            .aspectRatio(1F * bunekVHodine / maxBunekDne)
            .border(1.dp, MaterialTheme.colorScheme.secondary)
            .size(120.dp, 120.dp * maxBunekDne / bunekVHodine)
            .background(if (zbarvit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .matchParentSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = ucebna,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .clickable {
                            if (ucebna.isEmpty()) return@clickable
                            val vjec = mistnosti
                                .find { ucebna == it.zkratka } ?: return@clickable
                            kliklNaNeco(vjec)
                        },
                    textAlign = TextAlign.Start,
                    color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground
                )
                Text(
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
                    textAlign = TextAlign.End,
                    color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = predmet,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
            )
            Text(
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
                    }
                    .fillMaxWidth(
                        ucitel
                            .isNotEmpty()
                            .toInt()
                            .toFloat()
                    ),
                textAlign = TextAlign.Center,
                color = if (zbarvit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

fun Boolean.toInt() = if (this) 1 else 0