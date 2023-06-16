package cz.jaro.rozvrh

import androidx.datastore.preferences.core.preferencesOf
import cz.jaro.rozvrh.Repository.Companion.fromJson
import cz.jaro.rozvrh.rozvrh.Vjec
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class FourToFiveMigrationTest {
    private val tridy = listOf(
        Vjec.TridaVjec("Třídy"),
        Vjec.TridaVjec("1.E", "placeholder"),
        Vjec.TridaVjec("2.E", "placeholder"),
        Vjec.TridaVjec("3.E", "placeholder"),
        Vjec.TridaVjec("4.E", "placeholder"),
        Vjec.TridaVjec("5.E", "placeholder"),
    )

    private val fourToFiveMigration = FourToFiveMigration(flow { emit(emptyList()); emit(tridy) })

    @Test
    fun migrate1() {
        runBlocking {
            val preferences = preferencesOf(
                Repository.Keys.NASTAVENI to "{\"darkMode\":false,\"mojeTrida\":{\"type\":\"cz.jaro.rozvrh.rozvrh.Vjec.TridaVjec.E5\"},\"mojeSkupiny\":[]}"
            )

            val newData = fourToFiveMigration.migrate(preferences)
            assert(newData[Repository.Keys.NASTAVENI]?.fromJson<Nastaveni>()?.mojeTrida == tridy[5])
        }
    }

    @Test
    fun migrate2() {
        runBlocking {
            val preferences = preferencesOf(
                Repository.Keys.NASTAVENI to "{\"darkMode\":false,\"mojeTrida\":{\"type\":\"cz.jaro.rozvrh.rozvrh.Vjec.TridaVjec.A1\"},\"mojeSkupiny\":[]}"
            )

            val newData = fourToFiveMigration.migrate(preferences)
            assert(newData[Repository.Keys.NASTAVENI]?.fromJson<Nastaveni>()?.mojeTrida == tridy[1])
        }
    }

    @Test
    fun migrate3() {
        runBlocking {
            val preferences = preferencesOf(
                Repository.Keys.NASTAVENI to "{\"darkMode\":false,\"mojeTrida\":{\"type\":\"cz.jaro.rozvrh.rozvrh.Vjec.TridaVjec.S1\"},\"mojeSkupiny\":[]}"
            )

            val newData = fourToFiveMigration.migrate(preferences)
            assert(newData[Repository.Keys.NASTAVENI]?.fromJson<Nastaveni>()?.mojeTrida == tridy[1])
        }
    }
}