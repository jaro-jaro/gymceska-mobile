package cz.jaro.rozvrh.rozvrh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.Offline
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.destinations.RozvrhScreenDestination
import cz.jaro.rozvrh.nastaveni.nula
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class RozvrhViewModel(
    @InjectedParam private val params: Parameters,
    private val repo: Repository,
) : ViewModel() {
    data class Parameters(
        val vjec: Vjec?,
        val stalost: Stalost?,
        val navigovat: (Direction) -> Unit,
    )

    val tridy = repo.tridy
    val mistnosti = repo.mistnosti
    val vyucujici = repo.vyucujici

    val vjec = repo.nastaveni.map {
        params.vjec ?: it.mojeTrida
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), params.vjec)

    val stalost = params.stalost ?: Stalost.TentoTyden

    fun vybratRozvrh(vjec: Vjec) {
        params.navigovat(RozvrhScreenDestination(vjec, stalost))
    }

    fun zmenitStalost(stalost: Stalost) {
        params.navigovat(RozvrhScreenDestination(vjec.value, stalost))
    }

    private val _mujRozvrh = MutableStateFlow(false)
    val mujRozvrh = _mujRozvrh.asStateFlow()

    fun zmenitMujRozvrh() {
        _mujRozvrh.value = !_mujRozvrh.value
    }

    init {
        viewModelScope.launch {
            repo.nastaveni.collect {
                _mujRozvrh.value = it.defaultMujRozvrh
            }
        }
    }

    val zobrazitMujRozvrh = vjec.combine(repo.nastaveni) { vjec, nastaveni ->
        vjec == nastaveni.mojeTrida
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), true)

    val tabulka = combine(vjec, mujRozvrh, repo.nastaveni, zobrazitMujRozvrh) { vjec, mujRozvrh, nastaveni, zobrazitMujRozvrh ->
        if (vjec == null) null
        else when (vjec) {
            is Vjec.TridaVjec -> withContext(Dispatchers.IO) Nacitani@{
                repo.ziskatDocument(
                    trida = vjec,
                    stalost = stalost
                ).let { result ->
                    if (result !is Uspech) return@Nacitani null
                    TvorbaRozvrhu.vytvoritTabulku(result.document, mujRozvrh && zobrazitMujRozvrh, nastaveni.mojeSkupiny) to result.zdroj.let { zdroj ->
                        if (zdroj is Offline)
                            "Prohlížíte si verzi rozvrhu z ${zdroj.ziskano.dayOfMonth}. ${zdroj.ziskano.monthValue}. ${zdroj.ziskano.hour}:${zdroj.ziskano.minute.nula()}."
                        else null
                    }
                }
            }

            else -> vytvoritRozvrhPodleJinych(
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
            val plneTridy = tridy.value.drop(1).flatMap { trida ->
                progress("Prohledávám třídu\n${trida.zkratka}")
                TvorbaRozvrhu.vytvoritTabulku(repo.ziskatDocument(trida, stalost).let { result ->
                    if (result !is Uspech) {
                        onComplete(null)
                        return@launch
                    }
                    result.document
                }).drop(1)[den].drop(1)[hodina].map { bunka ->
                    bunka.ucebna
                }
            }
            progress("Už to skoro je")

            onComplete(mistnosti.value.drop(1).filter { it.zkratka !in plneTridy })
        }
    }

    suspend fun vytvoritRozvrhPodleJinych(
        vjec: Vjec,
        stalost: Stalost,
        repo: Repository,
    ): Pair<Tyden, String?> = withContext(Dispatchers.IO) {
        if (vjec is Vjec.TridaVjec) return@withContext emptyList<Den>() to ""

        val seznamNazvu = repo.tridy.value.drop(1)

        val novaTabulka = MutableList(6) { MutableList(17) { mutableListOf<Bunka>() } }

        val nejstarsi = seznamNazvu.fold(LocalDateTime.MAX) { zatimNejstarsi, trida ->

            val result = repo.ziskatDocument(trida, stalost)

            if (result !is Uspech) return@withContext emptyList<Den>() to ""

            val rozvrhTridy = TvorbaRozvrhu.vytvoritTabulku(result.document)

            rozvrhTridy.forEachIndexed { i, den ->
                den.forEachIndexed { j, hodina ->
                    hodina.forEach { bunka ->
                        if (bunka.ucitel.isEmpty() || bunka.predmet.isEmpty()) {
                            return@forEach
                        }
                        if (i == 0 || j == 0) {
                            novaTabulka[i][j] += bunka
                            return@forEach
                        }
                        val zajimavaVec = when (vjec) {
                            is Vjec.VyucujiciVjec -> bunka.ucitel.split(",").first()
                            is Vjec.MistnostVjec -> bunka.ucebna
                            else -> throw IllegalArgumentException()
                        }
                        if (zajimavaVec == vjec.zkratka) {
                            novaTabulka[i][j] += bunka.copy(tridaSkupina = "${trida.zkratka} ${bunka.tridaSkupina}".trim())
                            return@forEach
                        }
                    }
                }
            }

            if (result.zdroj !is Offline) zatimNejstarsi
            else if (result.zdroj.ziskano < zatimNejstarsi!!) result.zdroj.ziskano
            else zatimNejstarsi
        }
        novaTabulka.forEachIndexed { i, den ->
            den.forEachIndexed { j, hodina ->
                hodina.ifEmpty {
                    novaTabulka[i][j] += Bunka.prazdna
                }
            }
        }
        if (nejstarsi == LocalDateTime.MAX) novaTabulka to null
        else novaTabulka to "Nejstarší část tohoto rozvrhu pochází z ${nejstarsi.dayOfMonth}. ${nejstarsi.monthValue}. ${nejstarsi.hour}:${nejstarsi.minute.nula()}."
    }
}