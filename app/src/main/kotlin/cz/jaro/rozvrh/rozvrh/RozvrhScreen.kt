package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
                    else IconButton(
                        onClick = {
                            hide()
                            vybratRozvrh(Vjec.TridaVjec("HOME"))
                        }
                    ) {
                        Icon(Icons.Default.Home, null)
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
            horScrollState = horScrollState,
            verScrollState = verScrollState,
        )
    }
}

