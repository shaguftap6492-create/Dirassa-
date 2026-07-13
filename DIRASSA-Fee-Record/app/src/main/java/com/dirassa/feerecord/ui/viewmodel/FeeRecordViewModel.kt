package com.dirassa.feerecord.ui.viewmodel

import androidx.lifecycle.*
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.data.entity.StudentWithFee
import com.dirassa.feerecord.data.repository.FeeRecordRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Fee Record screen.
 */
class FeeRecordViewModel(private val repository: FeeRecordRepository) : ViewModel() {

    /** Currently selected student's fee records */
    private val _selectedStudentId = MutableLiveData<Int>()

    val feeRecordsForStudent: LiveData<List<FeeRecord>> =
        _selectedStudentId.switchMap { studentId ->
            repository.getFeeRecordsForStudent(studentId)
        }

    /** Operation result messages */
    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    /** Set selected student */
    fun setSelectedStudent(studentId: Int) {
        _selectedStudentId.value = studentId
    }

    /** Insert a new fee record */
    fun insertFeeRecord(record: FeeRecord) = viewModelScope.launch {
        try {
            val isDuplicate = repository.isDuplicate(record.studentId, record.month, record.year)
            if (isDuplicate) {
                _operationResult.postValue("DUPLICATE")
                return@launch
            }
            repository.insertFeeRecord(record)
            _operationResult.postValue("SAVED")
        } catch (e: Exception) {
            _operationResult.postValue("ERROR: ${e.message}")
        }
    }

    /** Update a fee record */
    fun updateFeeRecord(record: FeeRecord) = viewModelScope.launch {
        try {
            repository.updateFeeRecord(record)
            _operationResult.postValue("UPDATED")
        } catch (e: Exception) {
            _operationResult.postValue("ERROR: ${e.message}")
        }
    }

    /** Delete a fee record */
    fun deleteFeeRecord(record: FeeRecord) = viewModelScope.launch {
        try {
            repository.deleteFeeRecord(record)
            _operationResult.postValue("DELETED")
        } catch (e: Exception) {
            _operationResult.postValue("ERROR: ${e.message}")
        }
    }

    /** Check for duplicate */
    suspend fun isDuplicate(studentId: Int, month: String, year: Int): Boolean =
        repository.isDuplicate(studentId, month, year)

    /** Get fee record by ID */
    suspend fun getFeeRecordById(id: Int): FeeRecord? = repository.getFeeRecordById(id)
}

/** Factory for FeeRecordViewModel */
class FeeRecordViewModelFactory(private val repository: FeeRecordRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeeRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeeRecordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
