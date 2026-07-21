package dev.akexorcist.flipfoldcounter.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class CounterDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: CounterDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.counterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertCountForHour_insertsThenIncrementsTheSameHourBucket() = runBlocking {
        val hour = LocalDateTime.of(2026, 1, 15, 9, 0, 0)

        dao.upsertCountForHour(hour)
        dao.upsertCountForHour(hour)
        dao.upsertCountForHour(hour)

        val entities = dao.getAllCountsOrdered().first()
        assertEquals(1, entities.size)
        assertEquals(hour, entities.single().dateTime)
        assertEquals(3, entities.single().count)
    }

    @Test
    fun upsertCountForHour_roundTripsThroughRealDateConverter() = runBlocking {
        val hour = LocalDateTime.of(2026, 7, 4, 23, 0, 0)

        dao.upsertCountForHour(hour)

        assertEquals(hour, dao.getFirstEntry()?.dateTime)
    }

    @Test
    fun observeTotalCount_sumsAcrossAllEntries() = runBlocking {
        dao.upsertCountForHour(LocalDateTime.of(2026, 1, 1, 9, 0))
        dao.upsertCountForHour(LocalDateTime.of(2026, 1, 1, 10, 0))
        dao.incrementCount(LocalDateTime.of(2026, 1, 1, 9, 0))

        assertEquals(3, dao.observeTotalCount().first())
    }

    @Test
    fun observeTodayCount_excludesEntriesOutsideTheDateRange() = runBlocking {
        val startOfDay = LocalDateTime.of(2026, 3, 10, 0, 0)
        val endOfDay = startOfDay.plusDays(1)
        dao.upsertCountForHour(LocalDateTime.of(2026, 3, 10, 9, 0))
        dao.upsertCountForHour(LocalDateTime.of(2026, 3, 9, 9, 0))
        dao.upsertCountForHour(LocalDateTime.of(2026, 3, 11, 0, 0))

        assertEquals(1, dao.observeTodayCount(startOfDay, endOfDay).first())
    }

    @Test
    fun getFirstEntry_and_getLastEntry_returnOrderedBoundaries() = runBlocking {
        dao.upsertCountForHour(LocalDateTime.of(2026, 5, 1, 9, 0))
        dao.upsertCountForHour(LocalDateTime.of(2026, 6, 1, 9, 0))
        dao.upsertCountForHour(LocalDateTime.of(2026, 4, 1, 9, 0))

        assertEquals(LocalDateTime.of(2026, 4, 1, 9, 0), dao.getFirstEntry()?.dateTime)
        assertEquals(LocalDateTime.of(2026, 6, 1, 9, 0), dao.getLastEntry()?.dateTime)
    }

    @Test
    fun getFirstEntry_returnsNullWhenEmpty() = runBlocking {
        assertNull(dao.getFirstEntry())
        assertNull(dao.getLastEntry())
    }

    @Test
    fun clearAll_removesEveryRow() = runBlocking {
        dao.upsertCountForHour(LocalDateTime.of(2026, 1, 1, 9, 0))

        dao.clearAll()

        assertNull(dao.observeTotalCount().first())
    }
}
