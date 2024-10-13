package cz.jaro.rozvrh

import android.app.Application
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import cz.jaro.rozvrh.nastaveni.NastaveniViewModel
import cz.jaro.rozvrh.rozvrh.RozvrhViewModel
import cz.jaro.rozvrh.ukoly.UkolyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(module {
                viewModel { RozvrhViewModel(it.get(), repo = get()) }
                viewModel { UkolyViewModel(repo = get()) }
                viewModel { NastaveniViewModel(repo = get(), it.get(), it.get()) }
                single { Repository(ctx = get()) }
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
