package cz.jaro.rozvrh.ukoly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.App.Companion.navigate
import cz.jaro.rozvrh.destinations.SpravceUkoluScreenDestination
import org.koin.androidx.compose.koinViewModel

@Destination
@Composable
fun UkolyScreen(
    navigator: DestinationsNavigator,
) {

    val viewModel = koinViewModel<UkolyViewModel>()

    val ukoly by viewModel.ukoly.collectAsStateWithLifecycle()

    UkolyScreen(
        ukoly = ukoly,
        navigate = navigator.navigate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UkolyScreen(
    ukoly: List<String>?,
    navigate: (Direction) -> Unit,
) = Surface {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Domácí úkoly")
                },

                actions = {
                    var menu by rememberSaveable { mutableStateOf(false) }
                    DropdownMenu(
                        expanded = menu,
                        onDismissRequest = {
                            menu = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Spravovat úkoly")
                            },
                            onClick = {
                                navigate(SpravceUkoluScreenDestination)
                                menu = false
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            menu = true
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, "Více")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (ukoly == null) item { LinearProgressIndicator() }
            else items(ukoly) {
                Text(it)
            }
        }
    }
}