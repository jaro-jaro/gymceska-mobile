package cz.jaro.rozvrh

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.DestinationsNavHost
import cz.jaro.compose_dialog.dialogState
import cz.jaro.rozvrh.destinations.RozvrhDestination
import cz.jaro.rozvrh.destinations.UkolyDestination

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
        val destination by navController.appCurrentDestinationAsState()

        LaunchedEffect(Unit) {
            if (rozvrh) navController.navigateToDestination(RozvrhDestination())
            if (ukoly) navController.navigateToDestination(UkolyDestination())
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
                        selected = destination is RozvrhDestination,
                        onClick = {
                            navController.navigateToDestination(RozvrhDestination())
                        },
                        icon = {
                            Icon(Icons.Default.TableChart, null)
                        },
                        label = {
                            Text(stringResource(R.string.rozvrh))
                        }
                    )
                    NavigationBarItem(
                        selected = destination == UkolyDestination,
                        onClick = {
                            navController.navigateToDestination(UkolyDestination())
                        },
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null)
                        },
                        label = {
                            Text(stringResource(R.string.domaci_ukoly))
                        }
                    )
                }
            }
        ) { paddingValues ->
            cz.jaro.compose_dialog.AlertDialog(dialogState)
            DestinationsNavHost(navGraph = NavGraphs.root, Modifier.padding(paddingValues), navController = navController)
        }
    }
}