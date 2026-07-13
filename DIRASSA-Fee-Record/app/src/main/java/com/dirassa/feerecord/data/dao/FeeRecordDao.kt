package com.dirassa.feerecord.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.StudentWithFee

/**
 * Data Access Object for FeeRecords table.
 */
@Dao
interface FeeRecordDao {

    /** Insert a new fee record */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFeeRecord(record: FeeRecord): Long

    /** Update existing fee record */
    @Update
    suspend fun updateFeeRecord(record: FeeRecord)

    /** Delete a fee record */
    @Delete
    suspend fun deleteFeeRecord(record: FeeRecord)

    /** Get all fee records for a specific student ordered by year, month */
    @Query("""
        SELECT * FROM fee_records 
        WHERE student_id = :studentId 
        ORDER BY year DESC, 
            CASE month
                WHEN 'January' THEN 1 WHEN 'February' THEN 2 WHEN 'March' THEN 3
                WHEN 'April' THEN 4 WHEN 'May' THEN 5 WHEN 'June' THEN 6
                WHEN 'July' THEN 7 WHEN 'August' THEN 8 WHEN 'September' THEN 9
                WHEN 'October' THEN 10 WHEN 'November' THEN 11 WHEN 'December' THEN 12
            END DESC
    """)
    fun getFeeRecordsForStudent(studentId: Int): LiveData<List<FeeRecord>>

    /** Check if a record already exists for student + month + year */
    @Query("""
        SELECT COUNT(*) FROM fee_records 
        WHERE student_id = :studentId AND month = :month AND year = :year
    """)
    suspend fun checkDuplicate(studentId: Int, month: String, year: Int): Int

    /** Get a specific fee record by ID */
    @Query("SELECT * FROM fee_records WHERE record_id = :recordId")
    suspend fun getFeeRecordById(recordId: Int): FeeRecord?

    /** Get total amount received (Paid) for a month/year */
    @Query("""
        SELECT COALESCE(SUM(amount_paid), 0) FROM fee_records 
        WHERE month = :month AND year = :year AND status = 'Paid'
    """)
    suspend fun getTotalReceived(month: String, year: Int): Double

    /** Get total pending fee for a month/year */
    @Query("""
        SELECT COALESCE(SUM(s.monthly_fee - f.amount_paid), 0) 
        FROM fee_records f
        JOIN students s ON f.student_id = s.student_id
        WHERE f.month = :month AND f.year = :year AND f.status = 'Pending'
    """)
    suspend fun getTotalPending(month: String, year: Int): Double

    /**
     * Get all students joined with their fee record for a specific month/year.
     * Students without a record for that month are NOT included.
     */
    @Query("""
        SELECT s.student_id, s.student_name, s.class_name, s.monthly_fee,
               f.amount_paid, f.status, f.month, f.year
        FROM fee_records f
        JOIN students s ON f.student_id = s.student_id
        WHERE f.month = :month AND f.year = :year
        ORDER BY s.student_name ASC
    """)
    fun getStudentsWithFeeForMonth(month: String, year: Int): LiveData<List<StudentWithFee>>

    /**
     * Get students with fee filtered by status for a month/year.
     */
    @Query("""
        SELECT s.student_id, s.student_name, s.class_name, s.monthly_fee,
               f.amount_paid, f.status, f.month, f.year
        FROM fee_records f
        JOIN students s ON f.student_id = s.student_id
        WHERE f.month = :month AND f.year = :year AND f.status = :status
        ORDER BY s.student_name ASC
    """)
    fun getStudentsWithFeeFiltered(month: String, year: Int, status: String): LiveData<List<StudentWithFee>>

    /** All fee records across all months (for export) */
    @Query("""
        SELECT s.student_id, s.student_name, s.class_name, s.monthly_fee,
               f.amount_paid, f.status, f.month, f.year
        FROM fee_records f
        JOIN students s ON f.student_id = s.student_id
        ORDER BY f.year DESC, s.student_name ASC
    """)
    suspend fun getAllStudentsWithFeeOnce(): List<StudentWithFee>

    /** Delete all fee records (used in reset) */
    @Query("DELETE FROM fee_records")
    suspend fun deleteAllFeeRecords()
}
