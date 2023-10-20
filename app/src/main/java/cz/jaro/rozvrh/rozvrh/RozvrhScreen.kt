package cz.jaro.rozvrh.rozvrh

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.App.Companion.navigate
import cz.jaro.rozvrh.ResponsiveText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Destination
@RootNavGraph(start = true)
@Composable
fun RozvrhScreen(
    vjec: Vjec? = null,
    mujRozvrh: Boolean? = null,
    stalost: Stalost? = null,
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<RozvrhViewModel> {
        parametersOf(RozvrhViewModel.Parameters(vjec, stalost, mujRozvrh, navigator.navigate))
    }

    val tabulka by viewModel.tabulka.collectAsStateWithLifecycle()
    val realVjec by viewModel.vjec.collectAsStateWithLifecycle()

    val tridy by viewModel.tridy.collectAsStateWithLifecycle()
    val mistnosti by viewModel.mistnosti.collectAsStateWithLifecycle()
    val vyucujici by viewModel.vyucujici.collectAsStateWithLifecycle()
    val realMujRozvrh by viewModel.mujRozvrh.collectAsStateWithLifecycle()
    val zobrazitMujRozvrh by viewModel.zobrazitMujRozvrh.collectAsStateWithLifecycle()

    RozvrhScreen(
        tabulka = tabulka?.first,
        vjec = realVjec,
        stalost = viewModel.stalost,
        vybratRozvrh = viewModel::vybratRozvrh,
        zmenitStalost = viewModel::zmenitStalost,
        stahnoutVse = viewModel.stahnoutVse,
        navigate = navigator.navigate,
        najdiMiVolnouTridu = viewModel::najdiMivolnouTridu,
        najdiMiVolnehoUcitele = viewModel::najdiMiVolnehoUcitele,
        rozvrhOfflineWarning = tabulka?.second,
        tridy = tridy,
        mistnosti = mistnosti,
        vyucujici = vyucujici,
        mujRozvrh = realMujRozvrh,
        zmenitMujRozvrh = viewModel::zmenitMujRozvrh,
        zobrazitMujRozvrh = zobrazitMujRozvrh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RozvrhScreen(
    tabulka: Tyden?,
    vjec: Vjec?,
    stalost: Stalost,
    vybratRozvrh: (Vjec) -> Unit,
    zmenitStalost: (Stalost) -> Unit,
    stahnoutVse: ((String) -> Unit, () -> Unit) -> Unit,
    navigate: (Direction) -> Unit,
    najdiMiVolnouTridu: (Stalost, Int, Int, (String) -> Unit, (List<Vjec.MistnostVjec>?) -> Unit) -> Unit,
    najdiMiVolnehoUcitele: (Stalost, Int, Int, (String) -> Unit, (List<Vjec.VyucujiciVjec>?) -> Unit) -> Unit,
    rozvrhOfflineWarning: String?,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    mujRozvrh: Boolean?,
    zmenitMujRozvrh: () -> Unit,
    zobrazitMujRozvrh: Boolean,
) = Scaffold(
    topBar = {
        AppBar(
            stahnoutVse = stahnoutVse,
            navigate = navigate,
            najdiMiVolnouTridu = najdiMiVolnouTridu,
            najdiMiVolnehoUcitele = najdiMiVolnehoUcitele,
            tabulka = tabulka,
            vybratRozvrh = vybratRozvrh,
        )
    }
) { paddingValues ->
    if (vjec == null || mujRozvrh == null || tridy.size <= 1) LinearProgressIndicator(
        Modifier
            .padding(paddingValues)
            .fillMaxWidth()
    )
    else Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Vybiratko(
                value = if (vjec is Vjec.TridaVjec) vjec else null,
                seznam = tridy.drop(1),
                onClick = { i, _ -> vybratRozvrh(tridy[i + 1]) },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
                label = tridy.first().jmeno,
                trailingIcon = { hide ->
                    if (zobrazitMujRozvrh) IconButton(
                        onClick = {
                            hide()
                            zmenitMujRozvrh()
                        }
                    ) {
                        Icon(if (mujRozvrh) Icons.Default.PeopleAlt else Icons.Default.Person, null)
                    }
                },
            )

            Vybiratko(
                value = stalost,
                seznam = Stalost.dnesniEntries(),
                onClick = { _, stalost -> zmenitStalost(stalost) },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
            )
        }
        var napoveda by remember { mutableStateOf(false) }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Vybiratko(
                value = if (vjec is Vjec.MistnostVjec) vjec else null,
                seznam = mistnosti.drop(1),
                onClick = { i, _ -> vybratRozvrh(mistnosti[i + 1]) },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
                label = mistnosti.first().jmeno,
                trailingIcon = { hide ->
                    IconButton(
                        onClick = {
                            hide()
                            napoveda = true
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Help, null)
                    }
                },
            )

            Vybiratko(
                value = if (vjec is Vjec.VyucujiciVjec) vjec else null,
                seznam = vyucujici.drop(1),
                onClick = { i, _ -> vybratRozvrh(vyucujici[i + 1]) },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
                label = vyucujici.first().jmeno,
            )
        }
        if (napoveda) AlertDialog(
            onDismissRequest = {
                napoveda = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        napoveda = false
                    }
                ) {
                    Text("OK")
                }
            },
            title = {
                Text("Nápověda k místnostem")
            },
            text = {
                LazyColumn {
                    items(mistnosti.drop(1)) {
                        Text("${it.jmeno} - to je${it.napoveda}")
                    }
                }
            }
        )

        if (tabulka == null) LinearProgressIndicator(Modifier.fillMaxWidth())
        else Tabulka(
            vjec = vjec,
            tabulka = tabulka,
            kliklNaNeco = { vjec ->
                vybratRozvrh(vjec)
            },
            rozvrhOfflineWarning = rozvrhOfflineWarning,
            tridy = tridy,
            mistnosti = mistnosti,
            vyucujici = vyucujici,
            mujRozvrh = mujRozvrh,
        )
    }
}

