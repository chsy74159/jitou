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
        val sortedRecords = records.sortedBy { it.date }
        val intervals = sortedRecords.zipWithNext { previous, next ->
            ChronoUnit.DAYS.between(previous.date, next.date).toInt()
        }

        val weekday = records
            .groupingBy { it.date.dayOfWeek }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<DayOfWeek, Int>> { it.value }.thenBy { -it.key.value })
            ?.key

        return HaircutStats(
            averageIntervalDays = intervals.averageOrZero().roundToInt(),
            recentIntervals = intervals.takeLast(4).asReversed(),
            mostFrequentWeekday = weekday,
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
