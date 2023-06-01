package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.App.Companion.navigate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Destination
@RootNavGraph(start = true)
@Composable
fun RozvrhScreen(
    vjec: Vjec? = null,
    stalost: Stalost? = null,
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<RozvrhViewModel> {
        parametersOf(vjec, stalost, navigator.navigate)
    }

    val tabulka by viewModel.tabulka.collectAsStateWithLifecycle()

    RozvrhScreen(
        tabulka = tabulka,
        vjec = viewModel.vjec,
        stalost = viewModel.stalost,
        vybratRozvrh = viewModel::vybratRozvrh,
        zmenitStalost = viewModel::zmenitStalost,
        stahnoutVse = viewModel.stahnoutVse,
        navigate = navigator.navigate,
        najdiMiVolnouTridu = viewModel::najdiMivolnouTridu
    )
}

@Composable
fun RozvrhScreen(
    tabulka: Tyden?,
    vjec: Vjec,
    stalost: Stalost,
    vybratRozvrh: (Vjec) -> Unit,
    zmenitStalost: (Stalost) -> Unit,
    stahnoutVse: ((String) -> Unit) -> Unit,
    navigate: (Direction) -> Unit,
    najdiMiVolnouTridu: (Stalost, Int, Int, (String) -> Unit, (List<Vjec.MistnostVjec>?) -> Unit) -> Unit,
) = Scaffold(
    topBar = {
        AppBar(
            stahnoutVse = stahnoutVse,
            navigate = navigate,
            najdiMiVolnouTridu = najdiMiVolnouTridu,
        )
    }
) { paddingValues ->
    if (tabulka == null) AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Text(text = "Načítání")
        },
        text = {
            CircularProgressIndicator()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )
    else Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Vybiratko(
                seznam = Vjec.tridy,
                value = if (vjec is Vjec.TridaVjec) vjec else Vjec.TridaVjec.Tridy,
            ) { vjec ->
                if (vjec == Vjec.TridaVjec.Tridy) return@Vybiratko
                vybratRozvrh(vjec)
            }

            Vybiratko(
                seznam = Stalost.values().toList(),
                value = stalost
            ) { novaStalost ->
                zmenitStalost(novaStalost)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Vybiratko(
                seznam = Vjec.mistnosti,
                value = if (vjec is Vjec.MistnostVjec) vjec else Vjec.MistnostVjec.Mistnosti,
            ) { vjec ->
                if (vjec == Vjec.MistnostVjec.Mistnosti) return@Vybiratko
                vybratRozvrh(vjec)
            }

            Vybiratko(
                seznam = Vjec.vyucujici,
                value = if (vjec is Vjec.VyucujiciVjec) vjec else Vjec.VyucujiciVjec.Vyucujici,
            ) { vjec ->
                if (vjec == Vjec.VyucujiciVjec.Vyucujici) return@Vybiratko
                vybratRozvrh(vjec)
            }
        }

        Tabulka(
            tabulka = tabulka,
            kliklNaNeco = { vjec ->
                vybratRozvrh(vjec)
            },
        )
    }
}

@Composable
private fun Tabulka(
    tabulka: Tyden,
    kliklNaNeco: (vjec: Vjec) -> Unit,
) {
    if (tabulka.isEmpty()) return

    val horScrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .border(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1F)
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .size(60.dp, 60.dp)
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(horScrollState)
                .border(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            tabulka.first().drop(1).forEach { cisloHodiny ->

                Box(
                    modifier = Modifier
                        .aspectRatio(1F)
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                        .size(120.dp, 60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            text = cisloHodiny.first().predmet,
                            modifier = Modifier
                                .padding(all = 8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Text(
                            text = cisloHodiny.first().vyucujici,
                            modifier = Modifier
                                .padding(all = 8.dp)
                        )
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        item {
            Row {
                val maxy = tabulka.map { radek -> radek.maxOf { hodina -> hodina.size } }

                Column(
                    Modifier.horizontalScroll(rememberScrollState())
                ) {
                    tabulka.drop(1).map { it.first() }.forEachIndexed { i, den ->
                        Column(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1F)
                                    .border(1.dp, MaterialTheme.colorScheme.primary)
                                    .size(60.dp, 120.dp * maxy[i + 1])
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = den.first().predmet,
                                        modifier = Modifier
                                            .padding(all = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    Modifier.horizontalScroll(horScrollState)
                ) {
                    tabulka.drop(1).forEachIndexed { i, radek ->
                        Row {
                            radek.drop(1).forEach { hodina ->
                                Column(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    hodina.forEach { bunka ->
                                        bunka.Compose(
                                            bunekVHodine = hodina.size,
                                            maxBunekDne = maxy[i + 1],
                                            kliklNaNeco = kliklNaNeco
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Vjec> Vybiratko(
    seznam: List<T>,
    value: T,
    poklik: (vjec: T) -> Unit
) = Vybiratko(
    seznam = seznam.map { it.jmeno },
    aktualIndex = seznam.indexOf(value),
    poklik = {
        poklik(seznam[it])
    },
)

@Composable
fun Vybiratko(
    seznam: List<Stalost>,
    value: Stalost,
    poklik: (vjec: Stalost) -> Unit
) = Vybiratko(
    seznam = seznam.map { it.nazev },
    aktualIndex = seznam.indexOf(value),
    poklik = {
        poklik(seznam[it])
    },
)

@Composable
fun Vybiratko(
    seznam: List<String>,
    aktualIndex: Int,
    poklik: (i: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(all = 8.dp)
    ) {
        var vidimMenu by remember { mutableStateOf(false) }

        DropdownMenu(
            expanded = vidimMenu,
            onDismissRequest = { vidimMenu = false }
        ) {
            seznam.forEachIndexed { i, x ->

                DropdownMenuItem(
                    text = { Text(x) },
                    onClick = {
                        vidimMenu = false
                        poklik(i)
                    },
                )
            }
        }

        OutlinedButton(
            onClick = {
                vidimMenu = true
            }
        ) {
            Text(seznam[aktualIndex])
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = "Vyberte",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }
}