package com.jitou.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HaircutRecordEntity::class,
        AppointmentProposalEntity::class,
        AppointmentHistoryEntity::class,
        ReminderSettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class JitouDatabase : RoomDatabase() {
    abstract fun haircutRecordDao(): HaircutRecordDao
    abstract fun appointmentProposalDao(): AppointmentProposalDao
    abstract fun appointmentHistoryDao(): AppointmentHistoryDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao

    companion object {
        @Volatile
        private var instance: JitouDatabase? = null

        fun getInstance(context: Context): JitouDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                JitouDatabase::class.java,
                "jitou.db",
            ).build().also { instance = it }
        }
    }
}
