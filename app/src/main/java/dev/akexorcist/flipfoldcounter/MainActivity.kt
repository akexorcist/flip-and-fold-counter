package dev.akexorcist.flipfoldcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dev.akexorcist.flipfoldcounter.data.db.AppDatabase
import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import dev.akexorcist.flipfoldcounter.data.db.CounterEntity
import dev.akexorcist.flipfoldcounter.ui.navigation.NavGraph
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlipFoldCounterTheme {
                NavGraph()
            }
        }
//        lifecycleScope.launch {
//            prepopulateDatabase(AppDatabase.getDatabase(this@MainActivity).counterDao())
//        }
    }

    suspend fun prepopulateDatabase(counterDao: CounterDao) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -2)
        val oneMonthAgo = calendar.timeInMillis

        val now = System.currentTimeMillis()
        calendar.timeInMillis = oneMonthAgo

        val counters = mutableListOf<CounterEntity>()
        while (calendar.timeInMillis < now) {
            if (Random.nextBoolean()) {
                val dateTime = LocalDateTime.ofInstant(calendar.toInstant(), calendar.timeZone.toZoneId())
                val count = Random.nextInt(1, 10)
                counters.add(CounterEntity(dateTime = dateTime, count = count))
            }
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }
        counters.forEach {
            counterDao.insert(it)
        }
    }
}
