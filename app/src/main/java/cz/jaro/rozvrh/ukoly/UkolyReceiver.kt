package cz.jaro.rozvrh.ukoly

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.text.Html
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.core.app.NotificationManagerCompat
import cz.jaro.rozvrh.MainActivity
import cz.jaro.rozvrh.R
import java.util.Calendar

class UkolyReceiver : BroadcastReceiver() {

    private fun oznameni(ukoly: List<String>, ctx: Context?) {

        val ukolyBezDatAHtml = ukoly.map {
            val bezHtml = Html.fromHtml(it, 0).toString()
            val seznam = bezHtml.split(" – ", limit = 3)
            "${seznam[1]} – ${seznam[2]}"
        }

        val intent = Intent(ctx, MainActivity::class.java).apply {
            putExtra("ukoly", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(ctx!!, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val intent2 = Intent(ctx, MainActivity::class.java).apply {
            putExtra("ukoly", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("splnit", ukoly.toTypedArray())
        }
        val pendingIntent2: PendingIntent = PendingIntent.getActivity(ctx, 0, intent2, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "nastaveniOznameni"
        val channel = NotificationChannel(
            channelId,
            "Nastavení oznámení",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(ctx).notify(
            123456,
            Builder(ctx, "1").apply {
                setSmallIcon(R.drawable.ic_launcher)
                setContentTitle(ctx.getString(R.string.nesplnene_ukoly))
                setContentText(ctx.resources.getQuantityString(R.plurals.mate_ukoly, ukoly.size, ukoly.size))
                priority = PRIORITY_DEFAULT
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setVisibility(VISIBILITY_SECRET)
                setStyle(
                    BigTextStyle().bigText(
                        ukolyBezDatAHtml.joinToString("\n")
                    )
                )
                addAction(Action(
                    null, "Označit jako splněné", pendingIntent2
                ).apply {
                    extras.putStringArray("splnit", ukoly.toTypedArray())
                })
                setChannelId(channelId)
            }.build()
        )
    }

    private val mesice = listOf("Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec")

    override fun onReceive(ctx: Context?, intent: Intent?) {

        Log.d("ozn", "spusteno")

        val sp = ctx!!.getSharedPreferences("hm", Context.MODE_PRIVATE)


        // najit ukoly v zarizeni
        val ukoly = sp.getString("ukoly", null)!!.split("<br />")

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }

        Log.d("ozn", calendar.toString())

        val ukolyNaZitra = ukoly.filter {
            val bezHtml = Html.fromHtml(it, 0).toString()
            val datum = bezHtml.split(" – ")[0].replace(" ", "")
            val hmin =
                if (datum in mesice) {
                    listOf("1", (mesice.indexOf(datum) + 1).toString())
                } else {
                    datum.split(".")
                }
            calendar.get(Calendar.DAY_OF_MONTH) + 1 == hmin[0].toInt() && calendar.get(Calendar.MONTH) + 1 == hmin[1].toInt()
        }

        val skrtleUkoly = sp.getStringSet("skrtle_ukoly", mutableSetOf())!!.toMutableSet()

        val neskrtleUkolyNaZitra = ukolyNaZitra.filter {
            it !in skrtleUkoly
        }

        if (neskrtleUkolyNaZitra.any()) {

            Log.d("ozn", neskrtleUkolyNaZitra.toString())
            oznameni(neskrtleUkolyNaZitra, ctx)
        }

    }
}
