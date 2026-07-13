package com.dirassa.feerecord.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dirassa.feerecord.data.dao.FeeRecordDao
import com.dirassa.feerecord.data.dao.StudentDao
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.Student

/**
 * Room Database — single instance (Singleton pattern).
 * Database file: dirassa_fee_record.db
 */
@Database(
    entities = [Student::class, FeeRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun feeRecordDao(): FeeRecordDao

    companion object {
        const val DATABASE_NAME = "dirassa_fee_record.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         * Thread-safe via double-checked locking.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** Call this when restoring from backup to force re-open */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
