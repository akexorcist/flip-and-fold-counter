package dev.akexorcist.flipfoldcounter.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository // Updated import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

sealed class GraphType {
    data class Hourly(
        val date: LocalDate,
        val data: Map<LocalTime, Int>,
    ) : GraphType()

    data class Daily(
        val yearMonth: YearMonth, // Changed from month: Month to yearMonth: YearMonth
        val data: Map<LocalDate, Int>,
    ) : GraphType()

    data class Monthly( // Renamed from All
        val data: Map<YearMonth, Int>,
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
    private val statisticsRepository: StatisticsRepository // Changed from CounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadHourlyStats(date: LocalDate) {
        _uiState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            statisticsRepository.getHourlyStats(date)
                .catch { exception ->
                    _uiState.value = StatisticsUiState.Error(exception)
                }
                .collect { dataMap ->
                    _uiState.value = StatisticsUiState.Success(GraphType.Hourly(date = date, data = dataMap))
                }
        }
    }

    fun loadDailyStats(yearMonth: YearMonth) {
        _uiState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            statisticsRepository.getDailyStats(yearMonth)
                .catch { exception ->
                    _uiState.value = StatisticsUiState.Error(exception)
                }
                .collect { dataMap ->
                    _uiState.value = StatisticsUiState.Success(GraphType.Daily(yearMonth = yearMonth, data = dataMap))
                }
        }
    }

    fun loadMonthlyStats() {
        _uiState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            statisticsRepository.getAllMonthlyStats()
                .catch { exception ->
                    _uiState.value = StatisticsUiState.Error(exception)
                }
                .collect { dataMap ->
                    _uiState.value = StatisticsUiState.Success(GraphType.Monthly(data = dataMap))
                }
        }
    }
}
