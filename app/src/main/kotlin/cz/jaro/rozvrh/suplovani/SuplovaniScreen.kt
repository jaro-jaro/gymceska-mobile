package cz.jaro.rozvrh.suplovani

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.App.Companion.navigate
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.ResponsiveText
import cz.jaro.rozvrh.Result2
import cz.jaro.rozvrh.destinations.NastaveniDestination
import cz.jaro.rozvrh.destinations.RozvrhDestination
import cz.jaro.rozvrh.rozvrh.Bunka
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.rozvrh.Vybiratko
import cz.jaro.rozvrh.rozvrh.doubleScrollable
import cz.jaro.rozvrh.rozvrh.zakladniVelikostBunky
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate

@Destination
@Composable
fun Suplovani(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<SuplovaniViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val tridy by viewModel.tridy.collectAsStateWithLifecycle()
    val mistnosti by viewModel.mistnosti.collectAsStateWithLifecycle()
    val vyucujici by viewModel.vyucujici.collectAsStateWithLifecycle()

    SuplovaniContent(
        state = state,
        zmenitDatum = viewModel::zmenitDatum,
        navigate = navigator.navigate,
        tridy = tridy,
        mistnosti = mistnosti,
        vyucujici = vyucujici,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuplovaniContent(
    state: SuplovaniState,
    zmenitDatum: (LocalDate) -> Unit,
    navigate: (Direction) -> Unit,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
) = Surface {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.suplovani))
                },

                actions = {
                    IconButton(
                        onClick = {
                            navigate(NastaveniDestination)
                        }
                    ) {
                        Icon(Icons.Default.Settings, stringResource(R.string.nastaveni))
                    }
                }
            )
        }
    ) { paddingValues ->
        val verScrollState = rememberScrollState(0)
        val horScrollState = rememberScrollState(0)
        Column(
            Modifier
                .doubleScrollable(horScrollState, verScrollState)
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (state) {
                SuplovaniState.Nacitani -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is SuplovaniState.DatumNevybran -> {
                    Datumovatko(zmenitDatum, state.podporovanaData, "Datum nevybrán")
                }

                is SuplovaniState.NacitaniDat -> {
                    Datumovatko(zmenitDatum, state.podporovanaData, formatovatDatum(state.datum))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                is SuplovaniState.Chyba -> {
                    Datumovatko(zmenitDatum, state.podporovanaData, formatovatDatum(state.datum))
                    when (state.chyba) {
                        Result2.Error -> Text("Bohužel, nastala chyba, zkuste to znovu")
                        Result2.ZadnaData -> Text("Jste offline a nemáte stažená offline data")
                        is Result2.Uspech -> Unit
                    }
                }

                is SuplovaniState.Data -> {
                    Datumovatko(zmenitDatum, state.podporovanaData, formatovatDatum(state.datum))

                    val zobrazeni = remember { mutableIntStateOf(-1) }

                    Column(
                        Modifier.verticalScroll(verScrollState, enabled = false)
                    ) {
                        Cast(zobrazeni, 0, "Nepřítomné tridy") {
                            NepritomneTridy(state, horScrollState, tridy, navigate)
                        }
                        Cast(zobrazeni, 1, "Nepřítomní vyučují") {
                            NepritomniUcitele(state, horScrollState, vyucujici, navigate)
                        }
                        Cast(zobrazeni, 2, "Místnosti mimo provoz") {
                            MistnostiMimoProvoz(state, horScrollState, mistnosti, navigate)
                        }
                        Cast(zobrazeni, 3, "Zmeny v rozvrzích učitelů") {
                            ZmenyUctelu(state, horScrollState, tridy, mistnosti, vyucujici, navigate)
                        }
                        Cast(zobrazeni, 4, "Změny v rozvrzích trid") {
                            ZmenyTrid(state, horScrollState, tridy, mistnosti, vyucujici, navigate)
                        }
                        Cast(zobrazeni, 5, "Pedagogické dohledy u tříd") {
                            PedagogickeDohledy(state, horScrollState, tridy, mistnosti, vyucujici, navigate)
                        }
                        Poznamky(state, horScrollState, tridy, mistnosti, vyucujici, navigate)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.Cast(
    zobrazeni: MutableIntState,
    i: Int,
    nazev: String,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    Surface(
        onClick = {
            zobrazeni.intValue = if (zobrazeni.intValue == i) -1 else i
        },
        shape = CircleShape,
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.ArrowDropDown,
                "Rozbalit",
                Modifier.rotate(
                    animateFloatAsState(if (zobrazeni.intValue == i) 0F else -90F, label = "šipka dropdown").value
                )
            )
            Text(nazev)
        }
    }
    AnimatedVisibility(
        zobrazeni.intValue == i,
        enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(expandFrom = Alignment.Top),
        exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically(shrinkTowards = Alignment.Top),
        content = content,
    )
}

context(ColumnScope, AnimatedVisibilityScope)
@Composable
private fun NepritomneTridy(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    tridy: List<Vjec.TridaVjec>,
    navigate: (Direction) -> Unit,
) = Column {
    val zmeny = state.suplovani.zmeny.nepritomneTridy
    Row(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false)
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
                    .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
            )
        }
        Column(
            modifier = Modifier
                .horizontalScroll(horScrollState, enabled = false)
        ) {
            Row {
                zmeny.values.first().keys.forEach {
                    Box(Modifier.bunka(1F / 2), contentAlignment = Alignment.Center) {
                        ResponsiveText("$it")
                    }
                }
            }
        }
    }
    Row(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false)
        ) {
            zmeny.forEach { (trida, _) ->
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
                    contentAlignment = Alignment.Center,
                ) {
                    ResponsiveText(
                        text = trida,
                        modifier = Modifier.padding(all = 8.dp),
                        maxLines = 2,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .horizontalScroll(horScrollState, enabled = false)
        ) {
            zmeny.forEach { (_, hodiny) ->
                Row {
                    hodiny.forEach { (_, typ) ->
                        Box(Modifier.bunka(1F / 2), contentAlignment = Alignment.Center) {
                            ResponsiveText(typ)
                        }
                    }
                }
            }
        }
    }
}


context(AnimatedVisibilityScope)
@Composable
private fun NepritomniUcitele(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    vyucujici: List<Vjec.VyucujiciVjec>,
    navigate: (Direction) -> Unit,
) = Column {
    val zmeny = state.suplovani.zmeny.nepritomniUcitele
    Row(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false)
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
                    .size(zakladniVelikostBunky * 1.25F, zakladniVelikostBunky / 2),
            )
        }
        Column(
            modifier = Modifier
                .horizontalScroll(horScrollState, enabled = false)
        ) {
            Row {
                zmeny.values.first().keys.forEach {
                    Box(Modifier.bunka(1F / 2), contentAlignment = Alignment.Center) {
                        ResponsiveText("$it")
                    }
                }
            }
        }
    }
    Row(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false)
        ) {
            zmeny.forEach { (ucitel, _) ->
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                        .size(zakladniVelikostBunky * 1.25F, zakladniVelikostBunky / 2),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    ResponsiveText(
                        text = ucitel,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        maxLines = 2,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .horizontalScroll(horScrollState, enabled = false)
        ) {
            zmeny.forEach { (_, hodiny) ->
                Row {
                    hodiny.forEach { (_, typ) ->
                        Box(Modifier.bunka(1F / 2), contentAlignment = Alignment.Center) {
                            ResponsiveText(typ)
                        }
                    }
                }
            }
        }
    }
}


