package cz.jaro.rozvrh

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import cz.jaro.rozvrh.rozvrh.Vjec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class FourToFiveMigration(
    private val tridy: Flow<List<Vjec.TridaVjec>>
) : DataMigration<Preferences> {

    override suspend fun cleanUp() {}

    override suspend fun migrate(currentData: Preferences): Preferences {
        val tridy = tridy.first { it.size > 1 }

        val data = currentData.toMutablePreferences()

        val nastaveni = data[Repository.Keys.NASTAVENI]?.let { JSONObject(it) }
            ?: return currentData // Nepotřebujeme migrovat, protože když není žádné nastavení, není ani to pokažené

        val mojeTridaObject = nastaveni.optJSONObject("mojeTrida") ?: return currentData
        val mojeTridaType = mojeTridaObject.optString("type").ifBlank { null }
        val mojeTrida = mojeTridaType?.split(".")?.last()?.toList()?.reversed()?.joinToString(".")
        val mojeNovaTrida = tridy.find { it.jmeno == mojeTrida } ?: tridy.getOrNull(1) ?: tridy.first()

        nastaveni.put("mojeTrida", JSONObject(Json.encodeToString(mojeNovaTrida)))

        data[Repository.Keys.NASTAVENI] = nastaveni.toString()

        return data.toPreferences()
    }

    @Suppress("KotlinConstantConditions")
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val puvodniVerze = currentData[Repository.Keys.VERZE] ?: 4 // Nejvyšší verze ve které ještě nebyla implementována tato proměnná
        val aktualniVerze = BuildConfig.VERSION_CODE
        return puvodniVerze <= 4 && aktualniVerze >= 5
    }
}
