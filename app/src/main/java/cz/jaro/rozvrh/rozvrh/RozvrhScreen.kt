package cz.jaro.rozvrh.rozvrh

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import cz.jaro.rozvrh.ResponsiveText
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
        parametersOf(RozvrhViewModel.Parameters(vjec, stalost, navigator.navigate))
    }

    val tabulka by viewModel.tabulka.collectAsStateWithLifecycle()
    val realVjec by viewModel.vjec.collectAsStateWithLifecycle()

    val tridy by viewModel.tridy.collectAsStateWithLifecycle()
    val mistnosti by viewModel.mistnosti.collectAsStateWithLifecycle()
    val vyucujici by viewModel.vyucujici.collectAsStateWithLifecycle()
    val mujRozvrh by viewModel.mujRozvrh.collectAsStateWithLifecycle()
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
        rozvrhOfflineWarning = tabulka?.second,
        tridy = tridy,
        mistnosti = mistnosti,
        vyucujici = vyucujici,
        mujRozvrh = mujRozvrh,
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
    rozvrhOfflineWarning: String?,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    mujRozvrh: Boolean,
    zmenitMujRozvrh: () -> Unit,
    zobrazitMujRozvrh: Boolean,
) = Scaffold(
    topBar = {
        AppBar(
            stahnoutVse = stahnoutVse,
            navigate = navigate,
            najdiMiVolnouTridu = najdiMiVolnouTridu,
        )
    }
) { paddingValues ->
    if (vjec == null || tridy.size <= 1) LinearProgressIndicator(
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
            var expanded1 by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded1,
                onExpandedChange = { expanded1 = !expanded1 },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(),
                    readOnly = true,
                    value = if (vjec is Vjec.TridaVjec) vjec.jmeno else "",
                    onValueChange = {},
                    label = { Text(tridy.first().jmeno) },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1)

                            if (zobrazitMujRozvrh) IconButton(
                                onClick = {
                                    expanded1 = false
                                    zmenitMujRozvrh()
                                }
                            ) {
                                Icon(if (mujRozvrh) Icons.Default.PeopleAlt else Icons.Default.Person, null)
                            }
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expanded1,
                    onDismissRequest = { expanded1 = false },
                ) {
                    tridy.map { it.jmeno }.drop(1).forEachIndexed { i, option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                vybratRozvrh(tridy[i + 1])
                                expanded1 = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            var expanded2 by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded2,
                onExpandedChange = { expanded2 = !expanded2 },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(),
                    readOnly = true,
                    value = stalost.nazev,
                    onValueChange = {},
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = expanded2,
                    onDismissRequest = { expanded2 = false },
                ) {
                    Stalost.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.nazev) },
                            onClick = {
                                zmenitStalost(option)
                                expanded2 = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
        var napoveda by remember { mutableStateOf(false) }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            var expanded1 by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded1,
                onExpandedChange = { expanded1 = !expanded1 },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(),
                    readOnly = true,
                    value = if (vjec is Vjec.MistnostVjec) vjec.jmeno else "",
                    onValueChange = {},
                    label = { Text(mistnosti.first().jmeno) },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1)

                            IconButton(
                                onClick = {
                                    expanded1 = false
                                    napoveda = true
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Help, null)
                            }
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expanded1,
                    onDismissRequest = { expanded1 = false },
                ) {
                    mistnosti.map { it.jmeno }.drop(1).forEachIndexed { i, option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                vybratRozvrh(mistnosti[i + 1])
                                expanded1 = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
            var expanded2 by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded2,
                onExpandedChange = { expanded2 = !expanded2 },
                Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(),
                    readOnly = true,
                    value = if (vjec is Vjec.VyucujiciVjec) vjec.jmeno else "",
                    onValueChange = {},
                    label = { Text(vyucujici.first().jmeno) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expanded2,
                    onDismissRequest = { expanded2 = false },
                ) {
                    vyucujici.map { it.jmeno }.drop(1).forEachIndexed { i, option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                vybratRozvrh(vyucujici[i + 1])
                                expanded2 = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
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
                Text("Nápověda k místnostím")
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

context(ColumnScope)
@Composable
private fun Tabulka(
    tabulka: Tyden,
    kliklNaNeco: (vjec: Vjec) -> Unit,
    rozvrhOfflineWarning: String?,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    mujRozvrh: Boolean,
) {
    if (tabulka.isEmpty()) return

    val horScrollState = rememberScrollState()

    val maxy = tabulka.map { radek -> radek.maxOf { hodina -> hodina.size } }
    val polovicniBunky = remember(tabulka) {
        tabulka.map { radek -> radek.maxBy { it.size }.size >= (if (mujRozvrh) 2 else 4) }
    }

    Row(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
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
                .horizontalScroll(horScrollState)
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

    LazyColumn(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        item {
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
                    Modifier.horizontalScroll(horScrollState)
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
        }
        item {
            Text(
                rozvrhOfflineWarning?.plus(" Pro aktualizaci dat klikněte Stáhnout vše.") ?: "Prohlížíte si aktuální rozvrh.",
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun Vybiratko(
    seznam: List<Stalost>,
    value: Stalost,
    modifier: Modifier = Modifier,
    poklik: (vjec: Stalost) -> Unit,
) = Vybiratko(
    seznam = seznam.map { it.nazev },
    aktualIndex = seznam.indexOf(value).takeIf { it != -1 } ?: 0,
    modifier,
    poklik = {
        poklik(seznam[it])
    },
)

@Composable
fun Vybiratko(
    seznam: List<String>,
    aktualIndex: Int,
    modifier: Modifier = Modifier,
    nulaDisabled: Boolean = false,
    poklik: (i: Int) -> Unit,
) {
    Box(
        modifier = modifier
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
                    enabled = !(nulaDisabled && i == 0)
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