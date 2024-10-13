package cz.jaro.rozvrh.nastaveni

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.ukoly.today
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NastaveniViewModel(
    private val repo: Repository,
    private val getStartActivityForResult: (callback: (Uri) -> Unit) -> ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val contentResolver: ContentResolver,
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

            val launcher = getStartActivityForResult { uri ->
                contentResolver.openOutputStream(uri)!!.use {
                    it.write(data.encodeToByteArray())
                    it.flush()
                }
                finish(true)
            }

            val dnes = today()

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = """application/json"""
                putExtra(Intent.EXTRA_TITLE, "ROZVRH-${dnes.year}-${dnes.monthNumber}-${dnes.dayOfMonth}-$stalost")
            }

            launcher.launch(intent)
        }
    }
}
