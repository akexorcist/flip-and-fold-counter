package dev.akexorcist.flipfoldcounter.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.flipfoldcounter.data.AppSettingsRepository
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    counterRepository: CounterRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        counterRepository.observeTotalCount(),
        counterRepository.observeTodayCount(),
        counterRepository.observeCurrentMonthCount(),
    ) { totalCount, todayCount, thisMonthCount ->
        MainUiState(
            totalCount = totalCount,
            todayCount = todayCount,
            thisMonthCount = thisMonthCount,
            showBeforeUsingAgain = appSettingsRepository.shouldShowBeforeUsing(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            totalCount = 0,
            todayCount = 0,
            thisMonthCount = 0,
            showBeforeUsingAgain = appSettingsRepository.shouldShowBeforeUsing(),
        )
    )

    fun markAsDoNotShowBeforeUsingAgain() {
        appSettingsRepository.markAsDoNotShowBeforeUsingAgain()
    }
}

data class MainUiState(
    val totalCount: Int,
    val todayCount: Int,
    val thisMonthCount: Int,
    val showBeforeUsingAgain: Boolean,
)
