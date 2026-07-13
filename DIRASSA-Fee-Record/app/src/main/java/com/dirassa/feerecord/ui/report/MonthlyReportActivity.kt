package com.dirassa.feerecord.ui.report

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dirassa.feerecord.FeeRecordApplication
import com.dirassa.feerecord.R
import com.dirassa.feerecord.data.entity.StudentWithFee
import com.dirassa.feerecord.databinding.ActivityMonthlyReportBinding
import com.dirassa.feerecord.ui.adapter.ReportAdapter
import com.dirassa.feerecord.ui.viewmodel.ReportViewModel
import com.dirassa.feerecord.ui.viewmodel.ReportViewModelFactory
import com.dirassa.feerecord.util.PdfHelper
import kotlinx.coroutines.launch
import java.util.*

/**
 * Monthly Report screen with dashboard cards and filterable student list.
 */
class MonthlyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlyReportBinding
    private lateinit var reportAdapter: ReportAdapter

    private val viewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(
            (application as FeeRecordApplication).studentRepository,
            (application as FeeRecordApplication).feeRecordRepository
        )
    }

    private val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentListLiveData: LiveData<List<StudentWithFee>>? = null
    private var allRecords: List<StudentWithFee> = emptyList()
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.monthly_report_title)

        setupMonthSpinner()
        setupYearSpinner()
        setupStatusFilter()
        setupRecycler()
        setupSearch()
        observeViewModel()
        setupExportButtons()

        // Initial load
        refreshReport()
    }

    private fun setupMonthSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        binding.spinnerMonth.adapter = adapter
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.setMonth(months[position])
                refreshReport()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupYearSpinner() {
        val years = (currentYear - 3..currentYear + 1).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        binding.spinnerYear.adapter = adapter
        binding.spinnerYear.setSelection(3)
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.setYear(years[position].toInt())
                refreshReport()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupStatusFilter() {
        binding.rgStatusFilter.setOnCheckedChangeListener { _, _ -> applyFilter() }
    }

    private fun setupRecycler() {
        reportAdapter = ReportAdapter()
        binding.rvReport.layoutManager = LinearLayoutManager(this)
        binding.rvReport.adapter = reportAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString() ?: ""
                applyFilter()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeViewModel() {
        viewModel.totalStudents.observe(this) { binding.tvTotalStudents.text = it.toString() }
        viewModel.totalMonthlyFee.observe(this) { binding.tvTotalFee.text = "₹%.0f".format(it) }
        viewModel.totalReceived.observe(this) { binding.tvTotalReceived.text = "₹%.0f".format(it) }
        viewModel.totalPending.observe(this) { binding.tvTotalPending.text = "₹%.0f".format(it) }
    }

    private fun refreshReport() {
        val month = binding.spinnerMonth.selectedItem?.toString() ?: months[0]
        val yearStr = binding.spinnerYear.selectedItem?.toString() ?: currentYear.toString()
        val year = yearStr.toIntOrNull() ?: currentYear

        viewModel.refreshSummary(month, year)

        // Remove old observer
        currentListLiveData?.removeObservers(this)

        currentListLiveData = viewModel.getStudentsWithFeeForMonth(month, year)
        currentListLiveData?.observe(this) { records ->
            allRecords = records
            applyFilter()
        }
    }

    private fun applyFilter() {
        var filtered = allRecords

        // Search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.studentName.contains(searchQuery, ignoreCase = true) ||
                it.className.contains(searchQuery, ignoreCase = true)
            }
        }

        // Status filter
        when (binding.rgStatusFilter.checkedRadioButtonId) {
            R.id.rbFilterPaid -> filtered = filtered.filter { it.isPaid }
            R.id.rbFilterPending -> filtered = filtered.filter { !it.isPaid }
        }

        reportAdapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupExportButtons() {
        binding.btnGeneratePdf.setOnClickListener { generatePdfReport() }
        binding.btnSharePdf.setOnClickListener { generateAndSharePdf() }
    }

    private fun generatePdfReport() {
        lifecycleScope.launch {
            try {
                val month = binding.spinnerMonth.selectedItem?.toString() ?: return@launch
                val year = binding.spinnerYear.selectedItem?.toString()?.toInt() ?: currentYear
                val file = PdfHelper.createMonthlyReportPdf(
                    this@MonthlyReportActivity, month, year, allRecords
                )
                Toast.makeText(this@MonthlyReportActivity, getString(R.string.pdf_generated), Toast.LENGTH_SHORT).show()
                if (file != null) PdfHelper.sharePdf(this@MonthlyReportActivity, file)
            } catch (e: Exception) {
                Toast.makeText(this@MonthlyReportActivity, "Error generating PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateAndSharePdf() {
        generatePdfReport()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
