package cz.jaro.rozvrh

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.Keep
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
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
import org.jsoup.Jsoup
import org.koin.core.annotation.Single
import java.io.IOException
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class Repository(
    private val ctx: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    object Keys {
        val NASTAVENI = stringPreferencesKey("nastaveni")
        fun rozvrh(trida: Vjec.TridaVjec, stalost: Stalost) = stringPreferencesKey("rozvrh+_${trida.nazev}_${stalost.nazev}")
        fun rozvrhPosledni(trida: Vjec.TridaVjec, stalost: Stalost) = longPreferencesKey("rozvrh+_${trida.nazev}_${stalost.nazev}_posledni+")
        val SKRTLE_UKOLY = stringSetPreferencesKey("skrtle_ukoly")
        val UKOLY = stringPreferencesKey("ukoly")
        val VERZE = intPreferencesKey("verze")
    }

    private val firebase = Firebase

    private val database = firebase.database("https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/")
    private val remoteConfig = Firebase.remoteConfig

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
                    println(it)
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
                    preferences.edit {
                        it[Keys.UKOLY] = Json.encodeToString(noveUkoly)
                    }

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

    private val preferences = PreferenceDataStoreFactory.create(
        migrations = listOf(
            SharedPreferencesMigration({
                ctx.getSharedPreferences("hm", Context.MODE_PRIVATE)!!
            }),
            FourToFiveMigration(tridy)
        )
    ) {
        ctx.preferencesDataStoreFile("Gymceska_JARO_datastore")
    }.also {
        scope.launch {
            if (it.data.first().contains(booleanPreferencesKey("first")).not()) {
                if (!isOnline()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, "Je potřeba připojení k internetu!", Toast.LENGTH_LONG).show()
                    }
                    exitProcess(-1)
                }
                it.edit {
                    it[booleanPreferencesKey("first")] = false
                }
            }
            it.edit {
                it[Keys.VERZE] = BuildConfig.VERSION_CODE
            }
        }
    }

    private val offlineUkoly = preferences.data.map {
        it[Keys.UKOLY]?.let { it1 -> Json.decodeFromString<List<Ukol>>(it1) }
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

    private val json = Json {
        ignoreUnknownKeys = true
    }

    val nastaveni = preferences.data.combine(tridy) { it, tridy ->
        it[Keys.NASTAVENI]?.let { it1 -> json.decodeFromString<Nastaveni>(it1) } ?: defaultNastaveni(tridy)
    }
        .stateIn(scope, SharingStarted.Eagerly, defaultNastaveni(tridy.value))

    suspend fun zmenitNastaveni(edit: (Nastaveni) -> Nastaveni) {
        preferences.edit {
            it[Keys.NASTAVENI] =
                Json.encodeToString(edit(it[Keys.NASTAVENI]?.let { it1 -> json.decodeFromString<Nastaveni>(it1) } ?: Nastaveni(mojeTrida = tridy.value[1])))
        }
    }

    suspend fun stahnoutVse() {
        if (!isOnline()) return
        withContext(Dispatchers.IO) {
            tridy.value.drop(1).forEach { trida ->
                _currentlyDownloading.value = trida
                Stalost.entries.forEach { stalost ->

                    val doc = Jsoup.connect(trida.odkaz?.replace("###", stalost.odkaz) ?: return@withContext).get()

                    val rozvrh = TvorbaRozvrhu.vytvoritTabulku(
                        vjec = trida,
                        doc = doc,
                    )

                    preferences.edit {
                        it[Keys.rozvrh(trida, stalost)] = Json.encodeToString(rozvrh)
                        it[Keys.rozvrhPosledni(trida, stalost)] = Clock.System.now().epochSeconds / 60L * 60L
                    }
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

    private suspend fun pouzitOfflineRozvrh(trida: Vjec.TridaVjec, stalost: Stalost): Boolean {
        val limit = if (stalost == Stalost.Staly) 14.days else 1.hours
        val posledni = preferences.data.first()[Keys.rozvrhPosledni(trida, stalost)]?.let { Instant.fromEpochSeconds(it) } ?: return false
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
            val doc = Jsoup.connect(trida.odkaz.replace("###", stalost.odkaz)).get()

            val rozvrh = TvorbaRozvrhu.vytvoritTabulku(
                vjec = trida,
                doc = doc,
            )

            preferences.edit {
                it[Keys.rozvrh(trida, stalost)] = Json.encodeToString(rozvrh)
                it[Keys.rozvrhPosledni(trida, stalost)] = Clock.System.now().epochSeconds / 60L * 60L
            }
            _currentlyDownloading.value = null

            return@withContext Uspech(rozvrh, Online)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val kdy = preferences.data.first()[Keys.rozvrhPosledni(trida, stalost)]?.let { Instant.fromEpochSeconds(it) }
            ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                }
                return@withContext ZadnaData
            }

        val rozvrh = preferences.data.first()[Keys.rozvrh(trida, stalost)]?.let { Json.decodeFromString<Tyden>(it) }
            ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, R.string.neni_stazeno, Toast.LENGTH_LONG).show()
                }
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

    private fun isOnline(): Boolean = ctx.isOnline()

    companion object {
        fun Context.isOnline(): Boolean {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false

            return capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            )
        }

        inline fun <reified T> String.fromJson(): T = Json.decodeFromString(this)
    }

    val skrtleUkoly = preferences.data.map {
        it[Keys.SKRTLE_UKOLY]?.map { id -> Uuid.parse(id) }?.toSet() ?: emptySet()
    }

    suspend fun upravitSkrtleUkoly(edit: (Set<Uuid>) -> Set<Uuid>) {
        preferences.edit {
            it[Keys.SKRTLE_UKOLY] = edit(
                it[Keys.SKRTLE_UKOLY]?.map { id -> Uuid.parse(id) }?.toSet() ?: emptySet()
            ).map { id -> id.toString() }.toSet()
        }
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

        val ja = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)

        ja in povolene
    }.stateIn(scope, SharingStarted.Eagerly, false)

    val verzeNaRozbiti = configActive.map {
        remoteConfig["rozbitAplikaci"].asString().toIntOrNull() ?: -1
    }.stateIn(scope, SharingStarted.Eagerly, -1)

    private suspend fun jePotrebaAktualizovatAplikaci(): Boolean {
        val jeDebug = BuildConfig.DEBUG

        if (jeDebug) return false

        val response = try {
            withContext(Dispatchers.IO) {
                Jsoup
                    .connect("https://raw.githubusercontent.com/jaro-jaro/gymceska-mobile/main/app/version.txt")
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .execute()
            }
        } catch (e: IOException) {
            Firebase.crashlytics.recordException(e)
            return false
        }

        if (response.statusCode() != 200) return false

        val mistniVerze = BuildConfig.VERSION_NAME.toVersion(false)
        val nejnovejsiVerze = response.body().trim().toVersion(false)

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
    sharingStarted: SharingStarted,
    defaultInitialValue: T,
    crossinline predicate: (value: T) -> Boolean
): StateFlow<T> = filter(predicate)
        .stateIn(coroutineScope, sharingStarted, if (predicate(value)) value else defaultInitialValue)

