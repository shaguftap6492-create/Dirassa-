package com.dirassa.feerecord.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirassa.feerecord.R
import com.dirassa.feerecord.data.entity.StudentWithFee
import com.dirassa.feerecord.databinding.ItemReportBinding

/**
 * Adapter for the Monthly Report screen student list.
 */
class ReportAdapter : ListAdapter<StudentWithFee, ReportAdapter.ReportViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(
        private val binding: ItemReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudentWithFee) {
            val context = binding.root.context

            binding.tvStudentName.text = item.studentName
            binding.tvClass.text = item.className
            binding.tvMonthlyFee.text = "Fee: ₹%.0f".format(item.monthlyFee)
            binding.tvPaid.text = "Paid: ₹%.0f".format(item.amountPaid)
            binding.tvPending.text = "Pending: ₹%.0f".format(item.pendingAmount)
            binding.tvStatus.text = item.status

            // Avatar
            binding.tvAvatar.text = item.studentName.firstOrNull()?.uppercase() ?: "?"

            // Status styling
            if (item.isPaid) {
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.paid_green))
                binding.tvStatus.setBackgroundResource(R.drawable.bg_paid_badge)
                binding.tvPending.setTextColor(ContextCompat.getColor(context, R.color.hint_text))
            } else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.pending_red))
                binding.tvStatus.setBackgroundResource(R.drawable.bg_pending_badge)
                binding.tvPending.setTextColor(ContextCompat.getColor(context, R.color.pending_red))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StudentWithFee>() {
        override fun areItemsTheSame(a: StudentWithFee, b: StudentWithFee) =
            a.studentId == b.studentId && a.month == b.month && a.year == b.year
        override fun areContentsTheSame(a: StudentWithFee, b: StudentWithFee) = a == b
    }
}
