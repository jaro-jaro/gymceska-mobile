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
import kotlinx.coroutines.flow.combine as kombajn

@KoinViewModel
class UkolyViewModel(
    private val repo: Repository
) : ViewModel() {
    companion object {
        private const val prvniMesicVSkolnimRoce = 8
    }

    private val idTTBVU = UUID.randomUUID()

    val jeOnline = repo.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), false)

    val inteligentni = repo.jeZarizeniPovoleno()

    val ukoly = repo.ukoly.map { ukoly ->
        ukoly?.sortedBy { ukol ->
            val datum = ukol.datum.replace(" ", "").split(".")
            val den = datum.getOrNull(0)?.toIntOrNull() ?: return@sortedBy 0
            val mesic = datum.getOrNull(1)?.toIntOrNull() ?: return@sortedBy 0

            ((den - 1) + 31 * ((mesic - 1) + (12 - (prvniMesicVSkolnimRoce - 1)))) % (12 * 31)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val state = ukoly.kombajn(repo.skrtleUkoly) { ukoly, skrtle ->
        if (ukoly == null) UkolyState.Nacitani
        else UkolyState.Nacteno(
            ukoly = ukoly.map {
                it.zjednusit(stav = if (it.id in skrtle) StavUkolu.Skrtly else StavUkolu.Neskrtly)
            }
                .run {
                    if (any { it.stav == StavUkolu.Skrtly })
                        plus(JednoduchyUkol(id = idTTBVU, "", stav = StavUkolu.TakovaTaBlboVecUprostred))
                    else this
                }
                .sortedBy {
                    when (it.stav) {
                        StavUkolu.Neskrtly -> -1
                        StavUkolu.TakovaTaBlboVecUprostred -> 0
                        StavUkolu.Skrtly -> 1
                    }
                },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), UkolyState.Nacitani)

    fun skrtnout(id: UUID) {
        viewModelScope.launch {
            repo.upravitSkrtleUkoly {
                it + id
            }
        }
    }

    fun odskrtnout(id: UUID) {
        viewModelScope.launch {
            repo.upravitSkrtleUkoly {
                it - id
            }
        }
    }

    fun pridatUkol(callback: (UUID) -> Unit) {
        viewModelScope.launch {
            val novyUkol = Ukol.new()
            repo.upravitUkoly(ukoly.value!! + novyUkol)
            callback(novyUkol.id)
        }
    }

    fun odebratUkol(id: UUID) {
        viewModelScope.launch {
            repo.upravitUkoly(ukoly.value!!.filter { it.id != id })
        }
    }

    fun upravitUkol(ukol: Ukol) {
        viewModelScope.launch {
            val ukoly = ukoly.value!!.toMutableList()
            val index = ukoly.indexOfFirst { it.id == ukol.id }
            if (index == -1) return@launch
            ukoly[index] = ukol
            repo.upravitUkoly(ukoly)
        }
    }
}