package com.dirassa.feerecord.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.data.entity.StudentWithFee
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates PDF files using Android's built-in PdfDocument API.
 * No external library required.
 */
object PdfHelper {

    private const val PAGE_WIDTH  = 595   // A4 width  in points (72 dpi)
    private const val PAGE_HEIGHT = 842   // A4 height in points

    private const val MARGIN_LEFT  = 40f
    private const val MARGIN_RIGHT = 555f
    private const val LINE_HEIGHT  = 22f

    // ── Paint helpers ────────────────────────────────────────────────────────

    private fun titlePaint() = Paint().apply {
        color     = Color.parseColor("#1565C0")
        textSize  = 22f
        isFakeBoldText = true
        isAntiAlias    = true
    }

    private fun headingPaint() = Paint().apply {
        color     = Color.parseColor("#1565C0")
        textSize  = 14f
        isFakeBoldText = true
        isAntiAlias    = true
    }

    private fun labelPaint() = Paint().apply {
        color     = Color.parseColor("#555555")
        textSize  = 11f
        isAntiAlias = true
    }

    private fun valuePaint() = Paint().apply {
        color     = Color.BLACK
        textSize  = 11f
        isAntiAlias = true
    }

    private fun linePaint(color: Int = Color.parseColor("#DDDDDD")) =
        Paint().apply { this.color = color; strokeWidth = 1f }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Monthly report PDF — lists all fee records for a month/year.
     */
    fun createMonthlyReportPdf(
        context: Context,
        month: String,
        year: Int,
        records: List<StudentWithFee>
    ): File? {
        return try {
            val doc  = PdfDocument()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create())
            val c    = page.canvas
            var y    = 50f

            // Title
            c.drawText("DIRASSA CLASSES", MARGIN_LEFT, y, titlePaint()); y += 30f
            c.drawText("Monthly Fee Report — $month $year", MARGIN_LEFT, y, headingPaint()); y += 10f
            c.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, linePaint(Color.parseColor("#1565C0"))); y += 20f

            // Table header
            val hp = headingPaint().apply { textSize = 10f; color = Color.WHITE }
            val headerPaint = Paint().apply { color = Color.parseColor("#1565C0") }
            c.drawRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + 18f, headerPaint)
            c.drawText("Student ID", MARGIN_LEFT + 4,  y + 13f, hp)
            c.drawText("Name",       MARGIN_LEFT + 70, y + 13f, hp)
            c.drawText("Month",      MARGIN_LEFT + 220,y + 13f, hp)
            c.drawText("Paid (₹)",   MARGIN_LEFT + 300,y + 13f, hp)
            c.drawText("Status",     MARGIN_LEFT + 380,y + 13f, hp)
            y += 22f

            // Rows
            var totalPaid = 0.0
            records.forEachIndexed { idx, r ->
                if (y > PAGE_HEIGHT - 60) return@forEachIndexed
                val rowPaint = Paint().apply {
                    color = if (idx % 2 == 0) Color.parseColor("#F5F5F5") else Color.WHITE
                }
                c.drawRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + LINE_HEIGHT, rowPaint)
                val vp = valuePaint().apply { textSize = 10f }
                val displayId = "STU%03d".format(r.studentId)
                c.drawText(displayId,                    MARGIN_LEFT + 4,   y + 15f, vp)
                c.drawText(r.studentName.take(22),       MARGIN_LEFT + 70,  y + 15f, vp)
                c.drawText(r.month,                      MARGIN_LEFT + 220, y + 15f, vp)
                c.drawText("%.0f".format(r.amountPaid),  MARGIN_LEFT + 300, y + 15f, vp)
                val statusPaint = valuePaint().apply {
                    textSize = 10f
                    color = if (r.status == "Paid") Color.parseColor("#2E7D32")
                            else Color.parseColor("#C62828")
                }
                c.drawText(r.status, MARGIN_LEFT + 380, y + 15f, statusPaint)
                totalPaid += r.amountPaid
                y += LINE_HEIGHT
            }

            // Summary
            y += 10f
            c.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, linePaint()); y += 15f
            c.drawText("Total Records : ${records.size}", MARGIN_LEFT, y, headingPaint().apply { textSize = 11f }); y += LINE_HEIGHT
            c.drawText("Total Collected : ₹${"%.0f".format(totalPaid)}", MARGIN_LEFT, y, headingPaint().apply { textSize = 11f }); y += LINE_HEIGHT
            val paidCount    = records.count { it.status == "Paid" }
            val pendingCount = records.count { it.status == "Pending" }
            c.drawText("Paid: $paidCount   Pending: $pendingCount", MARGIN_LEFT, y,
                labelPaint().apply { textSize = 10f })

            // Footer
            val footerY = PAGE_HEIGHT - 30f
            c.drawLine(MARGIN_LEFT, footerY - 5, MARGIN_RIGHT, footerY - 5, linePaint())
            val generated = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            c.drawText("Generated: $generated", MARGIN_LEFT, footerY + 10f, labelPaint().apply { textSize = 9f })

            doc.finishPage(page)

            val fileName = "Report_${month}_${year}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF failed: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Single payment receipt PDF for one student + one fee record.
     */
    fun createReceiptPdf(
        context: Context,
        student: Student,
        record: FeeRecord
    ): File? {
        return try {
            val doc  = PdfDocument()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create())
            val c    = page.canvas
            var y    = 60f

            // Header
            c.drawText("DIRASSA CLASSES", MARGIN_LEFT, y, titlePaint()); y += 30f
            c.drawText("Payment Receipt", MARGIN_LEFT, y, headingPaint()); y += 8f
            c.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, linePaint(Color.parseColor("#1565C0"))); y += 28f

            // Receipt details
            fun row(label: String, value: String) {
                c.drawText(label, MARGIN_LEFT, y, labelPaint())
                c.drawText(value, MARGIN_LEFT + 160, y, valuePaint())
                y += LINE_HEIGHT
            }

            row("Receipt Date",  SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
            row("Student ID",    student.displayId)
            row("Student Name",  student.studentName)
            row("Father Name",   student.fatherName)
            row("Class",         student.className)
            row("Mobile",        student.mobile)
            y += 10f
            c.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, linePaint()); y += 20f

            row("Month / Year",  "${record.month} ${record.year}")
            row("Amount Paid",   "₹ ${"%.0f".format(record.amountPaid)}")
            row("Monthly Fee",   "₹ ${"%.0f".format(student.monthlyFee)}")
            val balance = student.monthlyFee - record.amountPaid
            row("Balance",       "₹ ${"%.0f".format(balance)}")
            row("Status",        record.status)
            if (record.paymentDate.isNotEmpty()) row("Payment Date", record.paymentDate)
            if (record.remarks.isNotEmpty())     row("Remarks",      record.remarks)

            y += 20f
            c.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, linePaint())

            // Footer
            val footerY = PAGE_HEIGHT - 80f
            c.drawText("Thank you!", MARGIN_LEFT, footerY, headingPaint())
            c.drawText(
                "Generated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                MARGIN_LEFT, footerY + 25f, labelPaint().apply { textSize = 9f }
            )
            c.drawText("DIRASSA CLASSES — Fee Management", MARGIN_LEFT, footerY + 40f,
                labelPaint().apply { textSize = 9f; color = Color.parseColor("#888888") })

            doc.finishPage(page)

            val fileName = "Receipt_${student.displayId}_${record.month}_${record.year}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Receipt failed: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /** Share any PDF file via Android share sheet. */
    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DIRASSA Fee Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF via"))
    }
}
