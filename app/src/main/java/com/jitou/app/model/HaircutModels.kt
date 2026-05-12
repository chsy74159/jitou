package com.jitou.app.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class HaircutRecord(
    val id: String,
    val date: LocalDate,
    val note: String? = null,
)

data class HaircutStats(
    val averageIntervalDays: Int,
    val recentIntervals: List<Int>,
    val mostFrequentWeekday: DayOfWeek?,
)

data class HaircutInterval(
    val from: LocalDate,
    val to: LocalDate,
    val days: Int,
)

data class HaircutProposal(
    val id: String,
    val proposedDate: LocalDate,
    val proposedTime: LocalTime = LocalTime.of(20, 30),
    val proposerName: String,
    val status: ProposalStatus,
    val reminderDaysBefore: Int = 1,
)

enum class ProposalStatus {
    PendingFriend,
    PendingMe,
    Confirmed,
}

data class AppointmentHistoryItem(
    val id: String,
    val date: LocalDate,
    val time: LocalTime,
    val companionName: String,
    val result: String,
)

data class ReminderUiState(
    val enabled: Boolean,
    val daysBefore: Int,
    val time: LocalTime,
)
