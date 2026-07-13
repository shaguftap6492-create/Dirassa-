package com.dirassa.feerecord.data.repository

import androidx.lifecycle.LiveData
import com.dirassa.feerecord.data.dao.StudentDao
import com.dirassa.feerecord.data.entity.Student

/**
 * Repository — mediates between ViewModel and StudentDao.
 * Keeps business logic out of the UI layer.
 */
class StudentRepository(private val studentDao: StudentDao) {

    /** LiveData list of all students for reactive UI */
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()

    /** Insert student and return the generated ID */
    suspend fun insertStudent(student: Student): Long =
        studentDao.insertStudent(student)

    /** Update student details */
    suspend fun updateStudent(student: Student) =
        studentDao.updateStudent(student)

    /** Delete student (cascades to fee records) */
    suspend fun deleteStudent(student: Student) =
        studentDao.deleteStudent(student)

    /** One-shot non-LiveData fetch */
    suspend fun getAllStudentsOnce(): List<Student> =
        studentDao.getAllStudentsOnce()

    /** Fetch a student by ID */
    suspend fun getStudentById(id: Int): Student? =
        studentDao.getStudentById(id)

    /** Search students by keyword */
    fun searchStudents(query: String): LiveData<List<Student>> =
        studentDao.searchStudents(query)

    /** Total student count */
    suspend fun getTotalStudentCount(): Int =
        studentDao.getTotalStudentCount()

    /** Sum of all monthly fees */
    suspend fun getTotalMonthlyFee(): Double =
        studentDao.getTotalMonthlyFee()

    /** Delete all students */
    suspend fun deleteAllStudents() =
        studentDao.deleteAllStudents()
}
