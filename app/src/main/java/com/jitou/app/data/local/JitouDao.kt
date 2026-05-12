package com.jitou.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HaircutRecordDao {
    @Query("SELECT * FROM haircut_records WHERE deletedAtMillis IS NULL ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<HaircutRecordEntity>>

    @Query("SELECT * FROM haircut_records WHERE deletedAtMillis IS NULL ORDER BY dateEpochDay ASC")
    suspend fun getAll(): List<HaircutRecordEntity>

    @Query("SELECT COUNT(*) FROM haircut_records")
    suspend fun count(): Int

    @Query("SELECT * FROM haircut_records WHERE syncState != 'SYNCED'")
    suspend fun pendingSync(): List<HaircutRecordEntity>

    @Query("SELECT * FROM haircut_records WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): HaircutRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: HaircutRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<HaircutRecordEntity>)
}

@Dao
interface AppointmentProposalDao {
    @Query("SELECT * FROM appointment_proposals WHERE id = :id AND deletedAtMillis IS NULL LIMIT 1")
    fun observeActive(id: String): Flow<AppointmentProposalEntity?>

    @Query("SELECT * FROM appointment_proposals WHERE id = :id LIMIT 1")
    suspend fun getActive(id: String): AppointmentProposalEntity?

    @Query("SELECT * FROM appointment_proposals WHERE syncState != 'SYNCED'")
    suspend fun pendingSync(): List<AppointmentProposalEntity>

    @Query("SELECT * FROM appointment_proposals WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): AppointmentProposalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(proposal: AppointmentProposalEntity)

    @Query("DELETE FROM appointment_proposals")
    suspend fun clear()
}

@Dao
interface AppointmentHistoryDao {
    @Query("SELECT * FROM appointment_history WHERE deletedAtMillis IS NULL ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<AppointmentHistoryEntity>>

    @Query("SELECT * FROM appointment_history WHERE syncState != 'SYNCED'")
    suspend fun pendingSync(): List<AppointmentHistoryEntity>

    @Query("SELECT * FROM appointment_history WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): AppointmentHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AppointmentHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AppointmentHistoryEntity>)
}

@Dao
interface ReminderSettingsDao {
    @Query("SELECT * FROM reminder_settings WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<ReminderSettingsEntity?>

    @Query("SELECT * FROM reminder_settings WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ReminderSettingsEntity?

    @Query("SELECT * FROM reminder_settings WHERE syncState != 'SYNCED'")
    suspend fun pendingSync(): List<ReminderSettingsEntity>

    @Query("SELECT * FROM reminder_settings WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): ReminderSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ReminderSettingsEntity)
}

@Dao
interface SyncMetadataDao {
    @Query("SELECT lastSyncAtMillis FROM sync_metadata WHERE `key` = :key LIMIT 1")
    suspend fun lastSyncAt(key: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadataEntity)
}
