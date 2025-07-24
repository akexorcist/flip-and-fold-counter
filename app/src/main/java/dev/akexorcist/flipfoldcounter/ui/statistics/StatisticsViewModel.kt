package dev.akexorcist.flipfoldcounter.ui.statistics

import androidx.lifecycle.ViewModel
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth

sealed class GraphType {
    data class Hourly(
        val date: LocalDate,
        val graphData: Map<LocalTime, Int>,
    ) : GraphType()

    data class Daily(
        val month: Month,
        val graphData: Map<LocalDate, Int>,
    ) : GraphType()

    data class All(
        val graphData: Map<YearMonth, Int>,
    ) : GraphType()
}

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data class Success(
        val graphType: GraphType,
    ) : StatisticsUiState

    data class Error(val error: Throwable) : StatisticsUiState
}

class StatisticsViewModel(
    private val counterRepository: CounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadHourlyStats(date: LocalDate) {
        _uiState.value = StatisticsUiState.Loading
        // TODO: Implement actual data fetching from counterRepository
    }

    fun loadDailyStats(yearMonth: YearMonth) {
        _uiState.value = StatisticsUiState.Loading
        // TODO: Implement actual data fetching from counterRepository
    }

    fun loadMonthlyStats() {
        _uiState.value = StatisticsUiState.Loading
        // TODO: Implement actual data fetching from counterRepository
    }
}
