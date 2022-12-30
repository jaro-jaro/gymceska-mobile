package cz.jaro.rozvrh.rozvrh

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import cz.jaro.rozvrh.MainActivity
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.RepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.DAY_OF_YEAR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND
import java.util.Locale
import kotlin.system.exitProcess

class OznameniReceiver : BroadcastReceiver() {

    companion object {
        private val dny =
            listOf(-1, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

        suspend fun zariditOznameniNaPristiHodinu(
            repo: Repository,
        ) {
            withContext(Dispatchers.IO) {

                val sp = (repo as RepositoryImpl).ctx.getSharedPreferences("hm", Context.MODE_PRIVATE)
                val c = Calendar.getInstance()
                println(c)

                var d = dny.indexOf(c[DAY_OF_WEEK]) // 1-5 (6-7)
                val h = c[HOUR_OF_DAY]
                val m = c[MINUTE]
                println(listOf(d, h, m))

                val doc = repo.ziskatDocument("Actual") ?: exitProcess(-1)

                val tabulka = TvorbaRozvrhu.vytvoritTabulku(doc)
                println(tabulka)

                val cisloHodiny = tabulka.first().drop(1).map { it.first() }.find { bunka ->
                    bunka.vyucujici.split(" - ")[0].split(":").let { it[0].toInt() > h || (it[0].toInt() == h && it[1].toInt() >= m) }
                }?.predmet?.toInt() ?: run { d += 1; 0 }
                println(cisloHodiny)

                tabulka
                    .asSequence()
                    .drop(1)
                    .map { it.drop(1) }
                    .mapIndexed { i, den ->
                        den.mapIndexed { j, hodina ->
                            (hodina.find { bunka ->
                                bunka.trida_skupina.isEmpty() || bunka.trida_skupina in repo.mojeSkupiny
                            } ?: hodina.first()) to (i to j)
                        }
                    }
                    .drop(d - 1)
                    .flatten()
                    .drop(cisloHodiny)
                    .filter { it.first.predmet.isNotBlank() }
                    .let { hodiny ->
                        if (hodiny.toList().isEmpty()) tabulka.asSequence()
                            .drop(1)
                            .map { it.drop(1) }
                            .mapIndexed { i, den ->
                                den.mapIndexed { j, hodina ->
                                    (hodina.find { bunka ->
                                        bunka.trida_skupina.isEmpty() || bunka.trida_skupina in repo.mojeSkupiny
                                    } ?: hodina.first()) to (i to j)
                                }
                            }
                            .flatten()
                            .filter { it.first.predmet.isNotBlank() }
                        else hodiny
                    }
                    .toList()
                    .let {
                        println(it)
                        val cas = when (repo.oznameni.hodinovyOznameniPodle) {
                            OznameniState.TypHodinovyhoOznameni.PoMinulyHodine -> tabulka
                                .first()
                                .drop(1)[it.first().second.second]
                                .first()
                                .vyucujici
                                .split(" - ")[1]

                            OznameniState.TypHodinovyhoOznameni.PredDalsiHodinou -> tabulka
                                .first()
                                .drop(1)[it[1].second.second]
                                .first()
                                .vyucujici
                                .split(" - ")
                                .first()
                        }
                        val zitra = when (repo.oznameni.hodinovyOznameniPodle) {
                            OznameniState.TypHodinovyhoOznameni.PoMinulyHodine -> it.first().second.first > (d - 1)
                            OznameniState.TypHodinovyhoOznameni.PredDalsiHodinou -> it[1].second.first > (d - 1)
                        }

                        val alarmManager = repo.ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val calendar: Calendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                            set(HOUR_OF_DAY, cas.split(":")[0].toInt())
                            set(MINUTE, cas.split(":")[1].toInt())
                            add(MINUTE, repo.oznameni.cas)
                            set(SECOND, 0)
                            set(MILLISECOND, 0)
                            if (zitra) add(DAY_OF_YEAR, 1)
                        }
                        println(calendar)

                        sp.edit {
                            putLong("casOznameni", Calendar.getInstance().apply {
                                timeInMillis = System.currentTimeMillis()
                                set(HOUR_OF_DAY, cas.split(":")[0].toInt())
                                set(MINUTE, cas.split(":")[1].toInt())
                                set(SECOND, 0)
                                set(MILLISECOND, 0)
                                if (zitra) add(DAY_OF_YEAR, 1)
                            }.timeInMillis)
                        }

                        val intent = Intent(repo.ctx, OznameniReceiver::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            intent.identifier = "OZNAMENIHODIN"
                        }

                        val pendingIntent = PendingIntent.getBroadcast(repo.ctx, 2, intent, FLAG_IMMUTABLE)

                        alarmManager.cancel(pendingIntent)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
            }
        }
    }

    private fun oznameni(hodina: Bunka, ctx: Context?) {

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(ctx!!, 0, intent, FLAG_IMMUTABLE)

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "oznameniHodina"
        val channel = NotificationChannel(
            channelId,
            "Oznámení o následující hodině",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(ctx).cancel(654321)
        NotificationManagerCompat.from(ctx).notify(
            654321,
            Builder(ctx, channelId).apply {
                setSmallIcon(R.drawable.ic_launcher)
                setContentTitle("Příští hodina")
                setContentText(hodina.run { "Bude $predmet s $vyucujici v $ucebna, to je$umisteni" })
                priority = NotificationCompat.PRIORITY_MAX
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setVisibility(VISIBILITY_PUBLIC)
                setChannelId(channelId)
            }.build()
        )
    }

    override fun onReceive(ctx: Context, intent: Intent?) {
        val sp = ctx.getSharedPreferences("hm", Context.MODE_PRIVATE)
        Log.d("ozn", "spusteno")

        val repo = RepositoryImpl(ctx)

        val c = Calendar.getInstance().apply {
            timeInMillis = sp.getLong("casOznameni", 0)
        }

        GlobalScope.launch {

            val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

            var d = dny.indexOf(c[DAY_OF_WEEK]) // 1-5 (6-7)
            val h = c[HOUR_OF_DAY]
            val m = c[MINUTE]
            println(listOf(d, h, m))

            val doc = repo.ziskatDocument("Actual") ?: exitProcess(-1)

            val tabulka = TvorbaRozvrhu.vytvoritTabulku(doc)
            println(tabulka)

            val cisloHodiny = tabulka.first().drop(1).map { it.first() }.find { bunka ->
                bunka.vyucujici.split(" - ")[0].split(":").let { it[0].toInt() > h || (it[0].toInt() == h && it[1].toInt() >= m) }
            }?.predmet?.toInt() ?: run { d += 1; 0 }
            println(cisloHodiny)

            val hodina = tabulka[d][cisloHodiny + 1]

            oznameni(
                hodina = hodina.find { bunka ->
                    bunka.trida_skupina.isEmpty() || bunka.trida_skupina in sp.getStringSet("sve_skupiny", setOf())!!
                } ?: hodina.first(),
                ctx = ctx
            )
            zariditOznameniNaPristiHodinu(repo)
        }
    }
}
