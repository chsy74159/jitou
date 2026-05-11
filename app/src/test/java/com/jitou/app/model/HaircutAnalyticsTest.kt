package com.jitou.app.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HaircutAnalyticsTest {
    @Test
    fun calculateReturnsAverageRecentIntervalsAndWeekday() {
        val records = listOf(
            HaircutRecord("1", LocalDate.of(2026, 1, 5)),
            HaircutRecord("2", LocalDate.of(2026, 2, 2)),
            HaircutRecord("3", LocalDate.of(2026, 3, 2)),
            HaircutRecord("4", LocalDate.of(2026, 3, 30)),
            HaircutRecord("5", LocalDate.of(2026, 4, 27)),
        )

        val stats = HaircutAnalytics.calculate(records)

        assertEquals(28, stats.averageIntervalDays)
        assertEquals(listOf(28, 28, 28, 28), stats.recentIntervals)
        assertEquals(DayOfWeek.MONDAY, stats.mostFrequentWeekday)
    }

    @Test
    fun daysSinceLastHaircutUsesLatestRecord() {
        val records = listOf(
            HaircutRecord("old", LocalDate.of(2026, 4, 1)),
            HaircutRecord("latest", LocalDate.of(2026, 4, 20)),
        )

        val days = HaircutAnalytics.daysSinceLastHaircut(
            records = records,
            today = LocalDate.of(2026, 5, 1),
        )

        assertEquals(11, days)
    }

    @Test
    fun emptyRecordsReturnNeutralStats() {
        val stats = HaircutAnalytics.calculate(emptyList())

        assertEquals(0, stats.averageIntervalDays)
        assertEquals(emptyList<Int>(), stats.recentIntervals)
        assertEquals(null, stats.mostFrequentWeekday)
        assertEquals(null, HaircutAnalytics.daysSinceLastHaircut(emptyList()))
    }
}
