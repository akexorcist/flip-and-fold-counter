package dev.akexorcist.flipfoldcounter.data

import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class StatisticsRepositoryTest {

    @Test
    fun `getHourlyStats fills every hour of the day and excludes other days`() = runTest {
        val dao = FakeCounterDao()
        val date = LocalDate.of(2026, 1, 15)
        dao.seed(
            CounterEntity(date.atTime(9, 0), count = 3),
            CounterEntity(date.atTime(9, 30), count = 2), // same hour bucket as above
            CounterEntity(date.atTime(23, 0), count = 1),
            CounterEntity(date.minusDays(1).atTime(9, 0), count = 100),
            CounterEntity(date.plusDays(1).atTime(9, 0), count = 100),
        )

        val stats = StatisticsRepository(dao).getHourlyStats(date).first()

        assertEquals(24, stats.size)
        assertEquals(5, stats[LocalTime.of(9, 0)])
        assertEquals(1, stats[LocalTime.of(23, 0)])
        assertEquals(0, stats[LocalTime.of(0, 0)])
        assertEquals(0, stats[LocalTime.of(12, 0)])
    }

    @Test
    fun `getDailyStats fills every day of the month and excludes other months`() = runTest {
        val dao = FakeCounterDao()
        val month = YearMonth.of(2026, 2) // 28 days, non-leap
        dao.seed(
            CounterEntity(month.atDay(1).atTime(10, 0), count = 10),
            CounterEntity(month.atDay(28).atTime(10, 0), count = 20),
            CounterEntity(month.minusMonths(1).atDay(1).atTime(10, 0), count = 100),
            CounterEntity(month.plusMonths(1).atDay(1).atTime(10, 0), count = 100),
        )

        val stats = StatisticsRepository(dao).getDailyStats(month).first()

        assertEquals(28, stats.size)
        assertEquals(10, stats[month.atDay(1)])
        assertEquals(20, stats[month.atDay(28)])
        assertEquals(0, stats[month.atDay(15)])
    }

    @Test
    fun `getAllMonthlyStats is empty when there is no data`() = runTest {
        val stats = StatisticsRepository(FakeCounterDao()).getAllMonthlyStats().first()

        assertEquals(emptyMap<YearMonth, Int>(), stats)
    }

    @Test
    fun `getAllMonthlyStats fills a zero-data month across a year boundary`() = runTest {
        val dao = FakeCounterDao()
        dao.seed(
            CounterEntity(YearMonth.of(2025, 11).atDay(1).atTime(9, 0), count = 30),
            // December has no row at all
            CounterEntity(YearMonth.of(2026, 1).atDay(1).atTime(9, 0), count = 50),
        )

        val stats = StatisticsRepository(dao).getAllMonthlyStats().first()

        assertEquals(30, stats[YearMonth.of(2025, 11)])
        assertEquals(0, stats[YearMonth.of(2025, 12)])
        assertEquals(50, stats[YearMonth.of(2026, 1)])
    }

    @Test
    fun `getAllMonthlyStats extends the fill up to the current month`() = runTest {
        val dao = FakeCounterDao()
        val twoMonthsAgo = YearMonth.now().minusMonths(2)
        dao.seed(CounterEntity(twoMonthsAgo.atDay(1).atTime(9, 0), count = 15))

        val stats = StatisticsRepository(dao).getAllMonthlyStats().first()

        assertEquals(3, stats.size) // twoMonthsAgo, lastMonth, currentMonth
        assertEquals(15, stats[twoMonthsAgo])
        assertEquals(0, stats[YearMonth.now().minusMonths(1)])
        assertEquals(0, stats[YearMonth.now()])
    }

    @Test
    fun `getAllMonthlyStats still fills the range when the only entry is dated after the current month`() = runTest {
        val dao = FakeCounterDao()
        val currentMonth = YearMonth.now()
        val futureMonth = currentMonth.plusMonths(3)
        dao.seed(CounterEntity(futureMonth.atDay(1).atTime(9, 0), count = 42))

        val stats = StatisticsRepository(dao).getAllMonthlyStats().first()

        assertEquals(4, stats.size)
        assertEquals(0, stats[currentMonth])
        assertEquals(0, stats[currentMonth.plusMonths(1)])
        assertEquals(0, stats[currentMonth.plusMonths(2)])
        assertEquals(42, stats[futureMonth])
    }

    @Test
    fun `getFirstEntryDate and getLastEntryDate return null when there is no data`() = runTest {
        val repository = StatisticsRepository(FakeCounterDao())

        assertNull(repository.getFirstEntryDate())
        assertNull(repository.getLastEntryDate())
    }

    @Test
    fun `getFirstEntryDate and getLastEntryDate return the earliest and latest dates`() = runTest {
        val dao = FakeCounterDao()
        dao.seed(
            CounterEntity(LocalDate.of(2025, 6, 10).atTime(9, 0), count = 1),
            CounterEntity(LocalDate.of(2026, 1, 5).atTime(9, 0), count = 1),
            CounterEntity(LocalDate.of(2025, 12, 25).atTime(9, 0), count = 1),
        )
        val repository = StatisticsRepository(dao)

        assertEquals(LocalDate.of(2025, 6, 10), repository.getFirstEntryDate())
        assertEquals(LocalDate.of(2026, 1, 5), repository.getLastEntryDate())
    }
}
