package cz.jaro.rozvrh.rozvrh.widgety

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cz.jaro.rozvrh.MainActivity
import cz.jaro.rozvrh.PrepnoutRozvrhWidget
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.rozvrh.Bunka
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu.vytvoritTabulku
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.LocalTime


class DnesWidget : GlanceAppWidget() {

    @Suppress("DEPRECATION")
    @Composable
    fun Content(
        context: Context
    ) = GlanceTheme {
        val prefs = currentState<Preferences>()
        val bunky = Json.decodeFromString<List<Bunka>>(prefs[stringPreferencesKey("hodiny")] ?: "[]")
        val den = prefs[stringPreferencesKey("den")] ?: "??. ??."

        val bg = ColorProvider(Color(context.resources.getColor(R.color.background_color)))
        val bg2 = ColorProvider(Color(context.resources.getColor(R.color.background_color_alt)))
        val onbg = ColorProvider(Color(context.resources.getColor(R.color.on_background_color)))
        val onbg2 = ColorProvider(Color(context.resources.getColor(R.color.on_background_color_alt)))

        Column(
            GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {

            bunky
                .ifEmpty {
                    listOf(Bunka("", "Žádné hodiny!", ""))
                }
                .let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) it else it.take(10)
                }.let { hodiny ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(bg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(den, GlanceModifier.defaultWeight(), style = TextStyle(color = onbg))
                        Image(
                            provider = ImageProvider(R.drawable.baseline_refresh_24),
                            colorFilter = ColorFilter.tint(onbg),
                            contentDescription = "Aktualizovat",
                            modifier = GlanceModifier.clickable {
                                updateAll(context)
                            },
                        )
                    }

                    Cara()

                    hodiny
                        .forEachIndexed { i, it ->
                            with(it) {
                                Column(
                                    GlanceModifier
                                        .clickable(actionStartActivity<MainActivity>())
                                        .defaultWeight()
                                        .fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = GlanceModifier
                                            .clickable(actionStartActivity<MainActivity>())
                                            .fillMaxWidth()
                                            .defaultWeight()
                                            .background(if (zbarvit) bg2 else bg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.TopStart,
                                            modifier = GlanceModifier
                                                .clickable(actionStartActivity<MainActivity>())
                                                .fillMaxSize()
                                        ) {
                                            Text(
                                                text = ucebna,
                                                modifier = GlanceModifier
                                                    .clickable(actionStartActivity<MainActivity>())
                                                    .padding(all = 8.dp),
                                                style = TextStyle(
                                                    color = if (zbarvit) onbg2 else onbg
                                                ),
                                            )
                                        }
                                        Box(
                                            contentAlignment = Alignment.TopEnd,
                                            modifier = GlanceModifier
                                                .clickable(actionStartActivity<MainActivity>())
                                                .fillMaxSize()
                                        ) {
                                            Text(
                                                text = tridaSkupina,
                                                modifier = GlanceModifier
                                                    .clickable(actionStartActivity<MainActivity>())
                                                    .padding(all = 8.dp),
                                                style = TextStyle(
                                                    color = if (zbarvit) onbg2 else onbg
                                                ),
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = GlanceModifier
                                                .clickable(actionStartActivity<MainActivity>())
                                                .fillMaxSize()
                                        ) {
                                            Text(text = "")
                                            Spacer(GlanceModifier.defaultWeight())
                                            Text(
                                                text = predmet,
                                                modifier = GlanceModifier
                                                    .clickable(actionStartActivity<MainActivity>())
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp),
                                                style = TextStyle(
                                                    color = if (zbarvit) onbg2 else onbg,
                                                    textAlign = TextAlign.Center
                                                ),
                                            )
                                            Spacer(GlanceModifier.defaultWeight())
                                            Text(
                                                text = ucitel,
                                                modifier = GlanceModifier
                                                    .clickable(actionStartActivity<MainActivity>())
                                                    .padding(bottom = 8.dp),
                                                style = TextStyle(
                                                    color = if (zbarvit) onbg2 else onbg
                                                ),
                                            )
                                        }
                                    }
                                    if (i < hodiny.lastIndex)
                                        Cara()
                                }
                            }
                        }
                }
        }
    }

    @Composable
    fun Cara() = Box(modifier = GlanceModifier.height(2.dp).fillMaxWidth().background(Color.Transparent)) {}

    companion object {

        const val EXTRA_KEY_WIDGET_IDS = "providerwidgetids"

        fun updateAll(context: Context) {
            context.sendBroadcast(Intent().apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

                val appWidgetManager = AppWidgetManager.getInstance(context)

                putExtra(
                    EXTRA_KEY_WIDGET_IDS, appWidgetManager.getAppWidgetIds(
                        ComponentName(context, Reciever::class.java)
                    )
                )
            })
        }

        class Reciever : GlanceAppWidgetReceiver(), KoinComponent {
            override val glanceAppWidget: GlanceAppWidget = DnesWidget()

            private val repo = get<Repository>()

            override fun onReceive(context: Context, intent: Intent) {
                if (intent.hasExtra(EXTRA_KEY_WIDGET_IDS)) {
                    val ids = intent.extras!!.getIntArray(EXTRA_KEY_WIDGET_IDS)
                    onUpdate(context, AppWidgetManager.getInstance(context), ids!!)
                } else super.onReceive(context, intent)
            }

            override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
                super.onUpdate(context, appWidgetManager, appWidgetIds)

                CoroutineScope(Dispatchers.IO).launch {
                    val nastaveni = repo.nastaveni.first()

                    val dnes = rozvrhZobrazitNaDnesek()

                    val den = LocalDate.now().plusDays(if (dnes) 0 else 1)
                    val cisloDne = den.dayOfWeek.value // 1-5 (2-7)

                    val stalost = if (cisloDne == 1 && !dnes) Stalost.PristiTyden else Stalost.TentoTyden

                    val hodiny = repo.ziskatDocument(stalost).let { result ->
                        if (result !is Uspech) return@let listOf(Bunka("", "Žádná data!", ""))

                        val tabulka = vytvoritTabulku(result.document)

                        tabulka
                            .getOrNull(cisloDne)
                            ?.asSequence()
                            ?.drop(1)
                            ?.mapIndexed { i, hodina -> i to hodina }
                            ?.filter { (_, hodina) -> hodina.first().predmet.isNotBlank() }
                            ?.map { (i, hodina) -> hodina.map { bunka -> bunka.copy(predmet = "$i. ${bunka.predmet}") } }
                            ?.mapNotNull { hodina ->
                                hodina.find { bunka ->
                                    bunka.tridaSkupina.isEmpty() || bunka.tridaSkupina in nastaveni.mojeSkupiny
                                }
                            }
                            ?.toList()
                            ?.ifEmpty {
                                listOf(
                                    Bunka("", "Žádné hodiny!", ""),
                                )
                            }
                            ?: listOf(Bunka("", "Víkend", ""))
                    }

                    appWidgetIds.forEach {
                        val id = GlanceAppWidgetManager(context).getGlanceIdBy(it)

                        updateAppWidgetState(context, id) { prefs ->
                            prefs[stringPreferencesKey("hodiny")] = Json.encodeToString(hodiny)
                            prefs[stringPreferencesKey("den")] = den.run { "$dayOfMonth. $monthValue." }
                        }
                        glanceAppWidget.update(context, id)
                    }
                }
            }

            private suspend fun rozvrhZobrazitNaDnesek() =
                when (val nastaveni = repo.nastaveni.first().prepnoutRozvrhWidget) {
                    is PrepnoutRozvrhWidget.OPulnoci -> true
                    is PrepnoutRozvrhWidget.VCas -> {
                        val cas = LocalTime.now()
                        cas < nastaveni.toLocalTime()
                    }

                    is PrepnoutRozvrhWidget.PoKonciVyucovani -> {
                        val cas = LocalTime.now()
                        println(cas)
                        val konecVyucovani = zjistitKonecVyucovani()
                        println(konecVyucovani)

                        cas < konecVyucovani.plusHours(nastaveni.poHodin.toLong())
                    }
                }

            private suspend fun zjistitKonecVyucovani(): LocalTime {
                val nastaveni = repo.nastaveni.first()

                val result = repo.ziskatDocument(Stalost.TentoTyden)
                println(result::class)

                if (result !is Uspech) return LocalTime.MIDNIGHT

                val tabulka = vytvoritTabulku(result.document)
                println(tabulka)

                val denTydne = LocalDate.now().dayOfWeek.value /* 1-5 (6-7) */
                println(denTydne)

                val den = tabulka.getOrNull(denTydne) ?: return LocalTime.NOON
                println(den)

                val hodina = den
                    .mapIndexed { i, hodina -> i to hodina }
                    .drop(1)
                    .filter { (_, hodina) -> hodina.first().predmet.isNotBlank() }
                    .lastOrNull { (_, hodina) ->
                        hodina.any { bunka ->
                            bunka.tridaSkupina.isEmpty() || bunka.tridaSkupina in nastaveni.mojeSkupiny
                        }
                    }
                    ?.first
                    ?: return LocalTime.NOON
                println(hodina)

                return tabulka.first()[hodina].first().ucitel.split(" - ")[1].split(":").let { LocalTime.of(it[0].toInt(), it[1].toInt()) }
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content(context) }
    }
}