fun <T: Any> StateFlow<T?>.filterNotNullState(
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted,
    defaultInitialValue: T,
): StateFlow<T> = filterNotNull()
        .stateIn(coroutineScope, sharingStarted, value ?: defaultInitialValue)

fun <T1, T2, R> StateFlow<T1>.combineStates(
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted,
    flow2: StateFlow<T2>,
    transform: (a: T1, b: T2) -> R
): StateFlow<R> = combineStates(coroutineScope, sharingStarted, this, flow2, transform)

fun <T1, T2, R> combineStates(
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted,
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    transform: (a: T1, b: T2) -> R
): StateFlow<R> = flow.combine(flow2, transform)
        .stateIn(coroutineScope, sharingStarted, transform(flow.value, flow2.value))

fun <T1, T2, T3, R> combineStates(
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted,
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    transform: (T1, T2, T3) -> R
): StateFlow<R> = combine(flow, flow2, flow3, transform)
    .stateIn(coroutineScope, sharingStarted, transform(flow.value, flow2.value, flow3.value))

fun <T1, T2, T3, T4, R> combine(
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted,
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    flow4: StateFlow<T4>,
    transform: (T1, T2, T3, T4) -> R
): StateFlow<R> = combine(flow, flow2, flow3, flow4, transform)
    .stateIn(coroutineScope, sharingStarted, transform(flow.value, flow2.value, flow3.value, flow4.value))