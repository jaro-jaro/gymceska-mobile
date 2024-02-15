package cz.jaro.rozvrh.nastaveni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class NastaveniViewModel(
    private val repo: Repository,
) : ViewModel() {

    val tridyFlow = repo.tridy

    val nastaveni = repo.nastaveni

    val skupiny = nastaveni.map {
        repo.ziskatSkupiny(it.mojeTrida)
    }

    fun upravitNastaveni(edit: (Nastaveni) -> Nastaveni) {
        viewModelScope.launch {
            repo.zmenitNastaveni(edit)
        }
    }

    fun kopirovatVse(stalost: Stalost, update: (String) -> Unit, finish: (String?) -> Unit) {
        viewModelScope.launch {
            val tridy = repo.tridy.value
            val vse = tridy.mapNotNull {
                update(it.jmeno)
                val res = repo.ziskatDocument(it, stalost)
                if (res !is Uspech) {
                    finish(null)
                    return@mapNotNull null
                }
                it.jmeno to TvorbaRozvrhu.vytvoritTabulku(it, res.document)
            }.toMap()
            update("Už to skoro je!")
            finish(Json.encodeToString(vse))
        }
    }
}
