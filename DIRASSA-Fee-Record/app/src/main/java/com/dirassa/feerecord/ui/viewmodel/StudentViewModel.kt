package com.dirassa.feerecord.ui.viewmodel

import androidx.lifecycle.*
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for student-related screens.
 * Survives configuration changes (screen rotation).
 */
class StudentViewModel(private val repository: StudentRepository) : ViewModel() {

    /** All students as LiveData — auto-updates UI when DB changes */
    val allStudents: LiveData<List<Student>> = repository.allStudents

    /** Search results */
    private val _searchQuery = MutableLiveData("")
    val searchedStudents: LiveData<List<Student>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) repository.allStudents
        else repository.searchStudents(query)
    }

    /** Operation result messages */
    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    /** Set search query */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Insert a new student */
    fun insertStudent(student: Student) = viewModelScope.launch {
        try {
            repository.insertStudent(student)
            _operationResult.postValue("SUCCESS")
        } catch (e: Exception) {
            _operationResult.postValue("ERROR: ${e.message}")
        }
    }

    /** Update an existing student */
    fun updateStudent(student: Student) = viewModelScope.launch {
        try {
            repository.updateStudent(student)
            _operationResult.postValue("UPDATED")
        } catch (e: Exception) {
            _operationResult.postValue("ERROR: ${e.message}")
        }
    }

    /** Delete a student */
    fun deleteStudent(student: Student) = viewModelScope.launch {
        try {
            repository.deleteStudent(student)
            _operationResult.postValue("DELETED")
        } catch (e: Exception) {
            _operationResult.postValue("ERROR: ${e.message}")
        }
    }

    /** Fetch student by ID (for edit screen) */
    suspend fun getStudentById(id: Int): Student? = repository.getStudentById(id)

    /** Get total count */
    suspend fun getTotalStudentCount(): Int = repository.getTotalStudentCount()

    /** Get total monthly fee sum */
    suspend fun getTotalMonthlyFee(): Double = repository.getTotalMonthlyFee()

    /** Get all students once (for PDF/Excel export) */
    suspend fun getAllStudentsOnce(): List<Student> = repository.getAllStudentsOnce()
}

/** Factory to pass repository into ViewModel */
class StudentViewModelFactory(private val repository: StudentRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
