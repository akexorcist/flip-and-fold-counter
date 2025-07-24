package dev.akexorcist.flipfoldcounter.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: CounterRepository) : ViewModel() {

    val totalCount: StateFlow<Int> = repository.observeTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayCount: StateFlow<Int> = repository.observeTodayCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val thisMonthCount: StateFlow<Int> = repository.observeCurrentMonthCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addCount() {
        viewModelScope.launch {
            repository.addCountForCurrentHour()
        }
    }
}