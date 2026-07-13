package com.dirassa.feerecord.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.data.entity.StudentWithFee
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exports student and fee data as CSV files (Excel opens CSV files directly).
 * No external library needed — pure Kotlin/Java IO.
 */
object ExcelHelper {

    /**
     * Creates a CSV export with two sections:
     *  1. Students list
     *  2. Fee records list
     *
     * @return the generated CSV File, or null on error
     */
    fun createExportCsv(
        context: Context,
        students: List<Student>,
        feeRecords: List<StudentWithFee>
    ): File? {
        return try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "DIRASSA_Export_$dateStr.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            FileWriter(file, Charsets.UTF_8).use { writer ->

                // ── STUDENTS SHEET ──────────────────────────────────────────
                writer.write("STUDENTS LIST\n")
                writer.write("Student ID,Name,Father Name,Class,Mobile,Monthly Fee,Admission Date\n")
                for (s in students) {
                    writer.write(
                        "${escapeCsv(s.displayId)}," +
                        "${escapeCsv(s.studentName)}," +
                        "${escapeCsv(s.fatherName)}," +
                        "${escapeCsv(s.className)}," +
                        "${escapeCsv(s.mobile)}," +
                        "${s.monthlyFee}," +
                        "${escapeCsv(s.admissionDate)}\n"
                    )
                }

                writer.write("\n\n")

                // ── FEE RECORDS SHEET ────────────────────────────────────────
                writer.write("FEE RECORDS\n")
                writer.write("Student ID,Name,Class,Month,Year,Amount Paid,Monthly Fee,Status\n")
                for (r in feeRecords) {
                    val displayId = "STU%03d".format(r.studentId)
                    writer.write(
                        "${escapeCsv(displayId)}," +
                        "${escapeCsv(r.studentName)}," +
                        "${escapeCsv(r.className)}," +
                        "${escapeCsv(r.month)}," +
                        "${r.year}," +
                        "${r.amountPaid}," +
                        "${r.monthlyFee}," +
                        "${escapeCsv(r.status)}\n"
                    )
                }

                // ── SUMMARY ──────────────────────────────────────────────────
                writer.write("\n\nSUMMARY\n")
                writer.write("Total Students,${students.size}\n")
                writer.write("Total Fee Records,${feeRecords.size}\n")
                val totalCollected = feeRecords.sumOf { it.amountPaid }
                writer.write("Total Amount Collected,${totalCollected}\n")
                writer.write(
                    "Exported On,${
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    }\n"
                )
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /** Share a CSV file via Android share sheet. */
    fun shareCsv(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DIRASSA Fee Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Export via"))
    }

    /** Wrap a field in quotes and escape any internal quotes for CSV safety. */
    private fun escapeCsv(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
