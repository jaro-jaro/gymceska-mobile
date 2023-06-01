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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.jaro.rozvrh.R
import kotlinx.serialization.Serializable

typealias Tyden = List<Den>
typealias Den = List<Hodina>
typealias Hodina = List<Bunka>

@Serializable
data class Bunka(
    val ucebna: String,
    val predmet: String,
    val vyucujici: String,
    val trida_skupina: String = "",
    val zbarvit: Boolean = false,
) {
    companion object {

        val prazdna = Bunka(
            ucebna = "",
            predmet = "",
            vyucujici = "",
            trida_skupina = "",
            zbarvit = false
        )
    }

    @Composable
    fun Compose(
        bunekVHodine: Int,
        maxBunekDne: Int,
        kliklNaNeco: (vjec: Vjec) -> Unit,
    ) = Box(
        modifier = Modifier
            .aspectRatio(1F * bunekVHodine / maxBunekDne)
            .border(1.dp, MaterialTheme.colorScheme.primary)
            .size(120.dp, 120.dp * maxBunekDne / bunekVHodine)
            .background(if (zbarvit) colorResource(id = R.color.pink) else MaterialTheme.colorScheme.background),
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
                            val vjec = Vjec.mistnosti.find { ucebna == it.zkratka } ?: return@clickable
                            kliklNaNeco(vjec)
                        },
                    textAlign = TextAlign.Start
                )
                Text(
                    text = trida_skupina,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .clickable {
                            if (trida_skupina.isEmpty()) return@clickable
                            val vjec = Vjec.tridy.find {
                                trida_skupina
                                    .split(" ")
                                    .first() == it.zkratka
                            } ?: return@clickable
                            kliklNaNeco(vjec)
                        },
                    textAlign = TextAlign.End
                )
            }
            Text(
                text = predmet,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                text = vyucujici,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .clickable {
                        if (vyucujici.isEmpty()) return@clickable
                        val vjec = Vjec.vyucujici.find {
                            vyucujici
                                .split(",")
                                .first() == it.zkratka
                        } ?: return@clickable
                        kliklNaNeco(vjec)
                    }
                    .fillMaxWidth(
                        vyucujici
                            .isNotEmpty()
                            .toInt()
                            .toFloat()
                    ),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun Boolean.toInt() = if (this) 1 else 0