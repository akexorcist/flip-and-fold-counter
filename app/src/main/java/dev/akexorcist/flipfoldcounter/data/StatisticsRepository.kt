package dev.akexorcist.flipfoldcounter.data

import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class StatisticsRepository(
    private val counterDao: CounterDao,
) {
    fun getHourlyStats(date: LocalDate): Flow<Map<LocalTime, Int>> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay()
        return counterDao.getCountsForDateRange(startOfDay, endOfDay)
            .map { entities ->
                val hourlyCounts = entities
                    .groupBy { entity -> LocalTime.of(entity.dateTime.hour, 0) }
                    .mapValues { entry -> entry.value.sumOf { it.count } }

                val completeHourlyStats = mutableMapOf<LocalTime, Int>()
                for (hour in 0..23) {
                    val time = LocalTime.of(hour, 0)
                    completeHourlyStats[time] = hourlyCounts[time] ?: 0
                }
                completeHourlyStats.toMap()
            }
    }

    fun getDailyStats(yearMonth: YearMonth): Flow<Map<LocalDate, Int>> {
        val startOfMonth = yearMonth.atDay(1).atStartOfDay()
        val endOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay()
        return counterDao.getCountsForDateRange(startOfMonth, endOfMonth)
            .map { entities ->
                val dailyCounts = entities
                    .groupBy { entity -> entity.dateTime.toLocalDate() }
                    .mapValues { entry -> entry.value.sumOf { it.count } }

                val completeDailyStats = mutableMapOf<LocalDate, Int>()
                val daysInMonth = yearMonth.lengthOfMonth()
                for (dayOfMonth in 1..daysInMonth) {
                    val date = yearMonth.atDay(dayOfMonth)
                    completeDailyStats[date] = dailyCounts[date] ?: 0
                }
                completeDailyStats.toMap()
            }
    }

    fun getAllMonthlyStats(): Flow<Map<YearMonth, Int>> {
        return counterDao.getAllCountsOrdered()
            .map { entities ->
                if (entities.isEmpty()) {
                    return@map emptyMap()
                }

                val monthlyCounts = entities
                    .groupBy { entity -> YearMonth.from(entity.dateTime) }
                    .mapValues { entry -> entry.value.sumOf { it.count } }

                val firstMonthWithData = monthlyCounts.keys.minOrNull() ?: YearMonth.now()
                val currentMonth = YearMonth.now()

                val completeMonthlyStats = mutableMapOf<YearMonth, Int>()
                var tempMonth = firstMonthWithData
                if (tempMonth.isAfter(currentMonth)) {
                }

                while (!tempMonth.isAfter(currentMonth)) {
                    completeMonthlyStats[tempMonth] = monthlyCounts[tempMonth] ?: 0
                    tempMonth = tempMonth.plusMonths(1)
                }
                completeMonthlyStats.toMap()
            }
    }

    suspend fun getFirstEntryDate(): LocalDate? = withContext(Dispatchers.IO) {
        counterDao.getFirstEntry()?.dateTime?.toLocalDate()
    }

    suspend fun getLastEntryDate(): LocalDate? = withContext(Dispatchers.IO) {
        counterDao.getLastEntry()?.dateTime?.toLocalDate()
    }
}
