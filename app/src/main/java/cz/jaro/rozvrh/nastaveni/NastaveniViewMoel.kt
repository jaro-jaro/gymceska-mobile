package cz.jaro.rozvrh.nastaveni

import androidx.lifecycle.ViewModel
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class NastaveniViewMoel(
    private val repo: Repository,
) : ViewModel() {

    private val _nastaveni = MutableStateFlow(repo.nastaveni)
    val nastaveni = _nastaveni.asStateFlow()

    fun save(onFinish: () -> Unit) {
        repo.nastaveni = nastaveni.value

        onFinish()
    }

    val skupiny = nastaveni.map {
        repo.ziskatSkupiny(it.mojeTrida)
    }

    fun upravitNastaveni(edit: (Nastaveni) -> Nastaveni) {
        _nastaveni.update(edit)
    }
}
