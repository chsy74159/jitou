package com.jitou.app.data.sync

import androidx.room.withTransaction
import com.jitou.app.data.local.ActiveProposalId
import com.jitou.app.data.local.AppointmentHistoryEntity
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.local.ReminderSettingsId
import com.jitou.app.data.local.SyncMetadataEntity
import com.jitou.app.data.local.SyncState
import com.jitou.app.data.local.nowMillis
import com.jitou.app.data.remote.RemoteHaircutRecord
import com.jitou.app.data.remote.RemoteJointHaircutPlan
import com.jitou.app.data.remote.RemoteReminderPreference
import com.jitou.app.data.remote.SupabaseRemoteDataSource
import com.jitou.app.data.remote.toRemoteTimestamp
import java.util.UUID

class SyncRepository(
    private val database: JitouDatabase,
    private val remote: SupabaseRemoteDataSource,
) {
    private val recordsDao = database.haircutRecordDao()
    private val proposalDao = database.appointmentProposalDao()
    private val historyDao = database.appointmentHistoryDao()
    private val reminderDao = database.reminderSettingsDao()
    private val syncMetadataDao = database.syncMetadataDao()

    suspend fun syncAll() {
        remote.currentUserId() ?: return
        pullRemoteChanges()
        pushPendingLocal()
    }

    suspend fun refreshRemoteChanges() {
        remote.currentUserId() ?: return
        pullRemoteChanges()
    }

    suspend fun currentUserNickname(): String? = remote.fetchCurrentProfile()
        ?.nickname
        ?.takeIf { it.isNotBlank() }

    suspend fun updateCurrentUserNickname(nickname: String): String? = remote.upsertCurrentProfileNickname(nickname)
        ?.nickname
        ?.takeIf { it.isNotBlank() }

    suspend fun friendNameForCurrentUser(): String? {
        val currentUserId = remote.currentUserId() ?: return null
        val members = remote.fetchPairMembers()
        val currentPairId = members.firstOrNull { it.userId == currentUserId }?.pairId ?: return null
        val friendMember = members
            .firstOrNull { it.pairId == currentPairId && it.userId != currentUserId }
            ?: return null
        val profileName = remote.fetchProfilesUpdatedSince(0L)
            .firstOrNull { it.id == friendMember.userId }
            ?.nickname
            ?.takeIf { it.isNotBlank() }
        return profileName ?: friendMember.displayName.takeIf { it.isNotBlank() }
    }

    suspend fun pushPendingLocal() {
        val currentUserId = remote.currentUserId() ?: return
        val members = remote.fetchPairMembers()
        val pairId = members.firstOrNull { it.userId == currentUserId }?.pairId

        recordsDao.pendingSync().forEach { local ->
            val remoteId = local.remoteId ?: UUID.randomUUID().toString()
            val deletedAt = local.deletedAtMillis?.toRemoteTimestamp()
            remote.upsertHaircutRecord(
                RemoteHaircutRecord(
                    id = remoteId,
                    userId = currentUserId,
                    haircutDate = java.time.LocalDate.ofEpochDay(local.dateEpochDay).toString(),
                    note = local.note,
                    updatedAt = local.updatedAtMillis.toRemoteTimestamp(),
                    deletedAt = deletedAt,
                ),
            )
            recordsDao.upsert(
                local.copy(
                    remoteId = remoteId,
                    syncState = SyncState.SYNCED.name,
                ),
            )
        }

        proposalDao.pendingSync().forEach { local ->
            if (pairId == null) return@forEach
            val remoteId = local.remoteId ?: UUID.randomUUID().toString()
            val localUpdatedAt = local.updatedAtMillis.toRemoteTimestamp()
            val status = when (SyncState.valueOf(local.syncState)) {
                SyncState.PENDING_DELETE -> "cancelled"
                else -> when (local.status) {
                    "Confirmed" -> "confirmed"
                    else -> "pending"
                }
            }
            if (status == "cancelled") {
                if (local.remoteId != null) {
                    remote.cancelJointPlan(local.remoteId, currentUserId, localUpdatedAt)
                }
                proposalDao.clear()
            } else if (status == "confirmed" && local.remoteId != null) {
                remote.confirmJointPlan(local.remoteId, currentUserId, localUpdatedAt)
                proposalDao.upsert(local.copy(syncState = SyncState.SYNCED.name, deletedAtMillis = null))
            } else {
                remote.upsertJointPlan(
                    RemoteJointHaircutPlan(
                        id = remoteId,
                        pairId = pairId,
                        proposerId = currentUserId,
                        proposedAt = proposedAtTimestamp(local.proposedDateEpochDay, local.proposedMinuteOfDay),
                        status = status,
                        cancelledBy = if (status == "cancelled") currentUserId else null,
                        cancelledAt = if (status == "cancelled") localUpdatedAt else null,
                        reminderDaysBefore = local.reminderDaysBefore,
                        updatedAt = localUpdatedAt,
                    ),
                )
                proposalDao.upsert(
                    local.copy(
                        remoteId = remoteId,
                        syncState = SyncState.SYNCED.name,
                        deletedAtMillis = null,
                    ),
                )
            }
        }

        reminderDao.pendingSync().forEach { local ->
            remote.upsertReminderPreference(
                RemoteReminderPreference(
                    userId = currentUserId,
                    enabled = local.enabled,
                    daysBefore = local.daysBefore,
                    reminderTime = minuteOfDayToTimeString(local.minuteOfDay),
                    updatedAt = local.updatedAtMillis.toRemoteTimestamp(),
                ),
            )
            reminderDao.upsert(
                local.copy(
                    remoteId = currentUserId,
                    syncState = SyncState.SYNCED.name,
                    deletedAtMillis = null,
                ),
            )
        }
    }

    private suspend fun pullRemoteChanges() {
        pullProfiles()
        val members = pullPairMembers()
        pullPairs()
        pullHaircutRecords()
        pullJointPlans(members)
        pullReminderPreferences()
    }

    private suspend fun pullProfiles() {
        val since = syncMetadataDao.lastSyncAt(SyncKeyProfiles) ?: 0L
        remote.fetchProfilesUpdatedSince(since)
        syncMetadataDao.upsert(SyncMetadataEntity(SyncKeyProfiles, nowMillis()))
    }

    private suspend fun pullPairs() {
        val since = syncMetadataDao.lastSyncAt(SyncKeyPairs) ?: 0L
        remote.fetchPairsUpdatedSince(since)
        syncMetadataDao.upsert(SyncMetadataEntity(SyncKeyPairs, nowMillis()))
    }

    private suspend fun pullPairMembers(): List<com.jitou.app.data.remote.RemoteHaircutPairMember> {
        val members = remote.fetchPairMembers()
        syncMetadataDao.upsert(SyncMetadataEntity(SyncKeyPairMembers, nowMillis()))
        return members
    }

    private suspend fun pullHaircutRecords() {
        val since = syncMetadataDao.lastSyncAt(SyncKeyRecords) ?: 0L
        val remoteRecords = remote.fetchHaircutRecordsUpdatedSince(since)

        database.withTransaction {
            remoteRecords.forEach { record ->
                val remoteUpdated = parseRemoteTimestamp(record.updatedAt) ?: return@forEach
                val local = recordsDao.findByRemoteId(record.id)
                val shouldKeepLocal = local != null &&
                    local.syncState != SyncState.SYNCED.name &&
                    local.updatedAtMillis > remoteUpdated

                if (!shouldKeepLocal) {
                    recordsDao.upsert(record.toLocalEntity(existingId = local?.id))
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncKeyRecords, nowMillis()))
        }
    }

    private suspend fun pullJointPlans(members: List<com.jitou.app.data.remote.RemoteHaircutPairMember>) {
        val currentUserId = remote.currentUserId() ?: return
        val since = syncMetadataDao.lastSyncAt(SyncKeyPlans) ?: 0L
        val plans = remote.fetchJointPlansUpdatedSince(since)

        database.withTransaction {
            plans.sortedBy { parseRemoteTimestamp(it.updatedAt) ?: 0L }.forEach { plan ->
                val remoteUpdated = parseRemoteTimestamp(plan.updatedAt) ?: return@forEach
                val local = proposalDao.findByRemoteId(plan.id) ?: proposalDao.getActive(ActiveProposalId)
                val shouldKeepLocal = local != null &&
                    local.syncState != SyncState.SYNCED.name &&
                    local.updatedAtMillis > remoteUpdated

                if (!shouldKeepLocal) {
                    when (plan.status) {
                        "pending", "confirmed" -> proposalDao.upsert(
                            plan.toActiveProposalEntity(
                                currentUserId = currentUserId,
                                members = members,
                            ),
                        )
                        "completed", "cancelled" -> {
                            if (local?.remoteId == plan.id || local?.id == ActiveProposalId) {
                                proposalDao.clear()
                            }
                            historyDao.upsert(plan.toHistoryEntity(members, currentUserId))
                        }
                    }
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncKeyPlans, nowMillis()))
        }
    }

    private suspend fun pullReminderPreferences() {
        val since = syncMetadataDao.lastSyncAt(SyncKeyReminders) ?: 0L
        val preferences = remote.fetchReminderPreferencesUpdatedSince(since)

        database.withTransaction {
            preferences.forEach { preference ->
                val remoteUpdated = parseRemoteTimestamp(preference.updatedAt) ?: return@forEach
                val local = reminderDao.findByRemoteId(preference.userId)
                val shouldKeepLocal = local != null &&
                    local.syncState != SyncState.SYNCED.name &&
                    local.updatedAtMillis > remoteUpdated

                if (!shouldKeepLocal) {
                    reminderDao.upsert(preference.toLocalEntity())
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncKeyReminders, nowMillis()))
        }
    }

    private fun RemoteJointHaircutPlan.toHistoryEntity(
        members: List<com.jitou.app.data.remote.RemoteHaircutPairMember>,
        currentUserId: String,
    ): AppointmentHistoryEntity {
        val proposedDateTime = java.time.OffsetDateTime.parse(proposedAt).toLocalDateTime()
        val collaboratorName = members
            .firstOrNull { it.pairId == pairId && it.userId != currentUserId }
            ?.displayName
            ?: "XX"
        return AppointmentHistoryEntity(
            id = "plan-$id",
            dateEpochDay = proposedDateTime.toLocalDate().toEpochDay(),
            minuteOfDay = proposedDateTime.toLocalTime().hour * 60 + proposedDateTime.toLocalTime().minute,
            companionName = collaboratorName,
            result = if (status == "completed") "已完成" else "已取消",
            remoteId = id,
            updatedAtMillis = parseRemoteTimestamp(updatedAt) ?: 0L,
            deletedAtMillis = null,
            syncState = SyncState.SYNCED.name,
        )
    }

    private fun minuteOfDayToTimeString(minuteOfDay: Int): String {
        val hour = minuteOfDay / 60
        val minute = minuteOfDay % 60
        return "%02d:%02d:00".format(hour, minute)
    }

    private companion object {
        const val SyncKeyProfiles = "profiles"
        const val SyncKeyPairs = "haircut_pairs"
        const val SyncKeyPairMembers = "haircut_pair_members"
        const val SyncKeyRecords = "haircut_records"
        const val SyncKeyPlans = "joint_haircut_plans"
        const val SyncKeyReminders = "reminder_preferences"
    }
}
