package dev.akexorcist.flipfoldcounter.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
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
        val max: Int,
        val average: Int,
    ) : GraphType()

    data class Daily(
        val yearMonth: YearMonth,
        val data: Map<LocalDate, Int>,
        val max: Int,
        val average: Int,
    ) : GraphType()

    data class Monthly(
        val data: Map<YearMonth, Int>,
        val max: Int,
        val average: Int,
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
    private val statisticsRepository: StatisticsRepository
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
                    val max = dataMap.values.maxOrNull() ?: 0
                    val average = if (dataMap.isNotEmpty()) dataMap.values.average().toInt() else 0
                    _uiState.value = StatisticsUiState.Success(
                        GraphType.Hourly(
                            date = date,
                            data = dataMap,
                            max = max,
                            average = average
                        )
                    )
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
                    val max = dataMap.values.maxOrNull() ?: 0
                    val average = if (dataMap.isNotEmpty()) dataMap.values.average().toInt() else 0
                    _uiState.value = StatisticsUiState.Success(
                        GraphType.Daily(
                            yearMonth = yearMonth,
                            data = dataMap,
                            max = max,
                            average = average
                        )
                    )
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
                    val max = dataMap.values.maxOrNull() ?: 0
                    val average = if (dataMap.isNotEmpty()) dataMap.values.average().toInt() else 0
                    _uiState.value = StatisticsUiState.Success(
                        GraphType.Monthly(
                            data = dataMap,
                            max = max,
                            average = average
                        )
                    )
                }
        }
    }
}
