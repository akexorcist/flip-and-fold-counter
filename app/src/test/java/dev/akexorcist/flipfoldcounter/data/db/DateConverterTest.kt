package dev.akexorcist.flipfoldcounter.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateConverterTest {
    private val converter = DateConverter()

    @Test
    fun `dateToTimestamp and fromTimestamp round-trip a value`() {
        val original = LocalDateTime.of(2026, 1, 15, 9, 0, 0)

        val timestamp = converter.dateToTimestamp(original)

        assertEquals(original.toEpochSecond(ZoneOffset.UTC), timestamp)
        assertEquals(original, converter.fromTimestamp(timestamp))
    }

    @Test
    fun `dateToTimestamp returns null for null input`() {
        assertNull(converter.dateToTimestamp(null))
    }

    @Test
    fun `fromTimestamp returns null for null input`() {
        assertNull(converter.fromTimestamp(null))
    }

    @Test
    fun `sub-second precision is lost through the round-trip`() {
        val withNanos = LocalDateTime.of(2026, 1, 15, 9, 0, 0, 500_000_000)

        val restored = converter.fromTimestamp(converter.dateToTimestamp(withNanos))

        assertEquals(withNanos.withNano(0), restored)
    }
}
