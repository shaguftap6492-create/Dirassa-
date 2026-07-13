package com.dirassa.feerecord.data.entity

/**
 * Aggregated data class used by Monthly Report queries.
 * Combines student info with fee summary for a given month/year.
 */
data class StudentWithFee(
    val studentId: Int,
    val studentName: String,
    val className: String,
    val monthlyFee: Double,
    val amountPaid: Double,
    val status: String,
    val month: String,
    val year: Int
) {
    val pendingAmount: Double
        get() = if (isPaid) 0.0 else monthlyFee - amountPaid

    val isPaid: Boolean
        get() = status == "Paid"
}
