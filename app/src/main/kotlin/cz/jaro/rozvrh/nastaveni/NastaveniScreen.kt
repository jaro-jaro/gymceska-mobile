package cz.jaro.rozvrh.nastaveni

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cz.jaro.rozvrh.BuildConfig
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.PrepnoutRozvrhWidget
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.rozvrh.Vybiratko
import cz.jaro.rozvrh.rozvrh.dnesniEntries
import cz.jaro.rozvrh.ui.theme.GymceskaTheme
import cz.jaro.rozvrh.ui.theme.Theme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.format.DateTimeParseException

private var callback: (Uri) -> Unit = {}

@Destination
@Composable
fun Nastaveni(
    navigator: DestinationsNavigator
) {

    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            callback(it.data!!.data!!)
        }
    }

    fun getStartActivityForResult(cb: (Uri) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
        callback = cb
        return launcher
    }

    val viewModel = koinViewModel<NastaveniViewModel> {
        parametersOf(::getStartActivityForResult, ctx.contentResolver)
    }

    val tridy by viewModel.tridyFlow.collectAsStateWithLifecycle(emptyList())
    val nastaveni by viewModel.nastaveni.collectAsStateWithLifecycle(null)
    val skupiny by viewModel.skupiny.collectAsStateWithLifecycle(null)

    NastaveniContent(
        navigateBack = navigator::navigateUp,
        nastaveni = nastaveni,
        upravitNastaveni = viewModel::upravitNastaveni,
        tridy = tridy,
        skupiny = skupiny,
        stahnoutVse = viewModel::stahnoutVse
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NastaveniContent(
    navigateBack: () -> Unit,
    nastaveni: Nastaveni?,
    upravitNastaveni: ((Nastaveni) -> Nastaveni) -> Unit,
    tridy: List<Vjec.TridaVjec>,
    skupiny: Sequence<String>?,
    stahnoutVse: (Stalost, (String) -> Unit, (Boolean) -> Unit) -> Unit,
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
        if (nastaveni == null) LinearProgressIndicator(
            Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        )
        else Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(all = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.urcit_tmavy_rezim_podle_systemu), Modifier.weight(1F))
                Switch(
                    checked = nastaveni.darkModePodleSystemu,
                    onCheckedChange = {
                        upravitNastaveni { nastaveni ->
                            nastaveni.copy(darkModePodleSystemu = it)
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.tmavy_rezim), Modifier.weight(1F))
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
            val dynamicColorsSupported = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
            Vybiratko(
                value = when {
                    dynamicColorsSupported && nastaveni.dynamicColors -> "Dynamické"
                    else -> nastaveni.tema.jmeno
                },
                seznam = remember {
                    buildList {
                        if (dynamicColorsSupported) add("Dynamické")
                        addAll(Theme.entries.map { it.jmeno })
                    }
                },
                onClick = { i, _ ->
                    upravitNastaveni { nastaveni ->
                        when {
                            dynamicColorsSupported && i == 0 -> nastaveni.copy(dynamicColors = true)
                            dynamicColorsSupported -> nastaveni.copy(tema = Theme.entries[i - 1], dynamicColors = false)
                            else -> nastaveni.copy(tema = Theme.entries[i], dynamicColors = false)
                        }
                    }
                },
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = "Téma aplikace",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.zapnout_na_mym), Modifier.weight(1F))
                Switch(
                    checked = nastaveni.defaultMujRozvrh,
                    onCheckedChange = {
                        upravitNastaveni { nastaveni ->
                            nastaveni.copy(defaultMujRozvrh = it)
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Vždy povolit dvouřádkové buňky.", Modifier.weight(1F))
                Switch(
                    checked = nastaveni.alwaysTwoRowCells,
                    onCheckedChange = {
                        upravitNastaveni { nastaveni ->
                            nastaveni.copy(alwaysTwoRowCells = it)
                        }
                    }
                )
            }
            var sliderValue by remember { mutableStateOf(nastaveni.zoom) }
            Text(text = "Přiblížení rozvrhu: ${(sliderValue * 100).toInt()} %")
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                },
                valueRange = 0.5F..1.5F,
                steps = 19,
                onValueChangeFinished = {
                    upravitNastaveni { nastaveni ->
                        nastaveni.copy(zoom = sliderValue)
                    }
                }
            )
            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = Dp.Hairline, color = MaterialTheme.colorScheme.outline)
            Vybiratko(
                index = when (nastaveni.prepnoutRozvrhWidget) {
                    is PrepnoutRozvrhWidget.OPulnoci -> 0
                    is PrepnoutRozvrhWidget.VCas -> 1
                    is PrepnoutRozvrhWidget.PoKonciVyucovani -> 2
                },
                seznam = remember {
                    listOf(
                        "Vždy o půlnoci",
                        "Ve specifický čas",
                        "Daný počet hodin po konci vyučování",
                    )
                },
                onClick = { i, _ ->
                    upravitNastaveni { nast ->
                        nast.copy(
                            prepnoutRozvrhWidget = when (i) {
                                0 -> PrepnoutRozvrhWidget.OPulnoci
                                1 -> PrepnoutRozvrhWidget.VCas(16, 0)
                                2 -> PrepnoutRozvrhWidget.PoKonciVyucovani(2)
                                else -> throw IllegalArgumentException("WTF")
                            }
                        )
                    }
                },
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = "Přepínat widget s rozvrhem další den",
            )
            if (nastaveni.prepnoutRozvrhWidget is PrepnoutRozvrhWidget.VCas) {
                var hm by remember { mutableStateOf(nastaveni.prepnoutRozvrhWidget.cas.toString()) }
                var dialog by remember { mutableStateOf(false) }

                val initialTime = try {
                    LocalTime.parse(hm)
                } catch (e: DateTimeParseException) {
                    nastaveni.prepnoutRozvrhWidget.cas
                }
                val state = rememberTimePickerState(
                    initialHour = initialTime.hour,
                    initialMinute = initialTime.minute,
                    is24Hour = true,
                )
                if (dialog) TimePickerDialog(
                    onCancel = { dialog = false },
                    onConfirm = {
                        dialog = false
                        val time = LocalTime(state.hour, state.minute)
                        hm = time.toString()
                        upravitNastaveni { nast ->
                            nast.copy(prepnoutRozvrhWidget = PrepnoutRozvrhWidget.VCas(time))
                        }
                    }
                ) {
                    TimePicker(state)
                }

                OutlinedTextField(
                    value = hm,
                    onValueChange = {},
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
            }
            if (nastaveni.prepnoutRozvrhWidget is PrepnoutRozvrhWidget.PoKonciVyucovani)
                Text("Pokud není rozvrh, počítá se jako konec vyučování poledne")

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
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = {
                        Text("Počet hodin")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = Dp.Hairline, color = MaterialTheme.colorScheme.outline)
            Vybiratko(
                value = nastaveni.mojeTrida.jmeno,
                seznam = remember { tridy.map { it.jmeno }.drop(1) },
                onClick = { i, _ ->
                    upravitNastaveni { nastaveni ->
                        nastaveni.copy(mojeTrida = tridy[i + 1])
                    }
                },
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = stringResource(R.string.zvolte_svou_tridu),
            )
            if (skupiny == null) LinearProgressIndicator()
            else
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    /*
                                            .padding(16.dp)*/
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val moje = skupiny.filter { it in nastaveni.mojeSkupiny }
                    Vybiratko(
                        value = moje.joinToString(),
                        seznam = skupiny.toList(),
                        onClick = { _, it ->
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(mojeSkupiny = if (it in moje) nastaveni.mojeSkupiny - it else nastaveni.mojeSkupiny + it)
                            }
                        },
                        Modifier
                            .fillMaxWidth(),
                        label = stringResource(R.string.zvolte_sve_skupiny),
                        zaskrtavatko = {
                            it in moje
                        },
                        zavirat = false,
                    )
                }

            var stahnoutNastaveniDialog by remember { mutableStateOf(false) }
            var stalost by remember { mutableStateOf(Stalost.dnesniEntries().first()) }
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
            if (stahnoutNastaveniDialog) AlertDialog(
                onDismissRequest = {
                    stahnoutNastaveniDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            nacitame = true
                            stahnoutNastaveniDialog = false
                            podrobnostiNacitani = "Generuji text"

                            stahnoutVse(
                                stalost,
                                {
                                    podrobnostiNacitani = it
                                },
                                {
                                    if (!it) {
                                        podrobnostiNacitani = "Nejste připojeni k internetu a nemáte staženou offline verzi všech rozvrhů tříd"
                                        return@stahnoutVse
                                    }
//                                    kopirovatDialog = true
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
                            stahnoutNastaveniDialog = false
                        }
                    ) {
                        Text(text = "Zrušit")
                    }
                },
                title = {
                    Text(text = "Stáhnout rozvrhy")
                },
                text = {
                    Column {
                        Vybiratko(
                            seznam = Stalost.entries,
                            value = stalost,
                            onClick = { _, it ->
                                stalost = it
                            },
                        )
                    }
                }
            )

            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = Dp.Hairline, color = MaterialTheme.colorScheme.outline)

            TextButton(
                onClick = {
                    stahnoutNastaveniDialog = true
                }
            ) {
                Text("Stáhnout rozvrhy")
            }

            val scope = rememberCoroutineScope()
            TextButton(
                onClick = {
                    scope.launch {
                        Firebase.remoteConfig.reset().await()
                        val configSettings = remoteConfigSettings {
                            minimumFetchIntervalInSeconds = 3600
                        }
                        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)
                        Firebase.remoteConfig.fetchAndActivate().await()
                    }
                }
            ) {
                Text("Obnovit seznamy")
            }

            Text(stringResource(R.string.verze_aplikace, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))

            Text("Simulate crash...", Modifier.clickable {
                throw RuntimeException("Test exception")
            }, fontSize = 10.sp)
        }
    }
}


@Composable
fun TimePickerDialog(
    title: String = "Vyberte čas",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier =
            Modifier.width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(modifier = Modifier.height(40.dp).fillMaxWidth()) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancel) { Text("Zrušit") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}
@Preview
@Composable
private fun NastaveniPreview() {
    GymceskaTheme(
        useDarkTheme = true,
        useDynamicColor = false,
        theme = Theme.Green
    ) {
        NastaveniContent(
            navigateBack = {},
            nastaveni = Nastaveni(
                mojeTrida = Vjec.TridaVjec("1.A")
            ),
            upravitNastaveni = {},
            tridy = emptyList(),
            skupiny = emptySequence(),
            stahnoutVse = { _, _, _ -> },
        )
    }
}