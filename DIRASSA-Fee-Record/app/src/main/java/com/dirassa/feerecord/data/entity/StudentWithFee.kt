package com.dirassa.feerecord.data.entity

import androidx.room.ColumnInfo

/**
 * Aggregated data class used by Monthly Report queries.
 * @ColumnInfo maps SQL snake_case column names to Kotlin camelCase fields.
 */
data class StudentWithFee(
    @ColumnInfo(name = "student_id")   val studentId: Int,
    @ColumnInfo(name = "student_name") val studentName: String,
    @ColumnInfo(name = "class_name")   val className: String,
    @ColumnInfo(name = "monthly_fee")  val monthlyFee: Double,
    @ColumnInfo(name = "amount_paid")  val amountPaid: Double,
    @ColumnInfo(name = "status")       val status: String,
    @ColumnInfo(name = "month")        val month: String,
    @ColumnInfo(name = "year")         val year: Int
) {
    val pendingAmount: Double
        get() = if (isPaid) 0.0 else monthlyFee - amountPaid

    val isPaid: Boolean
        get() = status == "Paid"
}
