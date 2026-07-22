package dev.akexorcist.flipfoldcounter.data

import app.cash.turbine.test
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class StatisticsRepositoryTest : StringSpec({

    "getHourlyStats fills every hour of the day and excludes other days" {
        val dao = FakeCounterDao()
        val date = LocalDate.of(2026, 1, 15)
        dao.seed(
            CounterEntity(date.atTime(9, 0), count = 3),
            CounterEntity(date.atTime(9, 30), count = 2), // same hour bucket as above
            CounterEntity(date.atTime(23, 0), count = 1),
            CounterEntity(date.minusDays(1).atTime(9, 0), count = 100),
            CounterEntity(date.plusDays(1).atTime(9, 0), count = 100),
        )

        StatisticsRepository(dao).getHourlyStats(date).test {
            val stats = awaitItem()
            stats.size shouldBe 24
            stats[LocalTime.of(9, 0)] shouldBe 5
            stats[LocalTime.of(23, 0)] shouldBe 1
            stats[LocalTime.of(0, 0)] shouldBe 0
            stats[LocalTime.of(12, 0)] shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }

    "getDailyStats fills every day of the month and excludes other months" {
        val dao = FakeCounterDao()
        val month = YearMonth.of(2026, 2) // 28 days, non-leap
        dao.seed(
            CounterEntity(month.atDay(1).atTime(10, 0), count = 10),
            CounterEntity(month.atDay(28).atTime(10, 0), count = 20),
            CounterEntity(month.minusMonths(1).atDay(1).atTime(10, 0), count = 100),
            CounterEntity(month.plusMonths(1).atDay(1).atTime(10, 0), count = 100),
        )

        StatisticsRepository(dao).getDailyStats(month).test {
            val stats = awaitItem()
            stats.size shouldBe 28
            stats[month.atDay(1)] shouldBe 10
            stats[month.atDay(28)] shouldBe 20
            stats[month.atDay(15)] shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }

    "getAllMonthlyStats is empty when there is no data" {
        StatisticsRepository(FakeCounterDao()).getAllMonthlyStats().test {
            awaitItem().shouldBeEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    "getAllMonthlyStats fills a zero-data month across a year boundary" {
        val dao = FakeCounterDao()
        dao.seed(
            CounterEntity(YearMonth.of(2025, 11).atDay(1).atTime(9, 0), count = 30),
            // December has no row at all
            CounterEntity(YearMonth.of(2026, 1).atDay(1).atTime(9, 0), count = 50),
        )

        StatisticsRepository(dao).getAllMonthlyStats().test {
            val stats = awaitItem()
            stats[YearMonth.of(2025, 11)] shouldBe 30
            stats[YearMonth.of(2025, 12)] shouldBe 0
            stats[YearMonth.of(2026, 1)] shouldBe 50
            cancelAndIgnoreRemainingEvents()
        }
    }

    "getAllMonthlyStats extends the fill up to the current month" {
        val dao = FakeCounterDao()
        val twoMonthsAgo = YearMonth.now().minusMonths(2)
        dao.seed(CounterEntity(twoMonthsAgo.atDay(1).atTime(9, 0), count = 15))

        StatisticsRepository(dao).getAllMonthlyStats().test {
            val stats = awaitItem()
            stats.size shouldBe 3 // twoMonthsAgo, lastMonth, currentMonth
            stats[twoMonthsAgo] shouldBe 15
            stats[YearMonth.now().minusMonths(1)] shouldBe 0
            stats[YearMonth.now()] shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }

    "getAllMonthlyStats still fills the range when the only entry is dated after the current month" {
        val dao = FakeCounterDao()
        val currentMonth = YearMonth.now()
        val futureMonth = currentMonth.plusMonths(3)
        dao.seed(CounterEntity(futureMonth.atDay(1).atTime(9, 0), count = 42))

        StatisticsRepository(dao).getAllMonthlyStats().test {
            val stats = awaitItem()
            stats.size shouldBe 4
            stats[currentMonth] shouldBe 0
            stats[currentMonth.plusMonths(1)] shouldBe 0
            stats[currentMonth.plusMonths(2)] shouldBe 0
            stats[futureMonth] shouldBe 42
            cancelAndIgnoreRemainingEvents()
        }
    }

    "getFirstEntryDate and getLastEntryDate return null when there is no data" {
        val repository = StatisticsRepository(FakeCounterDao())

        repository.getFirstEntryDate().shouldBeNull()
        repository.getLastEntryDate().shouldBeNull()
    }

    "getFirstEntryDate and getLastEntryDate return the earliest and latest dates" {
        val dao = FakeCounterDao()
        dao.seed(
            CounterEntity(LocalDate.of(2025, 6, 10).atTime(9, 0), count = 1),
            CounterEntity(LocalDate.of(2026, 1, 5).atTime(9, 0), count = 1),
            CounterEntity(LocalDate.of(2025, 12, 25).atTime(9, 0), count = 1),
        )
        val repository = StatisticsRepository(dao)

        repository.getFirstEntryDate() shouldBe LocalDate.of(2025, 6, 10)
        repository.getLastEntryDate() shouldBe LocalDate.of(2026, 1, 5)
    }
})
