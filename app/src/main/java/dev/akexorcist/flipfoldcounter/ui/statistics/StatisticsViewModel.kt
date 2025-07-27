package dev.akexorcist.flipfoldcounter.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.flipfoldcounter.data.StatisticsRepository
import dev.akexorcist.flipfoldcounter.ui.navigation.StatisticsTab
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
        val selectedDate: LocalDate,
        val selectedMonth: YearMonth,
        val isNextDayEnabled: Boolean,
        val isPreviousDayEnabled: Boolean,
        val isNextMonthEnabled: Boolean,
        val isPreviousMonthEnabled: Boolean,
    ) : StatisticsUiState

    data class Error(val error: Throwable) : StatisticsUiState
}

class StatisticsViewModel(
    private val statisticsRepository: StatisticsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    private var firstEntryDate: LocalDate? = null
    private var lastEntryDate: LocalDate? = null

    fun initial(initialTab: StatisticsTab) = viewModelScope.launch {
        firstEntryDate = statisticsRepository.getFirstEntryDate()
        lastEntryDate = statisticsRepository.getLastEntryDate() ?: LocalDate.now()
        val initialTabIndex = when (initialTab) {
            StatisticsTab.DAY -> 0
            StatisticsTab.MONTH -> 1
            StatisticsTab.OVERALL -> 2
        }
        _selectedTabIndex.value = initialTabIndex
        onGraphTypeSelected(initialTabIndex)
    }

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
        onGraphTypeSelected(index)
    }

    private fun onGraphTypeSelected(graphType: Int) {
        when (graphType) {
            0 -> loadHourlyStats(_selectedDate.value)
            1 -> loadDailyStats(_selectedMonth.value)
            2 -> loadMonthlyStats()
        }
    }

    fun onNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        loadHourlyStats(_selectedDate.value)
    }

    fun onPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        loadHourlyStats(_selectedDate.value)
    }

    fun onNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
        loadDailyStats(_selectedMonth.value)
    }

    fun onPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
        loadDailyStats(_selectedMonth.value)
    }

    private fun loadHourlyStats(date: LocalDate) {
        _uiState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            statisticsRepository.getHourlyStats(date)
                .catch { exception ->
                    _uiState.value = StatisticsUiState.Error(exception)
                }
                .collect { data ->
                    val max = data.values.maxOrNull() ?: 0
                    val average = if (data.isNotEmpty()) data.values.average().toInt() else 0
                    val isNextDayEnabled = lastEntryDate?.let { _selectedDate.value.isBefore(it) } ?: false
                    val isPreviousDayEnabled = firstEntryDate?.let { _selectedDate.value.isAfter(it) } ?: false
                    val isNextMonthEnabled = lastEntryDate?.let { _selectedMonth.value.isBefore(YearMonth.from(it)) } ?: false
                    val isPreviousMonthEnabled = firstEntryDate?.let { _selectedMonth.value.isAfter(YearMonth.from(it)) } ?: false
                    _uiState.value = StatisticsUiState.Success(
                        graphType = GraphType.Hourly(
                            date = date,
                            data = data,
                            max = max,
                            average = average
                        ),
                        selectedDate = _selectedDate.value,
                        selectedMonth = _selectedMonth.value,
                        isNextDayEnabled = isNextDayEnabled,
                        isPreviousDayEnabled = isPreviousDayEnabled,
                        isNextMonthEnabled = isNextMonthEnabled,
                        isPreviousMonthEnabled = isPreviousMonthEnabled,
                    )
                }
        }
    }

    private fun loadDailyStats(yearMonth: YearMonth) {
        _uiState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            statisticsRepository.getDailyStats(yearMonth)
                .catch { exception ->
                    _uiState.value = StatisticsUiState.Error(exception)
                }
                .collect { data ->
                    val max = data.values.maxOrNull() ?: 0
                    val average = if (data.isNotEmpty()) data.values.average().toInt() else 0
                    val isNextDayEnabled = lastEntryDate?.let { _selectedDate.value.isBefore(it) } ?: false
                    val isPreviousDayEnabled = firstEntryDate?.let { _selectedDate.value.isAfter(it) } ?: false
                    val isNextMonthEnabled = lastEntryDate?.let { _selectedMonth.value.isBefore(YearMonth.from(it)) } ?: false
                    val isPreviousMonthEnabled = firstEntryDate?.let { _selectedMonth.value.isAfter(YearMonth.from(it)) } ?: false
                    _uiState.value = StatisticsUiState.Success(
                        graphType = GraphType.Daily(
                            yearMonth = yearMonth,
                            data = data,
                            max = max,
                            average = average
                        ),
                        selectedDate = _selectedDate.value,
                        selectedMonth = _selectedMonth.value,
                        isNextDayEnabled = isNextDayEnabled,
                        isPreviousDayEnabled = isPreviousDayEnabled,
                        isNextMonthEnabled = isNextMonthEnabled,
                        isPreviousMonthEnabled = isPreviousMonthEnabled,
                    )
                }
        }
    }

    private fun loadMonthlyStats() {
        _uiState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            statisticsRepository.getAllMonthlyStats()
                .catch { exception ->
                    _uiState.value = StatisticsUiState.Error(exception)
                }
                .collect { data ->
                    val max = data.values.maxOrNull() ?: 0
                    val average = if (data.isNotEmpty()) data.values.average().toInt() else 0
                    val isNextDayEnabled = lastEntryDate?.let { _selectedDate.value.isBefore(it) } ?: false
                    val isPreviousDayEnabled = firstEntryDate?.let { _selectedDate.value.isAfter(it) } ?: false
                    val isNextMonthEnabled = lastEntryDate?.let { _selectedMonth.value.isBefore(YearMonth.from(it)) } ?: false
                    val isPreviousMonthEnabled = firstEntryDate?.let { _selectedMonth.value.isAfter(YearMonth.from(it)) } ?: false
                    _uiState.value = StatisticsUiState.Success(
                        graphType = GraphType.Monthly(
                            data = data,
                            max = max,
                            average = average
                        ),
                        selectedDate = _selectedDate.value,
                        selectedMonth = _selectedMonth.value,
                        isNextDayEnabled = isNextDayEnabled,
                        isPreviousDayEnabled = isPreviousDayEnabled,
                        isNextMonthEnabled = isNextMonthEnabled,
                        isPreviousMonthEnabled = isPreviousMonthEnabled,
                    )
                }
        }
    }
}
