package dev.akexorcist.flipfoldcounter.ui.statistics

import app.cash.turbine.test
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import dev.akexorcist.flipfoldcounter.ui.navigation.StatisticsTab
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

private class ThrowingCounterDao(delegate: CounterDao) : CounterDao by delegate {
    override fun getCountsForDateRange(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<CounterEntity>> =
        flow { throw IllegalStateException("boom") }
}

class StatisticsViewModelTest : StringSpec({

    "initial with Day tab loads hourly stats for today" {
        val dao = FakeCounterDao()
        val today = LocalDate.now()
        dao.seed(
            CounterEntity(today.atTime(9, 0), count = 3),
            CounterEntity(today.atTime(14, 0), count = 7),
        )
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))

        viewModel.initial(StatisticsTab.DAY)

        viewModel.selectedTabIndex.value shouldBe 0
        val state = viewModel.uiState.value as StatisticsUiState.Success
        val hourly = state.graphType as GraphType.Hourly
        hourly.date shouldBe today
        hourly.data[LocalTime.of(9, 0)] shouldBe 3
        hourly.data[LocalTime.of(14, 0)] shouldBe 7
        hourly.data[LocalTime.of(0, 0)] shouldBe 0
    }

    "loadHourlyStats transitions from Loading to Success" {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(LocalDate.now().atTime(9, 0), count = 1))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))

        viewModel.uiState.test {
            awaitItem() shouldBe StatisticsUiState.Loading
            viewModel.initial(StatisticsTab.DAY)
            awaitItem().shouldBeInstanceOf<StatisticsUiState.Success>()
            cancelAndIgnoreRemainingEvents()
        }
    }

    "day navigation enable flags respect first and last entry boundaries" {
        val dao = FakeCounterDao()
        val today = LocalDate.now()
        dao.seed(
            CounterEntity(today.minusDays(2).atTime(9, 0), count = 1),
            CounterEntity(today.atTime(9, 0), count = 1),
        )
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))

        viewModel.initial(StatisticsTab.DAY)
        var state = viewModel.uiState.value as StatisticsUiState.Success
        state.selectedDate shouldBe today
        state.isNextDayEnabled shouldBe false // today is the last entry date
        state.isPreviousDayEnabled shouldBe true

        viewModel.onPreviousDay()
        state = viewModel.uiState.value as StatisticsUiState.Success
        state.selectedDate shouldBe today.minusDays(1)
        state.isNextDayEnabled shouldBe true
        state.isPreviousDayEnabled shouldBe true

        viewModel.onPreviousDay()
        state = viewModel.uiState.value as StatisticsUiState.Success
        state.selectedDate shouldBe today.minusDays(2)
        state.isNextDayEnabled shouldBe true
        state.isPreviousDayEnabled shouldBe false // selected date is the first entry date
    }

    "month navigation enable flags respect first and last entry boundaries" {
        val dao = FakeCounterDao()
        val thisMonth = YearMonth.now()
        dao.seed(
            CounterEntity(thisMonth.minusMonths(2).atDay(1).atTime(9, 0), count = 1),
            CounterEntity(thisMonth.atDay(1).atTime(9, 0), count = 1),
        )
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))

        viewModel.initial(StatisticsTab.MONTH)
        var state = viewModel.uiState.value as StatisticsUiState.Success
        state.selectedMonth shouldBe thisMonth
        state.isNextMonthEnabled shouldBe false
        state.isPreviousMonthEnabled shouldBe true

        viewModel.onPreviousMonth()
        state = viewModel.uiState.value as StatisticsUiState.Success
        state.selectedMonth shouldBe thisMonth.minusMonths(1)
        state.isNextMonthEnabled shouldBe true
        state.isPreviousMonthEnabled shouldBe true

        viewModel.onPreviousMonth()
        state = viewModel.uiState.value as StatisticsUiState.Success
        state.selectedMonth shouldBe thisMonth.minusMonths(2)
        state.isNextMonthEnabled shouldBe true
        state.isPreviousMonthEnabled shouldBe false
    }

    "onTabSelected switches between hourly, daily and monthly graph types" {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(LocalDate.now().atTime(9, 0), count = 1))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))
        viewModel.initial(StatisticsTab.DAY)

        viewModel.onTabSelected(1)
        viewModel.selectedTabIndex.value shouldBe 1
        (viewModel.uiState.value as StatisticsUiState.Success).graphType.shouldBeInstanceOf<GraphType.Daily>()

        viewModel.onTabSelected(2)
        viewModel.selectedTabIndex.value shouldBe 2
        (viewModel.uiState.value as StatisticsUiState.Success).graphType.shouldBeInstanceOf<GraphType.Monthly>()

        viewModel.onTabSelected(0)
        viewModel.selectedTabIndex.value shouldBe 0
        (viewModel.uiState.value as StatisticsUiState.Success).graphType.shouldBeInstanceOf<GraphType.Hourly>()
    }

    "loadHourlyStats emits Error state when the repository flow throws" {
        val dao = ThrowingCounterDao(FakeCounterDao())
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))

        viewModel.initial(StatisticsTab.DAY)

        viewModel.uiState.value.shouldBeInstanceOf<StatisticsUiState.Error>()
    }

    "initial with an empty database disables every navigation direction" {
        val viewModel = StatisticsViewModel(StatisticsRepository(FakeCounterDao(), Dispatchers.Unconfined))

        viewModel.initial(StatisticsTab.DAY)

        val state = viewModel.uiState.value as StatisticsUiState.Success
        state.isNextDayEnabled shouldBe false
        state.isPreviousDayEnabled shouldBe false
        state.isNextMonthEnabled shouldBe false
        state.isPreviousMonthEnabled shouldBe false
    }

    "initial with Overall tab loads monthly stats" {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(LocalDate.now().atTime(9, 0), count = 5))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))

        viewModel.initial(StatisticsTab.OVERALL)

        viewModel.selectedTabIndex.value shouldBe 2
        (viewModel.uiState.value as StatisticsUiState.Success).graphType.shouldBeInstanceOf<GraphType.Monthly>()
    }

    "onNextDay moves the selected date forward and reloads hourly stats" {
        val dao = FakeCounterDao()
        val today = LocalDate.now()
        dao.seed(CounterEntity(today.plusDays(1).atTime(9, 0), count = 9))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))
        viewModel.initial(StatisticsTab.DAY)

        viewModel.onNextDay()

        val state = viewModel.uiState.value as StatisticsUiState.Success
        val hourly = state.graphType as GraphType.Hourly
        state.selectedDate shouldBe today.plusDays(1)
        hourly.data[LocalTime.of(9, 0)] shouldBe 9
    }

    "onNextMonth moves the selected month forward and reloads daily stats" {
        val dao = FakeCounterDao()
        val thisMonth = YearMonth.now()
        dao.seed(CounterEntity(thisMonth.plusMonths(1).atDay(10).atTime(9, 0), count = 6))
        val viewModel = StatisticsViewModel(StatisticsRepository(dao, Dispatchers.Unconfined))
        viewModel.initial(StatisticsTab.MONTH)

        viewModel.onNextMonth()

        val state = viewModel.uiState.value as StatisticsUiState.Success
        val daily = state.graphType as GraphType.Daily
        state.selectedMonth shouldBe thisMonth.plusMonths(1)
        daily.data[thisMonth.plusMonths(1).atDay(10)] shouldBe 6
    }
})
