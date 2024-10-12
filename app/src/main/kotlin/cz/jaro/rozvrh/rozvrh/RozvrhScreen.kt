package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.compose_dialog.dialogState
import cz.jaro.compose_dialog.show
import cz.jaro.rozvrh.App.Companion.navigate
import cz.jaro.rozvrh.ZdrojRozvrhu
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Destination
@RootNavGraph(start = true)
@Composable
fun Rozvrh(
    vjec: Vjec? = null,
    mujRozvrh: Boolean? = null,
    stalost: Stalost? = null,
    horScroll: Int? = null,
    verScroll: Int? = null,
    navigator: DestinationsNavigator,
) {
    val horScrollState = rememberScrollState(horScroll ?: Int.MAX_VALUE)
    val verScrollState = rememberScrollState(verScroll ?: Int.MAX_VALUE)

    val viewModel = koinViewModel<RozvrhViewModel> {
        parametersOf(
            RozvrhViewModel.Parameters(
                vjec = vjec,
                stalost = stalost,
                mujRozvrh = mujRozvrh,
                navigovat = navigator.navigate,
                horScrollState = horScrollState,
                verScrollState = verScrollState,
            )
        )
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

    RozvrhContent(
        tabulka = tabulka?.rozvrh,
        vjec = realVjec,
        stalost = viewModel.stalost,
        vybratRozvrh = viewModel::vybratRozvrh,
        zmenitStalost = viewModel::zmenitStalost,
        stahnoutVse = viewModel.stahnoutVse,
        navigate = navigator.navigate,
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
    )
}

@Composable
fun RozvrhContent(
    tabulka: Tyden?,
    vjec: Vjec?,
    stalost: Stalost,
    vybratRozvrh: (Vjec) -> Unit,
    zmenitStalost: (Stalost) -> Unit,
    stahnoutVse: ((String) -> Unit, () -> Unit) -> Unit,
    navigate: (Direction) -> Unit,
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
        Vybiratko(vjec, zobrazitMujRozvrh, zmenitMujRozvrh, mujRozvrh, vybratRozvrh, tridy, mistnosti, vyucujici)

        PrepinatkoStalosti(stalost, zmenitStalost)

        if (tabulka == null) LinearProgressIndicator(Modifier.fillMaxWidth())
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
private fun PrepinatkoStalosti(stalost: Stalost, zmenitStalost: (Stalost) -> Unit) = Row(
    modifier = Modifier
        .padding(horizontal = 8.dp)
        .padding(top = 4.dp),
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .weight(1F)
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
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Vybiratko(
    vjec: Vjec,
    zobrazitMujRozvrh: Boolean,
    zmenitMujRozvrh: () -> Unit,
    mujRozvrh: Boolean,
    vybratRozvrh: (Vjec) -> Unit,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            value = vjec.nazev,
            onValueChange = {},
            trailingIcon = {
                Row(
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

        val scrollState = rememberScrollState()
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            },
            scrollState = scrollState,
        ) {
            MenuVybiratka(tridy, mistnosti, vyucujici, vybratRozvrh) {
                expanded = false
                focusManager.clearFocus()
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
    repeat(seznamy.maxOf { it.size }) { i ->
        val vjeci = seznamy.map { it.getOrNull(i) }
        Row {
            vjeci.forEachIndexed { j, vjec ->
                Box(
                    Modifier.weight(listOf(5F, 7F, 12F)[j])
                ) {
                    if (vjec != null) DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    vjec.nazev
                                )
                                if (i == 0 && j == 1) NapovedaKMistostem(mistnosti)
                            }
                        },
                        onClick = {
                            vybratRozvrh(vjec)
                            hide()
                        },
                        enabled = i != 0,
                        colors = MenuDefaults.itemColors(
                            disabledTextColor = MaterialTheme.colorScheme.primary
                        ),
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