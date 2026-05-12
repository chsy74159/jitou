package com.jitou.app.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object HaircutAnalytics {
    fun daysSinceLastHaircut(records: List<HaircutRecord>, today: LocalDate = LocalDate.now()): Int? {
        val lastDate = records.maxByOrNull { it.date }?.date ?: return null
        return ChronoUnit.DAYS.between(lastDate, today).coerceAtLeast(0).toInt()
    }

    fun calculate(records: List<HaircutRecord>): HaircutStats {
        val intervals = calculateIntervals(records)
        val intervalDays = intervals.map { it.days }

        val weekday = records
            .groupingBy { it.date.dayOfWeek }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<DayOfWeek, Int>> { it.value }.thenBy { -it.key.value })
            ?.key

        return HaircutStats(
            averageIntervalDays = intervalDays.averageOrZero().roundToInt(),
            recentIntervals = intervalDays.takeLast(4).asReversed(),
            mostFrequentWeekday = weekday,
        )
    }

    fun calculateIntervals(records: List<HaircutRecord>): List<HaircutInterval> {
        val sortedRecords = records.sortedBy { it.date }
        return sortedRecords.zipWithNext { previous, next ->
            HaircutInterval(
                from = previous.date,
                to = next.date,
                days = ChronoUnit.DAYS.between(previous.date, next.date).toInt(),
            )
        }
    }

    fun historyEntries(records: List<HaircutRecord>, today: LocalDate = LocalDate.now()): List<HaircutHistoryEntry> =
        records
            .sortedByDescending { it.date }
            .mapIndexed { index, record ->
                HaircutHistoryEntry(
                    date = record.date,
                    daysAgo = ChronoUnit.DAYS.between(record.date, today).coerceAtLeast(0).toInt(),
                    isLatest = index == 0,
                )
            }
}

fun DayOfWeek.zhLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}

private fun List<Int>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