context(AnimatedVisibilityScope)
@Composable
private fun MistnostiMimoProvoz(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    mistnosti: List<Vjec.MistnostVjec>,
    navigate: (Direction) -> Unit,
) = Column {
    val zmeny = state.suplovani.zmeny.mistnostiMimoProvoz
    Row(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false)
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
                    .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
            )
        }
        Column(
            modifier = Modifier
                .horizontalScroll(horScrollState, enabled = false)
        ) {
            Row {
                zmeny.values.first().keys.forEach {
                    Box(Modifier.bunka(1F / 2), contentAlignment = Alignment.Center) {
                        ResponsiveText("$it")
                    }
                }
            }
        }
    }
    Row(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false)
        ) {
            zmeny.forEach { (mistnost, _) ->
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
                    contentAlignment = Alignment.Center,
                ) {
                    ResponsiveText(
                        text = mistnost,
                        modifier = Modifier.padding(all = 8.dp),
                        maxLines = 2,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .horizontalScroll(horScrollState, enabled = false)
        ) {
            zmeny.forEach { (_, hodiny) ->
                Row {
                    hodiny.forEach { (_, typ) ->
                        Box(Modifier.bunka(1F / 2), contentAlignment = Alignment.Center) {
                            ResponsiveText(typ)
                        }
                    }
                }
            }
        }
    }
}


