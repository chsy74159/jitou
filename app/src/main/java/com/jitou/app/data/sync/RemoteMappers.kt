package com.jitou.app.data.sync

import com.jitou.app.data.local.ActiveProposalId
import com.jitou.app.data.local.AppointmentProposalEntity
import com.jitou.app.data.local.HaircutRecordEntity
import com.jitou.app.data.local.ReminderSettingsEntity
import com.jitou.app.data.local.ReminderSettingsId
import com.jitou.app.data.local.SyncState
import com.jitou.app.data.remote.RemoteHaircutPairMember
import com.jitou.app.data.remote.RemoteHaircutRecord
import com.jitou.app.data.remote.RemoteJointHaircutPlan
import com.jitou.app.data.remote.RemoteReminderPreference
import com.jitou.app.model.ProposalStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun parseRemoteTimestamp(value: String?): Long? = value?.let {
    OffsetDateTime.parse(it).toInstant().toEpochMilli()
}

fun planStatusForCurrentUser(proposerId: String, currentUserId: String): ProposalStatus =
    if (proposerId == currentUserId) ProposalStatus.PendingFriend else ProposalStatus.PendingMe

fun RemoteHaircutRecord.toLocalEntity(existingId: String? = null): HaircutRecordEntity = HaircutRecordEntity(
    id = existingId ?: id,
    dateEpochDay = LocalDate.parse(haircutDate).toEpochDay(),
    note = note,
    remoteId = id,
    updatedAtMillis = parseRemoteTimestamp(updatedAt) ?: 0L,
    deletedAtMillis = parseRemoteTimestamp(deletedAt),
    syncState = SyncState.SYNCED.name,
)

fun RemoteJointHaircutPlan.toActiveProposalEntity(
    currentUserId: String,
    members: List<RemoteHaircutPairMember>,
): AppointmentProposalEntity {
    val proposedDateTime = OffsetDateTime.parse(proposedAt).toLocalDateTime()
    val collaboratorName = members
        .firstOrNull { it.pairId == pairId && it.userId != currentUserId }
        ?.displayName
        ?: "XX"
    val domainStatus = when (status) {
        "confirmed" -> ProposalStatus.Confirmed
        else -> planStatusForCurrentUser(proposerId, currentUserId)
    }

    return AppointmentProposalEntity(
        id = ActiveProposalId,
        proposedDateEpochDay = proposedDateTime.toLocalDate().toEpochDay(),
        proposedMinuteOfDay = proposedDateTime.toLocalTime().hour * 60 + proposedDateTime.toLocalTime().minute,
        proposerName = if (proposerId == currentUserId) "我" else collaboratorName,
        status = domainStatus.name,
        reminderDaysBefore = reminderDaysBefore,
        remoteId = id,
        updatedAtMillis = parseRemoteTimestamp(updatedAt) ?: 0L,
        deletedAtMillis = null,
        syncState = SyncState.SYNCED.name,
    )
}

fun RemoteReminderPreference.toLocalEntity(): ReminderSettingsEntity {
    val time = LocalTime.parse(reminderTime.take(5))
    return ReminderSettingsEntity(
        id = ReminderSettingsId,
        enabled = enabled,
        daysBefore = daysBefore,
        minuteOfDay = time.hour * 60 + time.minute,
        remoteId = userId,
        updatedAtMillis = parseRemoteTimestamp(updatedAt) ?: 0L,
        deletedAtMillis = null,
        syncState = SyncState.SYNCED.name,
    )
}

fun proposedAtTimestamp(dateEpochDay: Long, minuteOfDay: Int): String {
    val date = LocalDate.ofEpochDay(dateEpochDay)
    val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
    return ZonedDateTime.of(date, time, ZoneId.systemDefault()).toInstant().toString()
}
