package dev.akexorcist.flipfoldcounter.ui.statistics

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class MonthlyAxisEncoderTest {

    @Test
    fun `encode increases by exactly 1 between consecutive months across a year boundary`() {
        val november = MonthlyAxisEncoder.encode(YearMonth.of(2025, 11))
        val december = MonthlyAxisEncoder.encode(YearMonth.of(2025, 12))
        val january = MonthlyAxisEncoder.encode(YearMonth.of(2026, 1))
        val february = MonthlyAxisEncoder.encode(YearMonth.of(2026, 2))

        assertEquals(1, december - november)
        assertEquals(1, january - december)
        assertEquals(1, february - january)
    }

    @Test
    fun `decode reverses encode for every month in a multi-year range`() {
        var yearMonth = YearMonth.of(2023, 1)
        val end = YearMonth.of(2027, 12)
        while (!yearMonth.isAfter(end)) {
            val encoded = MonthlyAxisEncoder.encode(yearMonth)
            assertEquals(yearMonth, MonthlyAxisEncoder.decode(encoded))
            yearMonth = yearMonth.plusMonths(1)
        }
    }

    @Test
    fun `decode also resolves intermediate values a chart axis may interpolate to`() {
        val november = MonthlyAxisEncoder.encode(YearMonth.of(2025, 11))
        val january = MonthlyAxisEncoder.encode(YearMonth.of(2026, 1))

        for (value in november..january) {
            MonthlyAxisEncoder.decode(value)
        }
    }
}
