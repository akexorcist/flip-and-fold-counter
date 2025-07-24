package dev.akexorcist.flipfoldcounter.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CounterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(counter: CounterEntity): Long

    @Query("UPDATE counter SET count = count + 1 WHERE dateTime = :dateTime")
    suspend fun incrementCount(dateTime: LocalDateTime)

    @Transaction
    suspend fun upsertCountForHour(dateTime: LocalDateTime) {
        val result = insert(CounterEntity(dateTime = dateTime, count = 1))
        if (result == -1L) {
            incrementCount(dateTime)
        }
    }

    @Query("SELECT SUM(count) FROM counter")
    fun observeTotalCount(): Flow<Int?>

    @Query("SELECT SUM(count) FROM counter WHERE dateTime >= :startOfDay AND dateTime < :endOfDay")
    fun observeTodayCount(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<Int?>

    @Query("SELECT SUM(count) FROM counter WHERE dateTime >= :startOfMonth AND dateTime < :endOfMonth")
    fun observeCurrentMonthCount(startOfMonth: LocalDateTime, endOfMonth: LocalDateTime): Flow<Int?>

    @Query("SELECT SUM(count) FROM counter WHERE dateTime >= :startOfHour AND dateTime < :endOfHour")
    fun observeHourCount(startOfHour: LocalDateTime, endOfHour: LocalDateTime): Flow<Int?>

    @Query("DELETE FROM counter")
    suspend fun clearAll()
}
