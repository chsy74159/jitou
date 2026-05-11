package com.jitou.app.data.repository

import com.jitou.app.data.local.ActiveProposalId
import com.jitou.app.data.local.AppointmentHistoryEntity
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.local.ReminderSettingsId
import com.jitou.app.data.local.toDomain
import com.jitou.app.data.local.toEntity
import com.jitou.app.model.AppointmentHistoryItem
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.ProposalStatus
import com.jitou.app.model.ReminderUiState
import com.jitou.app.model.fakeHaircutRecords
import com.jitou.app.model.fakeReminderState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

class JitouRepository(
    private val database: JitouDatabase,
) {
    private val haircutRecordDao = database.haircutRecordDao()
    private val proposalDao = database.appointmentProposalDao()
    private val historyDao = database.appointmentHistoryDao()
    private val reminderDao = database.reminderSettingsDao()

    val haircutRecords: Flow<List<HaircutRecord>> = haircutRecordDao
        .observeAll()
        .map { records -> records.map { it.toDomain() } }

    val activeProposal: Flow<HaircutProposal?> = proposalDao
        .observeActive(ActiveProposalId)
        .map { it?.toDomain() }

    val appointmentHistory: Flow<List<AppointmentHistoryItem>> = historyDao
        .observeAll()
        .map { items -> items.map { it.toDomain() } }

    val reminderState: Flow<ReminderUiState> = reminderDao
        .observe(ReminderSettingsId)
        .map { it?.toDomain() ?: fakeReminderState() }

    suspend fun seedDefaultsIfNeeded(today: LocalDate = LocalDate.now()) {
        if (haircutRecordDao.count() > 0) return

        haircutRecordDao.upsertAll(fakeHaircutRecords(today).map { it.toEntity() })
        proposalDao.upsert(
            HaircutProposal(
                id = ActiveProposalId,
                proposedDate = today.plusDays(7),
                proposedTime = LocalTime.of(15, 0),
                proposerName = "XX",
                status = ProposalStatus.Confirmed,
            ).toEntity(),
        )
        historyDao.upsertAll(defaultHistory(today))
        reminderDao.upsert(fakeReminderState().toEntity())
    }

    suspend fun addHaircutRecord(date: LocalDate, note: String? = null) {
        haircutRecordDao.upsert(
            HaircutRecord(
                id = "record-${System.currentTimeMillis()}",
                date = date,
                note = note,
            ).toEntity(),
        )
    }

    suspend fun setActiveProposal(proposal: HaircutProposal?) {
        if (proposal == null) {
            proposalDao.clear()
        } else {
            proposalDao.upsert(proposal.toEntity())
        }
    }

    suspend fun addAppointmentHistory(item: AppointmentHistoryItem) {
        historyDao.upsert(item.toEntity())
    }

    suspend fun setReminder(reminder: ReminderUiState) {
        reminderDao.upsert(reminder.toEntity())
    }

    private fun defaultHistory(today: LocalDate): List<AppointmentHistoryEntity> = listOf(
        AppointmentHistoryItem(
            id = "history-1",
            date = today.minusDays(31),
            time = LocalTime.of(15, 0),
            companionName = "阿杰",
            result = "已完成",
        ).toEntity(),
        AppointmentHistoryItem(
            id = "history-2",
            date = today.minusDays(62),
            time = LocalTime.of(20, 30),
            companionName = "XX",
            result = "已完成",
        ).toEntity(),
    )
}
