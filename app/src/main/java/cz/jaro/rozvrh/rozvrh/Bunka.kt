package cz.jaro.rozvrh.rozvrh

import android.inputmethodservice.Keyboard
import android.os.Build.VERSION_CODES.R
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
import org.w3c.dom.Text

data class Bunka(
    val ucebna: String,
    val predmet: String,
    val vyucujici: String,
    val trida_skupina: String = "",
    val zbarvit: Boolean = false,
) {

    val umisteni: String
        get() = mapOf(
            "1.A" to " fyzika (1)",
            "1.E" to " třída PO Týmalový (2)",
            "1.S" to " smrdí jako pes (nejlepší z esek) (0)",
            "2.A" to " za dveřma (1)",
            "2.E" to " nad dveřma (2)",
            "2.S" to " jedna z tich dvou (0)",
            "3.A" to " děják (1)",
            "3.E" to " třída s mrkví (1)",
            "3.S" to " jedna z tich dvou (0)",
            "4.A" to " vedle nás (2)",
            "4.E" to " ta hnusná třída vedle fyziky (POZOR NA PRIMÁTY!) (1)",
            "4.S" to " ta s tou černou tabulí (za rohem) (0)",
            "5.E" to ", no, to snad víš",
            "6.E" to " pod schodama (s blbou akustikou) (2)",
            "7.E" to " chemie (poslanecká sněmovna) (2)",
            "8.E" to " bižule (špenát) (2)",
            "Aula" to " v aule (0)",
            "C1" to " ta blíž ke žlutýmu (3)",
            "C2" to " ta blíž hudebně (3)",
            "C3" to " ta u kolatého stolu (3)",
            "Hv" to " hudebna (3)",
            "Inf1" to " ta nejlepší třída na škole (podle R) (1)",
            "Inf2" to " ta nejužší učebna na škole (3)",
            "LBi" to " vedle bižule (2)",
            "LCh" to " za váhovnou (2)",
            "Tv" to " na dvoře (-½)",
            "Vv" to " věžička (4)",
            "mim" to " venku (-½)",
        )[ucebna] ?: " v řece (-1)"

    companion object {

        val prazdna = Bunka(
            ucebna = "",
            predmet = "",
            vyucujici = "",
            trida_skupina = "",
            zbarvit = false
        )

        @Composable
        fun ComposovatBunku(bunka: Bunka, kolikJichJe: Int, celkem: Int, kliklNaNeco: (TypRozvrhu, Int) -> Unit) =
            bunka.invoke(kolikJichJe, celkem, kliklNaNeco)
    }

    @Composable
    operator fun invoke(
        kolikJichJe: Int,
        celkem: Int,
        kliklNaNeco: (rozvrh: TypRozvrhu, i: Int) -> Unit,
    ) = Box(
        modifier = Modifier
            .aspectRatio(1F * kolikJichJe / celkem)
            .border(1.dp, colorResource(id = R.color.gymceska_modra))
            .size(120.dp, 120.dp * celkem / kolikJichJe)
            .background(if (zbarvit) colorResource(id = R.color.pink) else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .matchParentSize()
        ) {
            Keyboard.Row(
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
                            Seznamy.vyucujiciZkratky.indexOf(vyucujici.split(",").first()) + 1
                        )
                    }
                    .fillMaxWidth(vyucujici.isNotEmpty().toInt().toFloat()),
                textAlign = TextAlign.Center
            )
        }
    }
}
