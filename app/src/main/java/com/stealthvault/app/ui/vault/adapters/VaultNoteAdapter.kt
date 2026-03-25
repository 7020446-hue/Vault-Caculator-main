package com.stealthvault.app.ui.vault.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stealthvault.app.data.local.entities.VaultNote
import com.stealthvault.app.databinding.ItemVaultNoteBinding
import java.util.Calendar

class VaultNoteAdapter(private val onItemClick: (VaultNote) -> Unit) :
    ListAdapter<VaultNote, VaultNoteAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemVaultNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: VaultNote, clickListener: (VaultNote) -> Unit) {
            binding.tvNoteTitle.text = note.title
            val calendar = Calendar.getInstance().apply { timeInMillis = note.timestamp }
            binding.tvNoteDate.text = DateFormat.format("dd MMM yyyy", calendar)
            binding.root.setOnClickListener { clickListener(note) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVaultNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<VaultNote>() {
        override fun areItemsTheSame(oldItem: VaultNote, newItem: VaultNote) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: VaultNote, newItem: VaultNote) = oldItem == newItem
    }
}
