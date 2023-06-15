package cz.jaro.rozvrh.rozvrh.widgety

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cz.jaro.rozvrh.MainActivity
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
import java.util.Calendar
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.FRIDAY
import java.util.Calendar.MONDAY
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import java.util.Calendar.THURSDAY
import java.util.Calendar.TUESDAY
import java.util.Calendar.WEDNESDAY


class DnesWidget : GlanceAppWidget() {

    @Suppress("DEPRECATION")
    @Composable
    fun Content() = GlanceTheme {
        val prefs = currentState<Preferences>()
        val bunky = Json.decodeFromString<List<Bunka>>(prefs[stringPreferencesKey("hodiny")] ?: "[]")

        val bg = ColorProvider(Color(LocalContext.current.resources.getColor(R.color.background_color)))
        val bg2 = ColorProvider(Color(LocalContext.current.resources.getColor(R.color.background_color_alt)))
        val onbg = ColorProvider(Color(LocalContext.current.resources.getColor(R.color.on_background_color)))
        val onbg2 = ColorProvider(Color(LocalContext.current.resources.getColor(R.color.on_background_color_alt)))

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
                                                    .clickable(actionStartActivity<MainActivity>()),
                                                style = TextStyle(
                                                    color = if (zbarvit) onbg2 else onbg
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

        private val dny = listOf(-1, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

        class Reciever : GlanceAppWidgetReceiver(), KoinComponent {
            override val glanceAppWidget: GlanceAppWidget = DnesWidget()

            override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
                super.onUpdate(context, appWidgetManager, appWidgetIds)

                val repo = get<Repository>()

                CoroutineScope(Dispatchers.IO).launch {
                    val nastaveni = repo.nastaveni.first()
                    val hodiny = repo.ziskatDocument(Stalost.TentoTyden).let { result ->
                        if (result !is Uspech) return@let listOf(Bunka("", "Žádná data!", ""))

                        val tabulka = vytvoritTabulku(result.document)

                        val cisloDne = dny.indexOf(Calendar.getInstance()[DAY_OF_WEEK]) /* 1-5 (6-7) */

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
                                    Bunka("", "Žádné hodiny!", ""),
                                    Bunka("", "Žádné hodiny!", ""),
                                    Bunka("", "Žádné hodiny!", ""),
                                    Bunka("", "Žádné hodiny!", ""),
                                )
                            }
                            ?: listOf(Bunka("", "Víkend", ""))
                    }

                    appWidgetIds.forEach {
                        val id = GlanceAppWidgetManager(context).getGlanceIdBy(it)

                        updateAppWidgetState(context, id) { prefs ->
                            prefs[stringPreferencesKey("hodiny")] = Json.encodeToString(hodiny)
                        }
                        glanceAppWidget.update(context, id)
                    }
                }
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }
}
