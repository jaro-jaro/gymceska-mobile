package cz.jaro.rozvrh.rozvrh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.destinations.RozvrhScreenDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class RozvrhViewModel(
    @InjectedParam vjec1: Vjec?,
    @InjectedParam stalost1: Stalost?,
    @InjectedParam private val navigovat: (Direction) -> Unit,
    private val repo: Repository,
) : ViewModel() {
    val vjec = flow {
        emit(vjec1 ?: repo.nastaveni.first().mojeTrida)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), vjec1)

    val stalost = stalost1 ?: Stalost.TentoTyden

    fun vybratRozvrh(vjec: Vjec) {
        navigovat(RozvrhScreenDestination(vjec, stalost))
    }

    fun zmenitStalost(stalost: Stalost) {
        navigovat(RozvrhScreenDestination(vjec.value, stalost))
    }

    val tabulka = vjec.map { vjec ->
        if (vjec == null) null
        else when (vjec) {
            is Vjec.TridaVjec -> withContext(Dispatchers.IO) Nacitani@{
                repo.ziskatDocument(
                    trida = vjec,
                    stalost = stalost
                )?.let { doc ->
                    TvorbaRozvrhu.vytvoritTabulku(doc)
                } ?: return@Nacitani null
            }

            else -> TvorbaRozvrhu.vytvoritRozvrhPodleJinych(
                vjec = vjec,
                stalost = stalost,
                repo = repo
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val stahnoutVse: ((String) -> Unit, () -> Unit) -> Unit = { a, b ->
        viewModelScope.launch {
            repo.stahnoutVse(a, b)
        }
    }

    fun najdiMivolnouTridu(stalost: Stalost, den: Int, hodina: Int, progress: (String) -> Unit, onComplete: (List<Vjec.MistnostVjec>?) -> Unit) {
        viewModelScope.launch {
            val plneTridy = Vjec.tridy.drop(1).flatMap { trida ->
                progress("Prohledávám třídu\n${trida.zkratka}")
                TvorbaRozvrhu.vytvoritTabulku(repo.ziskatDocument(trida, stalost) ?: run {
                    onComplete(null)
                    return@launch
                }).drop(1)[den].drop(1)[hodina].map { bunka ->
                    bunka.ucebna
                }
            }
            progress("Už to skoro je")

            onComplete(Vjec.mistnosti.drop(1).filter { it.zkratka !in plneTridy })
        }
    }
}