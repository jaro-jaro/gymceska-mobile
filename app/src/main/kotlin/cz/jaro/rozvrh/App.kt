package cz.jaro.rozvrh

import android.app.Application
import android.content.Context
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(module {
                single { this@App } bind Context::class
                single { get<Context>().getSharedPreferences("prefs-gymceska-multiplatform", Context.MODE_PRIVATE) }
                single { SharedPreferencesSettings(delegate = get()) } bind ObservableSettings::class
            }, module {
                single { Repository(settings = get(), ctx = get()) }
            })
        }
    }

    companion object {
        val NavController.navigate
            get() = navigate@{ route: Route ->
                try {
                    navigate(route.also(::println))
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    Firebase.crashlytics.log("Pokus o navigaci na $route")
                    Firebase.crashlytics.recordException(e)
                }
            }
    }
}
