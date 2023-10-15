package cz.jaro.rozvrh.nastaveni

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
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
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.rozvrh.Vybiratko
import cz.jaro.rozvrh.ui.theme.Theme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlin.reflect.KFunction3

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
        kopirovatVse = viewModel::kopirovatVse
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
    kopirovatVse: KFunction3<Stalost, (String) -> Unit, (String?) -> Unit, Unit>,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.zpet))
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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val dynamicColorsSupported = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                    val options = remember {
                        buildList {
                            if (dynamicColorsSupported) add("Dynamické")
                            addAll(Theme.entries.map { it.jmeno })
                        }
                    }
                    var expanded by remember { mutableStateOf(false) }
                    val selectedOption by remember(nastaveni.dynamicColors, nastaveni.tema) {
                        derivedStateOf {
                            when {
                                dynamicColorsSupported && nastaveni.dynamicColors -> options.first()
                                else -> nastaveni.tema.jmeno
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        TextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = selectedOption,
                            onValueChange = {},
                            label = { Text("Téma aplikace") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            options.forEachIndexed { i, option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        upravitNastaveni { nastaveni ->
                                            when {
                                                dynamicColorsSupported && i == 0 -> nastaveni.copy(dynamicColors = true)
                                                dynamicColorsSupported -> nastaveni.copy(tema = Theme.entries[i - 1], dynamicColors = false)
                                                else -> nastaveni.copy(tema = Theme.entries[i], dynamicColors = false)
                                            }
                                        }
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.zapnout_na_mym))
                    Switch(
                        checked = nastaveni.defaultMujRozvrh,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(defaultMujRozvrh = it)
                            }
                        }
                    )
                }
            }
            item {
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline, thickness = Dp.Hairline)
            }
            item {
                Text("Přepínat widget s rozvrhem další den"/* Modifier.padding(top = 16.dp)*/)
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
                    Modifier.padding(top = 8.dp),
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
            }
            if (nastaveni.prepnoutRozvrhWidget is PrepnoutRozvrhWidget.VCas) item {
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
            if (nastaveni.prepnoutRozvrhWidget is PrepnoutRozvrhWidget.PoKonciVyucovani) item {
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
            item {
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline, thickness = Dp.Hairline)
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
                val clipboardManager = LocalClipboardManager.current

                var kopirovatNastaveniDialog by remember { mutableStateOf(false) }
                var kopirovatDialog by remember { mutableStateOf(false) }
                var stalost by remember { mutableStateOf(Stalost.TentoTyden) }
                var nacitame by remember { mutableStateOf(false) }
                var podrobnostiNacitani by remember { mutableStateOf("") }

                if (nacitame) AlertDialog(
                    onDismissRequest = {
                        nacitame = false
                    },
                    confirmButton = {},
                    title = {
                        Text(text = podrobnostiNacitani)
                    },
                    text = {
                        CircularProgressIndicator()
                    },
                )

                if (kopirovatDialog) AlertDialog(
                    onDismissRequest = {
                        kopirovatDialog = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                kopirovatDialog = false
                            }
                        ) {
                            Text(text = stringResource(android.R.string.ok))
                        }
                    },
                    dismissButton = {},
                    title = {
                        Text(text = "Kopírovat rozvrhy")
                    },
                    text = {
                        Text("Hotovo!")
                    }
                )

                if (kopirovatNastaveniDialog) AlertDialog(
                    onDismissRequest = {
                        kopirovatNastaveniDialog = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                nacitame = true
                                kopirovatNastaveniDialog = false
                                podrobnostiNacitani = "Generuji text"

                                kopirovatVse(
                                    stalost,
                                    {
                                        podrobnostiNacitani = it
                                    },
                                    {
                                        if (it == null) {
                                            podrobnostiNacitani = "Nejste připojeni k internetu a nemáte staženou offline verzi všech rozvrhů tříd"
                                            return@kopirovatVse
                                        }
                                        clipboardManager.setText(AnnotatedString(it))
                                        kopirovatDialog = true
                                        nacitame = false
                                    }
                                )
                            }
                        ) {
                            Text(text = "Vygenerovat")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                kopirovatNastaveniDialog = false
                            }
                        ) {
                            Text(text = "Zrušit")
                        }
                    },
                    title = {
                        Text(text = "Kopírovat rozvrhy")
                    },
                    text = {
                        Column {
                            Vybiratko(
                                seznam = Stalost.entries,
                                value = stalost,
                            ) {
                                stalost = it
                            }
                        }
                    }
                )

                TextButton(
                    onClick = {
                        kopirovatNastaveniDialog = true
                    }
                ) {
                    Text("Zkopírovat rozvrhy")
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
