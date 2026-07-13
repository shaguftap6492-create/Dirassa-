package com.dirassa.feerecord.ui.viewmodel

import androidx.lifecycle.*
import com.dirassa.feerecord.data.entity.StudentWithFee
import com.dirassa.feerecord.data.repository.FeeRecordRepository
import com.dirassa.feerecord.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Monthly Report screen.
 * Aggregates data from both repositories.
 */
class ReportViewModel(
    private val studentRepo: StudentRepository,
    private val feeRepo: FeeRecordRepository
) : ViewModel() {

    /** Current filter state */
    private val _selectedMonth = MutableLiveData("January")
    private val _selectedYear = MutableLiveData(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
    private val _statusFilter = MutableLiveData("All") // "All", "Paid", "Pending"

    val selectedMonth: LiveData<String> = _selectedMonth
    val selectedYear: LiveData<Int> = _selectedYear

    /** Students with fee for current filter */
    val studentsWithFee: LiveData<List<StudentWithFee>> =
        MediatorLiveData<List<StudentWithFee>>().apply {
            fun update() {
                val month = _selectedMonth.value ?: return
                val year = _selectedYear.value ?: return
                val status = _statusFilter.value ?: "All"
                // Remove old source — we'll add a new one
                // This simplified approach re-queries when filters change
                viewModelScope.launch {
                    // Collect once for simplicity; reactive approach below
                }
            }
            addSource(_selectedMonth) { update() }
            addSource(_selectedYear) { update() }
            addSource(_statusFilter) { update() }
        }

    /** Dashboard summary LiveData */
    private val _totalStudents = MutableLiveData(0)
    val totalStudents: LiveData<Int> = _totalStudents

    private val _totalMonthlyFee = MutableLiveData(0.0)
    val totalMonthlyFee: LiveData<Double> = _totalMonthlyFee

    private val _totalReceived = MutableLiveData(0.0)
    val totalReceived: LiveData<Double> = _totalReceived

    private val _totalPending = MutableLiveData(0.0)
    val totalPending: LiveData<Double> = _totalPending

    /** Reports list (used directly by Activity via separate LiveData per query) */
    fun getStudentsWithFeeForMonth(month: String, year: Int): LiveData<List<StudentWithFee>> =
        feeRepo.getStudentsWithFeeForMonth(month, year)

    fun getStudentsWithFeeFiltered(month: String, year: Int, status: String): LiveData<List<StudentWithFee>> =
        feeRepo.getStudentsWithFeeFiltered(month, year, status)

    /** Refresh dashboard summary cards */
    fun refreshSummary(month: String, year: Int) = viewModelScope.launch {
        _totalStudents.postValue(studentRepo.getTotalStudentCount())
        _totalMonthlyFee.postValue(studentRepo.getTotalMonthlyFee())
        _totalReceived.postValue(feeRepo.getTotalReceived(month, year))
        _totalPending.postValue(feeRepo.getTotalPending(month, year))
    }

    fun setMonth(month: String) { _selectedMonth.value = month }
    fun setYear(year: Int) { _selectedYear.value = year }
    fun setStatusFilter(status: String) { _statusFilter.value = status }

    suspend fun getAllStudentsWithFeeOnce(): List<StudentWithFee> =
        feeRepo.getAllStudentsWithFeeOnce()
}

/** Factory for ReportViewModel */
class ReportViewModelFactory(
    private val studentRepo: StudentRepository,
    private val feeRepo: FeeRecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(studentRepo, feeRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
