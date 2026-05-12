package com.jitou.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HaircutRecordEntity::class,
        AppointmentProposalEntity::class,
        AppointmentHistoryEntity::class,
        ReminderSettingsEntity::class,
        SyncMetadataEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class JitouDatabase : RoomDatabase() {
    abstract fun haircutRecordDao(): HaircutRecordDao
    abstract fun appointmentProposalDao(): AppointmentProposalDao
    abstract fun appointmentHistoryDao(): AppointmentHistoryDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var instance: JitouDatabase? = null

        private val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("haircut_records", "appointment_proposals", "appointment_history", "reminder_settings").forEach { table ->
                    db.execSQL("ALTER TABLE $table ADD COLUMN remoteId TEXT")
                    db.execSQL("ALTER TABLE $table ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE $table ADD COLUMN deletedAtMillis INTEGER")
                    db.execSQL("ALTER TABLE $table ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
                }
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_metadata (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        lastSyncAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        fun getInstance(context: Context): JitouDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                JitouDatabase::class.java,
                "jitou.db",
            )
                .addMigrations(Migration1To2)
                .build()
                .also { instance = it }
        }
    }
}
