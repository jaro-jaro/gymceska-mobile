package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.spec.Direction
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.destinations.RozvrhDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class RozvrhViewModel(
    @InjectedParam private val params: Parameters,
    private val repo: Repository,
) : ViewModel() {
    data class Parameters(
        val vjec: Vjec?,
        val stalost: Stalost?,
        val mujRozvrh: Boolean?,
        val navigovat: (Direction) -> Unit,
        val horScrollState: ScrollState,
        val verScrollState: ScrollState,
    )

    val tridy = repo.tridy
    val mistnosti = repo.mistnosti
    val vyucujici = repo.vyucujici
    private val vyucujici2 = repo.vyucujici2
    private val odemkleMistnosti = repo.odemkleMistnosti
    private val velkeMistnosti = repo.velkeMistnosti

    val vjec = repo.nastaveni.map {
        params.vjec ?: it.mojeTrida
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), params.vjec)

    val stalost = params.stalost ?: Stalost.dnesniEntries().first()

    private val _mujRozvrh = repo.nastaveni.map { nastaveni ->
        params.mujRozvrh ?: nastaveni.defaultMujRozvrh
    }

    val mujRozvrh = combine(_mujRozvrh, repo.nastaveni, vjec) { mujRozvrh, nastaveni, vjec ->
        if (vjec == null) null
        else mujRozvrh && vjec == nastaveni.mojeTrida
    }
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), false)

    fun vybratRozvrh(vjec: Vjec) {
        viewModelScope.launch {
            params.navigovat(
                RozvrhDestination(
                    vjec = if (vjec.nazev == "HOME") repo.nastaveni.first().mojeTrida else vjec,
                    mujRozvrh = _mujRozvrh.first(),
                    stalost = stalost,
                )
            )
        }
    }

    fun zmenitStalost(stalost: Stalost) {
        viewModelScope.launch {
            params.navigovat(
                RozvrhDestination(
                    vjec = vjec.value,
                    mujRozvrh = _mujRozvrh.first(),
                    stalost = stalost,
                    horScroll = params.horScrollState.value,
                    verScroll = params.verScrollState.value,
                )
            )
        }
    }

    fun zmenitMujRozvrh() {
        viewModelScope.launch {
            params.navigovat(
                RozvrhDestination(
                    vjec = vjec.value,
                    mujRozvrh = !_mujRozvrh.first(),
                    stalost = stalost,
                    horScroll = params.horScrollState.value,
                    verScroll = params.verScrollState.value,
                )
            )
        }
    }

    val zobrazitMujRozvrh = vjec.combine(repo.nastaveni) { vjec, nastaveni ->
        vjec == nastaveni.mojeTrida
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), true)

    val zoom = repo.nastaveni.map { nastaveni ->
        nastaveni.zoom
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), 1F)

    val alwaysTwoRowCells = repo.nastaveni.map { nastaveni ->
        nastaveni.alwaysTwoRowCells
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), false)

    val tabulka: StateFlow<Uspech?> = combine(vjec, mujRozvrh, repo.nastaveni, zobrazitMujRozvrh) { vjec, mujRozvrh, nastaveni, zobrazitMujRozvrh ->
        if (vjec == null) null
        else when (vjec) {
            is Vjec.TridaVjec -> repo.ziskatRozvrh(
                trida = vjec,
                stalost = stalost,
            ).upravitTabulku {
                it.filtrovatTabulku(
                    mujRozvrh = mujRozvrh && zobrazitMujRozvrh,
                    mojeSkupiny = nastaveni.mojeSkupiny,
                )
            }

            is Vjec.VyucujiciVjec,
            is Vjec.MistnostVjec -> TvorbaRozvrhu.vytvoritRozvrhPodleJinych(
                vjec = vjec,
                stalost = stalost,
                repo = repo
            )

            is Vjec.DenVjec,
            is Vjec.HodinaVjec -> TvorbaRozvrhu.vytvoritSpecialniRozvrh(
                vjec = vjec,
                stalost = stalost,
                repo = repo
            )
        }.takeIf { it is Uspech } as Uspech?
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val stahnoutVse: ((String) -> Unit, () -> Unit) -> Unit = { a, b ->
        viewModelScope.launch {
            repo.stahnoutVse(a, b)
        }
    }

    fun najdiMivolnouTridu(
        stalost: Stalost,
        den: Int,
        hodiny: List<Int>,
        filtry: List<FiltrNajdiMi>,
        progress: (String) -> Unit,
        onComplete: (List<Vjec.MistnostVjec>?) -> Unit
    ) {
        viewModelScope.launch {
            val plneTridy = tridy.value.drop(1).flatMap { trida ->
                progress("Prohledávám třídu\n${trida.zkratka}")
                repo.ziskatRozvrh(trida, stalost).let { result ->
                    if (result !is Uspech) {
                        onComplete(null)
                        return@launch
                    }
                    result.rozvrh
                }.drop(1)[den].drop(1).slice(hodiny).flatMap { hodina ->
                    hodina.map { bunka ->
                        bunka.ucebna
                    }
                }
            }
            progress("Už to skoro je")

            val vysledek = mistnosti.value.drop(1).filter { it.zkratka !in plneTridy }.toMutableList()

            if (FiltrNajdiMi.JenOdemcene in filtry) vysledek.retainAll {
                it.zkratka in odemkleMistnosti.value
            }
            if (FiltrNajdiMi.JenCele in filtry) vysledek.retainAll {
                it.zkratka in velkeMistnosti.value
            }

            onComplete(vysledek)
        }
    }

    fun najdiMiVolnehoUcitele(
        stalost: Stalost,
        den: Int,
        hodiny: List<Int>,
        filtry: List<FiltrNajdiMi>,
        progress: (String) -> Unit,
        onComplete: (List<Vjec.VyucujiciVjec>?) -> Unit
    ) {
        viewModelScope.launch {
            val zaneprazdneniUcitele = tridy.value.drop(1).flatMap { trida ->
                progress("Prohledávám třídu\n${trida.zkratka}")
                repo.ziskatRozvrh(trida, stalost).let { result ->
                    if (result !is Uspech) {
                        onComplete(null)
                        return@launch
                    }
                    result.rozvrh
                }.drop(1)[den].drop(1).slice(hodiny).flatMap { hodina ->
                    hodina.map { bunka ->
                        bunka.ucitel
                    }
                }
            }
            progress("Už to skoro je")

            val vysledek = vyucujici.value.drop(1).filter { it.zkratka !in zaneprazdneniUcitele && it.zkratka in vyucujici2.value }.toMutableList()

            val ucitele = repo.ziskaUcitele(repo.nastaveni.first().mojeTrida)
            if (FiltrNajdiMi.JenSvi in filtry) vysledek.retainAll {
                it.zkratka in ucitele
            }

            onComplete(vysledek)
        }
    }

}