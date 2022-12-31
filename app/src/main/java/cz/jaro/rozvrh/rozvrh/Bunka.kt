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
import androidx.core.graphics.toColor
import androidx.glance.GlanceModifier
import androidx.glance.layout.ColumnScope
import androidx.glance.unit.ColorProvider
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.oznameni.toInt
import kotlinx.serialization.Serializable

@Serializable
data class Bunka(
    val ucebna: String,
    val predmet: String,
    val vyucujici: String,
    val trida_skupina: String = "",
    val zbarvit: Boolean = false,
) {

    val umisteni: String
        get() = when (ucebna) {
            "1.A" -> " fyzika (1)"
            "1.E" -> " třída PO Týmalový (2)"
            "1.S" -> " smrdí jako pes (nejlepší z esek) (0)"
            "2.A" -> " za dveřma (1)"
            "2.E" -> " nad dveřma (2)"
            "2.S" -> " jedna z tich dvou (0)"
            "3.A" -> " děják (1)"
            "3.E" -> " třída s mrkví (1)"
            "3.S" -> " jedna z tich dvou (0)"
            "4.A" -> " vedle nás (2)"
            "4.E" -> " ta hnusná třída vedle fyziky (POZOR NA PRIMÁTY!) (1)"
            "4.S" -> " ta s tou černou tabulí (za rohem) (0)"
            "5.E" -> ", no, to snad víš"
            "6.E" -> " pod schodama (s blbou akustikou) (2)"
            "7.E" -> " chemie (poslanecká sněmovna) (2)"
            "8.E" -> " bižule (špenát) (2)"
            "Aula" -> " v aule (0)"
            "C1" -> " ta blíž ke žlutýmu (3)"
            "C2" -> " ta blíž hudebně (3)"
            "C3" -> " ta u kolatého stolu (3)"
            "Hv" -> " hudebna (3)"
            "Inf1" -> " ta nejlepší třída na škole (podle R) (1)"
            "Inf2" -> " ta nejužší učebna na škole (3)"
            "LBi" -> " vedle bižule (2)"
            "LCh" -> " za váhovnou (2)"
            "Tv" -> " na dvoře (-½)"
            "Vv" -> " věžička (4)"
            "mim" -> " venku (-½)"
            else -> " v řece (-1)"
        }

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
        kliklNaNeco: (rozvrh: TypRozvrhu, i: Int) -> Unit,
    ) = Box(
        modifier = Modifier
            .aspectRatio(1F * bunekVHodine / maxBunekDne)
            .border(1.dp, colorResource(id = R.color.gymceska_modra))
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
                            kliklNaNeco(
                                TypRozvrhu.Mistnost,
                                Seznamy.mistnosti.indexOf(ucebna)
                            )
                        },
                    textAlign = TextAlign.Start
                )
                Text(
                    text = trida_skupina,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .clickable {
                            if (trida_skupina.isEmpty()) return@clickable
                            kliklNaNeco(
                                TypRozvrhu.Trida,
                                Seznamy.tridy.indexOf(
                                    trida_skupina
                                        .split(" ")
                                        .first()
                                )
                            )
                        },
                    textAlign = TextAlign.End
                )
            }
            Text(
                text = predmet,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = vyucujici,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .clickable {
                        if (vyucujici.isEmpty()) return@clickable
                        kliklNaNeco(
                            TypRozvrhu.Vyucujici,
                            Seznamy.vyucujiciZkratky.indexOf(
                                vyucujici
                                    .split(",")
                                    .first()
                            ) + 1
                        )
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
