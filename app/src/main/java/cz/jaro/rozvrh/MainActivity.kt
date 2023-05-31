package cz.jaro.rozvrh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import cz.jaro.rozvrh.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

//    private lateinit var binding: ActivityMainBinding

    private val repo by inject<Repository>()

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.Theme_Rozvrh)
        super.onCreate(savedInstanceState)
//        FirebaseApp.initializeApp(this)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

//        val sp = getSharedPreferences("hm", Context.MODE_PRIVATE)
//
//        if (!sp.getBoolean("poprve", true)) {
//
//            if (sp.getBoolean("DM", false))
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            else
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        setSupportActionBar(binding.toolbar)
//
//        title = getString(R.string.rozvrh)
//
//        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
//
//        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
//
//            fragment.navController.popBackStack()
//
//            when(it.itemId) {
//                R.id.optionRozvrh -> {
//                    title = getString(R.string.rozvrh)
//                    fragment.navController.navigate(R.id.rozvrhFragment)
//                }
//                R.id.optionSuplovani -> {
//                    title = getString(R.string.suplovani)
//                    fragment.navController.navigate(R.id.suplovaniFragment)
//                }
//                R.id.optionUkoly -> {
//                    title = getString(R.string.domaci_ukoly)
//                    fragment.navController.navigate(R.id.ukolyFragment)
//                }
//            }
//
//            return@setOnNavigationItemSelectedListener true
//        }
//
//        Log.d("hm", intent.extras.toString())
//
//        if (intent.getBooleanExtra("rozvrh", false) || intent.getStringExtra("rozvrh") == "true" ) {
//            title = getString(R.string.rozvrh)
//            fragment.navController.navigate(R.id.rozvrhFragment)
//            binding.bottomNavigationView.selectedItemId = R.id.optionRozvrh
//        }
//        if (intent.getBooleanExtra("suplovani", false) || intent.getStringExtra("suplovani") == "true" ) {
//            title = getString(R.string.suplovani)
//            fragment.navController.navigate(R.id.suplovaniFragment)
//            binding.bottomNavigationView.selectedItemId = R.id.optionSuplovani
//        }
//        Log.d("funguj", sp.getStringSet("skrtle_ukoly", setOf()).toString())
//        Log.d("funguj", intent.getStringArrayExtra("splnit")?.toSet().toString())
//        if (intent.getStringArrayExtra("splnit")?.toSet().isNullOrEmpty().not()) {
//            Log.d("funguj", sp.getStringSet("skrtle_ukoly", setOf()).toString())
//
//            sp.edit {
//                putStringSet("skrtle_ukoly",
//                    sp.getStringSet("skrtle_ukoly", setOf())!! +
//                    (intent.getStringArrayExtra("splnit") ?: arrayOf()).toSet()
//                )
//            }
//
//            NotificationManagerCompat.from(this).cancelAll()
//        }
//        if (intent.getBooleanExtra("ukoly", false) || intent.getStringExtra("ukoly") == "true" ) {
//            title = getString(R.string.domaci_ukoly)
//            fragment.navController.navigate(R.id.ukolyFragment)
//            binding.bottomNavigationView.selectedItemId = R.id.optionUkoly
//        }
        setContent {
            AppTheme(
                useDarkTheme = if (repo.nastaveni.darkModePodleSystemu) isSystemInDarkTheme() else repo.nastaveni.darkMode,
                useDynamicColor = repo.nastaveni.dynamicColors,
            ) {
                MainSceeen()
            }
        }
    }
//
//    fun schovatToolbar() {
//        binding.toolbar.visibility = View.GONE
//    }
//    fun ukazatToolbar() {
//        binding.toolbar.visibility = View.VISIBLE
//    }
}
