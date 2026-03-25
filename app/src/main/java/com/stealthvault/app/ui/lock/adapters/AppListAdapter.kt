package com.stealthvault.app.ui.lock.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stealthvault.app.databinding.ItemAppLockBinding

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val isLocked: Boolean
)

class AppListAdapter(private val onToggle: (AppInfo, Boolean) -> Unit) :
    ListAdapter<AppInfo, AppListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemAppLockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppInfo, toggleListener: (AppInfo, Boolean) -> Unit) {
            binding.tvAppName.text = app.name
            binding.ivAppIcon.setImageDrawable(app.icon)
            
            // Remove listener before setting check state to avoid triggering on bind
            binding.swLock.setOnCheckedChangeListener(null)
            binding.swLock.isChecked = app.isLocked
            
            binding.swLock.setOnCheckedChangeListener { _, isChecked ->
                toggleListener(app, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppLockBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onToggle)
    }

    object DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) = 
            oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) = 
            oldItem == newItem
    }
}
