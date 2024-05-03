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
import androidx.compose.ui.text.style.TextAlign
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
    val typ: TypBunky = TypBunky.Normalni,
) {
    companion object {

        val prazdna = Bunka(
            ucebna = "",
            predmet = "",
            ucitel = "",
            tridaSkupina = "",
            typ = TypBunky.Normalni
        )
    }

}

@Composable
fun Bunka(
    bunka: Bunka,
    aspectRatio: Float,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    kliklNaNeco: (vjec: Vjec) -> Unit,
    forceOneColumnCells: Boolean = false,
) = Box(
    modifier = Modifier
        .border(1.dp, MaterialTheme.colorScheme.secondary)
        .then(
            if (bunka.typ == TypBunky.Volno && !forceOneColumnCells) Modifier
                .size(zakladniVelikostBunky * 10, zakladniVelikostBunky / aspectRatio)
            else Modifier
                .aspectRatio(aspectRatio)
                .size(zakladniVelikostBunky, zakladniVelikostBunky / aspectRatio)
        )
        .background(
            when (bunka.typ) {
                TypBunky.Suplovani -> MaterialTheme.colorScheme.errorContainer
                TypBunky.Volno, TypBunky.Trid -> MaterialTheme.colorScheme.tertiaryContainer
                TypBunky.Normalni -> MaterialTheme.colorScheme.background
            }
        ),
    contentAlignment = Alignment.Center
) {
    @Composable
    fun Ucebna(
        bunka: Bunka,
        mistnosti: List<Vjec.MistnostVjec>,
        kliklNaNeco: (vjec: Vjec) -> Unit
    ) {
        ResponsiveText(
            text = bunka.ucebna,
            modifier = Modifier
                .padding(all = 8.dp)
                .clickable {
                    if (bunka.ucebna.isEmpty()) return@clickable
                    val vjec = mistnosti
                        .find { bunka.ucebna == it.zkratka } ?: return@clickable
                    kliklNaNeco(vjec)
                },
            color = when (bunka.typ) {
                TypBunky.Suplovani -> MaterialTheme.colorScheme.onErrorContainer
                TypBunky.Volno, TypBunky.Trid -> MaterialTheme.colorScheme.onTertiaryContainer
                TypBunky.Normalni -> MaterialTheme.colorScheme.onBackground
            },
        )
    }

    @Composable
    fun Trida(
        bunka: Bunka,
        tridy: List<Vjec.TridaVjec>,
        kliklNaNeco: (vjec: Vjec) -> Unit
    ) {
        ResponsiveText(
            text = bunka.tridaSkupina,
            modifier = Modifier
                .padding(all = 8.dp)
                .clickable {
                    if (bunka.tridaSkupina.isEmpty()) return@clickable
                    val vjec = tridy.find {
                        bunka.tridaSkupina
                            .split(" ")
                            .first() == it.zkratka
                    } ?: return@clickable
                    kliklNaNeco(vjec)
                },
            color = when (bunka.typ) {
                TypBunky.Suplovani -> MaterialTheme.colorScheme.onErrorContainer
                TypBunky.Volno, TypBunky.Trid -> MaterialTheme.colorScheme.onTertiaryContainer
                TypBunky.Normalni -> MaterialTheme.colorScheme.onBackground
            },
        )
    }

    @Composable
    fun Predmet() = ResponsiveText(
        text = bunka.predmet,
        modifier = Modifier
            .padding(all = 8.dp),
        color = when (bunka.typ) {
            TypBunky.Suplovani -> MaterialTheme.colorScheme.onErrorContainer
            TypBunky.Volno, TypBunky.Trid -> MaterialTheme.colorScheme.onTertiaryContainer
            TypBunky.Normalni -> MaterialTheme.colorScheme.primary
        }
    )

    @Composable
    fun Ucitel() = ResponsiveText(
        text = bunka.ucitel,
        modifier = Modifier
            .padding(all = 8.dp)
            .clickable {
                if (bunka.ucitel.isEmpty()) return@clickable
                val vjec = vyucujici.find {
                    bunka.ucitel
                        .split(",")
                        .first() == it.zkratka
                } ?: return@clickable
                kliklNaNeco(vjec)
            },
        color = when (bunka.typ) {
            TypBunky.Suplovani -> MaterialTheme.colorScheme.onErrorContainer
            TypBunky.Volno, TypBunky.Trid -> MaterialTheme.colorScheme.onTertiaryContainer
            TypBunky.Normalni -> MaterialTheme.colorScheme.onBackground
        },
    )

    val divnyRozlozeni = aspectRatio > 1F
    val jesteDivnejsiRozlozeni = bunka.typ == TypBunky.Volno

    if (!jesteDivnejsiRozlozeni) Row(
        Modifier
            .matchParentSize(),
        verticalAlignment = Alignment.Top,
    ) {
        if (bunka.ucebna.isNotBlank()) Box(
            Modifier,
            contentAlignment = Alignment.TopStart,
        ) {
            Ucebna(bunka, mistnosti, kliklNaNeco)
        }
        if (bunka.tridaSkupina.isNotBlank()) Box(
            Modifier
                .weight(1F),
            contentAlignment = Alignment.TopEnd,
        ) {
            Trida(bunka, tridy, kliklNaNeco)
        }
    }

    if (divnyRozlozeni && !jesteDivnejsiRozlozeni) Row(
        Modifier
            .matchParentSize(),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (bunka.predmet.isNotBlank()) Box(
            Modifier,
            contentAlignment = Alignment.BottomStart,
        ) {
            Predmet()
        }
        if (bunka.ucitel.isNotBlank()) Box(
            Modifier
                .weight(1F),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Ucitel()
        }
    }

    if (!divnyRozlozeni && !jesteDivnejsiRozlozeni) Box(
        Modifier
            .matchParentSize(),
        contentAlignment = Alignment.Center,
    ) {
        Predmet()
    }
    if (!divnyRozlozeni && !jesteDivnejsiRozlozeni) Box(
        Modifier
            .matchParentSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Ucitel()
    }

    if (jesteDivnejsiRozlozeni) Box(
        Modifier
            .matchParentSize(),
        contentAlignment = if (forceOneColumnCells) Alignment.Center else Alignment.CenterStart,
    ) {
        ResponsiveText(
            text = bunka.predmet,
            modifier = Modifier
                .padding(all = 8.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
            maxLines = Int.MAX_VALUE,
        )
    }
}

val zakladniVelikostBunky = 128.dp

//fun Boolean.toInt() = if (this) 1 else 0