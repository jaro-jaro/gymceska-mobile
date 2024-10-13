package cz.jaro.rozvrh.nastaveni

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.Navigation
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.destinations.NastaveniDestination

@Composable
fun NastaveniNavigation(
    navigate: (Direction) -> Unit,
    navigateBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) = Navigation(
    title = stringResource(R.string.nastaveni),
    navigationIcon = {
        IconButton(
            onClick = navigateBack
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.zpet))
        }
    },
    currentDestination = NastaveniDestination,
    navigateToDestination = navigate,
    content = content,
    minorNavigationItems = {
        MinorNavigationItem(
            destination = NastaveniDestination,
            title = stringResource(R.string.nastaveni),
            icon = Icons.Default.Settings,
        )
    },
)