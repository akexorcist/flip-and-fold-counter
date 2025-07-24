package dev.akexorcist.flipfoldcounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.flipfoldcounter.data.CounterRepository
import kotlinx.coroutines.launch

class CounterViewModel(private val counterRepository: CounterRepository) : ViewModel() {

    suspend fun addCounter() {
        counterRepository.addCountForCurrentHour()
    }
}
