package cz.jaro.rozvrh.rozvrh

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import cz.jaro.rozvrh.FakeRepositoryImpl
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.RepositoryImpl
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu.vytvoritRozvrhPodleJinych
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu.vytvoritTabulku
import cz.jaro.rozvrh.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RozvrhScreen(
    repo: Repository
) {
    val sp = LocalContext.current.getSharedPreferences("hm", Context.MODE_PRIVATE)

    var index by remember { mutableStateOf(repo.indexMojiTridy) }
    var typRozvrhu by remember { mutableStateOf<TypRozvrhu>(TypRozvrhu.Trida) }

    var stalostIndex by remember { mutableStateOf(0) }
    val stalost by derivedStateOf { Seznamy.stalostOdkazy[stalostIndex] }

    var nacitame by remember { mutableStateOf(false) }

    val tabulka: List<List<List<Bunka>>> by produceState(initialValue = emptyList(), typRozvrhu, index, stalost) {
        nacitame = true
        when (typRozvrhu) {
            is TypRozvrhu.Trida -> withContext(Dispatchers.IO) Nacitani@{
                val seznam = typRozvrhu.seznamOdkazu

                val doc = if (repo.isOnline()) {
                    val odkaz = seznam[index - 1]
                    Jsoup.connect(odkaz.replace("###", stalost)).get().also { doc ->
                        (repo as RepositoryImpl).sharedPref.edit {
                            val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                            putString("rozvrh_${seznam[index - 1]}_$stalost", doc.toString())
                            putString("rozvrh_${seznam[index - 1]}_${stalost}_datum", formatter.format(Date()))
                        }
                    }
                } else {

                    val html = (repo as RepositoryImpl).sharedPref.getString("rozvrh_${seznam[index - 1]}_$stalost", null)
                        ?: run {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(repo.ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                            }
                            nacitame = false
                            return@Nacitani
                        }

                    Jsoup.parse(html)
                }

                value = vytvoritTabulku(doc)
            }

            else -> {
                val tabulka = vytvoritRozvrhPodleJinych(typRozvrhu, typRozvrhu.seznam[index], stalost, repo).also { println(it) }
                value = tabulka
            }
        }
        nacitame = false
    }

    LaunchedEffect(tabulka) {
        println(tabulka)
    }

    Surface {
        if (nacitame) AlertDialog(
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

        Scaffold(
            topBar = {
                AppBar(repo)
            }
        ) { paddingValues ->

            Column(
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
                        seznam = TypRozvrhu.Trida.seznam,
                        aktualIndex = if (typRozvrhu is TypRozvrhu.Trida) index else 0
                    ) { i ->
                        if (i == 0) return@Vybiratko
                        typRozvrhu = TypRozvrhu.Trida
                        index = i
                    }

                    Vybiratko(
                        seznam = Seznamy.stalost,
                        aktualIndex = stalostIndex
                    ) { i ->
                        stalostIndex = i
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Vybiratko(
                        seznam = TypRozvrhu.Mistnost.seznam,
                        aktualIndex = if (typRozvrhu is TypRozvrhu.Mistnost) index else 0
                    ) { i ->
                        if (i == 0) return@Vybiratko
                        typRozvrhu = TypRozvrhu.Mistnost
                        index = i
                    }

                    Vybiratko(
                        seznam = TypRozvrhu.Vyucujici.seznam,
                        aktualIndex = if (typRozvrhu is TypRozvrhu.Vyucujici) index else 0
                    ) { i ->
                        if (i == 0) return@Vybiratko
                        typRozvrhu = TypRozvrhu.Vyucujici
                        index = i
                    }
                }

                Tabulka(
                    tabulka = tabulka,
                ) { rozvrh, i ->
                    typRozvrhu = rozvrh
                    index = i
                }
            }
        }

    }
}

@Composable
private fun Tabulka(
    tabulka: List<List<List<Bunka>>>,
    kliklNaNeco: (rozvrh: TypRozvrhu, i: Int) -> Unit,
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
                .border(1.dp, colorResource(id = R.color.gymceska_modra))
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1F)
                    .border(1.dp, colorResource(id = R.color.gymceska_modra))
                    .size(60.dp, 60.dp)
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(horScrollState)
                .border(1.dp, colorResource(id = R.color.gymceska_modra))
        ) {
            tabulka.first().drop(1).forEach { cisloHodiny ->

                Box(
                    modifier = Modifier
                        .aspectRatio(1F)
                        .border(1.dp, colorResource(id = R.color.gymceska_modra))
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
                                .border(1.dp, colorResource(id = R.color.gymceska_modra))
                        ) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1F)
                                    .border(1.dp, colorResource(id = R.color.gymceska_modra))
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
                                        .border(1.dp, colorResource(id = R.color.gymceska_modra))
                                ) {
                                    hodina.forEach { bunka ->
                                        Bunka.ComposovatBunku(
                                            bunka = bunka,
                                            kolikJichJe = hodina.size,
                                            celkem = maxy[i + 1],
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

@Preview
@Composable
fun Preview() {
    AppTheme {
        RozvrhScreen(FakeRepositoryImpl())
    }
}
