package com.dirassa.feerecord.ui.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dirassa.feerecord.FeeRecordApplication
import com.dirassa.feerecord.R
import com.dirassa.feerecord.databinding.ActivityStudentListBinding
import com.dirassa.feerecord.ui.adapter.StudentAdapter
import com.dirassa.feerecord.ui.viewmodel.StudentViewModel
import com.dirassa.feerecord.ui.viewmodel.StudentViewModelFactory

/**
 * Lists all students with a search bar.
 * Tap a student to open the edit screen.
 */
class StudentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentListBinding
    private lateinit var adapter: StudentAdapter

    private val viewModel: StudentViewModel by viewModels {
        StudentViewModelFactory((application as FeeRecordApplication).studentRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Students"

        // Setup RecyclerView
        adapter = StudentAdapter { student ->
            val intent = Intent(this, AddEditStudentActivity::class.java).apply {
                putExtra(AddEditStudentActivity.EXTRA_STUDENT_ID, student.studentId)
            }
            startActivity(intent)
        }
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter

        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Observe students
        viewModel.searchedStudents.observe(this) { students ->
            adapter.submitList(students)
            binding.tvEmpty.visibility = if (students.isEmpty()) android.view.View.VISIBLE
                                          else android.view.View.GONE
        }

        // FAB — add new student
        binding.fabAddStudent.setOnClickListener {
            startActivity(Intent(this, AddEditStudentActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
