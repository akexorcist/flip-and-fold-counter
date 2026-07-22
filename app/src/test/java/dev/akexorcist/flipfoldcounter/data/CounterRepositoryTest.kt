package dev.akexorcist.flipfoldcounter.data

import app.cash.turbine.test
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class CounterRepositoryTest : StringSpec({

    "addCountForCurrentHour inserts a new hour bucket truncated to the top of the hour" {
        val dao = FakeCounterDao()
        val repository = CounterRepository(dao)

        repository.addCountForCurrentHour()

        val expectedHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        repository.observeTotalCount().test {
            awaitItem() shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
        dao.getAllCountsOrdered().test {
            awaitItem().single { it.dateTime == expectedHour }.count shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
    }

    "addCountForCurrentHour increments an existing hour bucket instead of duplicating it" {
        val dao = FakeCounterDao()
        val repository = CounterRepository(dao)

        repository.addCountForCurrentHour()
        repository.addCountForCurrentHour()
        repository.addCountForCurrentHour()

        dao.getAllCountsOrdered().test {
            val entities = awaitItem()
            entities.size shouldBe 1
            entities.single().count shouldBe 3
            cancelAndIgnoreRemainingEvents()
        }
    }

    "observeTotalCount reacts live as entries are added while collected" {
        val dao = FakeCounterDao()
        val repository = CounterRepository(dao)

        repository.observeTotalCount().test {
            awaitItem() shouldBe 0
            repository.addCountForCurrentHour()
            awaitItem() shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
    }

    "observeTodayCount only sums entries within today" {
        val dao = FakeCounterDao()
        val today = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        dao.seed(
            CounterEntity(dateTime = today, count = 5),
            CounterEntity(dateTime = today.minusDays(1), count = 100),
            CounterEntity(dateTime = today.plusDays(1), count = 100),
        )

        CounterRepository(dao).observeTodayCount().test {
            awaitItem() shouldBe 5
            cancelAndIgnoreRemainingEvents()
        }
    }

    "observeCurrentMonthCount excludes entries from adjacent months" {
        val dao = FakeCounterDao()
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        dao.seed(
            CounterEntity(dateTime = now, count = 7),
            CounterEntity(dateTime = now.minusMonths(1), count = 100),
            CounterEntity(dateTime = now.plusMonths(1), count = 100),
        )

        CounterRepository(dao).observeCurrentMonthCount().test {
            awaitItem() shouldBe 7
            cancelAndIgnoreRemainingEvents()
        }
    }

    "clearAll removes every entry" {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(dateTime = LocalDateTime.now(), count = 42))
        val repository = CounterRepository(dao)

        repository.clearAll()

        repository.observeTotalCount().test {
            awaitItem() shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }
})