@ExperimentalMaterial3Api
@Composable
fun Vybiratko(
    value: Stalost,
    seznam: List<Stalost>,
    onClick: (Int, Stalost) -> Unit,
    modifier: Modifier = Modifier,
    zbarvit: Boolean = false,
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
) = Vybiratko(
    value = value.nazev,
    seznam = seznam.map { it.nazev },
    onClick = { i, _ -> onClick(i, seznam[i]) },
    modifier = modifier,
    trailingIcon = trailingIcon,
    zbarvit = zbarvit,
    zaskrtavatko = { false }
)

@ExperimentalMaterial3Api
@Composable
fun Vybiratko(
    value: Vjec?,
    seznam: List<Vjec>,
    onClick: (Int, Vjec) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    zbarvit: Boolean = true,
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
) = Vybiratko(
    value = value?.jmeno ?: "",
    seznam = seznam.map { it.jmeno },
    onClick = { i, _ -> onClick(i, seznam[i]) },
    modifier = modifier,
    label = label,
    trailingIcon = trailingIcon,
    zbarvit = zbarvit,
    zaskrtavatko = { false }
)

@Composable
@ExperimentalMaterial3Api
fun Vybiratko(
    index: Int,
    seznam: List<String>,
    onClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    zaskrtavatko: (String) -> Boolean = { it == seznam[index] },
    zbarvit: Boolean = false,
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
) = Vybiratko(
    value = seznam[index],
    seznam = seznam,
    onClick = onClick,
    modifier = modifier,
    label = label,
    zbarvit = zbarvit,
    trailingIcon = trailingIcon,
    zaskrtavatko = zaskrtavatko,
)

@Composable
@ExperimentalMaterial3Api
fun Vybiratko(
    value: String,
    seznam: List<String>,
    onClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    zbarvit: Boolean = false,
    zaskrtavatko: (String) -> Boolean = { it == value },
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
    zavirat: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = value,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trailingIcon?.invoke {
                        expanded = false
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = if (zbarvit) ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
            ) else ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            val zaskrtavatka = seznam.map(zaskrtavatko)
            seznam.forEachIndexed { i, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onClick(i, option)
                        if (zavirat) expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    leadingIcon = if (zaskrtavatka.any { it }) (@Composable {
                        if (zaskrtavatka[i]) Icon(Icons.Default.Check, null)
                    }) else null
                )
            }
        }
    }
}

context(ColumnScope)
@Composable
private fun Tabulka(
    vjec: Vjec,
    tabulka: Tyden,
    kliklNaNeco: (vjec: Vjec) -> Unit,
    rozvrhOfflineWarning: String?,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    mujRozvrh: Boolean,
) {
    if (tabulka.isEmpty()) return

    val horScrollState = rememberScrollState(Int.MAX_VALUE)
    val verScrollState = rememberScrollState(Int.MAX_VALUE)

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
                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2)
                )
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(horScrollState, enabled = false, reverseScrolling = true)
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                tabulka.first().drop(1).map { it.first() }.forEach { bunka ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(2F / 1)
                            .border(1.dp, MaterialTheme.colorScheme.secondary)
                            .size(zakladniVelikostBunky, zakladniVelikostBunky / 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.matchParentSize(),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            ResponsiveText(
                                text = bunka.predmet,
                                modifier = Modifier
                                    .padding(all = 8.dp),
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
                                    .animateContentSize()
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
                                            .padding(all = 8.dp),
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
                        Row {
                            radek.drop(1).forEach { hodina ->
                                Column(
                                    modifier = Modifier
                                        .animateContentSize()
                                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                                ) {
                                    hodina.forEach { bunka ->
                                        bunka.Compose(
                                            bunekVHodine = if (polovicniBunky[i + 1]) hodina.size * 2 else hodina.size,
                                            maxBunekDne = maxy[i + 1],
                                            kliklNaNeco = kliklNaNeco,
                                            tridy = tridy,
                                            mistnosti = mistnosti,
                                            vyucujici = vyucujici,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                rozvrhOfflineWarning?.plus(" Pro aktualizaci dat klikněte Stáhnout vše.") ?: "Prohlížíte si aktuální rozvrh.",
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
    val maximumFlingVelocity = LocalViewConfiguration.current.maximumFlingVelocity.toFloat()

    pointerInput(maximumFlingVelocity) {
        detectDragGestures(
            onDrag = { pointerInputChange, offset ->
                coroutineScope.launch {
                    velocityTracker.addPointerInputChange(pointerInputChange)
                    scrollStateX.scrollBy(offset.x)
                    scrollStateY.scrollBy(offset.y)
                }
            },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity(maximumVelocity = Velocity(maximumFlingVelocity, maximumFlingVelocity))
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