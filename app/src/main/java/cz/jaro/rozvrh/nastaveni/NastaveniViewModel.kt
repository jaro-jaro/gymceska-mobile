package cz.jaro.rozvrh.nastaveni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.Repository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class NastaveniViewModel(
    private val repo: Repository,
) : ViewModel() {

    val nastaveni = repo.nastaveni

    val skupiny = nastaveni.map {
        repo.ziskatSkupiny(it.mojeTrida)
    }

    fun upravitNastaveni(edit: (Nastaveni) -> Nastaveni) {
        viewModelScope.launch {
            repo.zmenitNastaveni(edit)
        }
    }
}
