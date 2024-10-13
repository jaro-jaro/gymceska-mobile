package cz.jaro.rozvrh

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.destinations.Destination
import cz.jaro.rozvrh.destinations.RozvrhDestination
import cz.jaro.rozvrh.destinations.UkolyDestination

interface ActionScope {
    @Composable
    fun MinorNavigationItem(
        destination: Direction,
        title: String,
        icon: ImageVector,
    )

    @Composable
    fun Action(
        onClick: () -> Unit,
        title: String,
        icon: ImageVector,
    )
}

private fun ActionScope(
    isInTabletMode: Boolean,
    currentDestination: Destination,
    navigateToDestination: (Direction) -> Unit,
) = object : ActionScope {
    @Composable
    override fun MinorNavigationItem(destination: Direction, title: String, icon: ImageVector) {
        MinorNavigationItem(
            destination = destination,
            selected = destination == currentDestination,
            title = title,
            navigateToDestination = navigateToDestination,
            icon = icon,
            isInTabletMode = isInTabletMode,
        )
    }

    @Composable
    override fun Action(onClick: () -> Unit, title: String, icon: ImageVector) {
        Action(
            onClick = onClick,
            title = title,
            icon = icon,
            isInTabletMode = isInTabletMode,
        )
    }
}

private fun scopeComposable(scope: ActionScope, content: (@Composable ActionScope.() -> Unit)?): (@Composable () -> Unit)? =
    content?.let {
        ({
            content(scope)
        })
    }

@Composable
fun Navigation(
    title: String,
    actions: (@Composable ActionScope.() -> Unit)? = null,
    minorNavigationItems: (@Composable ActionScope.() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    currentDestination: Destination,
    navigateToDestination: (Direction) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    navigationIcon: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isInTabletMode = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
            || windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    val scope = ActionScope(isInTabletMode, currentDestination, navigateToDestination)
    val scopedMinorNavigationItems = scopeComposable(scope, minorNavigationItems)
    val scopedActions = scopeComposable(scope, actions)

    if (isInTabletMode) {
        Row {
            Rail(
                currentDestination = currentDestination,
                navigateToDestination = navigateToDestination,
                actions = scopedActions,
                titleContent = titleContent ?: floatingActionButton,
                minorNavigationItems = scopedMinorNavigationItems,
            )
            content(PaddingValues())
        }
    } else {
        Scaffold(
            bottomBar = {
                BottomBar(
                    currentDestination = currentDestination,
                    navigateToDestination = navigateToDestination,
                )
            },
            topBar = {
                TopBar(
                    title = title,
                    actions = scopedActions,
                    titleContent = titleContent,
                    navigationIcon = navigationIcon,
                    minorNavigationItems = scopedMinorNavigationItems,
                )
            },
            floatingActionButton = floatingActionButton ?: {},
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    actions: (@Composable () -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    minorNavigationItems: (@Composable () -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title)
                titleContent?.invoke()
            }
        },
        actions = { actions?.invoke(); minorNavigationItems?.invoke() },
        navigationIcon = navigationIcon ?: {},
    )
}

@Composable
private fun Action(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector,
    isInTabletMode: Boolean,
) = if (isInTabletMode) {
    NavigationRailItem(
        selected = false,
        onClick = {
            onClick()
        },
        icon = {
            Icon(icon, title)
        },
        label = {
            Text(title, textAlign = TextAlign.Center)
        },
    )
} else {
    IconButton(
        onClick = {
            onClick()
        },
    ) {
        Icon(icon, title)
    }
}

@Composable
private fun MinorNavigationItem(
    destination: Direction,
    selected: Boolean,
    title: String,
    navigateToDestination: (Direction) -> Unit,
    icon: ImageVector,
    isInTabletMode: Boolean,
) = if (isInTabletMode) {
    NavigationRailItem(
        selected = selected,
        onClick = {
            navigateToDestination(destination)
        },
        icon = {
            Icon(icon, title)
        },
        label = {
            Text(title, textAlign = TextAlign.Center)
        },
    )
} else {
    if (!selected) IconButton(
        onClick = {
            navigateToDestination(destination)
        },
    ) {
        Icon(icon, title)
    }
    else Unit
}

@Composable
private fun Rail(
    currentDestination: Destination,
    navigateToDestination: (Direction) -> Unit,
    actions: (@Composable () -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    minorNavigationItems: (@Composable () -> Unit)? = null,
) {
    NavigationRail(
        Modifier
            .fillMaxHeight()
            .widthIn(max = 80.0.dp),
        header = {
            Icon(painterResource(R.mipmap.ic_launcher_foreground), null, Modifier.size(64.dp))
            titleContent?.invoke()
        },
    ) {
        Spacer(Modifier.weight(1F))
        actions?.let {
            it()
            Spacer(Modifier.weight(1F))
        }
        minorNavigationItems?.invoke()
        NavigationRailItem(
            selected = currentDestination is RozvrhDestination,
            onClick = {
                navigateToDestination(RozvrhDestination())
            },
            icon = {
                Icon(Icons.Default.TableChart, null)
            },
            label = {
                Text(stringResource(R.string.rozvrh), textAlign = TextAlign.Center)
            }
        )
        NavigationRailItem(
            selected = currentDestination == UkolyDestination,
            onClick = {
                navigateToDestination(UkolyDestination())
            },
            icon = {
                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null)
            },
            label = {
                Text(stringResource(R.string.domaci_ukoly), textAlign = TextAlign.Center)
            }
        )
    }
}

@Composable
private fun BottomBar(
    currentDestination: Destination,
    navigateToDestination: (Direction) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination is RozvrhDestination,
            onClick = {
                navigateToDestination(RozvrhDestination())
            },
            icon = {
                Icon(Icons.Default.TableChart, null)
            },
            label = {
                Text(stringResource(R.string.rozvrh))
            }
        )
        NavigationBarItem(
            selected = currentDestination == UkolyDestination,
            onClick = {
                navigateToDestination(UkolyDestination())
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