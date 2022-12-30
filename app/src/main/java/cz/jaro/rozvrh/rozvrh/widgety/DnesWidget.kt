package cz.jaro.rozvrh.rozvrh.widgety

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.net.ConnectivityManager
import android.widget.RemoteViews
import androidx.core.content.edit
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.Bunka
import cz.jaro.rozvrh.rozvrh.Seznamy
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu.vytvoritTabulku
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.FRIDAY
import java.util.Calendar.MONDAY
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import java.util.Calendar.THURSDAY
import java.util.Calendar.TUESDAY
import java.util.Calendar.WEDNESDAY
import java.util.Calendar.getInstance
import java.util.Date
import java.util.Locale


class DnesWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        val zobrazit = mutableListOf<Bunka>()

        Thread {

            val doc: Document?

            val sp = context.getSharedPreferences("hm", Context.MODE_PRIVATE)

            val trida = sp.getString("sva_trida", "4.E")!!
            val tridaIndex = sp.getInt("sva_trida_index", 11)

            if (isOnline(context)) {
                doc = Jsoup.connect(Seznamy.tridyOdkazy[tridaIndex-1].replace("###", "Actual")).get()

                sp.edit {
                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                    putString("rozvrh_${trida}_Actual", doc.toString())
                    putString("rozvrh_${trida}_Actual_datum", formatter.format(Date()))
                }
            } else {

                val html = sp.getString("rozvrh_${trida}_Actual", "")

                doc = if (html == "") {
                    zobrazit += Bunka("", "Žádná data", "")
                    null
                } else Jsoup.parse(html!!)
            }

            val dny = listOf(-1, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

            if (doc != null) {
                val tabulka = vytvoritTabulku(doc)

                val c = getInstance()

                when (val d = dny.indexOf(c[DAY_OF_WEEK])) { /* 1-5 (6-7) */

                    in 1..5 -> zobrazit += tabulka[d].drop(1)
                        .mapIndexed { i, hodina -> hodina.map { bunka -> bunka.copy(predmet = "$i. ${bunka.predmet}") } }
                        .filter { hodina -> hodina.first().predmet.isNotBlank() }
                        .map { hodina -> hodina.find { bunka ->
                            bunka.trida_skupina.isEmpty() || bunka.trida_skupina in sp.getStringSet("sve_skupiny", setOf())!!
                        } ?: hodina.first() }
                    in 6..7 -> zobrazit += Bunka("", "Víkend", "")
                }

                if (zobrazit.isEmpty()) zobrazit += Bunka("", "Žádné hodiny!", "")
            }

            for (appWidgetId in appWidgetIds) {

                val rv = RemoteViews(context.packageName, R.layout.dnes_widget).apply {

                    removeAllViews(R.id.ll)

                    //setInt(R.id.ll, "setBackgroundResource", R.color.black)

                    zobrazit.forEach { bunka ->
                        val rvUvnitr = RemoteViews(context.packageName, R.layout.bunka).apply {

                            setTextViewText(R.id.tvPredmet, bunka.predmet)
                            setTextViewText(R.id.tvSkupina, bunka.trida_skupina)
                            setTextViewText(R.id.tvUcebna, bunka.ucebna)
                            setTextViewText(R.id.tvNikdo, bunka.vyucujici)

                            if (bunka.zbarvit) setInt(R.id.rlBunka, "setBackgroundResource", R.color.pink)
                            else setInt(R.id.rlBunka, "setBackgroundResource", R.color.white)
                        }

                        addView(R.id.ll, rvUvnitr)
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, rv)
            }
        }.start()
    }


    private fun isOnline(ctx: Context): Boolean {
        val connManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }
}
