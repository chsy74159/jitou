package com.jitou.app.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class ScheduledHaircutNotification(
    val dateTime: LocalDateTime,
    val message: String,
)

object HaircutNotificationRules {
    fun nextNotificationAfter(
        records: List<HaircutRecord>,
        proposalStatus: ProposalStatus?,
        now: LocalDateTime = LocalDateTime.now(),
    ): ScheduledHaircutNotification? {
        val lastDate = records.latestHaircutDate() ?: return null
        if (proposalStatus.shouldSuppressHaircutNotification()) return null

        val startDate = now.toLocalDate()
        for (dayOffset in 0..NotificationSearchWindowDays) {
            val date = startDate.plusDays(dayOffset.toLong())
            val next = notificationsForDate(lastDate, date).firstOrNull { it.dateTime.isAfter(now) }
            if (next != null) return next
        }
        return null
    }

    fun notificationAt(
        records: List<HaircutRecord>,
        proposalStatus: ProposalStatus?,
        at: LocalDateTime,
    ): ScheduledHaircutNotification? {
        val lastDate = records.latestHaircutDate() ?: return null
        if (proposalStatus.shouldSuppressHaircutNotification()) return null

        return notificationsForDate(lastDate, at.toLocalDate()).firstOrNull {
            it.dateTime.toLocalTime() == at.toLocalTime().withSecond(0).withNano(0)
        }
    }

    private fun notificationsForDate(
        lastDate: LocalDate,
        date: LocalDate,
    ): List<ScheduledHaircutNotification> {
        val daysSinceLast = ChronoUnit.DAYS.between(lastDate, date).toInt()
        return slotsForDay(daysSinceLast).map { slot ->
            ScheduledHaircutNotification(
                dateTime = LocalDateTime.of(date, slot.time),
                message = slot.message,
            )
        }
    }

    private fun slotsForDay(daysSinceLast: Int): List<NotificationSlot> = when {
        daysSinceLast == 22 -> listOf(
            NotificationSlot(LocalTime.of(9, 0), "已经头完三周了，准备几时头"),
        )

        daysSinceLast in 25..27 -> listOf(
            NotificationSlot(LocalTime.of(9, 0), "怎么说几头，差不多了了"),
        )

        daysSinceLast in 28..31 -> listOf(
            NotificationSlot(LocalTime.of(9, 0), "怎么说，几时头"),
            NotificationSlot(LocalTime.of(12, 30), "几时头，很关键"),
            NotificationSlot(LocalTime.of(17, 30), "今晚头。M集合"),
        )

        daysSinceLast > 31 -> listOf(
            NotificationSlot(LocalTime.of(8, 0), "几头？？流星雨了都"),
            NotificationSlot(LocalTime.of(11, 0), "几头？？流星雨了都"),
            NotificationSlot(LocalTime.of(15, 0), "几头？？流星雨了都"),
            NotificationSlot(LocalTime.of(17, 30), "几头？？流星雨了都"),
        )

        else -> emptyList()
    }

    private fun List<HaircutRecord>.latestHaircutDate(): LocalDate? = maxByOrNull { it.date }?.date

    private fun ProposalStatus?.shouldSuppressHaircutNotification(): Boolean =
        this == ProposalStatus.Confirmed || this == ProposalStatus.PendingFriend

    private data class NotificationSlot(
        val time: LocalTime,
        val message: String,
    )

    private const val NotificationSearchWindowDays = 370
}
