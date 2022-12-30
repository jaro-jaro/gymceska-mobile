package cz.jaro.rozvrh.rozvrh

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cz.jaro.rozvrh.MainActivity
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.RepositoryImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.system.exitProcess

class DenniReciever : BroadcastReceiver() {

    companion object {
        private val dny =
            listOf(-1, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
    }

    private fun oznameni(rozvrh: List<Bunka>, ctx: Context, repo: Repository) {

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "oznameniRozvrh"
        val channel = NotificationChannel(
            channelId,
            "Denní oznámení o rozvrhu",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(ctx).cancel(987456)
        NotificationManagerCompat.from(ctx).notify(
            987456,
            NotificationCompat.Builder(ctx, channelId).apply {
                setSmallIcon(R.drawable.ic_launcher)
                setContentTitle("Rozvrh na ${if (repo.oznameni.denniOznameniNaZitrek) "zítra" else "dnešek"}")
                setContentText("Máte ${rozvrh.size} hodin (rozbal oznámení pro seznam)")
                setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        rozvrh.joinToString("\n") {
                            it.run { "$predmet s $vyucujici v $ucebna" }
                        }
                    )
                )
                priority = NotificationCompat.PRIORITY_MAX
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setChannelId(channelId)
            }.build()
        )
    }

    override fun onReceive(ctx: Context, intent: Intent?) {
        Log.d("ozn", "spusteno")

        val repo = RepositoryImpl(ctx)

        GlobalScope.launch {

            val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

            val d = dny.indexOf(Calendar.getInstance()[Calendar.DAY_OF_WEEK]) // 1-5 (6-7)
            println(d)

            val doc = repo.ziskatDocument("Actual") ?: exitProcess(-1)

            val tabulka = TvorbaRozvrhu.vytvoritTabulku(doc)
            println(tabulka)

            val cisloDne = d + repo.oznameni.denniOznameniNaZitrek.toInt()

            if (cisloDne in 1..5) {
                oznameni(
                    rozvrh = tabulka[cisloDne].drop(1)
                        .map { hodina ->
                            hodina.find { bunka ->
                                bunka.trida_skupina.isEmpty() || bunka.trida_skupina in repo.mojeSkupiny
                            } ?: hodina.first()
                        }
                        .mapIndexed { i, it -> it.copy(predmet = "$i. ${it.predmet}") }
                        .filter { it.predmet.isNotBlank() },
                    ctx = ctx,
                    repo = repo
                )
            }
        }
    }
}

fun Boolean.toInt() = if (this) 1 else 0
