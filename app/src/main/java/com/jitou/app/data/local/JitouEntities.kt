package com.jitou.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jitou.app.model.AppointmentHistoryItem
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.ProposalStatus
import com.jitou.app.model.ReminderUiState
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "haircut_records")
data class HaircutRecordEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val note: String?,
    val remoteId: String? = null,
    val updatedAtMillis: Long = nowMillis(),
    val deletedAtMillis: Long? = null,
    val syncState: String = SyncState.SYNCED.name,
)

@Entity(tableName = "appointment_proposals")
data class AppointmentProposalEntity(
    @PrimaryKey val id: String,
    val proposedDateEpochDay: Long,
    val proposedMinuteOfDay: Int,
    val proposerName: String,
    val status: String,
    val reminderDaysBefore: Int,
    val remoteId: String? = null,
    val updatedAtMillis: Long = nowMillis(),
    val deletedAtMillis: Long? = null,
    val syncState: String = SyncState.SYNCED.name,
)

@Entity(tableName = "appointment_history")
data class AppointmentHistoryEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val minuteOfDay: Int,
    val companionName: String,
    val result: String,
    val remoteId: String? = null,
    val updatedAtMillis: Long = nowMillis(),
    val deletedAtMillis: Long? = null,
    val syncState: String = SyncState.SYNCED.name,
)

@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey val id: String = ReminderSettingsId,
    val enabled: Boolean,
    val daysBefore: Int,
    val minuteOfDay: Int,
    val remoteId: String? = null,
    val updatedAtMillis: Long = nowMillis(),
    val deletedAtMillis: Long? = null,
    val syncState: String = SyncState.SYNCED.name,
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val lastSyncAtMillis: Long,
)

const val ActiveProposalId = "active"
const val ReminderSettingsId = "default"

enum class SyncState {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
}

fun HaircutRecordEntity.toDomain(): HaircutRecord = HaircutRecord(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    note = note,
)

fun HaircutRecord.toEntity(
    remoteId: String? = null,
    updatedAtMillis: Long = nowMillis(),
    deletedAtMillis: Long? = null,
    syncState: SyncState = SyncState.SYNCED,
): HaircutRecordEntity = HaircutRecordEntity(
    id = id,
    dateEpochDay = date.toEpochDay(),
    note = note,
    remoteId = remoteId,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis,
    syncState = syncState.name,
)

fun AppointmentProposalEntity.toDomain(): HaircutProposal = HaircutProposal(
    id = id,
    proposedDate = LocalDate.ofEpochDay(proposedDateEpochDay),
    proposedTime = minuteOfDayToTime(proposedMinuteOfDay),
    proposerName = proposerName,
    status = ProposalStatus.valueOf(status),
    reminderDaysBefore = reminderDaysBefore,
)

fun HaircutProposal.toEntity(
    remoteId: String? = null,
    updatedAtMillis: Long = nowMillis(),
    deletedAtMillis: Long? = null,
    syncState: SyncState = SyncState.SYNCED,
): AppointmentProposalEntity = AppointmentProposalEntity(
    id = ActiveProposalId,
    proposedDateEpochDay = proposedDate.toEpochDay(),
    proposedMinuteOfDay = proposedTime.toMinuteOfDay(),
    proposerName = proposerName,
    status = status.name,
    reminderDaysBefore = reminderDaysBefore,
    remoteId = remoteId,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis,
    syncState = syncState.name,
)

fun AppointmentHistoryEntity.toDomain(): AppointmentHistoryItem = AppointmentHistoryItem(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    time = minuteOfDayToTime(minuteOfDay),
    companionName = companionName,
    result = result,
)

fun AppointmentHistoryItem.toEntity(
    remoteId: String? = null,
    updatedAtMillis: Long = nowMillis(),
    deletedAtMillis: Long? = null,
    syncState: SyncState = SyncState.SYNCED,
): AppointmentHistoryEntity = AppointmentHistoryEntity(
    id = id,
    dateEpochDay = date.toEpochDay(),
    minuteOfDay = time.toMinuteOfDay(),
    companionName = companionName,
    result = result,
    remoteId = remoteId,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis,
    syncState = syncState.name,
)

fun ReminderSettingsEntity.toDomain(): ReminderUiState = ReminderUiState(
    enabled = enabled,
    daysBefore = daysBefore,
    time = minuteOfDayToTime(minuteOfDay),
)

fun ReminderUiState.toEntity(
    remoteId: String? = null,
    updatedAtMillis: Long = nowMillis(),
    deletedAtMillis: Long? = null,
    syncState: SyncState = SyncState.SYNCED,
): ReminderSettingsEntity = ReminderSettingsEntity(
    enabled = enabled,
    daysBefore = daysBefore,
    minuteOfDay = time.toMinuteOfDay(),
    remoteId = remoteId,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis,
    syncState = syncState.name,
)

fun nowMillis(): Long = System.currentTimeMillis()

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

private fun minuteOfDayToTime(value: Int): LocalTime = LocalTime.of(value / 60, value % 60)
