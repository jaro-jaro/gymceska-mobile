package cz.jaro.rozvrh

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import cz.jaro.rozvrh.destinations.RozvrhScreenDestination
import cz.jaro.rozvrh.destinations.UkolyScreenDestination

@Composable
fun MainSceeen(
    rozvrh: Boolean,
    ukoly: Boolean,
) {
    Surface {
        val navController = rememberNavController()
        val destination by navController.appCurrentDestinationAsState()

        LaunchedEffect(Unit) {
            if (rozvrh) navController.navigate(RozvrhScreenDestination())
            if (ukoly) navController.navigate(UkolyScreenDestination())
        }

        LaunchedEffect(Unit) {
            val destinationFlow = navController.appCurrentDestinationFlow

            destinationFlow.collect { destination ->
                Firebase.analytics.logEvent("navigation") {
                    param("route", destination.route)
                }
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = destination is RozvrhScreenDestination,
                        onClick = {
                            navController.navigate(RozvrhScreenDestination())
                        },
                        icon = {
                            Icon(Icons.Default.TableChart, null)
                        },
                        label = {
                            Text(stringResource(R.string.rozvrh))
                        }
                    )
                    NavigationBarItem(
                        selected = destination == UkolyScreenDestination,
                        onClick = {
                            navController.navigate(UkolyScreenDestination())
                        },
                        icon = {
                            Icon(Icons.Default.FormatListBulleted, null)
                        },
                        label = {
                            Text(stringResource(R.string.domaci_ukoly))
                        }
                    )
                }
            }
        ) { paddingValues ->
            DestinationsNavHost(navGraph = NavGraphs.root, Modifier.padding(paddingValues), navController = navController)
        }
    }
}