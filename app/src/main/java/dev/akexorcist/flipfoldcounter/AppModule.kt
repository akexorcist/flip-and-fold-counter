package dev.akexorcist.flipfoldcounter

import dev.akexorcist.flipfoldcounter.data.AppSettingsDataSource
import dev.akexorcist.flipfoldcounter.data.AppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import dev.akexorcist.flipfoldcounter.data.DefaultAppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
import dev.akexorcist.flipfoldcounter.data.db.AppDatabase
import dev.akexorcist.flipfoldcounter.ui.main.MainViewModel
import dev.akexorcist.flipfoldcounter.ui.statistics.StatisticsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(get()) }
    factory { get<AppDatabase>().counterDao() }
    factory { AppSettingsDataSource(androidContext()) }
    factory<AppSettingsRepository> { DefaultAppSettingsRepository(get()) }
    factoryOf(::CounterRepository)
    factory { StatisticsRepository(get()) }
    viewModelOf(::MainViewModel)
    viewModelOf(::CounterViewModel)
    viewModelOf(::StatisticsViewModel)
}
