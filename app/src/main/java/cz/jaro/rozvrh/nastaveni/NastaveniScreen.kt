package cz.jaro.rozvrh.nastaveni

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cz.jaro.rozvrh.BuildConfig
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.rozvrh.Vybiratko
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Destination
@Composable
fun NastaveniScreen(
    navigator: DestinationsNavigator
) {

    val viewModel = koinViewModel<NastaveniViewModel> {
        parametersOf()
    }

    val nastaveni by viewModel.nastaveni.collectAsStateWithLifecycle(null)
    val skupiny by viewModel.skupiny.collectAsStateWithLifecycle(null)

    NastaveniScreen(
        navigateBack = navigator::navigateUp,
        nastaveni = nastaveni,
        upravitNastaveni = viewModel::upravitNastaveni,
        skupiny = skupiny,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NastaveniScreen(
    navigateBack: () -> Unit,
    nastaveni: Nastaveni?,
    upravitNastaveni: ((Nastaveni) -> Nastaveni) -> Unit,
    skupiny: Sequence<String>?,
) = Surface {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.nastaveni))
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(Icons.Default.ArrowBack, "Zpět")
                    }
                }
            )
        }
    ) {
        if (nastaveni == null) LinearProgressIndicator(Modifier.padding(it))
        else LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(it)
                .padding(all = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Určit tmavý režim podle sytému")
                    Switch(
                        checked = nastaveni.darkModePodleSystemu,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(darkModePodleSystemu = it)
                            }
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(id = R.string.tmavy_rezim))
                    Switch(
                        checked = if (nastaveni.darkModePodleSystemu) isSystemInDarkTheme() else nastaveni.darkMode,
                        enabled = !nastaveni.darkModePodleSystemu,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(darkMode = it)
                            }
                        }
                    )
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Použícat dynamické barvy")
                    Switch(
                        checked = nastaveni.dynamicColors,
                        onCheckedChange = {
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(dynamicColors = it)
                            }
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(id = R.string.zvolte_svou_tridu))
                    Vybiratko(
                        seznam = Vjec.tridy,
                        value = nastaveni.mojeTrida
                    ) {
                        upravitNastaveni { nastaveni ->
                            nastaveni.copy(mojeTrida = it)
                        }
                    }
                }
            }
            if (skupiny == null) item {
                LinearProgressIndicator()
            }
            else items(skupiny.toList()) { skupina ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = skupina in nastaveni.mojeSkupiny,
                        onCheckedChange = { chciJi ->
                            upravitNastaveni { nastaveni ->
                                nastaveni.copy(mojeSkupiny = if (chciJi) nastaveni.mojeSkupiny + skupina else nastaveni.mojeSkupiny - skupina)
                            }
                        }
                    )
                    Text(skupina)
                }
            }
            item {
                Text("Verze aplikace: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }
            item {
                Text("Simulate crash...", Modifier.clickable {
                    throw RuntimeException("Test exception")
                }, fontSize = 10.sp)
            }
        }
    }
}