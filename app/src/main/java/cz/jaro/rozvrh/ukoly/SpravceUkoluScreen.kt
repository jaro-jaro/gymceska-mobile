package cz.jaro.rozvrh.ukoly

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.util.UUID

@Destination
@Composable
fun SpravceUkoluScreen(
    navigator: DestinationsNavigator
) {
    val viewModel = koinViewModel<UkolyViewModel>()

    val ukoly by viewModel.rawUkoly.collectAsStateWithLifecycle()

    SpravceUkoluScreen(
        ukoly = ukoly,
        pridatUkol = viewModel::pridatUkol,
        odebratUkol = viewModel::odebratUkol,
        zmenitUkol = viewModel::upravitUkol,
        navigateBack = navigator::navigateUp,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SpravceUkoluScreen(
    ukoly: List<Ukol>?,
    pridatUkol: ((UUID) -> Unit) -> Unit,
    odebratUkol: (UUID) -> Unit,
    zmenitUkol: (Ukol) -> Unit,
    navigateBack: () -> Unit
) = Surface {
    var upravovat by rememberSaveable { mutableStateOf(null as UUID?) }
    var datum by rememberSaveable { mutableStateOf("") }
    var predmet by rememberSaveable { mutableStateOf("") }
    var nazev by rememberSaveable { mutableStateOf("") }
    fun reset() {
        datum = ""
        predmet = ""
        nazev = ""
    }

    fun update() {
        val ukol = ukoly?.find { it.id == upravovat } ?: return
        datum = ukol.datum
        predmet = ukol.predmet
        nazev = ukol.nazev
    }

    var datumDialog by rememberSaveable { mutableStateOf(false) }

    if (upravovat != null) AlertDialog(
        onDismissRequest = {
            upravovat = null
            reset()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    zmenitUkol(Ukol(datum = datum, nazev = nazev, predmet = predmet, id = upravovat!!))
                    upravovat = null
                    reset()
                }
            ) {
                Text("Uložit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    upravovat = null
                    reset()
                }
            ) {
                Text("Zrušit")
            }
        },
        title = {
            Text("Upravit úkol")
        },
        icon = {
            Icon(Icons.Default.Edit, null)
        },
        text = {
            val focusManager = LocalFocusManager.current
            Column {
                if (datumDialog) DatePickerDialog(
                    onDismissRequest = {
                        datumDialog = false
                    },
                    onDateChange = {
                        datum = "${it.dayOfMonth}. ${it.monthValue}."
                        datumDialog = false
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    title = {
                        Text("Vyberte datum")
                    },
                    initialDate = LocalDate.now(),
                )

                TextField(
                    value = datum,
                    onValueChange = {
                        datum = it
                    },
                    Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                datumDialog = true
                            }
                        ) {
                            Icon(Icons.Default.CalendarMonth, "Vybrat datum")
                        }
                    },
                    keyboardActions = KeyboardActions {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    label = {
                        Text("Datum")
                    },
                    singleLine = true,
                )
                TextField(
                    value = predmet,
                    onValueChange = {
                        predmet = it
                    },
                    Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    label = {
                        Text("Předmět")
                    },
                    singleLine = true,
                )
                TextField(
                    value = nazev,
                    onValueChange = {
                        nazev = it
                    },
                    Modifier.fillMaxWidth(),
                    label = {
                        Text("Název")
                    },
                    singleLine = true,
                )
            }
        },
    )

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text("Nový úkol")
                },
                icon = {
                    Icon(Icons.Default.Add, null)
                },
                onClick = {
                    pridatUkol {
                        upravovat = it
                        datumDialog = true
                        update()
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Spravovat úkoly")
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(Icons.Default.ArrowBack, "Zpět")
                    }
                }
            )
        },
    ) { paddingValues ->
        if (ukoly == null) LinearProgressIndicator(Modifier.padding(paddingValues))
        else LazyColumn(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(ukoly, key = { it.id }) { ukol ->
                ListItem(
                    headlineContent = {
                        Text(ukol.asString())
                    },
                    Modifier.animateItemPlacement(),
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = {
                                    odebratUkol(ukol.id)
                                }
                            ) {
                                Icon(Icons.Default.DeleteForever, "Odstranit úkol")
                            }
                            IconButton(
                                onClick = {
                                    upravovat = ukol.id
                                    update()
                                }
                            ) {
                                Icon(Icons.Default.Edit, "Upravit úkol")
                            }
                        }
                    },
                )
            }
        }
    }
}