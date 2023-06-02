package cz.jaro.rozvrh.ukoly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class UkolyViewModel(
    repo: Repository
) : ViewModel() {
    val ukoly = repo.ukoly.map { ukoly ->
        ukoly?.map {
            "${it.datum} - ${it.predmet} - ${it.nazev}"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)
}