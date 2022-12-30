package cz.jaro.rozvrh.rozvrh

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.RepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    repo: Repository,
) {
    var nacitame by remember { mutableStateOf(false) }
    var podrobnostiNacitani by remember { mutableStateOf("Načítání") }

    if (nacitame) AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Text(text = podrobnostiNacitani)
        },
        text = {
            CircularProgressIndicator()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )

    CenterAlignedTopAppBar(
        title = {
            Text(text = "Rozvrh")
        },
        actions = {
            IconButton(
                onClick = {

                    val seznamyOdkazu = listOf(
//                    Seznamy.mistnostiOdkazy.toMutableList(),
//                    Seznamy.vyucujiciOdkazy.toMutableList(),
                        Seznamy.tridyOdkazy.toMutableList(),
                    )
                    val seznamyNazvu = listOf(
//                    Seznamy.mistnosti.toMutableList(),
//                    Seznamy.vyucujici.toMutableList(),
                        Seznamy.tridy.toMutableList(),
                    )

                    val stalostOdkazy = Seznamy.stalostOdkazy
                    val stalostNazvy = Seznamy.stalost

                    seznamyNazvu.map {
                        it.removeAt(0)
                    }

                    if (!repo.isOnline()) return@IconButton

                    nacitame = true

                    for ((seznamOdkazu, seznamNazvu) in seznamyOdkazu zip seznamyNazvu) {
                        for ((odkaz, nazev) in seznamOdkazu zip seznamNazvu) {
                            for ((odkazStalosti, nazevStalosti) in stalostOdkazy zip stalostNazvy) {

                                podrobnostiNacitani = "Stahování: $nazev – $nazevStalosti"

                                val doc = Jsoup.connect(odkaz.replace("###", odkazStalosti)).get()

                                (repo as? RepositoryImpl)?.ctx?.getSharedPreferences("hm", Context.MODE_PRIVATE)?.edit {
                                    putString(
                                        "rozvrh_${odkaz}_$odkazStalosti",
                                        doc.toString()
                                    )

                                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                                    putString(
                                        "rozvrh_${odkaz}_${odkazStalosti}_datum",
                                        formatter.format(Date())
                                    )
                                }
                            }
                        }
                    }
                    nacitame = false
                    podrobnostiNacitani = "Načítání"
                }
            ) {
                Icon(Icons.Default.CloudDownload, "Stáhnout všechny rozvrhy")
            }

            var nastaveniDialog by remember { mutableStateOf(false) }
            var novaTrida by remember { mutableStateOf("5.E") }
            var noveSkupiny by remember { mutableStateOf(listOf<String>()) }
            var darkMode by remember { mutableStateOf(true) }
            var oznameniState by remember { mutableStateOf(OznameniState()) }
            val skupiny: Sequence<String> by produceState(emptySequence(), novaTrida) {
                withContext(Dispatchers.IO) {
                    val doc = repo.ziskatDocument(novaTrida, "Permanent") ?: exitProcess(-1)

                    value = TvorbaRozvrhu.vytvoritTabulku(doc)
                        .asSequence()
                        .flatten()
                        .filter { it.size > 1 }
                        .flatten()
                        .map { it.trida_skupina }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()
                }
            }
            val scope = rememberCoroutineScope()

            println(Build.VERSION.SDK_INT)

            val notifications =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS).also { println(it) } else null
            val scheduleAlarm =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) rememberPermissionState(Manifest.permission.SCHEDULE_EXACT_ALARM).also { println(it) } else null

            if (nastaveniDialog) AlertDialog(
                onDismissRequest = {
                    nastaveniDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            nastaveniDialog = false

                            scope.launch {
                                if (oznameniState.denniOznameni || oznameniState.hodinovyOznameni) {
                                    println(notifications?.status)
                                    println(scheduleAlarm?.status)
                                    notifications?.launchPermissionRequest()
                                    scheduleAlarm?.launchPermissionRequest()
                                }


                                if (!repo.oznameni.hodinovyOznameni && oznameniState.hodinovyOznameni) {
                                    OznameniReceiver.zariditOznameniNaPristiHodinu(repo)
                                } else if (repo.oznameni.hodinovyOznameni && !oznameniState.hodinovyOznameni) {

                                    val alarmManager = (repo as RepositoryImpl).ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                    val intent = Intent(repo.ctx, OznameniReceiver::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        intent.identifier = "OZNAMENIHODIN"
                                    }

                                    val pendingIntent = PendingIntent.getBroadcast(repo.ctx, 2, intent, PendingIntent.FLAG_IMMUTABLE)

                                    alarmManager.cancel(pendingIntent)
                                }

                                if (!repo.oznameni.denniOznameni && oznameniState.denniOznameni) {

                                    val alarmManager = (repo as RepositoryImpl).ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                    val c = Calendar.getInstance().apply {
                                        timeInMillis = System.currentTimeMillis()
                                        set(Calendar.HOUR_OF_DAY, oznameniState.denniOznameniCas.split(":")[0].toInt())
                                        set(Calendar.MINUTE, oznameniState.denniOznameniCas.split(":")[1].toInt())
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    val intent = Intent(repo.ctx, DenniReciever::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        intent.identifier = "OZNAMENIDENNI"
                                    }

                                    val pendingIntent = PendingIntent.getBroadcast(repo.ctx, 2, intent, PendingIntent.FLAG_IMMUTABLE)

                                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, 24 * 60 * 60 * 1000, pendingIntent)

                                } else if (repo.oznameni.denniOznameni && !oznameniState.denniOznameni) {

                                    val alarmManager = (repo as RepositoryImpl).ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                    val intent = Intent(repo.ctx, DenniReciever::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        intent.identifier = "OZNAMENIDENNI"
                                    }

                                    val pendingIntent = PendingIntent.getBroadcast(repo.ctx, 2, intent, PendingIntent.FLAG_IMMUTABLE)

                                    alarmManager.cancel(pendingIntent)
                                }

                                repo.mojeTrida = novaTrida
                                repo.indexMojiTridy = Seznamy.tridy.indexOf(novaTrida)
                                repo.mojeSkupiny = noveSkupiny
                                repo.darkMode = darkMode
                                repo.oznameni = oznameniState
                            }
                        }
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            nastaveniDialog = false
                        }
                    ) {
                        Text(text = "Zrušit")
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.nastaveni))
                },
                text = {
                    LazyColumn {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = stringResource(id = R.string.tmavy_rezim))
                                Switch(
                                    checked = darkMode,
                                    onCheckedChange = {
                                        darkMode = it
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
                                Text(text = "Denní oznámení o rozvrhu")
                                Switch(
                                    checked = oznameniState.denniOznameni,
                                    onCheckedChange = {
                                        oznameniState = oznameniState.copy(denniOznameni = it)
                                    }
                                )
                            }
                        }
                        if (oznameniState.denniOznameni) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Vyberte čas:", Modifier.weight(1F))
                                    val h by produceState("", oznameniState) {
                                        value = oznameniState.denniOznameniCas.split(':').first()
                                    }
                                    val m by produceState("", oznameniState) {
                                        value = oznameniState.denniOznameniCas.split(':')[1]
                                    }

                                    OutlinedTextField(
                                        value = h,
                                        onValueChange = {
                                            oznameniState = oznameniState.copy(denniOznameniCas = "$it:$m")
                                        },
                                        modifier = Modifier.weight(1F)
                                    )
                                    Text(
                                        ":",
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp),
                                        fontSize = 20.sp
                                    )
                                    OutlinedTextField(
                                        value = m,
                                        onValueChange = {
                                            oznameniState = oznameniState.copy(denniOznameniCas = "$h:$it")
                                        },
                                        modifier = Modifier.weight(1F)
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
                                    RadioButton(
                                        selected = !oznameniState.denniOznameniNaZitrek,
                                        onClick = {
                                            oznameniState = oznameniState.copy(denniOznameniNaZitrek = false)
                                        }
                                    )
                                    Text("Oznámení o rozvrhu na tento den")
                                }
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = oznameniState.denniOznameniNaZitrek,
                                        onClick = {
                                            oznameniState = oznameniState.copy(denniOznameniNaZitrek = true)
                                        }
                                    )
                                    Text("Oznámení o rozvrhu na další den")
                                }
                            }
                        }
                        item {
                            Text(text = "Experimentální funkce: Nemusí fungovat na všech zařízeních či s různým nastavením, nic nezaručujeme", color = Color.Red)
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = "Oznámení o následující hodině")
                                Switch(
                                    checked = oznameniState.hodinovyOznameni,
                                    onCheckedChange = {
                                        oznameniState = oznameniState.copy(hodinovyOznameni = it)
                                    }
                                )
                            }
                        }
                        if (oznameniState.hodinovyOznameni) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = oznameniState.hodinovyOznameniPodle == OznameniState.TypHodinovyhoOznameni.PoMinulyHodine,
                                        onClick = {
                                            oznameniState =
                                                oznameniState.copy(hodinovyOznameniPodle = OznameniState.TypHodinovyhoOznameni.PoMinulyHodine)
                                        }
                                    )
                                    Text("Oznámení o následující hodině po skončení hod. minulé")
                                }
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = oznameniState.hodinovyOznameniPodle == OznameniState.TypHodinovyhoOznameni.PredDalsiHodinou,
                                        onClick = {
                                            oznameniState =
                                                oznameniState.copy(hodinovyOznameniPodle = OznameniState.TypHodinovyhoOznameni.PredDalsiHodinou)
                                        }
                                    )
                                    Text("Oznámení o následující hodině před jejím začátkem")
                                }
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        when (oznameniState.hodinovyOznameniPodle) {
                                            OznameniState.TypHodinovyhoOznameni.PoMinulyHodine -> "Minut po hod.:"
                                            OznameniState.TypHodinovyhoOznameni.PredDalsiHodinou -> "Minut před hod.:"
                                        }, Modifier.weight(1F)
                                    )

                                    OutlinedTextField(
                                        value = oznameniState.casString,
                                        onValueChange = {
                                            oznameniState = oznameniState.copy(casString = it)
                                        },
                                        modifier = Modifier.weight(1F)
                                    )
                                }
                            }
                        }
                        item {
                            Text(text = "Experimentální funkce: Nemusí fungovat na všech zařízeních či s různým nastavením, nic nezaručujeme", color = Color.Red)
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = stringResource(id = R.string.zvolte_svou_tridu))
                                Vybiratko(
                                    seznam = Seznamy.tridy,
                                    aktualIndex = Seznamy.tridy.indexOf(novaTrida)
                                ) {
                                    novaTrida = Seznamy.tridy[it]
                                }
                            }
                        }
                        items(skupiny.toList()) { skupina ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = skupina in noveSkupiny,
                                        onCheckedChange = { chciJi ->
                                            noveSkupiny = if (chciJi) noveSkupiny + skupina else noveSkupiny - skupina
                                        }
                                    )
                                    Text(skupina)
                                }
                            }
                        }

                }
            )
            IconButton(
                onClick = {
                    nastaveniDialog = true
                    darkMode = repo.darkMode
                    novaTrida = repo.mojeTrida
                    noveSkupiny = repo.mojeSkupiny
                    oznameniState = repo.oznameni
                }
            ) {
                Icon(Icons.Default.Settings, "Nastavení")
            }

            var volnaTridaNastaveniDialog by remember { mutableStateOf(false) }
            var volnaTridaDialog by remember { mutableStateOf(false) }
            var volneTridy by remember { mutableStateOf(setOf<String>()) }
            var stalostIndex by remember { mutableStateOf(0) }
            var denIndex by remember { mutableStateOf(0) }
            var hodinaIndex by remember { mutableStateOf(0) }

            if (volnaTridaDialog) AlertDialog(
                onDismissRequest = {
                    volnaTridaDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            volnaTridaDialog = false
                        }
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {},
                title = {
                    Text(text = "Najdi mi volnou třídu")
                },
                text = {
                    LazyColumn {
                        item {
                            Text("Na škole jsou ${Seznamy.stalost2[stalostIndex]} ${Seznamy.dny6Pad[denIndex]} ${Seznamy.hodiny4Pad[hodinaIndex]} volné tyto učebny:")
                        }
                        items(volneTridy.toList()) {
                            Text(it)
                        }
                    }
                }
            )

            if (volnaTridaNastaveniDialog) AlertDialog(
                onDismissRequest = {
                    volnaTridaNastaveniDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            nacitame = true
                            volnaTridaNastaveniDialog = false
                            podrobnostiNacitani = "Načítání"

                            scope.launch {

                                volneTridy = Seznamy.mistnosti.drop(1).toSet()

                                Seznamy.tridy.drop(1).forEach { trida ->
                                    TvorbaRozvrhu.vytvoritTabulku(repo.ziskatDocument(trida, Seznamy.stalostOdkazy[stalostIndex]) ?: run {
                                        nacitame = false; return@launch
                                    })
                                        .drop(1)[denIndex].drop(1)[hodinaIndex].forEach { bunka ->
                                        volneTridy = volneTridy - bunka.ucebna
                                    }
                                }

                                volnaTridaDialog = true
                                nacitame = false
                            }
                        }
                    ) {
                        Text(text = "Vyhledat")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            volnaTridaNastaveniDialog = false
                        }
                    ) {
                        Text(text = "Zrušit")
                    }
                },
                title = {
                    Text(text = "Najdi mi volnou třídu")
                },
                text = {
                    Column {
                        Vybiratko(
                            seznam = Seznamy.stalost,
                            aktualIndex = stalostIndex,
                        ) {
                            stalostIndex = it
                        }
                        Vybiratko(
                            seznam = Seznamy.dny1Pad,
                            aktualIndex = denIndex,
                        ) {
                            denIndex = it
                        }
                        Vybiratko(
                            seznam = Seznamy.hodiny1Pad,
                            aktualIndex = hodinaIndex,
                        ) {
                            hodinaIndex = it
                        }
                    }
                }
            )
            IconButton(
                onClick = {
                    volnaTridaNastaveniDialog = true
                }
            ) {
                Icon(Icons.Default.Search, "Najdi volnou učebnu")
            }
        }
    )
}
