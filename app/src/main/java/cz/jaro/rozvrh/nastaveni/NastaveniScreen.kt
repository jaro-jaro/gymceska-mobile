package cz.jaro.rozvrh.nastaveni

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cz.jaro.rozvrh.BuildConfig
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.PrepnoutRozvrhWidget
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.rozvrh.Vybiratko
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Destination
@Composable
fun NastaveniScreen(
    navigator: DestinationsNavigator
) {
    val viewModel = koinViewModel<NastaveniViewModel> {
        parametersOf()
    }

    val tridy by viewModel.tridyFlow.collectAsStateWithLifecycle(emptyList())
    val nastaveni by viewModel.nastaveni.collectAsStateWithLifecycle(null)
    val skupiny by viewModel.skupiny.collectAsStateWithLifecycle(null)

    NastaveniScreen(
        navigateBack = navigator::navigateUp,
        nastaveni = nastaveni,
        upravitNastaveni = viewModel::upravitNastaveni,
        tridy = tridy,
        skupiny = skupiny,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun NastaveniScreen(
    navigateBack: () -> Unit,
    nastaveni: Nastaveni?,
    upravitNastaveni: ((Nastaveni) -> Nastaveni) -> Unit,
    tridy: List<Vjec.TridaVjec>,
    skupiny: Sequence<String>?,
) = Surface {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.nastaveni))
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.zpet))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (nastaveni == null) LinearProgressIndicator(Modifier.padding(paddingValues))
        else LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.urcit_tmavy_rezim_podle_systemu))
                    Switch(
                        checked = nastaveni.darkModePodleSystemu,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(darkModePodleSystemu = it)
                            }
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.tmavy_rezim))
                    Switch(
                        checked = if (nastaveni.darkModePodleSystemu) isSystemInDarkTheme() else nastaveni.darkMode,
                        enabled = !nastaveni.darkModePodleSystemu,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(darkMode = it)
                            }
                        }
                    )
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.pouzivat_dynamicke_barvy))
                    Switch(
                        checked = nastaveni.dynamicColors,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(dynamicColors = it)
                            }
                        }
                    )
                }
            }
            item {
                Text("Podle čeho chcete určit přepnutí widgetu na další den?")
                Vybiratko(
                    seznam = listOf(
                        "Vždy o půlnoci",
                        "V specifický čas",
                        "Daný počet hodin po konci vyučování",
                    ),
                    aktualIndex = when (nastaveni.prepnoutRozvrhWidget) {
                        is PrepnoutRozvrhWidget.OPulnoci -> 0
                        is PrepnoutRozvrhWidget.VCas -> 1
                        is PrepnoutRozvrhWidget.PoKonciVyucovani -> 2
                    },
                    poklik = {
                        upravitNastaveni { nast ->
                            nast.copy(
                                prepnoutRozvrhWidget = when (it) {
                                    0 -> PrepnoutRozvrhWidget.OPulnoci
                                    1 -> PrepnoutRozvrhWidget.VCas(16, 0)
                                    2 -> PrepnoutRozvrhWidget.PoKonciVyucovani(2)
                                    else -> throw IllegalArgumentException("WTF")
                                }
                            )
                        }
                    },
                )
                if (nastaveni.prepnoutRozvrhWidget is PrepnoutRozvrhWidget.VCas) {
                    var hm by remember { mutableStateOf(nastaveni.prepnoutRozvrhWidget.cas.toString()) }
                    var dialog by remember { mutableStateOf(false) }
                    if (dialog) TimePickerDialog(
                        initialTime = try {
                            LocalTime.parse(hm)
                        } catch (e: DateTimeParseException) {
                            nastaveni.prepnoutRozvrhWidget.cas
                        },
                        onTimeChange = {
                            dialog = false
                            hm = it.toString()
                            upravitNastaveni { nast ->
                                nast.copy(prepnoutRozvrhWidget = PrepnoutRozvrhWidget.VCas(it))
                            }
                        },
                        title = {
                            Text("Vyberte čas")
                        },
                        onDismissRequest = {
                            dialog = false
                        },
                    )

                    OutlinedTextField(
                        value = hm,
                        onValueChange = {},
                        Modifier
                            .fillMaxWidth()
                            .onKeyEvent {
                                if (it.key == Key.Enter) {
                                    dialog = true
                                }
                                return@onKeyEvent it.key == Key.Enter
                            },
                        label = {
                            Text("Čas")
                        },
                        singleLine = true,
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            dialog = true
                                        }
                                    }
                                }
                            },
                        readOnly = true,
                    )
                    Text(if (hm == nastaveni.prepnoutRozvrhWidget.cas.toString()) "" else "Uloženo: ${nastaveni.prepnoutRozvrhWidget.cas}")
                }
                if (nastaveni.prepnoutRozvrhWidget is PrepnoutRozvrhWidget.PoKonciVyucovani) {
                    var h by remember { mutableStateOf(nastaveni.prepnoutRozvrhWidget.poHodin.toString()) }
                    OutlinedTextField(
                        value = h,
                        onValueChange = {
                            h = it
                            it.toIntOrNull() ?: return@OutlinedTextField
                            upravitNastaveni { nast ->
                                nast.copy(prepnoutRozvrhWidget = PrepnoutRozvrhWidget.PoKonciVyucovani(poHodin = it.toInt()))
                            }
                        },
                        Modifier.fillMaxWidth(),
                        label = {
                            Text("Počet hodin")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                    )
                    Text(if (h == nastaveni.prepnoutRozvrhWidget.poHodin.toString()) "" else "Uloženo: ${nastaveni.prepnoutRozvrhWidget.poHodin}")
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.zvolte_svou_tridu))
                    Vybiratko(
                        seznam = tridy.map { it.jmeno },
                        aktualIndex = tridy.indexOf(nastaveni.mojeTrida)
                    ) {
                        upravitNastaveni { nastaveni ->
                            nastaveni.copy(mojeTrida = tridy[it])
                        }
                    }
                }
            }
            if (skupiny == null) item {
                LinearProgressIndicator()
            }
            else items(skupiny.toList()) { skupina ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = skupina in nastaveni.mojeSkupiny,
                        onCheckedChange = { chciJi ->
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(mojeSkupiny = if (chciJi) nastaveni.mojeSkupiny + skupina else nastaveni.mojeSkupiny - skupina)
                            }
                        }
                    )
                    Text(skupina)
                }
            }
            item {
                Text(stringResource(R.string.verze_aplikace, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
            }
            item {
                Text("Simulate crash...", Modifier.clickable {
                    throw RuntimeException("Test exception")
                }, fontSize = 10.sp)
            }
        }
    }
}

fun Int.nula(): String = if ("$this".length == 1) "0$this" else "$this"
