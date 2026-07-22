package dev.akexorcist.flipfoldcounter.ui.statistics

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.YearMonth

class MonthlyAxisEncoderTest : StringSpec({

    "encode increases by exactly 1 between consecutive months across a year boundary" {
        val november = MonthlyAxisEncoder.encode(YearMonth.of(2025, 11))
        val december = MonthlyAxisEncoder.encode(YearMonth.of(2025, 12))
        val january = MonthlyAxisEncoder.encode(YearMonth.of(2026, 1))
        val february = MonthlyAxisEncoder.encode(YearMonth.of(2026, 2))

        (december - november) shouldBe 1
        (january - december) shouldBe 1
        (february - january) shouldBe 1
    }

    "decode reverses encode for every month in a multi-year range" {
        var yearMonth = YearMonth.of(2023, 1)
        val end = YearMonth.of(2027, 12)
        while (!yearMonth.isAfter(end)) {
            val encoded = MonthlyAxisEncoder.encode(yearMonth)
            MonthlyAxisEncoder.decode(encoded) shouldBe yearMonth
            yearMonth = yearMonth.plusMonths(1)
        }
    }

    "decode also resolves intermediate values a chart axis may interpolate to" {
        val november = MonthlyAxisEncoder.encode(YearMonth.of(2025, 11))
        val january = MonthlyAxisEncoder.encode(YearMonth.of(2026, 1))

        for (value in november..january) {
            MonthlyAxisEncoder.decode(value)
        }
    }
})
