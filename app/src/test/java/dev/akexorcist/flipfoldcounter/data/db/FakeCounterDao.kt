package dev.akexorcist.flipfoldcounter.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class FakeCounterDao : CounterDao {
    private val state = MutableStateFlow<List<CounterEntity>>(emptyList())

    fun seed(vararg entities: CounterEntity) {
        state.value = entities.toList()
    }

    override suspend fun insert(counter: CounterEntity): Long {
        if (state.value.any { it.dateTime == counter.dateTime }) return -1L
        state.value = state.value + counter
        return 1L
    }

    override suspend fun incrementCount(dateTime: LocalDateTime) {
        state.value = state.value.map { if (it.dateTime == dateTime) it.copy(count = it.count + 1) else it }
    }

    override fun observeTotalCount(): Flow<Int?> = state.map { it.sumCountsOrNull() }

    override fun observeTodayCount(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<Int?> =
        state.map { it.inRange(startOfDay, endOfDay).sumCountsOrNull() }

    override fun observeCurrentMonthCount(startOfMonth: LocalDateTime, endOfMonth: LocalDateTime): Flow<Int?> =
        state.map { it.inRange(startOfMonth, endOfMonth).sumCountsOrNull() }

    override fun observeHourCount(startOfHour: LocalDateTime, endOfHour: LocalDateTime): Flow<Int?> =
        state.map { it.inRange(startOfHour, endOfHour).sumCountsOrNull() }

    override suspend fun clearAll() {
        state.value = emptyList()
    }

    override fun getCountsForDateRange(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<CounterEntity>> =
        state.map { it.inRange(startOfDay, endOfDay).sortedBy { entity -> entity.dateTime } }

    override fun getAllCountsOrdered(): Flow<List<CounterEntity>> =
        state.map { it.sortedBy { entity -> entity.dateTime } }

    override fun getFirstEntry(): CounterEntity? = state.value.minByOrNull { it.dateTime }

    override fun getLastEntry(): CounterEntity? = state.value.maxByOrNull { it.dateTime }

    private fun List<CounterEntity>.inRange(start: LocalDateTime, endExclusive: LocalDateTime) =
        filter { it.dateTime >= start && it.dateTime < endExclusive }

    private fun List<CounterEntity>.sumCountsOrNull(): Int? = if (isEmpty()) null else sumOf { it.count }
}
