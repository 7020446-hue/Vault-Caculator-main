package com.stealthvault.app.ui.vault.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stealthvault.app.R
import com.stealthvault.app.data.local.entities.VaultFile
import com.stealthvault.app.databinding.ItemVaultFileBinding
import java.io.File

class VaultFileAdapter(private val onItemClick: (VaultFile) -> Unit) :
    ListAdapter<VaultFile, VaultFileAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemVaultFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: VaultFile, clickListener: (VaultFile) -> Unit) {
            binding.tvFileName.text = file.fileName
            
            Glide.with(binding.root.context)
                .load(File(file.encryptedPath)) 
                .placeholder(R.drawable.ic_vault_logo)
                .into(binding.ivThumbnail)

            binding.root.setOnClickListener { clickListener(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVaultFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<VaultFile>() {
        override fun areItemsTheSame(oldItem: VaultFile, newItem: VaultFile) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: VaultFile, newItem: VaultFile) = oldItem == newItem
    }
}
