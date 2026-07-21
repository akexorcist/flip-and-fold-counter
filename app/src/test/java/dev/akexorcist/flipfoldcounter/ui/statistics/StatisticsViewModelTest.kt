@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package dev.akexorcist.flipfoldcounter.ui.statistics

import dev.akexorcist.flipfoldcounter.MainDispatcherRule
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import dev.akexorcist.flipfoldcounter.ui.navigation.StatisticsTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

private class ThrowingCounterDao(delegate: CounterDao) : CounterDao by delegate {
    override fun getCountsForDateRange(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<CounterEntity>> =
        flow { throw IllegalStateException("boom") }
}

class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial with Day tab loads hourly stats for today`() = runTest {
        val dao = FakeCounterDao()
        val today = LocalDate.now()
        dao.seed(
            CounterEntity(today.atTime(9, 0), count = 3),
            CounterEntity(today.atTime(14, 0), count = 7),
        )
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))

        viewModel.initial(StatisticsTab.DAY)
        advanceUntilIdle()

        assertEquals(0, viewModel.selectedTabIndex.value)
        val state = viewModel.uiState.value as StatisticsUiState.Success
        val hourly = state.graphType as GraphType.Hourly
        assertEquals(today, hourly.date)
        assertEquals(3, hourly.data[LocalTime.of(9, 0)])
        assertEquals(7, hourly.data[LocalTime.of(14, 0)])
        assertEquals(0, hourly.data[LocalTime.of(0, 0)])
    }

    @Test
    fun `day navigation enable flags respect first and last entry boundaries`() = runTest {
        val dao = FakeCounterDao()
        val today = LocalDate.now()
        dao.seed(
            CounterEntity(today.minusDays(2).atTime(9, 0), count = 1),
            CounterEntity(today.atTime(9, 0), count = 1),
        )
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))

        viewModel.initial(StatisticsTab.DAY)
        advanceUntilIdle()
        var state = viewModel.uiState.value as StatisticsUiState.Success
        assertEquals(today, state.selectedDate)
        assertFalse("today is the last entry date, so next should be disabled", state.isNextDayEnabled)
        assertTrue(state.isPreviousDayEnabled)

        viewModel.onPreviousDay()
        advanceUntilIdle()
        state = viewModel.uiState.value as StatisticsUiState.Success
        assertEquals(today.minusDays(1), state.selectedDate)
        assertTrue(state.isNextDayEnabled)
        assertTrue(state.isPreviousDayEnabled)

        viewModel.onPreviousDay()
        advanceUntilIdle()
        state = viewModel.uiState.value as StatisticsUiState.Success
        assertEquals(today.minusDays(2), state.selectedDate)
        assertTrue(state.isNextDayEnabled)
        assertFalse("selected date is the first entry date, so previous should be disabled", state.isPreviousDayEnabled)
    }

    @Test
    fun `month navigation enable flags respect first and last entry boundaries`() = runTest {
        val dao = FakeCounterDao()
        val thisMonth = YearMonth.now()
        dao.seed(
            CounterEntity(thisMonth.minusMonths(2).atDay(1).atTime(9, 0), count = 1),
            CounterEntity(thisMonth.atDay(1).atTime(9, 0), count = 1),
        )
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))

        viewModel.initial(StatisticsTab.MONTH)
        advanceUntilIdle()
        var state = viewModel.uiState.value as StatisticsUiState.Success
        assertEquals(thisMonth, state.selectedMonth)
        assertFalse(state.isNextMonthEnabled)
        assertTrue(state.isPreviousMonthEnabled)

        viewModel.onPreviousMonth()
        advanceUntilIdle()
        state = viewModel.uiState.value as StatisticsUiState.Success
        assertEquals(thisMonth.minusMonths(1), state.selectedMonth)
        assertTrue(state.isNextMonthEnabled)
        assertTrue(state.isPreviousMonthEnabled)

        viewModel.onPreviousMonth()
        advanceUntilIdle()
        state = viewModel.uiState.value as StatisticsUiState.Success
        assertEquals(thisMonth.minusMonths(2), state.selectedMonth)
        assertTrue(state.isNextMonthEnabled)
        assertFalse(state.isPreviousMonthEnabled)
    }

    @Test
    fun `onTabSelected switches between hourly, daily and monthly graph types`() = runTest {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(LocalDate.now().atTime(9, 0), count = 1))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))
        viewModel.initial(StatisticsTab.DAY)
        advanceUntilIdle()

        viewModel.onTabSelected(1)
        advanceUntilIdle()
        assertEquals(1, viewModel.selectedTabIndex.value)
        assertTrue((viewModel.uiState.value as StatisticsUiState.Success).graphType is GraphType.Daily)

        viewModel.onTabSelected(2)
        advanceUntilIdle()
        assertEquals(2, viewModel.selectedTabIndex.value)
        assertTrue((viewModel.uiState.value as StatisticsUiState.Success).graphType is GraphType.Monthly)

        viewModel.onTabSelected(0)
        advanceUntilIdle()
        assertEquals(0, viewModel.selectedTabIndex.value)
        assertTrue((viewModel.uiState.value as StatisticsUiState.Success).graphType is GraphType.Hourly)
    }

    @Test
    fun `loadHourlyStats emits Error state when the repository flow throws`() = runTest {
        val dao = ThrowingCounterDao(FakeCounterDao())
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))

        viewModel.initial(StatisticsTab.DAY)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is StatisticsUiState.Error)
    }

    @Test
    fun `initial with an empty database disables every navigation direction`() = runTest {
        val viewModel = StatisticsViewModel(StatisticsRepository(FakeCounterDao(), mainDispatcherRule.testDispatcher))

        viewModel.initial(StatisticsTab.DAY)
        advanceUntilIdle()

        val state = viewModel.uiState.value as StatisticsUiState.Success
        assertFalse(state.isNextDayEnabled)
        assertFalse(state.isPreviousDayEnabled)
        assertFalse(state.isNextMonthEnabled)
        assertFalse(state.isPreviousMonthEnabled)
    }

    @Test
    fun `initial with Overall tab loads monthly stats`() = runTest {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(LocalDate.now().atTime(9, 0), count = 5))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))

        viewModel.initial(StatisticsTab.OVERALL)
        advanceUntilIdle()

        assertEquals(2, viewModel.selectedTabIndex.value)
        assertTrue((viewModel.uiState.value as StatisticsUiState.Success).graphType is GraphType.Monthly)
    }

    @Test
    fun `onNextDay moves the selected date forward and reloads hourly stats`() = runTest {
        val dao = FakeCounterDao()
        val today = LocalDate.now()
        dao.seed(CounterEntity(today.plusDays(1).atTime(9, 0), count = 9))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))
        viewModel.initial(StatisticsTab.DAY)
        advanceUntilIdle()

        viewModel.onNextDay()
        advanceUntilIdle()

        val state = viewModel.uiState.value as StatisticsUiState.Success
        val hourly = state.graphType as GraphType.Hourly
        assertEquals(today.plusDays(1), state.selectedDate)
        assertEquals(9, hourly.data[LocalTime.of(9, 0)])
    }

    @Test
    fun `onNextMonth moves the selected month forward and reloads daily stats`() = runTest {
        val dao = FakeCounterDao()
        val thisMonth = YearMonth.now()
        dao.seed(CounterEntity(thisMonth.plusMonths(1).atDay(10).atTime(9, 0), count = 6))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, mainDispatcherRule.testDispatcher))
        viewModel.initial(StatisticsTab.MONTH)
        advanceUntilIdle()

        viewModel.onNextMonth()
        advanceUntilIdle()

        val state = viewModel.uiState.value as StatisticsUiState.Success
        val daily = state.graphType as GraphType.Daily
        assertEquals(thisMonth.plusMonths(1), state.selectedMonth)
        assertEquals(6, daily.data[thisMonth.plusMonths(1).atDay(10)])
    }
}
