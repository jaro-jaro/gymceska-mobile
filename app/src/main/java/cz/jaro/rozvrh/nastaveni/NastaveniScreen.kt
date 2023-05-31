package cz.jaro.rozvrh.nastaveni

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.glance.text.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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

    val viewModel = koinViewModel<NastaveniViewMoel> {
        parametersOf()
    }

    val nastaveni by viewModel.nastaveni.collectAsStateWithLifecycle()
    val skupiny by viewModel.skupiny.collectAsStateWithLifecycle(null)

    NastaveniScreen(
        navigateBack = navigator::navigateUp,
        save = viewModel::save,
        nastaveni = nastaveni,
        upravitNastaveni = viewModel::upravitNastaveni,
        skupiny = skupiny,
    )
}

@Composable
fun NastaveniScreen(
    navigateBack: () -> Unit,
    save: (() -> Unit) -> Unit,
    nastaveni: Nastaveni,
    upravitNastaveni: ((Nastaveni) -> Nastaveni) -> Unit,
    skupiny: Sequence<String>?,
) {

    Column(
        Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(id = R.string.nastaveni), style = MaterialTheme.typography.displaySmall)

        LazyColumn {
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
                        checked = nastaveni.darkMode,
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
            if (skupiny != null) items(skupiny.toList()) { skupina ->
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
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    navigateBack()
                }
            ) {
                Text(text = "Zrušit")
            }

            TextButton(
                onClick = {
                    save {
                        navigateBack()
                    }
                }
            ) {
                Text(text = "Uložit")
            }
        }
    }
}