package cz.jaro.rozvrh.ukoly

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import org.koin.androidx.compose.koinViewModel

@Destination
@Composable
fun UkolyScreen() {

    val viewModel = koinViewModel<UkolyViewModel>()

    val ukoly by viewModel.ukoly.collectAsStateWithLifecycle()

    UkolyScreen(
        ukoly = ukoly
    )
}

@Composable
fun UkolyScreen(
    ukoly: List<String>?
) {
    LazyColumn {
        if (ukoly == null) item { LinearProgressIndicator() }
        else items(ukoly) {
            Text(it)
        }
    }
}