package cz.jaro.rozvrh.rozvrh.widgety

import android.appwidget.AppWidgetManager
import android.content.Context
import android.hardware.ConsumerIrManager.CarrierFrequencyRange
import android.widget.RemoteViews
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cz.jaro.rozvrh.MainActivity
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.RepositoryImpl
import cz.jaro.rozvrh.rozvrh.Bunka
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu.vytvoritTabulku
import cz.jaro.rozvrh.ui.theme.AppTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.FRIDAY
import java.util.Calendar.MONDAY
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import java.util.Calendar.THURSDAY
import java.util.Calendar.TUESDAY
import java.util.Calendar.WEDNESDAY


class DnesWidget(
//    private val bunky: List<Bunka>
) : GlanceAppWidget() {

    @Composable
    override fun Content() {

        val prefs = currentState<Preferences>()
        val bunky = Json.decodeFromString<List<Bunka>>(prefs[stringPreferencesKey("hodiny")] ?: "[]")
        val dm = prefs[booleanPreferencesKey("dm")] ?: false
        AppTheme(useDarkTheme = dm, useThemed = true, localContext = LocalContext) {
            Column(
                GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Cara()
                bunky
                    .ifEmpty { listOf(Bunka("", "Žádné hodiny!", "")) }
                    .forEach {
                        with(it) {
                            Box(
                                modifier = GlanceModifier
                                    .clickable(actionStartActivity<MainActivity>())
                                    .defaultWeight()
                                    .wrapContentWidth()
//                                    .border(1.dp, ColorProvider(R.color.gymceska_modra))
                                    .background(if (zbarvit) ColorProvider(R.color.pink) else ColorProvider(MaterialTheme.colorScheme.background)),
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
                                            color = ColorProvider(if (dm) Color.White else Color.Black)
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
                                        text = trida_skupina,
                                        modifier = GlanceModifier
                                            .clickable(actionStartActivity<MainActivity>())
                                            .padding(all = 8.dp),
                                        style = TextStyle(
                                            color = ColorProvider(if (dm) Color.White else Color.Black)
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
                                            color = ColorProvider(if (dm) Color.White else Color.Black)
                                        ),
                                    )
                                    Spacer(GlanceModifier.defaultWeight())
                                    Text(
                                        text = vyucujici,
                                        modifier = GlanceModifier
                                            .clickable(actionStartActivity<MainActivity>())
                                            .padding(bottom = 8.dp),
                                        style = TextStyle(
                                            color = ColorProvider(if (dm) Color.White else Color.Black)
                                        ),
                                    )
                                }
                            }
                        }
                        Cara()
                    }
            }
        }
    }

    @Composable
    fun Cara() = Box(modifier = GlanceModifier.height(2.dp).background(R.color.gymceska_modra)) {}

    companion object {

        private val dny = listOf(-1, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

        class Reciever : GlanceAppWidgetReceiver() {
            override val glanceAppWidget: GlanceAppWidget = DnesWidget()

            override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
                super.onUpdate(context, appWidgetManager, appWidgetIds)

                val repo = RepositoryImpl(context)

                MainScope().launch {
                    val hodiny = repo.ziskatDocument("Actual")?.let { doc ->
                        val tabulka = vytvoritTabulku(doc)

                        val cisloDne = dny.indexOf(Calendar.getInstance()[DAY_OF_WEEK]) /* 1-5 (6-7) */

                        tabulka
                            .getOrNull(cisloDne)
                            ?.asSequence()
                            ?.drop(1)
                            ?.mapIndexed { i, hodina -> i to hodina }
                            ?.filter { (i, hodina) -> hodina.first().predmet.isNotBlank() }
                            ?.map { (i, hodina) -> hodina.map { bunka -> bunka.copy(predmet = "$i. ${bunka.predmet}") } }
                            ?.mapNotNull { hodina ->
                                hodina.find { bunka ->
                                    bunka.trida_skupina.isEmpty() || bunka.trida_skupina in repo.mojeSkupiny
                                }
                            }
                            ?.toList()
                            ?.ifEmpty { listOf(Bunka("", "Žádné hodiny!", "")) }
                            ?: listOf(Bunka("", "Víkend", ""))
                    } ?: listOf(Bunka("", "Žádná data!", ""))

                    println(GlanceAppWidgetManager(context).getGlanceIds(DnesWidget::class.java) to GlanceAppWidgetManager(context))
                    appWidgetIds.forEach {
                        val id = GlanceAppWidgetManager(context).getGlanceIdBy(it)
                        println(it)

                        updateAppWidgetState(context, id) { prefs ->
                            prefs[stringPreferencesKey("hodiny")] = Json.encodeToString(hodiny)
                            prefs[booleanPreferencesKey("dm")] = repo.darkMode
                        }
                        glanceAppWidget.update(context, id)
                    }
                }
            }
        }
    }
}
