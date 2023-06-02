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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import cz.jaro.rozvrh.destinations.RozvrhScreenDestination
import cz.jaro.rozvrh.destinations.UkolyScreenDestination

@Composable
fun MainSceeen() {
    Surface {
        val navController = rememberNavController()
        val destination by navController.appCurrentDestinationAsState()

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
                            Text("Rozvrh")
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
                            Text("Domácí úkoly")
                        }
                    )
                }
            }
        ) { paddingValues ->
            DestinationsNavHost(navGraph = NavGraphs.root, Modifier.padding(paddingValues), navController = navController)
        }
    }
}