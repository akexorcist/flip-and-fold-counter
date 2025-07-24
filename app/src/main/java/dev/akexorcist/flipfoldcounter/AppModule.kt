package dev.akexorcist.flipfoldcounter

import dev.akexorcist.flipfoldcounter.data.CounterRepository
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
import dev.akexorcist.flipfoldcounter.data.db.AppDatabase
import dev.akexorcist.flipfoldcounter.ui.main.MainViewModel
import dev.akexorcist.flipfoldcounter.ui.statistics.StatisticsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(get()) }
    factory { get<AppDatabase>().counterDao() }
    factoryOf(::CounterRepository)
    factoryOf(::StatisticsRepository)
    viewModelOf(::MainViewModel)
    viewModelOf(::CounterViewModel)
    viewModelOf(::StatisticsViewModel)
}
