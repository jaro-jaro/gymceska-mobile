package cz.jaro.rozvrh.ukoly

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.Navigation
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.destinations.NastaveniDestination
import cz.jaro.rozvrh.destinations.SpravceUkoluDestination
import cz.jaro.rozvrh.destinations.UkolyDestination

@Composable
fun UkolyNavigation(
    navigate: (Direction) -> Unit,
    smiSpravovat: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) = Navigation(
    title = stringResource(R.string.domaci_ukoly),
    currentDestination = UkolyDestination,
    navigateToDestination = navigate,
    content = content,
    minorNavigationItems = {
        if (smiSpravovat) MinorNavigationItem(
            destination = SpravceUkoluDestination,
            title = "Spravovat úkoly",
            icon = Icons.Default.Edit,
        )
        MinorNavigationItem(
            destination = NastaveniDestination,
            title = stringResource(R.string.nastaveni),
            icon = Icons.Default.Settings,
        )
    },
)

@Composable
fun SpravceUkoluNavigation(
    navigate: (Direction) -> Unit,
    navigateBack: () -> Unit,
    pridatUkol: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) = Navigation(
    title = "Spravovat úkoly",
    currentDestination = SpravceUkoluDestination,
    navigateToDestination = navigate,
    content = content,
    navigationIcon = {
        IconButton(
            onClick = navigateBack
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.zpet))
        }
    },
    floatingActionButton = {
        FloatingActionButton(
            onClick = {
                pridatUkol()
            },
        ) {
            Icon(Icons.Default.Add, null)
        }
    },
    minorNavigationItems = {
        MinorNavigationItem(
            destination = SpravceUkoluDestination,
            title = "Spravovat úkoly",
            icon = Icons.Default.Edit,
        )
        MinorNavigationItem(
            destination = NastaveniDestination,
            title = stringResource(R.string.nastaveni),
            icon = Icons.Default.Settings,
        )
    },
)