package com.dirassa.feerecord.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * FeeRecord entity — maps to "fee_records" table.
 * Each record is unique per (student_id, month, year).
 */
@Entity(
    tableName = "fee_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["student_id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE   // Delete records when student is deleted
        )
    ],
    indices = [
        Index(value = ["student_id"]),
        Index(value = ["student_id", "month", "year"], unique = true)  // Prevent duplicates
    ]
)
data class FeeRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    val recordId: Int = 0,

    @ColumnInfo(name = "student_id")
    val studentId: Int,

    /** Month name e.g. "January" */
    @ColumnInfo(name = "month")
    val month: String,

    /** Year e.g. 2024 */
    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "amount_paid")
    val amountPaid: Double,

    /** "Paid" or "Pending" */
    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "payment_date")
    val paymentDate: String = "",

    @ColumnInfo(name = "remarks")
    val remarks: String = ""
) {
    val isPaid: Boolean get() = status == "Paid"
}
