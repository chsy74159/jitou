package com.jitou.app.ui.home

import androidx.compose.ui.graphics.Color
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.ProposalStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
internal val HomeBackground = Color(0xFFF8F7F2)
internal val Ink = Color(0xFF171717)
internal val MutedInk = Color(0xFF72706A)
internal val Yellow = Color(0xFFFFD84D)
internal val Mint = Color(0xFFCFECE1)
internal val Peach = Color(0xFFFFD8C9)
internal val WarmPanel = Color(0xFFF0ECE2)
internal val SoftLine = Color(0x14000000)

internal fun HaircutProposal.statusLabel(): String = when (status) {
    ProposalStatus.PendingFriend -> "待确认"
    ProposalStatus.PendingMe -> "待我确认"
    ProposalStatus.Confirmed -> "已达成"
}

internal fun haircutStatus(daysSinceLast: Int, averageIntervalDays: Int): String = when {
    averageIntervalDays == 0 -> "先记一剪"
    daysSinceLast >= averageIntervalDays + 5 -> "该剪了"
    daysSinceLast >= averageIntervalDays - 8 -> "差不多该约了"
    else -> "还撑得住"
}

internal fun LocalTime.toReminderText(): String = "%02d:%02d".format(hour, minute)

internal fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