context(AnimatedVisibilityScope)
@Composable
private fun ZmenyUctelu(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    navigate: (Direction) -> Unit,
) = Column {
    state.suplovani.zmeny.zmenyVRozvrzichUcitelu.forEach { (ucitel, nefiltrZmeny) ->
        val zmeny = nefiltrZmeny.filter { it !is ZmenaVyucujiciho.ZmenaPlus }
        if (zmeny.isEmpty()) return@forEach
        Box(
            modifier = Modifier
                .border(1.dp, Color.Transparent)
                .height(zakladniVelikostBunky / 2)
                .clickable {
                    navigate(
                        RozvrhDestination(
                            vjec = tridy.find { it.jmeno == ucitel },
                            stalost = Stalost.TentoTyden, // todo
                        )
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(ucitel, fontWeight = FontWeight.Bold)
        }
        Row(
            Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.horizontalScroll(rememberScrollState(), enabled = false)
            ) {

                zmeny.groupBy { it.hodiny.toSet() }.toList().sortedBy { it.first.first() }.forEach { (hodiny, zmeny) ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.secondary)
                            .size(zakladniVelikostBunky / 2, zmeny.size * zakladniVelikostBunky / 2),
                        contentAlignment = Alignment.Center,
                    ) {
                        ResponsiveText(
                            text = hodiny.hodinyHezky(),
                            modifier = Modifier.padding(all = 8.dp),
                            maxLines = 2,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .horizontalScroll(horScrollState, enabled = false)
            ) {
//                Box(
//                    modifier = Modifier
//                        .border(1.dp, Color.Transparent)
//                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
//                )

                zmeny.forEach { zmena ->

                    val uniZmena = zmena.toUniverzalniZmenaVyucujiciho()
                    if (zmena is ZmenaVyucujiciho.Absence) Row {
                        Box(Modifier.bunka(1F / 3), contentAlignment = Alignment.Center) {
                            ResponsiveText(uniZmena.typ)
                        }
                        Box(Modifier.bunka(2F), contentAlignment = Alignment.CenterStart) {
                            ResponsiveText(
                                uniZmena.poznamka,
                                Modifier.padding(8.dp)
                            )
                        }
                    }
                    else Row {
                        Box(Modifier.bunka(1F / 3), contentAlignment = Alignment.Center) {
                            ResponsiveText(uniZmena.typ)
                        }
                        Bunka(
                            bunka = Bunka(
                                ucebna = uniZmena.mistnost,
                                predmet = uniZmena.predmet,
                                ucitel = "",
                                tridaSkupina = uniZmena.tridaSkupina,
                            ),
                            aspectRatio = 2F,
                            tridy = tridy,
                            mistnosti = mistnosti,
                            vyucujici = vyucujici,
                            kliklNaNeco = { vjec ->
                                navigate(
                                    RozvrhDestination(
                                        vjec = vjec,
                                        stalost = Stalost.TentoTyden, // todo
                                    )
                                )
                            },
                        )
                        Box(Modifier.bunka(1F), contentAlignment = Alignment.CenterStart) {
                            ResponsiveText(uniZmena.poznamka, Modifier.padding(horizontal = 8.dp), maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}


context(AnimatedVisibilityScope)
@Composable
private fun ZmenyTrid(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    navigate: (Direction) -> Unit,
) = Column {
    state.suplovani.zmeny.zmenyVRozvrzichTrid.forEach { (trida, zmeny) ->
        Row(
            Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.horizontalScroll(rememberScrollState(), enabled = false)
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Transparent)
                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2)
                        .clickable {
                            navigate(
                                RozvrhDestination(
                                    vjec = tridy.find { it.zkratka == trida },
                                    stalost = Stalost.TentoTyden, // todo
                                )
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(trida, fontWeight = FontWeight.Bold)
                }

                zmeny.groupBy { it.hodiny.toSet() }.toList().sortedBy { it.first.first() }.forEach { (hodiny, zmeny) ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.secondary)
                            .size(zakladniVelikostBunky / 2, zmeny.size * zakladniVelikostBunky / 2),
                        contentAlignment = Alignment.Center,
                    ) {
                        ResponsiveText(
                            text = hodiny.hodinyHezky(),
                            modifier = Modifier.padding(all = 8.dp),
                            maxLines = 2,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .horizontalScroll(horScrollState, enabled = false)
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Transparent)
                        .size(zakladniVelikostBunky / 2, zakladniVelikostBunky / 2),
                )

                zmeny.forEach { zmena ->
                    val uniZmena = zmena.toUniverzalniZmenaTridy()
                    if (zmena is ZmenaTridy.Absence) Row {
                        Box(Modifier.bunka(1F / 3), contentAlignment = Alignment.Center) {
                            ResponsiveText(uniZmena.typ)
                        }
                        Box(Modifier.bunka(2F), contentAlignment = Alignment.CenterStart) {
                            ResponsiveText(
                                if (uniZmena.skupina.isNotBlank()) "(${uniZmena.skupina}) ${uniZmena.poznamka}" else uniZmena.poznamka,
                                Modifier.padding(8.dp)
                            )
                        }
                    }
                    else Row {
                        Box(Modifier.bunka(1F / 3), contentAlignment = Alignment.Center) {
                            ResponsiveText(uniZmena.typ)
                        }
                        Bunka(
                            bunka = Bunka(
                                ucebna = uniZmena.mistnost,
                                predmet = uniZmena.predmet,
                                ucitel = uniZmena.vyucujici,
                                tridaSkupina = uniZmena.skupina,
                            ),
                            aspectRatio = 2F,
                            tridy = tridy,
                            mistnosti = mistnosti,
                            vyucujici = vyucujici,
                            kliklNaNeco = { vjec ->
                                navigate(
                                    RozvrhDestination(
                                        vjec = vjec,
                                        stalost = Stalost.TentoTyden, // todo
                                    )
                                )
                            },
                        )
                        Box(Modifier.bunka(1F), contentAlignment = Alignment.CenterStart) {
                            ResponsiveText(uniZmena.poznamka, Modifier.padding(horizontal = 8.dp), maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}


context(AnimatedVisibilityScope)
@Composable
private fun PedagogickeDohledy(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    navigate: (Direction) -> Unit,
) {
}


context(ColumnScope)
@Composable
private fun Poznamky(
    state: SuplovaniState.Data,
    horScrollState: ScrollState,
    tridy: List<Vjec.TridaVjec>,
    mistnosti: List<Vjec.MistnostVjec>,
    vyucujici: List<Vjec.VyucujiciVjec>,
    navigate: (Direction) -> Unit,
) {
    state.suplovani.zmeny.poznamky.forEach {
        Text(it)
    }
}

@Composable
private fun Modifier.bunka(sirka: Float) = this
    .border(1.dp, MaterialTheme.colorScheme.secondary)
    .size(zakladniVelikostBunky * sirka, zakladniVelikostBunky / 2)

infix fun String.a(other: String) = buildAnnotatedString {
    append("    ")
    withStyle(
        SpanStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
        )
    ) {
        append(this@a)
    }
    append("    ")
    append(other)
}

//infix fun AnnotatedString.a(other: String) = this + AnnotatedString(other)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Datumovatko(
    zmenitDatum: (LocalDate) -> Unit,
    podporovanaData: List<LocalDate>,
    value: String
) {
    Vybiratko(
        value = value,
        seznam = podporovanaData.map(::formatovatDatum),
        onClick = {
            zmenitDatum(podporovanaData[it.index])
        },
        enabled = {
            (podporovanaData[it.index].dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        }
    )
}

private fun formatovatDatum(it: LocalDate) = "${TvorbaRozvrhu.dny[it.dayOfWeek.value - 1]} ${it.dayOfMonth}. ${it.monthValue}."