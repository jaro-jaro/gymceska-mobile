package cz.jaro.rozvrh.rozvrh

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import cz.jaro.compose_dialog.dialogState
import cz.jaro.compose_dialog.show
import cz.jaro.rozvrh.App.Companion.navigate
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Route
import cz.jaro.rozvrh.ZdrojRozvrhu
import org.koin.compose.getKoin

@Composable
fun Rozvrh(
    args: Route.Rozvrh,
    navController: NavController,
) {
    val horScrollState = rememberScrollState(args.horScroll ?: Int.MAX_VALUE)
    val verScrollState = rememberScrollState(args.verScroll ?: Int.MAX_VALUE)

    val repo = getKoin().get<Repository>()
    val viewModel = viewModel<RozvrhViewModel> {
        RozvrhViewModel(
            repo = repo,
            params = RozvrhViewModel.Parameters(
                vjec = args.vjec,
                stalost = args.stalost?.let { Stalost.valueOf(it) },
                mujRozvrh = args.mujRozvrh,
                horScrollState = horScrollState,
                verScrollState = verScrollState,
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.navigovat = navController.navigate
    }

    val tabulka by viewModel.tabulka.collectAsStateWithLifecycle()
    val realVjec by viewModel.vjec.collectAsStateWithLifecycle()

    val tridy by viewModel.tridy.collectAsStateWithLifecycle()
    val mistnosti by viewModel.mistnosti.collectAsStateWithLifecycle()
    val vyucujici by viewModel.vyucujici.collectAsStateWithLifecycle()
    val realMujRozvrh by viewModel.mujRozvrh.collectAsStateWithLifecycle()
    val zobrazitMujRozvrh by viewModel.zobrazitMujRozvrh.collectAsStateWithLifecycle()
    val zoom by viewModel.zoom.collectAsStateWithLifecycle()
    val alwaysTwoRowCells by viewModel.alwaysTwoRowCells.collectAsStateWithLifecycle()
    val currentlyDownloading by viewModel.currentlyDownloading.collectAsStateWithLifecycle()

    RozvrhContent(
        tabulka = tabulka?.rozvrh,
        vjec = realVjec,
        stalost = viewModel.stalost,
        vybratRozvrh = viewModel::vybratRozvrh,
        zmenitStalost = viewModel::zmenitStalost,
        stahnoutVse = viewModel.stahnoutVse,
        navigate = navController.navigate,
        najdiMiVolnouTridu = viewModel::najdiMivolnouTridu,
        najdiMiVolnehoUcitele = viewModel::najdiMiVolnehoUcitele,
        rozvrhOfflineWarning = tabulka?.zdroj,
        tridy = tridy,
        mistnosti = mistnosti,
        vyucujici = vyucujici,
        mujRozvrh = realMujRozvrh,
        zmenitMujRozvrh = viewModel::zmenitMujRozvrh,
        zobrazitMujRozvrh = zobrazitMujRozvrh,
        horScrollState = horScrollState,
        verScrollState = verScrollState,
        zoom = zoom,
        alwaysTwoRowCells = alwaysTwoRowCells,
        currentlyDownloading = currentlyDownloading,
    )
}

@Composable
fun RozvrhContent(
    tabulka: Tyden?,
    vjec: Vjec?,
    stalost: Stalost,
    vybratRozvrh: (Vjec) -> Unit,
    zmenitStalost: (Stalost) -> Unit,
    stahnoutVse: () -> Unit,
    navigate: (Route) -> Unit,
    najdiMiVolnouTridu: (Stalost, Int, List<Int>, List<FiltrNajdiMi>, (String) -> Unit, (List<Vjec.MistnostVjec>?) -> Unit) -> Unit,
    najdiMiVolnehoUcitele: (Stalost, Int, List<Int>, List<FiltrNajdiMi>, (String) -> Unit, (List<Vjec.VyucujiciVjec>?) -> Unit) -> Unit,
    rozvrhOfflineWarning: ZdrojRozvrhu?,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    mujRozvrh: Boolean?,
    zmenitMujRozvrh: () -> Unit,
    zobrazitMujRozvrh: Boolean,
    horScrollState: ScrollState,
    verScrollState: ScrollState,
    zoom: Float,
    alwaysTwoRowCells: Boolean,
    currentlyDownloading: Vjec.TridaVjec?,
) = RozvrhNavigation(
    stahnoutVse = stahnoutVse,
    navigate = navigate,
    najdiMiVolnouTridu = najdiMiVolnouTridu,
    najdiMiVolnehoUcitele = najdiMiVolnehoUcitele,
    tabulka = tabulka,
    vybratRozvrh = vybratRozvrh,
    currentlyDownloading = currentlyDownloading,
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val isInTabletMode = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
                || windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

        if (isInTabletMode) Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Vybiratko(vjec, zobrazitMujRozvrh, zmenitMujRozvrh, mujRozvrh, vybratRozvrh, tridy, mistnosti, vyucujici, Modifier.weight(1F))
            PrepinatkoStalosti(stalost, zmenitStalost, Modifier.weight(1F))
        } else {
            Vybiratko(vjec, zobrazitMujRozvrh, zmenitMujRozvrh, mujRozvrh, vybratRozvrh, tridy, mistnosti, vyucujici)
            PrepinatkoStalosti(stalost, zmenitStalost, Modifier.fillMaxWidth())
        }

        if (tabulka == null || vjec == null || mujRozvrh == null) LinearProgressIndicator(Modifier.fillMaxWidth())
        else CompositionLocalProvider(LocalBunkaZoom provides zoom) {
            Tabulka(
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
                horScrollState = horScrollState,
                verScrollState = verScrollState,
                alwaysTwoRowCells = alwaysTwoRowCells,
            )
        }
    }
}

