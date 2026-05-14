package com.jitou.app.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jitou.app.ui.theme.jitouColors
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.ProposalStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
internal val HomeBackground: Color
    @Composable get() = MaterialTheme.jitouColors.background
internal val Surface: Color
    @Composable get() = MaterialTheme.jitouColors.surface
internal val Ink: Color
    @Composable get() = MaterialTheme.jitouColors.ink
internal val MutedInk: Color
    @Composable get() = MaterialTheme.jitouColors.mutedInk
internal val Yellow: Color
    @Composable get() = MaterialTheme.jitouColors.accent
internal val Mint: Color
    @Composable get() = MaterialTheme.jitouColors.sage
internal val Peach: Color
    @Composable get() = MaterialTheme.jitouColors.clay
internal val WarmPanel: Color
    @Composable get() = MaterialTheme.jitouColors.surfaceMuted
internal val SoftLine: Color
    @Composable get() = MaterialTheme.jitouColors.line

internal fun HaircutProposal.statusLabel(): String = when (status) {
    ProposalStatus.PendingFriend -> "待确认"
    ProposalStatus.PendingMe -> "待我确认"
    ProposalStatus.Confirmed -> "已达成"
}

internal fun haircutStatus(daysSinceLast: Int, averageIntervalDays: Int): String = when {
    averageIntervalDays == 0 -> "先记一剪"
    daysSinceLast <= 10 -> "清爽得很"
    daysSinceLast >= averageIntervalDays - 1 -> "是时候头了"
    daysSinceLast >= averageIntervalDays - 8 -> "差不多该约了，几时头"
    else -> "还撑得住"
}

internal fun todayLineText(date: LocalDate): String = "今天是${date.format(DateFormatter)}"

internal fun LocalTime.toReminderText(): String = "%02d:%02d".format(hour, minute)

internal fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
