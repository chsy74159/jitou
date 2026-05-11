package com.jitou.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HaircutRecordDao {
    @Query("SELECT * FROM haircut_records ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<HaircutRecordEntity>>

    @Query("SELECT COUNT(*) FROM haircut_records")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: HaircutRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<HaircutRecordEntity>)
}

@Dao
interface AppointmentProposalDao {
    @Query("SELECT * FROM appointment_proposals WHERE id = :id LIMIT 1")
    fun observeActive(id: String): Flow<AppointmentProposalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(proposal: AppointmentProposalEntity)

    @Query("DELETE FROM appointment_proposals")
    suspend fun clear()
}

@Dao
interface AppointmentHistoryDao {
    @Query("SELECT * FROM appointment_history ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<AppointmentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AppointmentHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AppointmentHistoryEntity>)
}

@Dao
interface ReminderSettingsDao {
    @Query("SELECT * FROM reminder_settings WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<ReminderSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ReminderSettingsEntity)
}
