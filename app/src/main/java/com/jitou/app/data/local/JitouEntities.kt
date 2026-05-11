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
)

@Entity(tableName = "appointment_proposals")
data class AppointmentProposalEntity(
    @PrimaryKey val id: String,
    val proposedDateEpochDay: Long,
    val proposedMinuteOfDay: Int,
    val proposerName: String,
    val status: String,
    val reminderDaysBefore: Int,
)

@Entity(tableName = "appointment_history")
data class AppointmentHistoryEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val minuteOfDay: Int,
    val companionName: String,
    val result: String,
)

@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey val id: String = ReminderSettingsId,
    val enabled: Boolean,
    val daysBefore: Int,
    val minuteOfDay: Int,
)

const val ActiveProposalId = "active"
const val ReminderSettingsId = "default"

fun HaircutRecordEntity.toDomain(): HaircutRecord = HaircutRecord(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    note = note,
)

fun HaircutRecord.toEntity(): HaircutRecordEntity = HaircutRecordEntity(
    id = id,
    dateEpochDay = date.toEpochDay(),
    note = note,
)

fun AppointmentProposalEntity.toDomain(): HaircutProposal = HaircutProposal(
    id = id,
    proposedDate = LocalDate.ofEpochDay(proposedDateEpochDay),
    proposedTime = minuteOfDayToTime(proposedMinuteOfDay),
    proposerName = proposerName,
    status = ProposalStatus.valueOf(status),
    reminderDaysBefore = reminderDaysBefore,
)

fun HaircutProposal.toEntity(): AppointmentProposalEntity = AppointmentProposalEntity(
    id = ActiveProposalId,
    proposedDateEpochDay = proposedDate.toEpochDay(),
    proposedMinuteOfDay = proposedTime.toMinuteOfDay(),
    proposerName = proposerName,
    status = status.name,
    reminderDaysBefore = reminderDaysBefore,
)

fun AppointmentHistoryEntity.toDomain(): AppointmentHistoryItem = AppointmentHistoryItem(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    time = minuteOfDayToTime(minuteOfDay),
    companionName = companionName,
    result = result,
)

fun AppointmentHistoryItem.toEntity(): AppointmentHistoryEntity = AppointmentHistoryEntity(
    id = id,
    dateEpochDay = date.toEpochDay(),
    minuteOfDay = time.toMinuteOfDay(),
    companionName = companionName,
    result = result,
)

fun ReminderSettingsEntity.toDomain(): ReminderUiState = ReminderUiState(
    enabled = enabled,
    daysBefore = daysBefore,
    time = minuteOfDayToTime(minuteOfDay),
)

fun ReminderUiState.toEntity(): ReminderSettingsEntity = ReminderSettingsEntity(
    enabled = enabled,
    daysBefore = daysBefore,
    minuteOfDay = time.toMinuteOfDay(),
)

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

private fun minuteOfDayToTime(value: Int): LocalTime = LocalTime.of(value / 60, value % 60)
