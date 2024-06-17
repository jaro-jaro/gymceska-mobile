package cz.jaro.rozvrh.suplovani

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Result2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class SuplovaniViewModel(
    repo: Repository,
    repository: SuplovaniRepository,
) : ViewModel() {

    private val _datum = MutableStateFlow(null as LocalDate?)
    val datum = _datum.asStateFlow()

    private val podporovanaData = (-7L..4L).map {
        LocalDate.now().plusDays(it)
    }

    val data = _datum.map { datum ->
        if (datum == null) null
        else repository.suplovani(datum)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val state = _datum.combine(data) { datum, data ->
        if (datum == null) SuplovaniState.DatumNevybran(
            podporovanaData = podporovanaData,
        )
        else if (data == null || (data is Result2.Uspech && data.datum != datum)) SuplovaniState.NacitaniDat(
            podporovanaData = podporovanaData,
            datum = datum,
        )
        else if (data !is Result2.Uspech) SuplovaniState.Chyba(
            podporovanaData = podporovanaData,
            datum = datum,
            chyba = data,
        )
        else SuplovaniState.Data(
            podporovanaData = podporovanaData,
            datum = datum,
            suplovani = data,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), SuplovaniState.Nacitani)

    fun zmenitDatum(datum: LocalDate) {
        _datum.value = datum
    }

    val tridy = repo.tridy
    val mistnosti = repo.mistnosti
    val vyucujici = repo.vyucujici
}