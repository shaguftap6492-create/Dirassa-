package com.dirassa.feerecord.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirassa.feerecord.data.entity.Student
import com.dirassa.feerecord.databinding.ItemStudentBinding

/**
 * RecyclerView adapter for the Student list.
 * Uses ListAdapter + DiffUtil for efficient updates.
 */
class StudentAdapter(
    private val onItemClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.tvStudentId.text   = student.displayId
            binding.tvStudentName.text = student.studentName
            binding.tvFatherName.text  = "Father: ${student.fatherName}"
            binding.tvClass.text       = student.className
            binding.tvMobile.text      = student.mobile
            binding.tvFee.text         = "₹%.0f/month".format(student.monthlyFee)

            binding.root.setOnClickListener { onItemClick(student) }

            // Avatar initial letter
            binding.tvAvatar.text = student.studentName.firstOrNull()?.uppercase() ?: "?"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(old: Student, new: Student) = old.studentId == new.studentId
        override fun areContentsTheSame(old: Student, new: Student) = old == new
    }
}
