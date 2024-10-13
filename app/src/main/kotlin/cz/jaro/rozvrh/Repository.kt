package cz.jaro.rozvrh

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.contains
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.coroutines.getStringOrNullStateFlow
import com.russhwolf.settings.set
import cz.jaro.rozvrh.rozvrh.Stalost
import cz.jaro.rozvrh.rozvrh.TvorbaRozvrhu
import cz.jaro.rozvrh.rozvrh.Tyden
import cz.jaro.rozvrh.rozvrh.Vjec
import cz.jaro.rozvrh.ukoly.Ukol
import io.github.z4kn4fein.semver.toVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalSettingsApi::class)
class Repository(
    private val settings: ObservableSettings,
    private val userOnlineManager: UserOnlineManager,
    private val userIdProvider: UserIdProvider,
    firebase: FirebaseApp,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    object Keys {
        const val NASTAVENI = "nastaveni"
        fun rozvrh(trida: Vjec.TridaVjec, stalost: Stalost) = "rozvrh-_${trida.nazev}_${stalost.nazev}"
        fun rozvrhPosledni(trida: Vjec.TridaVjec, stalost: Stalost) = "rozvrh-_${trida.nazev}_${stalost.nazev}_posledni"
        const val SKRTLE_UKOLY = "skrtle_ukoly"
        const val UKOLY = "ukoly"
        const val VERZE = "verze"
    }

    private val database = Firebase.database(firebase, "https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/")
    private val remoteConfig = Firebase.remoteConfig(firebase)

    suspend fun resetRemoteConfig() {
        remoteConfig.reset().await()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().await()
    }

    private val ukolyRef = database.getReference("ukoly")

    val isOnlineFlow = flow {
        while (currentCoroutineContext().isActive) {
            emit(isOnline())
            delay(5.seconds)
        }
    }

    private val onlineUkoly = MutableStateFlow(null as List<Ukol>?)

    @Keep
    object TI : GenericTypeIndicator<List<Map<String, String>>?>()

    init {
        ukolyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ukoly = snapshot.getValue(TI)
                val noveUkoly = ukoly?.mapNotNull {
                    Ukol(
                        datum = it["datum"] ?: return@mapNotNull null,
                        nazev = it["nazev"] ?: return@mapNotNull null,
                        predmet = it["predmet2"] ?: it["predmet"] ?: return@mapNotNull null,
                        skupina = it["skupina"] ?: "",
                        id = it["id"]?.let { id -> Uuid.parse(id) } ?: Uuid.random(),
                    )
                }
                onlineUkoly.value = noveUkoly

                scope.launch {
                    settings[Keys.UKOLY] = Json.encodeToString(noveUkoly)

                    upravitSkrtleUkoly { skrtle ->
                        skrtle.filter { uuid ->
                            uuid in (noveUkoly?.map { it.id } ?: emptyList())
                        }.toSet()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })
    }

    private val configActive = flow {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        if (!isOnline())
            emit(remoteConfig.activate().await())
        else
            emit(remoteConfig.fetchAndActivate().await())
    }

    val tridy = configActive.map {
        listOf(Vjec.TridaVjec("Třídy")) + remoteConfig["tridy"].asString().fromJson<List<Vjec.TridaVjec>>()
    }.stateIn(scope, SharingStarted.Eagerly, listOf(Vjec.TridaVjec("Třídy")))
    val mistnosti = configActive.map {
        listOf(Vjec.MistnostVjec("Místnosti")) + remoteConfig["mistnosti"].asString().fromJson<List<Vjec.MistnostVjec>>()
    }.stateIn(scope, SharingStarted.Eagerly, listOf(Vjec.MistnostVjec("Místnosti")))
    val vyucujici = configActive.map {
        listOf(Vjec.VyucujiciVjec("Vyučující", "")) + remoteConfig["vyucujici"].asString().fromJson<List<Vjec.VyucujiciVjec>>()
    }.stateIn(scope, SharingStarted.Eagerly, listOf(Vjec.VyucujiciVjec("Vyučující", "")))
    val vyucujici2 = configActive.map {
        remoteConfig["vyucujici2"].asString().fromJson<List<String>>()
    }.stateIn(scope, SharingStarted.Eagerly, listOf())
    val odemkleMistnosti = configActive.map {
        remoteConfig["odemkleMistnosti"].asString().fromJson<List<String>>()
    }.stateIn(scope, SharingStarted.Eagerly, listOf())
    val velkeMistnosti = configActive.map {
        remoteConfig["velkeMistnosti"].asString().fromJson<List<String>>()
    }.stateIn(scope, SharingStarted.Eagerly, listOf())

    init {
        scope.launch {
            if ("first" !in settings) {
                settings["first"] = false
            }
            settings[Keys.VERZE] = BuildConfig.VERSION_CODE
        }
    }

    private val offlineUkoly = settings.getStringOrNullFlow(Keys.UKOLY).map {
        it?.fromJson<List<Ukol>>()
    }

    @OptIn(ExperimentalUuidApi::class)
    private val fakeUkol = Uuid.parse("00000000-0000-0000-0000-000000000000")

    @OptIn(ExperimentalUuidApi::class)
    val ukoly = combine(isOnlineFlow, onlineUkoly, offlineUkoly) { isOnline, onlineUkoly, offlineUkoly ->
        if (isOnline) onlineUkoly else offlineUkoly
    }.map { ukoly ->
        ukoly?.filter {
            it.id != fakeUkol
        }
    }

    private fun defaultNastaveni(tridy: List<Vjec.TridaVjec>) = Nastaveni(mojeTrida = tridy.getOrElse(1) { tridy.first() })

    val nastaveni = settings.getStringOrNullStateFlow(scope, Keys.NASTAVENI).combineStates(scope, tridy) { it, tridy ->
        it?.fromJson<Nastaveni>() ?: defaultNastaveni(tridy)
    }

    fun zmenitNastaveni(edit: (Nastaveni) -> Nastaveni) {
        settings[Keys.NASTAVENI] = Json.encodeToString(edit(nastaveni.value))
    }

    suspend fun stahnoutVse() {
        if (!isOnline()) return
        withContext(Dispatchers.IO) {
            tridy.value.drop(1).forEach { trida ->
                _currentlyDownloading.value = trida
                Stalost.entries.forEach { stalost ->

                    val doc = Ksoup.parseGetRequest(trida.odkaz?.replace("###", stalost.odkaz) ?: return@withContext)

                    val rozvrh = TvorbaRozvrhu.vytvoritTabulku(
                        vjec = trida,
                        doc = doc,
                    )

                    settings[Keys.rozvrh(trida, stalost)] = Json.encodeToString(rozvrh)
                    settings[Keys.rozvrhPosledni(trida, stalost)] = Clock.System.now().epochSeconds / 60L * 60L
                }
            }
            _currentlyDownloading.value = null
        }
    }

    suspend fun ziskatSkupiny(trida: Vjec.TridaVjec): Sequence<String> {
        val result = ziskatRozvrh(trida, Stalost.Staly)

        if (result !is Uspech) return emptySequence()

        return result.rozvrh
            .asSequence()
            .flatten()
            .flatten()
            .map { it.tridaSkupina }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    suspend fun ziskaUcitele(trida: Vjec.TridaVjec): Sequence<String> {
        val result = ziskatRozvrh(trida, Stalost.Staly)

        if (result !is Uspech) return emptySequence()

        return result.rozvrh
            .asSequence()
            .flatten()
            .flatten()
            .map { it.ucitel }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    private fun pouzitOfflineRozvrh(trida: Vjec.TridaVjec, stalost: Stalost): Boolean {
        val limit = if (stalost == Stalost.Staly) 14.days else 1.hours
        val posledni = settings.getLongOrNull(Keys.rozvrhPosledni(trida, stalost))?.let { Instant.fromEpochSeconds(it) } ?: return false
        val starost = Clock.System.now() - posledni
        return starost < limit
    }

    private val _currentlyDownloading = MutableStateFlow<Vjec.TridaVjec?>(null)
    val currentlyDownloading = _currentlyDownloading.asStateFlow()

    suspend fun ziskatRozvrh(
        trida: Vjec.TridaVjec,
        stalost: Stalost,
    ): Result = withContext(Dispatchers.IO) {
        if (trida.odkaz == null) return@withContext TridaNeexistuje

        if (isOnline() && !pouzitOfflineRozvrh(trida, stalost)) try {
            _currentlyDownloading.value = trida
            val doc = Ksoup.parseGetRequest(trida.odkaz.replace("###", stalost.odkaz))

            val rozvrh = TvorbaRozvrhu.vytvoritTabulku(
                vjec = trida,
                doc = doc,
            )

            settings[Keys.rozvrh(trida, stalost)] = Json.encodeToString(rozvrh)
            settings[Keys.rozvrhPosledni(trida, stalost)] = Clock.System.now().epochSeconds / 60L * 60L

            _currentlyDownloading.value = null

            return@withContext Uspech(rozvrh, Online)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val kdy = settings.getLongOrNull(Keys.rozvrhPosledni(trida, stalost))?.let { Instant.fromEpochSeconds(it) }
            ?: run {
                return@withContext ZadnaData
            }

        val rozvrh = settings.getStringOrNull(Keys.rozvrh(trida, stalost))?.fromJson<Tyden>()
            ?: run {
                return@withContext ZadnaData
            }

        try {
            Uspech(rozvrh, Offline(kdy.toLocalDateTime(TimeZone.currentSystemDefault())))
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
            Error
        }
    }

    suspend fun ziskatRozvrh(
        stalost: Stalost,
    ): Result = ziskatRozvrh(nastaveni.first().mojeTrida, stalost)

    private fun isOnline(): Boolean = userOnlineManager.isOnline()

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
        }

        inline fun <reified T> String.fromJson(): T = json.decodeFromString(this)
    }

    val skrtleUkoly = settings.getStringOrNullFlow(Keys.SKRTLE_UKOLY).map {
        it?.fromJson<Set<String>>()?.map { id -> Uuid.parse(id) }?.toSet() ?: emptySet()
    }

    fun upravitSkrtleUkoly(edit: (Set<Uuid>) -> Set<Uuid>) {
        settings[Keys.SKRTLE_UKOLY] = edit(
            settings.getStringOrNull(Keys.SKRTLE_UKOLY)?.fromJson<List<String>>()?.map { id -> Uuid.parse(id) }?.toSet() ?: emptySet()
        ).map { id -> id.toString() }.toSet().let { Json.encodeToString(it) }
    }

    suspend fun upravitUkoly(ukoly: List<Ukol>) {
        ukolyRef.setValue(ukoly.map {
            mapOf(
                "datum" to it.datum,
                "nazev" to it.nazev,
                "skupina" to it.skupina,
                "predmet2" to it.predmet,
                "predmet" to listOf(it.predmet, it.skupina).filter(String::isNotEmpty).joinToString(" "),
                "id" to it.id.toString()
            )
        }).await()
    }

    @SuppressLint("HardwareIds")
    val jeZarizeniPovoleno = configActive.map {
        val povolene = remoteConfig["povolenaZarizeni"].asString().fromJson<List<String>>()

        val ja = userIdProvider.getUserId()

        ja in povolene
    }.stateIn(scope, SharingStarted.Eagerly, false)

    val verzeNaRozbiti = configActive.map {
        remoteConfig["rozbitAplikaci"].asString().toIntOrNull() ?: -1
    }.stateIn(scope, SharingStarted.Eagerly, -1)

    private suspend fun jePotrebaAktualizovatAplikaci(): Boolean {
        val jeDebug = BuildConfig.DEBUG

        if (jeDebug) return false

        val document = try {
            withContext(Dispatchers.IO) {
                Ksoup.parseGetRequest("https://raw.githubusercontent.com/jaro-jaro/gymceska-mobile/main/app/version.txt")
            }
        } catch (e: IOException) {
            Firebase.crashlytics.recordException(e)
            return false
        }

        val mistniVerze = BuildConfig.VERSION_NAME.toVersion(false)
        val nejnovejsiVerze = document.text().trim().toVersion(false)

        return mistniVerze < nejnovejsiVerze
    }

    val jePotrebaAktualizovatAplikaci = flow {
        emit(jePotrebaAktualizovatAplikaci())
    }
}

inline fun <T, R> StateFlow<T>.mapState(
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted,
    crossinline transform: (value: T) -> R
): StateFlow<R> = map(transform)
    .stateIn(coroutineScope, sharingStarted, transform(value))

inline fun <T> StateFlow<T>.filterState(
    coroutineScope: CoroutineScope,
    defaultInitialValue: T,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
    crossinline predicate: (value: T) -> Boolean
): StateFlow<T> = filter(predicate)
    .stateIn(coroutineScope, sharingStarted, if (predicate(value)) value else defaultInitialValue)

fun <T : Any> StateFlow<T?>.filterNotNullState(
    coroutineScope: CoroutineScope,
    defaultInitialValue: T,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
): StateFlow<T> = filterNotNull()
    .stateIn(coroutineScope, sharingStarted, value ?: defaultInitialValue)

fun <T1, T2, R> StateFlow<T1>.combineStates(
    coroutineScope: CoroutineScope,
    flow2: StateFlow<T2>,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
    transform: (a: T1, b: T2) -> R
): StateFlow<R> = combineStates(coroutineScope, this, flow2, sharingStarted, transform)

fun <T1, T2, R> combineStates(
    coroutineScope: CoroutineScope,
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
    transform: (a: T1, b: T2) -> R
): StateFlow<R> = flow.combine(flow2, transform)
    .stateIn(coroutineScope, sharingStarted, transform(flow.value, flow2.value))

fun <T1, T2, T3, R> combineStates(
    coroutineScope: CoroutineScope,
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
    transform: (T1, T2, T3) -> R
): StateFlow<R> = combine(flow, flow2, flow3, transform)
    .stateIn(coroutineScope, sharingStarted, transform(flow.value, flow2.value, flow3.value))

fun <T1, T2, T3, T4, R> combineStates(
    coroutineScope: CoroutineScope,
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    flow4: StateFlow<T4>,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
    transform: (T1, T2, T3, T4) -> R
): StateFlow<R> = combine(flow, flow2, flow3, flow4, transform)
    .stateIn(coroutineScope, sharingStarted, transform(flow.value, flow2.value, flow3.value, flow4.value))

fun interface UserIdProvider {
    fun getUserId(): String
}

fun interface UserOnlineManager {
    fun isOnline(): Boolean
}