package dev.akexorcist.flipfoldcounter.ui.main

import app.cash.turbine.test
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import dev.akexorcist.flipfoldcounter.data.FakeAppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class MainViewModelTest : StringSpec({

    "uiState reflects total, today and this-month counts once collected" {
        val dao = FakeCounterDao()
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        dao.seed(
            CounterEntity(dateTime = now, count = 4),
            CounterEntity(dateTime = now.minusMonths(2), count = 10),
        )
        val viewModel = MainViewModel(CounterRepository(dao), FakeAppSettingsRepository())

        viewModel.uiState.test {
            val state = awaitItem()
            state.totalCount shouldBe 14
            state.todayCount shouldBe 4
            state.thisMonthCount shouldBe 4
            cancelAndIgnoreRemainingEvents()
        }
    }

    "showBeforeUsingAgain reflects the settings repository" {
        val viewModel = MainViewModel(
            CounterRepository(FakeCounterDao()),
            FakeAppSettingsRepository(initialShouldShow = false),
        )

        viewModel.uiState.test {
            awaitItem().showBeforeUsingAgain shouldBe false
            cancelAndIgnoreRemainingEvents()
        }
    }

    "markAsDoNotShowBeforeUsingAgain delegates to the settings repository" {
        val settingsRepository = FakeAppSettingsRepository(initialShouldShow = true)
        val viewModel = MainViewModel(CounterRepository(FakeCounterDao()), settingsRepository)

        viewModel.markAsDoNotShowBeforeUsingAgain()

        settingsRepository.shouldShowBeforeUsing() shouldBe false
    }
})
