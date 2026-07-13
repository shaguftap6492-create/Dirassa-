package com.dirassa.feerecord.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.data.entity.StudentWithFee
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for generating PDF reports using iText.
 */
object PdfHelper {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("hi", "IN"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // iText fonts
    private val FONT_TITLE = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(21, 101, 192))
    private val FONT_SUBTITLE = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.DARK_GRAY)
    private val FONT_HEADER = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
    private val FONT_BODY = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, BaseColor.BLACK)
    private val FONT_PAID = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor(46, 125, 50))
    private val FONT_PENDING = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor(198, 40, 40))

    /**
     * Generate a monthly report PDF file.
     * @return File path of generated PDF
     */
    fun generateMonthlyReport(
        context: Context,
        month: String,
        year: Int,
        records: List<StudentWithFee>
    ): File {
        val fileName = "DIRASSA_Report_${month}_${year}_${System.currentTimeMillis()}.pdf"
        val file = File(context.filesDir, fileName)

        val document = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        // ─── Title block ───
        val titlePara = Paragraph("DIRASSA CLASSES", FONT_TITLE)
        titlePara.alignment = Element.ALIGN_CENTER
        document.add(titlePara)

        val subtitlePara = Paragraph("Fee Report — $month $year", FONT_SUBTITLE)
        subtitlePara.alignment = Element.ALIGN_CENTER
        subtitlePara.spacingAfter = 4f
        document.add(subtitlePara)

        val datePara = Paragraph("Generated: ${dateFormat.format(Date())}", FONT_BODY)
        datePara.alignment = Element.ALIGN_CENTER
        datePara.spacingAfter = 16f
        document.add(datePara)

        // ─── Summary box ───
        val totalFee = records.sumOf { it.monthlyFee }
        val totalPaid = records.filter { it.isPaid }.sumOf { it.amountPaid }
        val totalPending = records.filter { !it.isPaid }.sumOf { it.monthlyFee - it.amountPaid }

        val summaryTable = PdfPTable(4)
        summaryTable.widthPercentage = 100f
        summaryTable.spacingAfter = 16f
        val summaryHeaders = listOf("Total Students", "Total Fee", "Received", "Pending")
        val summaryValues = listOf(
            records.size.toString(),
            formatAmount(totalFee),
            formatAmount(totalPaid),
            formatAmount(totalPending)
        )
        val summaryColors = listOf(
            BaseColor(21, 101, 192),
            BaseColor(0, 131, 143),
            BaseColor(46, 125, 50),
            BaseColor(198, 40, 40)
        )
        for (i in summaryHeaders.indices) {
            val cell = PdfPCell()
            cell.backgroundColor = summaryColors[i]
            cell.setPadding(8f)
            cell.horizontalAlignment = Element.ALIGN_CENTER
            val headerP = Paragraph(summaryHeaders[i], FONT_HEADER)
            headerP.alignment = Element.ALIGN_CENTER
            val valueP = Paragraph(summaryValues[i], Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.WHITE))
            valueP.alignment = Element.ALIGN_CENTER
            cell.addElement(headerP)
            cell.addElement(valueP)
            summaryTable.addCell(cell)
        }
        document.add(summaryTable)

        // ─── Data table ───
        val table = PdfPTable(6)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(0.5f, 2.5f, 1f, 1f, 1f, 1f))

        val headers = listOf("#", "Student Name", "Class", "Monthly Fee", "Paid", "Status")
        for (h in headers) {
            val cell = PdfPCell(Phrase(h, FONT_HEADER))
            cell.backgroundColor = BaseColor(21, 101, 192)
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.setPadding(6f)
            table.addCell(cell)
        }

        records.forEachIndexed { index, rec ->
            addCell(table, (index + 1).toString(), FONT_BODY, Element.ALIGN_CENTER)
            addCell(table, rec.studentName, FONT_BODY)
            addCell(table, rec.className, FONT_BODY, Element.ALIGN_CENTER)
            addCell(table, formatAmount(rec.monthlyFee), FONT_BODY, Element.ALIGN_RIGHT)
            addCell(table, formatAmount(rec.amountPaid), FONT_BODY, Element.ALIGN_RIGHT)
            val statusFont = if (rec.isPaid) FONT_PAID else FONT_PENDING
            addCell(table, rec.status, statusFont, Element.ALIGN_CENTER)
        }
        document.add(table)

        // ─── Footer ───
        val footer = Paragraph("\nDIRASSA CLASSES — Confidential Fee Record", FONT_BODY)
        footer.alignment = Element.ALIGN_CENTER
        document.add(footer)

        document.close()
        return file
    }

    /**
     * Generate a receipt PDF for a single student's payment.
     */
    fun generateReceipt(
        context: Context,
        student: Student,
        amountPaid: Double,
        month: String,
        year: Int,
        paymentDate: String,
        remarks: String
    ): File {
        val fileName = "Receipt_${student.displayId}_${month}_${year}.pdf"
        val file = File(context.filesDir, fileName)

        val document = Document(PageSize.A5, 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        // Title
        val title = Paragraph("DIRASSA CLASSES", FONT_TITLE)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        val receiptTitle = Paragraph("FEE RECEIPT", FONT_SUBTITLE)
        receiptTitle.alignment = Element.ALIGN_CENTER
        receiptTitle.spacingAfter = 12f
        document.add(receiptTitle)

        // Receipt details table
        val detailsTable = PdfPTable(2)
        detailsTable.widthPercentage = 100f
        detailsTable.spacingAfter = 12f

        addDetailRow(detailsTable, "Receipt No.", student.displayId + "-" + System.currentTimeMillis() % 1000)
        addDetailRow(detailsTable, "Student Name", student.studentName)
        addDetailRow(detailsTable, "Father's Name", student.fatherName)
        addDetailRow(detailsTable, "Class", student.className)
        addDetailRow(detailsTable, "Mobile", student.mobile)
        addDetailRow(detailsTable, "Month", "$month $year")
        addDetailRow(detailsTable, "Monthly Fee", formatAmount(student.monthlyFee))
        addDetailRow(detailsTable, "Amount Paid", formatAmount(amountPaid))
        addDetailRow(detailsTable, "Balance", formatAmount(student.monthlyFee - amountPaid))
        addDetailRow(detailsTable, "Payment Date", paymentDate)
        if (remarks.isNotBlank()) addDetailRow(detailsTable, "Remarks", remarks)

        document.add(detailsTable)

        val statusText = if (amountPaid >= student.monthlyFee) "✓ PAID IN FULL" else "⚠ PARTIALLY PAID"
        val statusColor = if (amountPaid >= student.monthlyFee) BaseColor(46, 125, 50) else BaseColor(198, 40, 40)
        val statusPara = Paragraph(statusText, Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, statusColor))
        statusPara.alignment = Element.ALIGN_CENTER
        document.add(statusPara)

        val footer = Paragraph("\n\nThank you!\nDIRASSA CLASSES", FONT_BODY)
        footer.alignment = Element.ALIGN_CENTER
        document.add(footer)

        document.close()
        return file
    }

    /** Share a PDF file via Android share sheet */
    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    // ─── Internal helpers ───

    private fun addCell(
        table: PdfPTable,
        text: String,
        font: Font,
        alignment: Int = Element.ALIGN_LEFT
    ) {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = alignment
        cell.setPadding(4f)
        table.addCell(cell)
    }

    private fun addDetailRow(table: PdfPTable, label: String, value: String) {
        val labelCell = PdfPCell(Phrase(label, FONT_SUBTITLE))
        labelCell.setPadding(4f)
        labelCell.backgroundColor = BaseColor(240, 244, 255)
        table.addCell(labelCell)

        val valueCell = PdfPCell(Phrase(value, FONT_BODY))
        valueCell.setPadding(4f)
        table.addCell(valueCell)
    }

    private fun formatAmount(amount: Double): String = "₹%.2f".format(amount)
}
