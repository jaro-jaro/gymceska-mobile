package cz.jaro.rozvrh

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        println("A")
        FirebaseApp.initializeApp(this)
    }
}
