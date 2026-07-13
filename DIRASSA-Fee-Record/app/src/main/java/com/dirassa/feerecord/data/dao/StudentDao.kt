package com.dirassa.feerecord.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dirassa.feerecord.data.entity.Student

/**
 * Data Access Object for Students table.
 * All DB operations are suspend functions for coroutine support.
 */
@Dao
interface StudentDao {

    /** Insert a new student; returns the new row ID */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: Student): Long

    /** Update existing student record */
    @Update
    suspend fun updateStudent(student: Student)

    /** Delete a student (also deletes their fee records via CASCADE) */
    @Delete
    suspend fun deleteStudent(student: Student)

    /** Get all students ordered by name — returns LiveData for reactive UI */
    @Query("SELECT * FROM students ORDER BY student_name ASC")
    fun getAllStudents(): LiveData<List<Student>>

    /** One-shot fetch for all students (non-reactive) */
    @Query("SELECT * FROM students ORDER BY student_name ASC")
    suspend fun getAllStudentsOnce(): List<Student>

    /** Get student by ID */
    @Query("SELECT * FROM students WHERE student_id = :id")
    suspend fun getStudentById(id: Int): Student?

    /** Search students by name or father name */
    @Query("""
        SELECT * FROM students 
        WHERE student_name LIKE '%' || :query || '%' 
           OR father_name LIKE '%' || :query || '%'
           OR class_name LIKE '%' || :query || '%'
        ORDER BY student_name ASC
    """)
    fun searchStudents(query: String): LiveData<List<Student>>

    /** Count total number of students */
    @Query("SELECT COUNT(*) FROM students")
    suspend fun getTotalStudentCount(): Int

    /** Sum of all monthly fees */
    @Query("SELECT COALESCE(SUM(monthly_fee), 0) FROM students")
    suspend fun getTotalMonthlyFee(): Double

    /** Delete all students (used in reset) */
    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}
