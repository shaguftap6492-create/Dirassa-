package com.dirassa.feerecord.ui.fee

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dirassa.feerecord.FeeRecordApplication
import com.dirassa.feerecord.R
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.databinding.ActivityFeeRecordBinding
import com.dirassa.feerecord.ui.adapter.FeeHistoryAdapter
import com.dirassa.feerecord.ui.viewmodel.FeeRecordViewModel
import com.dirassa.feerecord.ui.viewmodel.FeeRecordViewModelFactory
import com.dirassa.feerecord.ui.viewmodel.StudentViewModel
import com.dirassa.feerecord.ui.viewmodel.StudentViewModelFactory
import com.dirassa.feerecord.util.PdfHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fee Record screen:
 * - Select student (searchable spinner)
 * - Select month + year
 * - Enter amount, status, date, remarks
 * - Save / Update / Delete
 * - Shows payment history below
 */
class FeeRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeeRecordBinding
    private val studentViewModel: StudentViewModel by viewModels {
        StudentViewModelFactory((application as FeeRecordApplication).studentRepository)
    }
    private val feeViewModel: FeeRecordViewModel by viewModels {
        FeeRecordViewModelFactory((application as FeeRecordApplication).feeRecordRepository)
    }

    private lateinit var historyAdapter: FeeHistoryAdapter
    private var studentList: List<Student> = emptyList()
    private var selectedStudent: Student? = null
    private var editingRecord: FeeRecord? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeeRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.fee_record_title)

        setupStudentSpinner()
        setupMonthSpinner()
        setupYearSpinner()
        setupStatusRadio()
        setupDatePicker()
        setupHistoryRecycler()
        observeViewModel()
        setupButtons()
    }

    // ─── Setup functions ───

    private fun setupStudentSpinner() {
        studentViewModel.allStudents.observe(this) { students ->
            studentList = students
            val names = students.map { "${it.displayId} — ${it.studentName}" }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
            binding.spinnerStudent.adapter = adapter

            // Search filter
            binding.etStudentSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val query = s?.toString() ?: ""
                    val filtered = students.filter {
                        it.studentName.contains(query, ignoreCase = true) ||
                        it.displayId.contains(query, ignoreCase = true)
                    }
                    val filteredNames = filtered.map { "${it.displayId} — ${it.studentName}" }
                    binding.spinnerStudent.adapter = ArrayAdapter(
                        this@FeeRecordActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        filteredNames
                    )
                    studentList = filtered
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        binding.spinnerStudent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < studentList.size) {
                    selectedStudent = studentList[position]
                    feeViewModel.setSelectedStudent(selectedStudent!!.studentId)
                    updatePendingAmount()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupMonthSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        binding.spinnerMonth.adapter = adapter
        // Default to current month
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePendingAmount()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupYearSpinner() {
        val years = (currentYear - 3..currentYear + 1).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        binding.spinnerYear.adapter = adapter
        binding.spinnerYear.setSelection(3) // current year
    }

    private fun setupStatusRadio() {
        binding.rgStatus.setOnCheckedChangeListener { _, checkedId ->
            val isPaid = checkedId == R.id.rbPaid
            binding.tilAmountPaid.isVisible = isPaid
            if (!isPaid) {
                binding.tvPendingAmount.text = "Pending: Full fee"
            }
        }
    }

    private fun setupDatePicker() {
        val cal = Calendar.getInstance()
        binding.etPaymentDate.setText(dateFormat.format(cal.time))
        binding.etPaymentDate.isFocusable = false
        binding.etPaymentDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                binding.etPaymentDate.setText(dateFormat.format(cal.time))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupHistoryRecycler() {
        historyAdapter = FeeHistoryAdapter { record ->
            // Tap history item to load into form for editing
            loadRecordForEdit(record)
        }
        binding.rvFeeHistory.layoutManager = LinearLayoutManager(this)
        binding.rvFeeHistory.adapter = historyAdapter
    }

    private fun setupButtons() {
        binding.btnSaveRecord.setOnClickListener { validateAndSave() }
        binding.btnUpdate.setOnClickListener { validateAndUpdate() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        binding.btnGenerateReceipt.setOnClickListener { generateReceipt() }

        // Initially hide Update/Delete
        binding.btnUpdate.isVisible = false
        binding.btnDelete.isVisible = false
        binding.btnGenerateReceipt.isVisible = false
    }

    private fun observeViewModel() {
        feeViewModel.feeRecordsForStudent.observe(this) { records ->
            historyAdapter.submitList(records)
            binding.tvNoHistory.isVisible = records.isEmpty()
        }

        feeViewModel.operationResult.observe(this) { result ->
            when (result) {
                "SAVED" -> {
                    Toast.makeText(this, getString(R.string.record_saved), Toast.LENGTH_SHORT).show()
                    clearForm()
                }
                "UPDATED" -> {
                    Toast.makeText(this, getString(R.string.record_updated), Toast.LENGTH_SHORT).show()
                    clearForm()
                }
                "DELETED" -> {
                    Toast.makeText(this, getString(R.string.record_deleted), Toast.LENGTH_SHORT).show()
                    clearForm()
                }
                "DUPLICATE" -> {
                    Toast.makeText(this, getString(R.string.duplicate_entry), Toast.LENGTH_LONG).show()
                }
                else -> if (result?.startsWith("ERROR") == true) {
                    Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ─── Business logic ───

    private fun updatePendingAmount() {
        val student = selectedStudent ?: return
        val monthlyFee = student.monthlyFee
        val amountPaid = binding.etAmountPaid.text?.toString()?.toDoubleOrNull() ?: 0.0
        val pending = monthlyFee - amountPaid
        binding.tvPendingAmount.text = "Pending: ₹%.2f".format(maxOf(0.0, pending))
    }

    private fun validateAndSave() {
        val student = selectedStudent ?: run {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show()
            return
        }
        val month = binding.spinnerMonth.selectedItem?.toString() ?: return
        val yearStr = binding.spinnerYear.selectedItem?.toString() ?: return
        val year = yearStr.toInt()
        val statusId = binding.rgStatus.checkedRadioButtonId
        val status = if (statusId == R.id.rbPaid) "Paid" else "Pending"
        val amountPaid = binding.etAmountPaid.text?.toString()?.toDoubleOrNull() ?: 0.0
        val paymentDate = binding.etPaymentDate.text?.toString() ?: ""
        val remarks = binding.etRemarks.text?.toString() ?: ""

        val record = FeeRecord(
            studentId = student.studentId,
            month = month,
            year = year,
            amountPaid = amountPaid,
            status = status,
            paymentDate = paymentDate,
            remarks = remarks
        )
        feeViewModel.insertFeeRecord(record)
    }

    private fun validateAndUpdate() {
        val record = editingRecord ?: return
        val status = if (binding.rgStatus.checkedRadioButtonId == R.id.rbPaid) "Paid" else "Pending"
        val amountPaid = binding.etAmountPaid.text?.toString()?.toDoubleOrNull() ?: 0.0
        val paymentDate = binding.etPaymentDate.text?.toString() ?: ""
        val remarks = binding.etRemarks.text?.toString() ?: ""

        val updated = record.copy(
            amountPaid = amountPaid,
            status = status,
            paymentDate = paymentDate,
            remarks = remarks
        )
        feeViewModel.updateFeeRecord(updated)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Delete this fee record?")
            .setPositiveButton("Yes") { _, _ ->
                editingRecord?.let { feeViewModel.deleteFeeRecord(it) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadRecordForEdit(record: FeeRecord) {
        editingRecord = record
        binding.spinnerMonth.setSelection(months.indexOf(record.month))
        binding.etAmountPaid.setText(record.amountPaid.toString())
        binding.etPaymentDate.setText(record.paymentDate)
        binding.etRemarks.setText(record.remarks)
        if (record.isPaid) binding.rbPaid.isChecked = true else binding.rbPending.isChecked = true

        binding.btnSaveRecord.isVisible = false
        binding.btnUpdate.isVisible = true
        binding.btnDelete.isVisible = true
        binding.btnGenerateReceipt.isVisible = true
    }

    private fun clearForm() {
        editingRecord = null
        binding.etAmountPaid.text?.clear()
        binding.etRemarks.text?.clear()
        binding.rbPaid.isChecked = true
        binding.btnSaveRecord.isVisible = true
        binding.btnUpdate.isVisible = false
        binding.btnDelete.isVisible = false
        binding.btnGenerateReceipt.isVisible = false
    }

    private fun generateReceipt() {
        val student = selectedStudent ?: return
        val record = editingRecord ?: return
        lifecycleScope.launch {
            try {
                val file = PdfHelper.createReceiptPdf(
                    this@FeeRecordActivity, student, record
                )
                if (file != null) PdfHelper.sharePdf(this@FeeRecordActivity, file)
            } catch (e: Exception) {
                Toast.makeText(this@FeeRecordActivity, "Error generating receipt", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
