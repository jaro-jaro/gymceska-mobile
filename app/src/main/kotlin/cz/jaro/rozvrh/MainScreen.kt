package cz.jaro.rozvrh

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import cz.jaro.compose_dialog.dialogState
import cz.jaro.rozvrh.nastaveni.Nastaveni
import cz.jaro.rozvrh.rozvrh.Rozvrh
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.ukoly.SpravceUkolu
import cz.jaro.rozvrh.ukoly.Ukoly
import kotlin.reflect.KType

inline fun <reified T : Route> typeMap() = when (T::class) {
    Route.Rozvrh::class -> mapOf(
        serializationTypePair<Int?>(),
        serializationTypePair<Boolean?>(),
        serializationTypePair<Vjec?>(),
        nullableEnumTypePair<Stalost>(),
    )
    else -> emptyMap<KType, NavType<*>>()
}

@Composable
fun MainContent(
    rozvrh: Boolean,
    ukoly: Boolean,
    jePotrebaAktualizovatAplikaci: Boolean,
    aktualizovatAplikaci: () -> Unit,
) {
    if (jePotrebaAktualizovatAplikaci) {
        var zobrazitDialog by remember { mutableStateOf(true) }

        if (zobrazitDialog) AlertDialog(
            onDismissRequest = {
                zobrazitDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        zobrazitDialog = false
                        aktualizovatAplikaci()
                    }
                ) {
                    Text("Ano")
                }
            },
            title = {
                Text("Aktualizace aplikace")
            },
            text = {
                Text("Je k dispozici nová verze aplikace, chcete ji aktualizovat?")
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        zobrazitDialog = false
                    }
                ) {
                    Text("Ne")
                }
            },
        )
    }
    Surface {
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            if (rozvrh) navController.navigate(Route.Rozvrh())
            if (ukoly) navController.navigate(Route.Ukoly)
        }

        LaunchedEffect(Unit) {
            val destinationFlow = navController.currentBackStackEntryFlow

            destinationFlow.collect { destination ->
                Firebase.analytics.logEvent("navigation") {
                    param("route", destination.generateRouteWithArgs() ?: "")
                }
            }
        }

        cz.jaro.compose_dialog.AlertDialog(dialogState)
        NavHost(
            navController = navController,
            startDestination = Route.Rozvrh(),
            popEnterTransition = {
                scaleIn(
                    animationSpec = tween(
                        durationMillis = 100,
                        delayMillis = 35,
                    ),
                    initialScale = 1.1F,
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 100,
                        delayMillis = 35,
                    ),
                )
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.9F,
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 35,
                        easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f),
                    ),
                )
            },
        ) {
            route<Route.Rozvrh> { Rozvrh(args = it, navController = navController) }
            route<Route.Ukoly> { Ukoly(args = it, navController = navController) }
            route<Route.SpravceUkolu> { SpravceUkolu(args = it, navController = navController) }
            route<Route.Nastaveni> { Nastaveni(args = it, navController = navController) }
        }
    }
}