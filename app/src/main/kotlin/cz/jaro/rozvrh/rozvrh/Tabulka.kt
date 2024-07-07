package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.dp
import cz.jaro.rozvrh.Offline
import cz.jaro.rozvrh.OfflineRuzneCasti
import cz.jaro.rozvrh.Online
import cz.jaro.rozvrh.ResponsiveText
import cz.jaro.rozvrh.ZdrojRozvrhu
import kotlinx.coroutines.launch

context(ColumnScope)
@Composable
fun Tabulka(
    vjec: Vjec,
    tabulka: Tyden,
    kliklNaNeco: (vjec: Vjec) -> Unit,
    rozvrhOfflineWarning: ZdrojRozvrhu?,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    mujRozvrh: Boolean,
    horScrollState: ScrollState,
    verScrollState: ScrollState,
) {
    if (tabulka.isEmpty()) return

    Column(
        Modifier.doubleScrollable(horScrollState, verScrollState)
    ) {
        val maxy = tabulka.map { radek -> radek.maxOf { hodina -> hodina.size } }
        val polovicniBunky = remember(tabulka) {
            val minLimit = if (mujRozvrh || vjec !is Vjec.TridaVjec) 2 else 4
            tabulka.map { radek -> radek.maxBy { it.size }.size >= minLimit }
        }

        Row(
            modifier = Modifier
                .verticalScroll(rememberScrollState(), enabled = false)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        ) {

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState(), enabled = false)
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1F)
                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
                    contentAlignment = Alignment.Center,
                ) {
                    ResponsiveText(
                        text = tabulka[0][0][0].predmet,
                        modifier = Modifier
                            .padding(all = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(horScrollState, enabled = false, reverseScrolling = true)
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                tabulka.first().drop(1).map { it.first() }.forEachIndexed { i, bunka ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(2F / 1)
                            .border(1.dp, MaterialTheme.colorScheme.secondary)
                            .size(zakladniVelikostBunky, zakladniVelikostBunky / 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.matchParentSize(),
                            contentAlignment = if (bunka.ucitel.isBlank()) Alignment.Center else Alignment.TopCenter,
                        ) {
                            ResponsiveText(
                                text = bunka.predmet,
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .clickable {
                                        if (bunka.predmet.isEmpty()) return@clickable
                                        kliklNaNeco(if (vjec is Vjec.HodinaVjec) tridy.find {
                                            bunka.predmet == it.zkratka
                                        } ?: return@clickable else Vjec.HodinaVjec(
                                            zkratka = bunka.predmet.split(".")[0],
                                            jmeno = "${bunka.predmet} hodina",
                                            index = i + 1
                                        ))
                                    },
                            )
                        }
                        Box(
                            Modifier.matchParentSize(),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            ResponsiveText(
                                text = bunka.ucitel,
                                modifier = Modifier
                                    .padding(all = 8.dp),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .verticalScroll(verScrollState, enabled = false, reverseScrolling = true),
        ) {
            Row {
                Column(
                    Modifier.horizontalScroll(rememberScrollState())
                ) {
                    tabulka.drop(1).map { it.first().first() }.forEachIndexed { i, bunka ->
                        Column(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio((if (polovicniBunky[i + 1]) 2F else 1F) / maxy[i + 1])
                                    .border(1.dp, MaterialTheme.colorScheme.secondary)
                                    .size(zakladniVelikostBunky / 2, zakladniVelikostBunky * maxy[i + 1] / (if (polovicniBunky[i + 1]) 2F else 1F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    Modifier.matchParentSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ResponsiveText(
                                        text = bunka.predmet,
                                        modifier = Modifier
                                            .padding(all = 8.dp)
                                            .clickable {
                                                if (bunka.predmet.isEmpty()) return@clickable
                                                kliklNaNeco(
                                                    if (vjec is Vjec.DenVjec) tridy.find {
                                                        bunka.predmet == it.zkratka
                                                    } ?: return@clickable else Seznamy.dny.find { it.zkratka == bunka.predmet }!!
                                                )
                                            },
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    Modifier.horizontalScroll(horScrollState, enabled = false, reverseScrolling = true)
                ) {
                    tabulka.drop(1).forEachIndexed { i, radek ->
                        val nasobitelVyskyCeleRadky = when {
                            polovicniBunky[i + 1] -> 1F / 2F
                            else -> 1F
                        }
                        Row {
                            radek.drop(1).forEach { hodina ->
                                Column(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                                ) {
                                    hodina.forEach { bunka ->
                                        val nasobitelVyskyTetoBunky = when {
                                            !mujRozvrh && vjec is Vjec.TridaVjec && hodina.size == 1 && bunka.tridaSkupina.isNotBlank() -> 4F / 5F
                                            else -> 1F
                                        }
                                        Bunka(
                                            bunka = bunka,
                                            aspectRatio = hodina.size / (maxy[i + 1] * nasobitelVyskyTetoBunky * nasobitelVyskyCeleRadky),
                                            tridy = tridy,
                                            mistnosti = mistnosti,
                                            vyucujici = vyucujici,
                                            kliklNaNeco = kliklNaNeco,
                                            forceOneColumnCells = vjec is Vjec.HodinaVjec,
                                        )
                                        if (nasobitelVyskyTetoBunky < 1F) Bunka(
                                            bunka = Bunka.empty,
                                            aspectRatio = hodina.size / (maxy[i + 1] * (1F - nasobitelVyskyTetoBunky) * nasobitelVyskyCeleRadky),
                                            tridy = tridy,
                                            mistnosti = mistnosti,
                                            vyucujici = vyucujici,
                                            kliklNaNeco = {},
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                rozvrhOfflineWarning?.let {
                    when (it) {
                        is Offline -> "Prohlížíte si verzi rozvrhu z ${it.ziskano.dayOfMonth}. ${it.ziskano.monthValue}. ${it.ziskano.hour}:${it.ziskano.minute.nula()}. "
                        is OfflineRuzneCasti -> "Nejstarší část tohoto rozvrhu pochází z ${it.nejstarsi.dayOfMonth}. ${it.nejstarsi.monthValue}. ${it.nejstarsi.hour}:${it.nejstarsi.minute.nula()}. "
                        Online -> ""
                    }
                }?.plus("Pro aktualizaci dat klikněte Stáhnout vše.") ?: "Prohlížíte si aktuální rozvrh.",
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

private fun Modifier.doubleScrollable(
    scrollStateX: ScrollState,
    scrollStateY: ScrollState
) = composed {
    val coroutineScope = rememberCoroutineScope()

    val flingBehaviorX = ScrollableDefaults.flingBehavior()
    val flingBehaviorY = ScrollableDefaults.flingBehavior()

    val velocityTracker = remember { VelocityTracker() }
    val nestedScrollDispatcher = remember { NestedScrollDispatcher() }

    pointerInput(Unit) {
        detectDragGestures(
            onDrag = { pointerInputChange, offset ->
                coroutineScope.launch {
                    velocityTracker.addPointerInputChange(pointerInputChange)
                    scrollStateX.scrollBy(offset.x)
                    scrollStateY.scrollBy(offset.y)
                }
            },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity()
                velocityTracker.resetTracking()
                coroutineScope.launch {
                    scrollStateX.scroll {
                        val scrollScope = object : ScrollScope {
                            override fun scrollBy(pixels: Float): Float {
                                val consumedByPreScroll = nestedScrollDispatcher.dispatchPreScroll(Offset(pixels, 0F), NestedScrollSource.Fling).x
                                val scrollAvailableAfterPreScroll = pixels - consumedByPreScroll
                                val consumedBySelfScroll = this@scroll.scrollBy(scrollAvailableAfterPreScroll)
                                val deltaAvailableAfterScroll = scrollAvailableAfterPreScroll - consumedBySelfScroll
                                val consumedByPostScroll = nestedScrollDispatcher.dispatchPostScroll(
                                    Offset(consumedBySelfScroll, 0F),
                                    Offset(deltaAvailableAfterScroll, 0F),
                                    NestedScrollSource.Fling
                                ).x
                                return consumedByPreScroll + consumedBySelfScroll + consumedByPostScroll
                            }
                        }

                        with(flingBehaviorX) {
                            scrollScope.performFling(velocity.x)
                        }
                    }
                }
                coroutineScope.launch {
                    scrollStateY.scroll {
                        val scrollScope = object : ScrollScope {
                            override fun scrollBy(pixels: Float): Float {
                                val consumedByPreScroll = nestedScrollDispatcher.dispatchPreScroll(Offset(0F, pixels), NestedScrollSource.Fling).y
                                val scrollAvailableAfterPreScroll = pixels - consumedByPreScroll
                                val consumedBySelfScroll = this@scroll.scrollBy(scrollAvailableAfterPreScroll)
                                val deltaAvailableAfterScroll = scrollAvailableAfterPreScroll - consumedBySelfScroll
                                val consumedByPostScroll = nestedScrollDispatcher.dispatchPostScroll(
                                    Offset(0F, consumedBySelfScroll),
                                    Offset(0F, deltaAvailableAfterScroll),
                                    NestedScrollSource.Fling
                                ).y
                                return consumedByPreScroll + consumedBySelfScroll + consumedByPostScroll
                            }
                        }

                        with(flingBehaviorY) {
                            scrollScope.performFling(velocity.y)
                        }
                    }
                }
            },
            onDragStart = {
                velocityTracker.resetTracking()
            }
        )
    }
}

fun Int.nula(): String = if ("$this".length == 1) "0$this" else "$this"