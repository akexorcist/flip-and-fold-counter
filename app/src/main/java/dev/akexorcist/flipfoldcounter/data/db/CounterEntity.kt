package dev.akexorcist.flipfoldcounter.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "counter")
data class CounterEntity(
    @PrimaryKey
    val dateTime: LocalDateTime,
    val count: Int
)
