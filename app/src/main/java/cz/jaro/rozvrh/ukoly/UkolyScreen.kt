package cz.jaro.rozvrh.ukoly

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.App.Companion.navigate
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.destinations.NastaveniScreenDestination
import cz.jaro.rozvrh.destinations.SpravceUkoluScreenDestination
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Destination
@Composable
fun UkolyScreen(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<UkolyViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val jeOnline by viewModel.jeOnline.collectAsStateWithLifecycle()
    val jeInteligentni by viewModel.inteligentni.collectAsStateWithLifecycle()

    UkolyScreen(
        state = state,
        skrtnout = viewModel::skrtnout,
        odskrtnout = viewModel::odskrtnout,
        navigate = navigator.navigate,
        jeOffline = !jeOnline,
        jeInteligentni = jeInteligentni,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UkolyScreen(
    state: UkolyState,
    skrtnout: (UUID) -> Unit,
    odskrtnout: (UUID) -> Unit,
    navigate: (Direction) -> Unit,
    jeOffline: Boolean,
    jeInteligentni: Boolean,
) = Surface {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.domaci_ukoly))
                },

                actions = {
                    IconButton(
                        onClick = {
                            navigate(NastaveniScreenDestination)
                        }
                    ) {
                        Icon(Icons.Default.Settings, stringResource(R.string.nastaveni))
                    }

                    if (jeInteligentni && !jeOffline) IconButton(
                        onClick = {
                            navigate(SpravceUkoluScreenDestination)
                        }
                    ) {
                        Icon(Icons.Default.Edit, stringResource(R.string.spravovat_ukoly))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (state) {
                UkolyState.Nacitani -> item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                is UkolyState.Nacteno -> {
                    items(state.ukoly.size, key = { state.ukoly[it].id }) { i ->
                        val ukol = state.ukoly[i]

                        if (ukol.stav == StavUkolu.TakovaTaBlboVecUprostred) {
                            val alpha by animateFloatAsState(if (i != state.ukoly.lastIndex) 1F else 0F, label = "alpha")
                            Text(stringResource(R.string.splnene_ukoly),
                                Modifier
                                    .animateItemPlacement()
                                    .alpha(alpha))
                        }
                        else Row(
                            Modifier.animateItemPlacement(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = ukol.stav == StavUkolu.Skrtly,
                                onCheckedChange = {
                                    (if (ukol.stav == StavUkolu.Skrtly) odskrtnout else skrtnout)(ukol.id)
                                }
                            )
                            if (ukol.stav == StavUkolu.Skrtly) Text(
                                text = ukol.text,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .38F)
                            )
                            else Text(
                                text = ukol.text,
                            )
                        }
                    }
                    if (state.ukoly.isEmpty()) item {
                        Text("Žádné úkoly nejsou! Jupí!!!")
                    }
                }
            }
        }
    }
}