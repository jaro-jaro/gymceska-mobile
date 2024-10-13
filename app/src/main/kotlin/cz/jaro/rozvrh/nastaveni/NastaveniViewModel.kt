package cz.jaro.rozvrh.nastaveni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.ukoly.today
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    fun stahnoutVse(stalost: Stalost, update: (String) -> Unit, finish: (Boolean) -> Unit) {
        viewModelScope.launch {
            val tridy = repo.tridy.value
            val vse = tridy.mapNotNull {
                update(it.nazev)
                val res = repo.ziskatRozvrh(it, stalost)
                if (res !is Uspech) {
                    finish(false)
                    return@mapNotNull null
                }
                it.nazev to res.rozvrh
            }.toMap()
            update("Už to skoro je!")
            val data = Json.encodeToString(vse)

            val dnes = today()

            FileKit.saveFile(
                extension = "json",
                baseName = "ROZVRH-${dnes.year}-${dnes.monthNumber}-${dnes.dayOfMonth}-$stalost",
                bytes = data.encodeToByteArray(),
            )
        }
    }
    fun resetRemoteConfig() {
        viewModelScope.launch {
            repo.resetRemoteConfig()
        }
    }
}
