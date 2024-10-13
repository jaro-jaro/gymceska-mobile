package cz.jaro.rozvrh

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.coroutineScope
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.ui.theme.GymceskaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.net.SocketTimeoutException

class MainActivity : ComponentActivity() {

    private val repo by inject<Repository>()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge(SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT), SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))

        Firebase.analytics.setUserId(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
        Firebase.crashlytics.setUserId(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))

        val rozvrh = intent.getBooleanExtra("rozvrh", false) || intent.getStringExtra("rozvrh") == "true"
        val ukoly = intent.getBooleanExtra("ukoly", false) || intent.getStringExtra("ukoly") == "true"

        val aktualizovatAplikaci = {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                val document = try {
                    withContext(Dispatchers.IO) {
                        Ksoup.parseGetRequest("https://raw.githubusercontent.com/jaro-jaro/gymceska-mobile/main/app/version.txt")
                    }
                } catch (e: SocketTimeoutException) {
                    Firebase.crashlytics.recordException(e)
                    return@launch
                }

                val nejnovejsiVerze = document.text()

                startActivity(Intent().apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("https://github.com/jaro-jaro/gymceska-mobile/releases/download/v$nejnovejsiVerze/Gymceska-v$nejnovejsiVerze.apk")
                })
            }
            Unit
        }

        setContent {
            val nastaveni by repo.nastaveni.collectAsStateWithLifecycle(Nastaveni(mojeTrida = Vjec.TridaVjec("")))
            val verzeNaRozbiti by repo.verzeNaRozbiti.collectAsStateWithLifecycle()
            val jePotrebaAktualizovatAplikaci by repo.jePotrebaAktualizovatAplikaci.collectAsStateWithLifecycle(false)

            GymceskaTheme(
                useDarkTheme = if (nastaveni.darkModePodleSystemu) isSystemInDarkTheme() else nastaveni.darkMode,
                useDynamicColor = nastaveni.dynamicColors,
                theme = nastaveni.tema,
            ) {
                if (verzeNaRozbiti >= BuildConfig.VERSION_CODE) AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                startActivity(Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    data = Uri.parse("https://github.com/jaro-jaro/gymceska-mobile/releases/latest")
                                })
                            }
                        ) {
                            Text("Přejít na GitHub")
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                    ),
                    title = {
                        Text("Tato aplikace je zastaralá")
                    },
                    text = {
                        Text("tak buď chytrý a nainstaluj si novou verzi")
                    },
                )

                MainContent(
                    rozvrh = rozvrh,
                    ukoly = ukoly,
                    jePotrebaAktualizovatAplikaci = jePotrebaAktualizovatAplikaci,
                    aktualizovatAplikaci = aktualizovatAplikaci
                )
            }
        }
    }
}
