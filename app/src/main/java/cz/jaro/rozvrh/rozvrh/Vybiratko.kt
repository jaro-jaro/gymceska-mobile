package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@ExperimentalMaterial3Api
@Composable
fun Vybiratko(
    value: Stalost,
    seznam: List<Stalost>,
    onClick: (Int, Stalost) -> Unit,
    modifier: Modifier = Modifier,
    zbarvit: Boolean = false,
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
) = Vybiratko(
    value = value.nazev,
    seznam = seznam.map { it.nazev },
    onClick = { i, _ -> onClick(i, seznam[i]) },
    modifier = modifier,
    trailingIcon = trailingIcon,
    zbarvit = zbarvit,
    zaskrtavatko = { false }
)

@ExperimentalMaterial3Api
@Composable
fun Vybiratko(
    value: Vjec?,
    seznam: List<Vjec>,
    onClick: (Int, Vjec) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    zbarvit: Boolean = true,
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
) = Vybiratko(
    value = value?.jmeno ?: "",
    seznam = seznam.map { it.jmeno },
    onClick = { i, _ -> onClick(i, seznam[i]) },
    modifier = modifier,
    label = label,
    trailingIcon = trailingIcon,
    zbarvit = zbarvit,
    zaskrtavatko = { false }
)

@Composable
@ExperimentalMaterial3Api
fun Vybiratko(
    index: Int,
    seznam: List<String>,
    onClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    zaskrtavatko: (String) -> Boolean = { it == seznam[index] },
    zbarvit: Boolean = false,
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
) = Vybiratko(
    value = seznam[index],
    seznam = seznam,
    onClick = onClick,
    modifier = modifier,
    label = label,
    zbarvit = zbarvit,
    trailingIcon = trailingIcon,
    zaskrtavatko = zaskrtavatko,
)

@Composable
@ExperimentalMaterial3Api
fun Vybiratko(
    value: String,
    seznam: List<String>,
    onClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    zbarvit: Boolean = false,
    zaskrtavatko: (String) -> Boolean = { it == value },
    trailingIcon: (@Composable (hide: () -> Unit) -> Unit)? = null,
    zavirat: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = value,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trailingIcon?.invoke {
                        expanded = false
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = if (zbarvit) ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
            ) else ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            val zaskrtavatka = seznam.map(zaskrtavatko)
            seznam.forEachIndexed { i, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onClick(i, option)
                        if (zavirat) expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    leadingIcon = if (zaskrtavatka.any { it }) (@Composable {
                        if (zaskrtavatka[i]) Icon(Icons.Default.Check, null)
                    }) else null
                )
            }
        }
    }
}