package dev.akexorcist.flipfoldcounter.data

import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.data.db.FakeCounterDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class CounterRepositoryTest {

    @Test
    fun `addCountForCurrentHour inserts a new hour bucket truncated to the top of the hour`() = runTest {
        val dao = FakeCounterDao()
        val repository = CounterRepository(dao)

        repository.addCountForCurrentHour()

        val expectedHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        assertEquals(1, repository.observeTotalCount().first())
        assertEquals(1, dao.getAllCountsOrdered().first().single { it.dateTime == expectedHour }.count)
    }

    @Test
    fun `addCountForCurrentHour increments an existing hour bucket instead of duplicating it`() = runTest {
        val dao = FakeCounterDao()
        val repository = CounterRepository(dao)

        repository.addCountForCurrentHour()
        repository.addCountForCurrentHour()
        repository.addCountForCurrentHour()

        val entities = dao.getAllCountsOrdered().first()
        assertEquals(1, entities.size)
        assertEquals(3, entities.single().count)
    }

    @Test
    fun `observeTotalCount emits 0 when there is no data`() = runTest {
        val repository = CounterRepository(FakeCounterDao())

        assertEquals(0, repository.observeTotalCount().first())
    }

    @Test
    fun `observeTodayCount only sums entries within today`() = runTest {
        val dao = FakeCounterDao()
        val today = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        dao.seed(
            CounterEntity(dateTime = today, count = 5),
            CounterEntity(dateTime = today.minusDays(1), count = 100),
            CounterEntity(dateTime = today.plusDays(1), count = 100),
        )

        assertEquals(5, CounterRepository(dao).observeTodayCount().first())
    }

    @Test
    fun `observeCurrentMonthCount excludes entries from adjacent months`() = runTest {
        val dao = FakeCounterDao()
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        dao.seed(
            CounterEntity(dateTime = now, count = 7),
            CounterEntity(dateTime = now.minusMonths(1), count = 100),
            CounterEntity(dateTime = now.plusMonths(1), count = 100),
        )

        assertEquals(7, CounterRepository(dao).observeCurrentMonthCount().first())
    }

    @Test
    fun `clearAll removes every entry`() = runTest {
        val dao = FakeCounterDao()
        dao.seed(CounterEntity(dateTime = LocalDateTime.now(), count = 42))
        val repository = CounterRepository(dao)

        repository.clearAll()

        assertEquals(0, repository.observeTotalCount().first())
    }
}
