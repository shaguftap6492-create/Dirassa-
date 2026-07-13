package com.dirassa.feerecord.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.data.entity.StudentWithFee
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for generating Excel (.xlsx) reports using Apache POI.
 */
object ExcelHelper {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Generate an Excel report with Students sheet and Fee Records sheet.
     * @return Generated File
     */
    fun generateExcelReport(
        context: Context,
        students: List<Student>,
        feeRecords: List<StudentWithFee>
    ): File {
        val fileName = "DIRASSA_Export_${System.currentTimeMillis()}.xlsx"
        val file = File(context.filesDir, fileName)

        val workbook = XSSFWorkbook()

        // ─── Styles ───
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.ROYAL_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            alignment = HorizontalAlignment.CENTER
        }

        val titleStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 14
                color = IndexedColors.DARK_BLUE.index
            }
            setFont(font)
        }

        val paidStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.GREEN.index
            }
            setFont(font)
        }

        val pendingStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.RED.index
            }
            setFont(font)
        }

        // ─── Sheet 1: Students ───
        val studentSheet = workbook.createSheet("Students")
        studentSheet.setColumnWidth(0, 4000)
        studentSheet.setColumnWidth(1, 6000)
        studentSheet.setColumnWidth(2, 6000)
        studentSheet.setColumnWidth(3, 4000)
        studentSheet.setColumnWidth(4, 5000)
        studentSheet.setColumnWidth(5, 4000)
        studentSheet.setColumnWidth(6, 5000)

        // Title row
        val titleRow = studentSheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("DIRASSA CLASSES — Student List")
        titleCell.cellStyle = titleStyle

        // Header row
        val sHeaderRow = studentSheet.createRow(1)
        val sHeaders = listOf("Student ID", "Name", "Father's Name", "Class", "Mobile", "Monthly Fee", "Admission Date")
        sHeaders.forEachIndexed { i, h ->
            val cell = sHeaderRow.createCell(i)
            cell.setCellValue(h)
            cell.cellStyle = headerStyle
        }

        // Data rows
        students.forEachIndexed { index, student ->
            val row = studentSheet.createRow(index + 2)
            row.createCell(0).setCellValue(student.displayId)
            row.createCell(1).setCellValue(student.studentName)
            row.createCell(2).setCellValue(student.fatherName)
            row.createCell(3).setCellValue(student.className)
            row.createCell(4).setCellValue(student.mobile)
            row.createCell(5).setCellValue(student.monthlyFee)
            row.createCell(6).setCellValue(student.admissionDate)
        }

        // ─── Sheet 2: Fee Records ───
        val feeSheet = workbook.createSheet("Fee Records")
        feeSheet.setColumnWidth(0, 3000)
        feeSheet.setColumnWidth(1, 6000)
        feeSheet.setColumnWidth(2, 3000)
        feeSheet.setColumnWidth(3, 4000)
        feeSheet.setColumnWidth(4, 3000)
        feeSheet.setColumnWidth(5, 4000)
        feeSheet.setColumnWidth(6, 4000)
        feeSheet.setColumnWidth(7, 4000)

        val feeTitleRow = feeSheet.createRow(0)
        val feeTitleCell = feeTitleRow.createCell(0)
        feeTitleCell.setCellValue("DIRASSA CLASSES — Fee Records")
        feeTitleCell.cellStyle = titleStyle

        val fHeaderRow = feeSheet.createRow(1)
        val fHeaders = listOf("Student ID", "Name", "Class", "Month", "Year", "Monthly Fee", "Amount Paid", "Status")
        fHeaders.forEachIndexed { i, h ->
            val cell = fHeaderRow.createCell(i)
            cell.setCellValue(h)
            cell.cellStyle = headerStyle
        }

        feeRecords.forEachIndexed { index, rec ->
            val row = feeSheet.createRow(index + 2)
            row.createCell(0).setCellValue("STU%03d".format(rec.studentId))
            row.createCell(1).setCellValue(rec.studentName)
            row.createCell(2).setCellValue(rec.className)
            row.createCell(3).setCellValue(rec.month)
            row.createCell(4).setCellValue(rec.year.toDouble())
            row.createCell(5).setCellValue(rec.monthlyFee)
            row.createCell(6).setCellValue(rec.amountPaid)
            val statusCell = row.createCell(7)
            statusCell.setCellValue(rec.status)
            statusCell.cellStyle = if (rec.isPaid) paidStyle else pendingStyle
        }

        // Write to file
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    /** Share an Excel file */
    fun shareExcel(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Excel"))
    }
}
