package cz.jaro.rozvrh.rozvrh

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jaro.rozvrh.Nastaveni
import cz.jaro.rozvrh.Repository
import cz.jaro.rozvrh.Route
import cz.jaro.rozvrh.Uspech
import cz.jaro.rozvrh.combineStates
import cz.jaro.rozvrh.filterNotNullState
import cz.jaro.rozvrh.mapState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class RozvrhViewModel(
    private val params: Parameters,
    private val repo: Repository,
) : ViewModel() {
    data class Parameters(
        val vjec: Vjec?,
        val stalost: Stalost?,
        val mujRozvrh: Boolean?,
        val horScrollState: ScrollState,
        val verScrollState: ScrollState,
    )

    lateinit var navigovat: (Route) -> Unit

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

    private val _mujRozvrh = repo.nastaveni.mapState(
        viewModelScope, SharingStarted.WhileSubscribed(5.seconds)
    ) { nastaveni ->
        params.mujRozvrh ?: nastaveni.defaultMujRozvrh
    }

    val mujRozvrh = combineStates(
        viewModelScope, SharingStarted.WhileSubscribed(5.seconds),
        _mujRozvrh, repo.nastaveni, vjec
    ) { mujRozvrh, nastaveni, vjec ->
        if (vjec == null) null
        else mujRozvrh && vjec == nastaveni.mojeTrida
    }.filterNotNullState(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), false)

    fun vybratRozvrh(vjec: Vjec) {
        viewModelScope.launch {
            navigovat(
                Route.Rozvrh(
                    vjec = if (vjec.nazev == "HOME") repo.nastaveni.first().mojeTrida else vjec,
                    mujRozvrh = _mujRozvrh.first(),
                    stalost = stalost,
                )
            )
        }
    }

    fun zmenitStalost(stalost: Stalost) {
        viewModelScope.launch {
            navigovat(
                Route.Rozvrh(
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
            navigovat(
                Route.Rozvrh(
                    vjec = vjec.value,
                    mujRozvrh = !_mujRozvrh.first(),
                    stalost = stalost,
                    horScroll = params.horScrollState.value,
                    verScroll = params.verScrollState.value,
                )
            )
        }
    }

    val zobrazitMujRozvrh = combineStates(
        viewModelScope, SharingStarted.WhileSubscribed(5.seconds),
        vjec, repo.nastaveni
    ) { vjec, nastaveni ->
        vjec == nastaveni.mojeTrida
    }

    val zoom = repo.nastaveni.mapState(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), Nastaveni::zoom)

    val currentlyDownloading = repo.currentlyDownloading

    val alwaysTwoRowCells = repo.nastaveni.mapState(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), Nastaveni::alwaysTwoRowCells)

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
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val stahnoutVse: () -> Unit = {
        viewModelScope.launch {
            repo.stahnoutVse()
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