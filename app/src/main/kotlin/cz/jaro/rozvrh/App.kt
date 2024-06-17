package cz.jaro.rozvrh

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.suplovani.SuplovaniApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import retrofit2.Retrofit

class App : Application() {

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            defaultModule()
            modules(module {
                single {
                    PreferenceDataStoreFactory.create(
                        migrations = listOf(
                            SharedPreferencesMigration({
                                getSharedPreferences("hm", Context.MODE_PRIVATE)!!
                            }),
                        )
                    ) {
                        preferencesDataStoreFile("Gymceska_JARO_datastore")
                    }
                }
                single {
                    Retrofit.Builder()
                        .baseUrl("https://gymceska.bakalari.cz/next/")
                        .build()
                        .create(SuplovaniApi::class.java)
                }
            })
        }
    }

    companion object {
        val DestinationsNavigator.navigate
            get() = { it: Direction ->
                navigate(it)
            }
    }
}
