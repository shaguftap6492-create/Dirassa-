package com.dirassa.feerecord.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student entity — maps to "students" table in Room DB.
 * StudentID is auto-incremented by Room.
 */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "student_id")
    val studentId: Int = 0,

    @ColumnInfo(name = "student_name")
    val studentName: String,

    @ColumnInfo(name = "father_name")
    val fatherName: String = "",

    @ColumnInfo(name = "class_name")
    val className: String = "",

    @ColumnInfo(name = "mobile")
    val mobile: String = "",

    @ColumnInfo(name = "monthly_fee")
    val monthlyFee: Double,

    @ColumnInfo(name = "admission_date")
    val admissionDate: String = ""
) {
    /** Display-friendly ID like "STU001" */
    val displayId: String
        get() = "STU%03d".format(studentId)
}
