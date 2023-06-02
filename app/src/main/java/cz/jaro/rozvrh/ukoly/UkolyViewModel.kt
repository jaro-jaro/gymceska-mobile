package cz.jaro.rozvrh.ukoly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class UkolyViewModel(
    private val repo: Repository
) : ViewModel() {
    val rawUkoly = repo.ukoly.map { ukoly ->
        println(ukoly)
        ukoly?.sortedBy { ukol ->
            val datum = ukol.datum.replace(" ", "").split(".")
            val den = datum.getOrNull(0)?.toIntOrNull() ?: return@sortedBy 0
            val mesic = datum.getOrNull(1)?.toIntOrNull() ?: return@sortedBy 0

            ((den + mesic * 31) + (5 * 31)) % (12 * 31)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val ukoly = rawUkoly.map { ukoly ->
        ukoly?.map {
            it.asString()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    fun pridatUkol(callback: (UUID) -> Unit) {
        viewModelScope.launch {
            val novyUkol = Ukol.new()
            repo.upravitUkoly(rawUkoly.value!! + novyUkol)
            callback(novyUkol.id)
        }
    }

    fun odebratUkol(id: UUID) {
        viewModelScope.launch {
            repo.upravitUkoly(rawUkoly.value!!.filter { it.id != id })
        }
    }

    fun upravitUkol(ukol: Ukol) {
        viewModelScope.launch {
            val ukoly = rawUkoly.value!!.toMutableList()
            val index = ukoly.indexOfFirst { it.id == ukol.id }
            if (index == -1) return@launch
            ukoly[index] = ukol
            repo.upravitUkoly(ukoly)
        }
    }
}