package cz.jaro.rozvrh.ukoly

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Destination
@Composable
fun SpravceUkolu(
    navigator: DestinationsNavigator
) {
    val viewModel = koinViewModel<UkolyViewModel>()

    val ukoly by viewModel.ukoly.collectAsStateWithLifecycle()

    SpravceUkoluContent(
        ukoly = ukoly,
        pridatUkol = viewModel::pridatUkol,
        odebratUkol = viewModel::odebratUkol,
        zmenitUkol = viewModel::upravitUkol,
        navigateBack = navigator::navigateUp,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun SpravceUkoluContent(
    ukoly: List<Ukol>?,
    pridatUkol: ((Uuid) -> Unit) -> Unit,
    odebratUkol: (Uuid) -> Unit,
    zmenitUkol: (Ukol) -> Unit,
    navigateBack: () -> Unit
) = Surface {
    var upravovat by rememberSaveable { mutableStateOf(null as Uuid?) }
    var datum by rememberSaveable { mutableStateOf("") }
    var predmet by rememberSaveable { mutableStateOf("") }
    var skupina by rememberSaveable { mutableStateOf("") }
    var nazev by rememberSaveable { mutableStateOf("") }
    fun reset() {
        datum = ""
        predmet = ""
        skupina = ""
        nazev = ""
    }

    fun update() {
        val ukol = ukoly?.find { it.id == upravovat } ?: return
        datum = ukol.datum
        predmet = ukol.predmet
        skupina = ukol.skupina
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
                    zmenitUkol(Ukol(datum = datum, nazev = nazev, skupina = skupina, predmet = predmet, id = upravovat!!))
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
                val predvybranyDatum by remember(datum) {
                    derivedStateOf {
                        dateFromString(datum) ?: today()
                    }
                }

                val state = rememberDatePickerState(
                    initialSelectedDateMillis = predvybranyDatum.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
                    initialDisplayMode = DisplayMode.Picker,
                )
                if (datumDialog) DatePickerDialog(
                    onDismissRequest = {
                        datumDialog = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datumDialog = false
                                val chosenDate = Instant.fromEpochMilliseconds(state.selectedDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                                datum = "${chosenDate.dayOfMonth}. ${chosenDate.monthNumber}."
                                datumDialog = false
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        ) {
                            Text("OK")
                        }
                    },
                ) {
                    DatePicker(
                        state = state,
                        title = {
                            DatePickerDefaults.DatePickerTitle(
                                displayMode = state.displayMode,
                                Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp))
                            )
                        },
                    )
                }

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
                    value = skupina,
                    onValueChange = {
                        skupina = it
                    },
                    Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    label = {
                        Text("Skupina")
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
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