@Composable
private fun PrepinatkoStalosti(
    stalost: Stalost,
    zmenitStalost: (Stalost) -> Unit,
    modifier: Modifier = Modifier,
) = SingleChoiceSegmentedButtonRow(
    modifier = modifier
        .padding(horizontal = 8.dp)
        .padding(top = 4.dp)
        .height(IntrinsicSize.Max),
) {
    Stalost.entries.forEachIndexed { i, it ->
        SegmentedButton(
            selected = stalost == it,
            onClick = { zmenitStalost(it) },
            shape = SegmentedButtonDefaults.itemShape(i, Stalost.entries.count()),
            Modifier.fillMaxHeight(),
        ) {
            Text(it.nazev)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Vybiratko(
    vjec: Vjec?,
    zobrazitMujRozvrh: Boolean,
    zmenitMujRozvrh: () -> Unit,
    mujRozvrh: Boolean?,
    vybratRozvrh: (Vjec) -> Unit,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = vjec?.nazev ?: "",
            onValueChange = {},
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(horizontal = 8.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            placeholder = {
                CircularProgressIndicator()
            },
            trailingIcon = {
                if (mujRozvrh != null) Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (zobrazitMujRozvrh) IconButton(
                        onClick = {
                            zmenitMujRozvrh()
                            expanded = false
                            focusManager.clearFocus()
                        }
                    ) { Icon(if (mujRozvrh) Icons.Default.PeopleAlt else Icons.Default.Person, null) }
                    else IconButton(
                        onClick = {
                            vybratRozvrh(Vjec.TridaVjec("HOME"))
                            expanded = false
                            focusManager.clearFocus()
                        }
                    ) { Icon(Icons.Default.Home, null) }

                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded, Modifier.minimumInteractiveComponentSize())
                }
            },
        )
        MyExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            }
        ) {
            MenuVybiratka(tridy, mistnosti, vyucujici, vybratRozvrh) {
                expanded = false
                focusManager.clearFocus()
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ExposedDropdownMenuBoxScope.MyExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    if (expandedState.currentState || expandedState.targetState) {
        Popup(
            onDismissRequest = {
                onDismissRequest()
            },
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = false,
                excludeFromSystemGesture = false,
            ),
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ) = anchorBounds.bottomLeft
            }
        ) {
            val transition = rememberTransition(expandedState, "DropDownMenu")

            val scale by transition.animateFloat(
                transitionSpec = {
                    if (false isTransitioningTo true) {
                        tween(durationMillis = 120, easing = LinearOutSlowInEasing)
                    } else {
                        tween(durationMillis = 1, delayMillis = 74)
                    }
                }
            ) { expanded ->
                if (expanded) 1F else 0.8F
            }

            val alpha by transition.animateFloat(
                transitionSpec = {
                    if (false isTransitioningTo true) {
                        tween(durationMillis = 30)
                    } else {
                        tween(durationMillis = 75)
                    }
                }
            ) { expanded ->
                if (expanded) 1F else 0F
            }

            Surface(
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    transformOrigin = TransformOrigin.Center
                },
                shape = MenuDefaults.shape,
                color = MenuDefaults.containerColor,
                tonalElevation = MenuDefaults.TonalElevation,
                shadowElevation = MenuDefaults.ShadowElevation,
            ) {
                Column(
                    Modifier.Companion
                        .exposedDropdownSize(false)
                        .padding(vertical = 8.dp)
                        .width(IntrinsicSize.Max),
                    content = content,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MenuVybiratka(
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    vybratRozvrh: (Vjec) -> Unit,
    hide: () -> Unit,
) {
    val seznamy = listOf(tridy, mistnosti, vyucujici)
    val nadpisy = seznamy.map { it.first().nazev }
    Row {
        nadpisy.forEachIndexed { j, nadpis ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(nadpis)
                        if (j == 1) NapovedaKMistostem(mistnosti)
                    }
                },
                onClick = {},
                Modifier.weight(listOf(5F, 7F, 12F)[j]),
                colors = MenuDefaults.itemColors(
                    disabledTextColor = MaterialTheme.colorScheme.primary
                ),
                enabled = false,
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
        }
    }
    Column(Modifier.verticalScroll(rememberScrollState())) {
        repeat(seznamy.maxOf { it.size - 1 }) { i ->
            val vjeci = seznamy.map { it.getOrNull(i + 1) }
            Row {
                vjeci.forEachIndexed { j, vjec ->
                    DropdownMenuItem(
                        text = {
                            if (vjec != null) Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(vjec.nazev)
                            }
                        },
                        onClick = {
                            vybratRozvrh(vjec!!)
                            hide()
                        },
                        Modifier.weight(listOf(5F, 7F, 12F)[j]),
                        enabled = vjec != null,
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun NapovedaKMistostem(mistnosti: List<Vjec.MistnostVjec>) = IconButton(
    onClick = {
        dialogState.show(
            confirmButton = { TextButton(::hide) { Text("OK") } },
            title = { Text("Nápověda k místnostem") },
            content = {
                LazyColumn {
                    items(mistnosti.drop(1)) {
                        Text("${it.nazev} - to je${it.napoveda}")
                    }
                }
            },
        )
    }
) {
    Icon(Icons.AutoMirrored.Filled.Help, null)
}