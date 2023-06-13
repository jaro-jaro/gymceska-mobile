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
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import cz.jaro.rozvrh.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

//    private lateinit var binding: ActivityMainBinding

    private val repo by inject<Repository>()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.analytics.setUserId(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
        Firebase.crashlytics.setUserId(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))

        val rozbitFlow = repo.verzeNaRozbiti

        val rozvrh = intent.getBooleanExtra("rozvrh", false) || intent.getStringExtra("rozvrh") == "true"
        val ukoly = intent.getBooleanExtra("ukoly", false) || intent.getStringExtra("ukoly") == "true"
        setContent {
            val nastaveni by repo.nastaveni.collectAsStateWithLifecycle(Nastaveni())

            val rozbit by rozbitFlow.collectAsStateWithLifecycle()

            AppTheme(
                useDarkTheme = if (nastaveni.darkModePodleSystemu) isSystemInDarkTheme() else nastaveni.darkMode,
                useDynamicColor = nastaveni.dynamicColors,
            ) {
                if (rozbit >= BuildConfig.VERSION_CODE) AlertDialog(
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

                MainSceeen(
                    rozvrh = rozvrh,
                    ukoly = ukoly,
                )
            }
        }
    }
}
