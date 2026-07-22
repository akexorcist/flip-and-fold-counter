package dev.akexorcist.flipfoldcounter

import dev.akexorcist.flipfoldcounter.data.AppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import dev.akexorcist.flipfoldcounter.data.FakeAppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import dev.akexorcist.flipfoldcounter.ui.main.MainViewModel
import dev.akexorcist.flipfoldcounter.ui.statistics.StatisticsViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class AppModuleTest : StringSpec({

    // AppDatabase needs a real Android/Room-backed Context, which isn't available on the JVM
    // unit test classpath, and AppSettingsRepository needs a real SharedPreferences-backed
    // Context. Overriding the CounterDao/AppSettingsRepository bindings directly (rather than
    // AppDatabase itself) means the real single { AppDatabase.getDatabase(get()) } definition
    // is simply never resolved, so Room/SQLite is never touched.
    val testOverrides = module {
        factory<CounterDao> { FakeCounterDao() }
        factory<AppSettingsRepository> { FakeAppSettingsRepository() }
    }

    afterTest {
        stopKoin()
    }

    "appModule resolves every repository and view model" {
        val koin = startKoin { modules(appModule, testOverrides) }.koin

        koin.get<CounterRepository>().shouldNotBeNull()
        koin.get<StatisticsRepository>().shouldNotBeNull()
        koin.get<AppSettingsRepository>().shouldNotBeNull()
        koin.get<MainViewModel>().shouldNotBeNull()
        koin.get<StatisticsViewModel>().shouldNotBeNull()
        koin.get<CounterViewModel>().shouldNotBeNull()
    }
})
