package cz.jaro.rozvrh

import cz.jaro.rozvrh.rozvrh.oznameni.OznameniState
import org.jsoup.nodes.Document

class FakeRepositoryImpl : Repository {
    override var mojeTrida: String
        get() = TODO("Not yet implemented")
        set(value) {}
    override var indexMojiTridy: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var mojeSkupiny: List<String>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var darkMode: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var oznameni: OznameniState
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun ziskatDocument(trida: String, stalost: String): Document? {
        TODO("Not yet implemented")
    }

    override suspend fun ziskatDocument(stalost: String): Document? {
        TODO("Not yet implemented")
    }

    override fun isOnline(): Boolean {
        TODO("Not yet implemented")
    }

    override var poprve: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
}
