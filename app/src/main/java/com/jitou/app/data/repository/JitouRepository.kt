package com.jitou.app.data.repository

import com.jitou.app.data.local.ActiveProposalId
import com.jitou.app.data.local.AppointmentHistoryEntity
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.local.ReminderSettingsId
import com.jitou.app.data.local.SyncState
import com.jitou.app.data.local.nowMillis
import com.jitou.app.data.local.toDomain
import com.jitou.app.data.local.toEntity
import com.jitou.app.data.sync.SyncRepository
import com.jitou.app.model.AppointmentHistoryItem
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.ProposalStatus
import com.jitou.app.model.ReminderUiState
import com.jitou.app.model.fakeHaircutRecords
import com.jitou.app.model.fakeReminderState
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JitouRepository(
    private val database: JitouDatabase,
    private val syncRepository: SyncRepository? = null,
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

    suspend fun syncAll() {
        runCatching { syncRepository?.syncAll() }
    }

    suspend fun refreshRemoteChanges() {
        runCatching { syncRepository?.refreshRemoteChanges() }
    }

    suspend fun friendName(): String? = runCatching {
        syncRepository?.friendNameForCurrentUser()
    }.getOrNull()

    suspend fun profileNickname(): String? = runCatching {
        syncRepository?.currentUserNickname()
    }.getOrNull()

    suspend fun updateProfileNickname(nickname: String): String? = runCatching {
        syncRepository?.updateCurrentUserNickname(nickname)
    }.getOrNull()

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
            ).toEntity(
                updatedAtMillis = nowMillis(),
                syncState = SyncState.PENDING_CREATE,
            ),
        )
        runCatching { syncRepository?.pushPendingLocal() }
    }

    suspend fun completeActiveProposal(note: String? = null) {
        val active = proposalDao.getActive(ActiveProposalId) ?: return
        val updatedAt = nowMillis()
        haircutRecordDao.upsert(
            HaircutRecord(
                id = "record-${System.currentTimeMillis()}",
                date = LocalDate.ofEpochDay(active.proposedDateEpochDay),
                note = note,
            ).toEntity(
                updatedAtMillis = updatedAt,
                syncState = SyncState.PENDING_CREATE,
            ),
        )
        proposalDao.upsert(
            active.copy(
                updatedAtMillis = updatedAt,
                deletedAtMillis = null,
                syncState = SyncState.PENDING_COMPLETE.name,
            ),
        )
        runCatching { syncRepository?.pushPendingLocal() }
    }

    suspend fun setActiveProposal(proposal: HaircutProposal?) {
        if (proposal == null) {
            val active = proposalDao.getActive(ActiveProposalId)
            if (active?.remoteId == null) {
                proposalDao.clear()
            } else {
                proposalDao.upsert(
                    active.copy(
                        updatedAtMillis = nowMillis(),
                        deletedAtMillis = nowMillis(),
                        syncState = SyncState.PENDING_DELETE.name,
                    ),
                )
            }
        } else {
            val existing = proposalDao.getActive(ActiveProposalId)
            proposalDao.upsert(
                proposal.toEntity(
                    remoteId = existing?.remoteId,
                    updatedAtMillis = nowMillis(),
                    syncState = if (existing?.remoteId == null) {
                        SyncState.PENDING_CREATE
                    } else {
                        SyncState.PENDING_UPDATE
                    },
                ),
            )
        }
        runCatching { syncRepository?.pushPendingLocal() }
    }

    suspend fun addAppointmentHistory(item: AppointmentHistoryItem) {
        historyDao.upsert(item.toEntity(syncState = SyncState.SYNCED))
    }

    suspend fun setReminder(reminder: ReminderUiState) {
        val existing = reminderDao.get(ReminderSettingsId)
        val pendingState = SyncState.PENDING_UPDATE
        reminderDao.upsert(
            reminder.toEntity(
                remoteId = existing?.remoteId,
                updatedAtMillis = nowMillis(),
                syncState = pendingState,
            ),
        )
        runCatching { syncRepository?.pushPendingLocal() }
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
