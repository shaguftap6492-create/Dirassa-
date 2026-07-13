package com.dirassa.feerecord

import android.app.Application
import com.dirassa.feerecord.data.database.AppDatabase
import com.dirassa.feerecord.data.repository.FeeRecordRepository
import com.dirassa.feerecord.data.repository.StudentRepository

/**
 * Application class — initializes singletons and repositories.
 * Provides a simple service-locator pattern so Activities and ViewModels
 * can obtain repositories without dependency injection frameworks.
 */
class FeeRecordApplication : Application() {

    /** Lazily initialized database */
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    /** Repositories exposed for ViewModelFactory */
    val studentRepository: StudentRepository by lazy {
        StudentRepository(database.studentDao())
    }

    val feeRecordRepository: FeeRecordRepository by lazy {
        FeeRecordRepository(database.feeRecordDao())
    }
}
