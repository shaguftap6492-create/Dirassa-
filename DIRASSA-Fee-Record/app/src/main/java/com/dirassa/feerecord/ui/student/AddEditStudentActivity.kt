package com.dirassa.feerecord.ui.student

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dirassa.feerecord.FeeRecordApplication
import com.dirassa.feerecord.R
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.databinding.ActivityAddEditStudentBinding
import com.dirassa.feerecord.ui.viewmodel.StudentViewModel
import com.dirassa.feerecord.ui.viewmodel.StudentViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for Adding or Editing a student.
 * Pass EXTRA_STUDENT_ID (Int) to open in edit mode.
 */
class AddEditStudentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    private lateinit var binding: ActivityAddEditStudentBinding
    private val viewModel: StudentViewModel by viewModels {
        StudentViewModelFactory((application as FeeRecordApplication).studentRepository)
    }

    private var editingStudentId: Int = -1
    private var existingStudent: Student? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Determine mode
        editingStudentId = intent.getIntExtra(EXTRA_STUDENT_ID, -1)
        val isEditMode = editingStudentId != -1

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) getString(R.string.edit_student_title)
                                   else getString(R.string.add_student_title)

        // Show auto-generated ID hint
        if (!isEditMode) {
            binding.tvStudentId.text = "Auto Generated"
        }

        // Date picker — field is non-focusable, opens picker on click
        binding.etAdmissionDate.setOnClickListener { showDatePicker() }
        binding.etAdmissionDate.isFocusable = false

        // Buttons
        binding.btnSave.setOnClickListener { validateAndSave() }
        binding.btnClear.setOnClickListener { clearForm() }
        binding.btnDelete.isVisible = isEditMode
        binding.btnDelete.setOnClickListener { confirmDelete() }

        // Observe operation results from ViewModel
        viewModel.operationResult.observe(this) { result ->
            when {
                result == "SUCCESS" -> {
                    Toast.makeText(this, getString(R.string.student_added), Toast.LENGTH_LONG).show()
                    clearForm()
                }
                result == "UPDATED" -> {
                    Toast.makeText(this, getString(R.string.student_updated), Toast.LENGTH_SHORT).show()
                    finish()
                }
                result == "DELETED" -> {
                    Toast.makeText(this, getString(R.string.student_deleted), Toast.LENGTH_SHORT).show()
                    finish()
                }
                result?.startsWith("ERROR") == true -> {
                    Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Load existing student data if in edit mode
        if (isEditMode) {
            loadStudentForEdit()
        }
    }

    private fun loadStudentForEdit() {
        lifecycleScope.launch {
            existingStudent = viewModel.getStudentById(editingStudentId)
            existingStudent?.let { student ->
                binding.tvStudentId.text = student.displayId
                binding.etStudentName.setText(student.studentName)
                binding.etFatherName.setText(student.fatherName)
                binding.etClass.setText(student.className)
                binding.etMobile.setText(student.mobile)
                binding.etMonthlyFee.setText(student.monthlyFee.toString())
                binding.etAdmissionDate.setText(student.admissionDate)
            }
        }
    }

    private fun validateAndSave() {
        // Gather inputs
        val name         = binding.etStudentName.text?.toString()?.trim() ?: ""
        val fatherName   = binding.etFatherName.text?.toString()?.trim() ?: ""
        val className    = binding.etClass.text?.toString()?.trim() ?: ""
        val mobile       = binding.etMobile.text?.toString()?.trim() ?: ""
        val feeStr       = binding.etMonthlyFee.text?.toString()?.trim() ?: ""
        val admissionDate = binding.etAdmissionDate.text?.toString()?.trim() ?: ""

        // ─── Validation ───
        if (name.isEmpty()) {
            binding.tilStudentName.error = getString(R.string.error_name_required)
            binding.etStudentName.requestFocus()
            return
        } else {
            binding.tilStudentName.error = null
        }

        if (feeStr.isEmpty()) {
            binding.tilMonthlyFee.error = getString(R.string.error_fee_required)
            binding.etMonthlyFee.requestFocus()
            return
        }

        val fee = feeStr.toDoubleOrNull()
        if (fee == null || fee < 0) {
            binding.tilMonthlyFee.error = getString(R.string.error_fee_numeric)
            binding.etMonthlyFee.requestFocus()
            return
        } else {
            binding.tilMonthlyFee.error = null
        }

        if (mobile.isNotEmpty() && mobile.length != 10) {
            binding.tilMobile.error = getString(R.string.error_mobile_length)
            binding.etMobile.requestFocus()
            return
        } else {
            binding.tilMobile.error = null
        }

        // ─── Build entity ───
        val student = if (editingStudentId != -1) {
            // Edit mode — preserve original ID
            Student(
                studentId = editingStudentId,
                studentName = name,
                fatherName = fatherName,
                className = className,
                mobile = mobile,
                monthlyFee = fee,
                admissionDate = admissionDate
            )
        } else {
            // Add mode — Room auto-generates studentId
            Student(
                studentName = name,
                fatherName = fatherName,
                className = className,
                mobile = mobile,
                monthlyFee = fee,
                admissionDate = admissionDate
            )
        }

        if (editingStudentId != -1) {
            viewModel.updateStudent(student)
        } else {
            viewModel.insertStudent(student)
        }
    }

    private fun clearForm() {
        binding.etStudentName.text?.clear()
        binding.etFatherName.text?.clear()
        binding.etClass.text?.clear()
        binding.etMobile.text?.clear()
        binding.etMonthlyFee.text?.clear()
        binding.etAdmissionDate.text?.clear()
        binding.tilStudentName.error = null
        binding.tilMonthlyFee.error = null
        binding.tilMobile.error = null
        binding.etStudentName.requestFocus()
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                binding.etAdmissionDate.setText(dateFormat.format(cal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Student")
            .setMessage("Delete this student? All their fee records will also be deleted.")
            .setPositiveButton("Yes") { _, _ ->
                existingStudent?.let { viewModel.deleteStudent(it) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
