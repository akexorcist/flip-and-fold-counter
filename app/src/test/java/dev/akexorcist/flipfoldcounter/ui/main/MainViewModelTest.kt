@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package dev.akexorcist.flipfoldcounter.ui.main

import dev.akexorcist.flipfoldcounter.MainDispatcherRule
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import dev.akexorcist.flipfoldcounter.data.FakeAppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState reflects total, today and this-month counts once collected`() = runTest {
        val dao = FakeCounterDao()
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        dao.seed(
            CounterEntity(dateTime = now, count = 4),
            CounterEntity(dateTime = now.minusMonths(2), count = 10),
        )
        val viewModel = MainViewModel(CounterRepository(dao), FakeAppSettingsRepository())

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(14, state.totalCount)
        assertEquals(4, state.todayCount)
        assertEquals(4, state.thisMonthCount)
        job.cancel()
    }

    @Test
    fun `showBeforeUsingAgain reflects the settings repository`() = runTest {
        val viewModel = MainViewModel(
            CounterRepository(FakeCounterDao()),
            FakeAppSettingsRepository(initialShouldShow = false),
        )

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showBeforeUsingAgain)
        job.cancel()
    }

    @Test
    fun `markAsDoNotShowBeforeUsingAgain delegates to the settings repository`() {
        val settingsRepository = FakeAppSettingsRepository(initialShouldShow = true)
        val viewModel = MainViewModel(CounterRepository(FakeCounterDao()), settingsRepository)

        viewModel.markAsDoNotShowBeforeUsingAgain()

        assertFalse(settingsRepository.shouldShowBeforeUsing())
    }
}
