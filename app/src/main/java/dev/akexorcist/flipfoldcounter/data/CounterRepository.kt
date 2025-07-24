package dev.akexorcist.flipfoldcounter.data

import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class CounterRepository(private val counterDao: CounterDao) {

    suspend fun addCountForCurrentHour() {
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        counterDao.upsertCountForHour(now)
    }

    fun observeTotalCount(): Flow<Int> = counterDao.observeTotalCount().map { it ?: 0 }

    fun observeTodayCount(): Flow<Int> {
        val today = LocalDateTime.now().toLocalDate().atStartOfDay()
        val endOfDay = today.plusDays(1)
        return counterDao.observeTodayCount(today, endOfDay).map { it ?: 0 }
    }

    fun observeCurrentMonthCount(): Flow<Int> {
        val today = LocalDateTime.now()
        val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
        val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).toLocalDate().atStartOfDay()
        return counterDao.observeCurrentMonthCount(startOfMonth, endOfMonth).map { it ?: 0 }
    }

    suspend fun clearAll() {
        counterDao.clearAll()
    }
}
