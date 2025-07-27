package dev.akexorcist.flipfoldcounter

import androidx.lifecycle.ViewModel
import dev.akexorcist.flipfoldcounter.data.CounterRepository

class CounterViewModel(private val counterRepository: CounterRepository) : ViewModel() {

    suspend fun addCounter() {
        counterRepository.addCountForCurrentHour()
    }
}
