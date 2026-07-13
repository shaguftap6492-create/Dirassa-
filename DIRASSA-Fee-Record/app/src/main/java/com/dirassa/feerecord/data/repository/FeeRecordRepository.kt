package com.dirassa.feerecord.data.repository

import androidx.lifecycle.LiveData
import com.dirassa.feerecord.data.dao.FeeRecordDao
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.StudentWithFee

/**
 * Repository for fee record operations.
 */
class FeeRecordRepository(private val feeRecordDao: FeeRecordDao) {

    /** Insert a fee record. Returns new row ID or throws on duplicate. */
    suspend fun insertFeeRecord(record: FeeRecord): Long =
        feeRecordDao.insertFeeRecord(record)

    /** Update an existing fee record */
    suspend fun updateFeeRecord(record: FeeRecord) =
        feeRecordDao.updateFeeRecord(record)

    /** Delete a fee record */
    suspend fun deleteFeeRecord(record: FeeRecord) =
        feeRecordDao.deleteFeeRecord(record)

    /** Get all fee records for a student as LiveData */
    fun getFeeRecordsForStudent(studentId: Int): LiveData<List<FeeRecord>> =
        feeRecordDao.getFeeRecordsForStudent(studentId)

    /**
     * Check if a record already exists for student + month + year.
     * Returns true if duplicate.
     */
    suspend fun isDuplicate(studentId: Int, month: String, year: Int): Boolean =
        feeRecordDao.checkDuplicate(studentId, month, year) > 0

    /** Fetch a specific fee record by ID */
    suspend fun getFeeRecordById(id: Int): FeeRecord? =
        feeRecordDao.getFeeRecordById(id)

    /** Total amount received for a month/year */
    suspend fun getTotalReceived(month: String, year: Int): Double =
        feeRecordDao.getTotalReceived(month, year)

    /** Total pending amount for a month/year */
    suspend fun getTotalPending(month: String, year: Int): Double =
        feeRecordDao.getTotalPending(month, year)

    /** Students with fee records for a month/year */
    fun getStudentsWithFeeForMonth(month: String, year: Int): LiveData<List<StudentWithFee>> =
        feeRecordDao.getStudentsWithFeeForMonth(month, year)

    /** Filtered by status */
    fun getStudentsWithFeeFiltered(month: String, year: Int, status: String): LiveData<List<StudentWithFee>> =
        feeRecordDao.getStudentsWithFeeFiltered(month, year, status)

    /** All data for export */
    suspend fun getAllStudentsWithFeeOnce(): List<StudentWithFee> =
        feeRecordDao.getAllStudentsWithFeeOnce()

    /** Delete all fee records */
    suspend fun deleteAllFeeRecords() =
        feeRecordDao.deleteAllFeeRecords()
}
