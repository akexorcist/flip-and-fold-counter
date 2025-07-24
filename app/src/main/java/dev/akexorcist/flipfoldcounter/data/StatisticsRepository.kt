package dev.akexorcist.flipfoldcounter.data

import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
                    .groupBy { entity -> entity.dateTime.toLocalTime() }
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

                // Determine the first month with data from the grouped results
                // If monthlyCounts is empty (though 'entities.isEmpty()' should catch this),
                // or if minOrNull returns null (should not happen if monthlyCounts is not empty),
                // default to current month as a fallback for range start.
                val firstMonthWithData = monthlyCounts.keys.minOrNull() ?: YearMonth.now()
                val currentMonth = YearMonth.now()

                val completeMonthlyStats = mutableMapOf<YearMonth, Int>()
                var tempMonth = firstMonthWithData
                // Ensure the loop does not run indefinitely if firstMonthWithData is after currentMonth
                if (tempMonth.isAfter(currentMonth)) {
                    // If the earliest data is somehow in the future,
                    // just show the current month with its count or 0.
                    // Or, one might choose to return an empty map or throw an error.
                    // For now, let's ensure at least current month is considered if range is odd.
                    // This specific case (firstMonthWithData > currentMonth) should be rare if data is historical.
                    // A more robust way, if monthlyCounts.keys is guaranteed non-empty and sorted by nature of data,
                    // is `entities.minOfOrNull { YearMonth.from(it.dateTime) } ?: YearMonth.now()`
                    // but `monthlyCounts.keys.minOrNull()` is fine after grouping.
                }

                while (!tempMonth.isAfter(currentMonth)) {
                    completeMonthlyStats[tempMonth] = monthlyCounts[tempMonth] ?: 0
                    tempMonth = tempMonth.plusMonths(1)
                }
                completeMonthlyStats.toMap()
            }
    }
}
