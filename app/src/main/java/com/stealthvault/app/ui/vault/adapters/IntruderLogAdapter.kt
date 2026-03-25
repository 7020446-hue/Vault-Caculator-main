package com.stealthvault.app.ui.vault.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stealthvault.app.data.local.entities.IntruderLog
import com.stealthvault.app.databinding.ItemIntruderLogBinding
import java.util.Calendar

class IntruderLogAdapter : ListAdapter<IntruderLog, IntruderLogAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemIntruderLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(log: IntruderLog) {
            val calendar = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            binding.tvLogTimestamp.text = DateFormat.format("dd MMM yyyy, HH:mm", calendar)
            binding.tvLogDetail.text = log.attemptType
            
            Glide.with(binding.root.context)
                .load(log.photoPath)
                .centerCrop()
                .into(binding.ivIntruderPhoto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIntruderLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<IntruderLog>() {
        override fun areItemsTheSame(oldItem: IntruderLog, newItem: IntruderLog) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: IntruderLog, newItem: IntruderLog) = oldItem == newItem
    }
}
