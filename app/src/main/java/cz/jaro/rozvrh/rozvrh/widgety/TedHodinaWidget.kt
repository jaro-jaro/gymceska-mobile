package cz.jaro.rozvrh.rozvrh.widgety

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.net.ConnectivityManager
import android.widget.RemoteViews
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.Seznamy
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu.vytvoritTabulku
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*


class TedHodinaWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        var predmet   = ""
        var skupina   = ""
        var ucebna    = ""
        var vyucujici = ""

        val sp = context.getSharedPreferences("hm", Context.MODE_PRIVATE)

        Thread {

            val doc = try {

                val trida = sp.getString("sva_trida", "4.E")!!
                val tridaIndex = sp.getInt("sva_trida_index", 11)

                if (isOnline(context)) {
                    val doc = Jsoup.connect(Seznamy.tridyOdkazy[tridaIndex-1].replace("###", "Actual")).get()

                    sp.edit().apply {
                        putString("rozvrh_${trida}_Actual", doc.toString())

                        val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                        putString("rozvrh_${trida}_Actual_datum", formatter.format(Date()))

                        apply()
                    }

                    doc

                } else {

                    val html = sp.getString("rozvrh_${trida}_Actual", "")

                    if (html == "") {
                        predmet = "Žádná data"
                        null
                    } else {
                        Jsoup.parse(html!!)
                    }

                }

            } catch (e: Exception) {

                e.printStackTrace()

                predmet = "Žádná data"
                null
            }

            val dny = listOf(-1, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

            if (doc != null) {
                val tabulka = vytvoritTabulku(doc)

                val hodiny = tabulka[0].drop(1)
                    .map { it[0].vyucujici.split(" - ").map { it.split(":").map { it.toInt() } } }

                val c = getInstance()
                var d = dny.indexOf(c.get(DAY_OF_WEEK)) /* 1-5 (6-7) */
                
                var hodina = hodiny.indices.indexOfFirst { i ->

                    c.set(1, 1, 2)
                    val hKonCal = getInstance()
                    hKonCal.set(1, 1, 2, hodiny[i][1][0], hodiny[i][1][1])

                    if (d == 6 || d == 7) return@indexOfFirst false

                    c.toInstant().toEpochMilli() < hKonCal.toInstant().toEpochMilli()
                            && tabulka[d][i+1].isNotEmpty()
                } + 1

                if (hodina == 0) d++
                if (hodina == 0) hodina = hodiny.indices.indexOfFirst { i ->

                    c.set(1, 1, 1)
                    val hKonCal = getInstance()
                    hKonCal.set(1, 1, 2, hodiny[i][1][0], hodiny[i][1][1])

                    if (d == 8) d = 1

                    if (d == 6 || d == 7) return@indexOfFirst false

                    c.toInstant().toEpochMilli() < hKonCal.toInstant().toEpochMilli()
                            && tabulka[d][i+1].isNotEmpty()
                } + 1

                if (d == 6 || d == 7 || hodina == 0) {
                    predmet = "Žádná hodina"
                }
                else {
                    val bunka = tabulka[d][hodina].first {
                        it.trida_skupina.isEmpty() ||  it.trida_skupina in sp.getStringSet("sve_skupiny", setOf())!!
                    }

                    predmet = bunka.predmet
                    skupina = bunka.trida_skupina
                    ucebna = bunka.ucebna
                    vyucujici = bunka.vyucujici
                }

            }

            for (appWidgetId in appWidgetIds) {

                val views = RemoteViews(context.packageName, R.layout.bunka).apply {
                    setTextViewText(R.id.tvPredmet, predmet)
                    setTextViewText(R.id.tvSkupina, skupina)
                    setTextViewText(R.id.tvUcebna, ucebna)
                    setTextViewText(R.id.tvNikdo, vyucujici)

                    setInt(R.id.rlBunka, "setBackgroundResource", R.color.white);
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)

            }
        }.start()
    }


    private fun isOnline(ctx: Context): Boolean {
        val connManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }


}
