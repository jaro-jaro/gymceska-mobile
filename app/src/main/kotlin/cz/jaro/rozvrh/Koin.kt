package cz.jaro.rozvrh

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(platformSpecificModule: Module) {
    startKoin {
        modules(platformSpecificModule, commonModule)
    }
}

val commonModule = module {
    single { Repository(get(), get(), get(), get()) }
}