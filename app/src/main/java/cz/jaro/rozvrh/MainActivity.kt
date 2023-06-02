package cz.jaro.rozvrh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.jaro.rozvrh.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

//    private lateinit var binding: ActivityMainBinding

    private val repo by inject<Repository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rozvrh = intent.getBooleanExtra("rozvrh", false) || intent.getStringExtra("rozvrh") == "true"
        val ukoly = intent.getBooleanExtra("ukoly", false) || intent.getStringExtra("ukoly") == "true"
        setContent {
            val nastaveni by repo.nastaveni.collectAsStateWithLifecycle(Nastaveni())

            AppTheme(
                useDarkTheme = if (nastaveni.darkModePodleSystemu) isSystemInDarkTheme() else nastaveni.darkMode,
                useDynamicColor = nastaveni.dynamicColors,
            ) {
                MainSceeen(
                    rozvrh = rozvrh,
                    ukoly = ukoly,
                )
            }
        }
    }
}
