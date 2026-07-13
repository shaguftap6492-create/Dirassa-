package com.dirassa.feerecord.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirassa.feerecord.R
import com.dirassa.feerecord.data.entity.FeeRecord
import com.dirassa.feerecord.databinding.ItemFeeHistoryBinding

/**
 * Adapter for displaying a student's payment history.
 * Tap an item to load it into the fee form for editing.
 */
class FeeHistoryAdapter(
    private val onItemClick: (FeeRecord) -> Unit
) : ListAdapter<FeeRecord, FeeHistoryAdapter.FeeHistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeeHistoryViewHolder {
        val binding = ItemFeeHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FeeHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeeHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeeHistoryViewHolder(
        private val binding: ItemFeeHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: FeeRecord) {
            binding.tvMonthYear.text = "${record.month} ${record.year}"
            binding.tvAmountPaid.text = "₹%.2f".format(record.amountPaid)
            binding.tvPaymentDate.text = record.paymentDate
            binding.tvStatus.text = record.status
            binding.tvRemarks.text = record.remarks.ifBlank { "—" }

            // Style status badge
            val context = binding.root.context
            if (record.isPaid) {
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.paid_green))
                binding.tvStatus.setBackgroundResource(R.drawable.bg_paid_badge)
            } else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.pending_red))
                binding.tvStatus.setBackgroundResource(R.drawable.bg_pending_badge)
            }

            binding.root.setOnClickListener { onItemClick(record) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FeeRecord>() {
        override fun areItemsTheSame(old: FeeRecord, new: FeeRecord) = old.recordId == new.recordId
        override fun areContentsTheSame(old: FeeRecord, new: FeeRecord) = old == new
    }
}
