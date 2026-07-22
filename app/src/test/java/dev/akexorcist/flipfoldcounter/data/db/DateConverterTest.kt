package dev.akexorcist.flipfoldcounter.data.db

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateConverterTest : StringSpec({
    val converter = DateConverter()

    "dateToTimestamp and fromTimestamp round-trip a value" {
        val original = LocalDateTime.of(2026, 1, 15, 9, 0, 0)

        val timestamp = converter.dateToTimestamp(original)

        timestamp shouldBe original.toEpochSecond(ZoneOffset.UTC)
        converter.fromTimestamp(timestamp) shouldBe original
    }

    "dateToTimestamp returns null for null input" {
        converter.dateToTimestamp(null).shouldBeNull()
    }

    "fromTimestamp returns null for null input" {
        converter.fromTimestamp(null).shouldBeNull()
    }

    "sub-second precision is lost through the round-trip" {
        val withNanos = LocalDateTime.of(2026, 1, 15, 9, 0, 0, 500_000_000)

        val restored = converter.fromTimestamp(converter.dateToTimestamp(withNanos))

        restored shouldBe withNanos.withNano(0)
    }
})
