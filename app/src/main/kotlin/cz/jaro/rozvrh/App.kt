package cz.jaro.rozvrh

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.app
import com.google.firebase.crashlytics.crashlytics
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.bind
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(module {
            single { this@App } bind Context::class
            single { get<Context>().getSharedPreferences("prefs-gymceska-multiplatform", Context.MODE_PRIVATE) }
            single { SharedPreferencesSettings(get()) } bind ObservableSettings::class
            single { UserOnlineManager { get<Context>().isOnline() } }
            single { UserIdProvider { get<Context>().getUserId() } }
            single { Firebase.app }
        })

        Firebase.analytics.setUserId(getUserId())
        Firebase.crashlytics.setUserId(getUserId())
    }

    companion object {
        @SuppressLint("HardwareIds")
        fun Context.getUserId(): String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        fun Context.isOnline(): Boolean {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false

            return capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            )
        }
    }
}
