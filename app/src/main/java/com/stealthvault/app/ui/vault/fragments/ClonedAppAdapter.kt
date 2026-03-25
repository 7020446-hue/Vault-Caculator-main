package com.stealthvault.app.ui.vault.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stealthvault.app.data.local.entities.ClonedApp
import com.stealthvault.app.databinding.ItemClonedAppBinding

class ClonedAppAdapter(
    private val onLaunch: (ClonedApp) -> Unit,
    private val onDelete: (ClonedApp) -> Unit
) : RecyclerView.Adapter<ClonedAppAdapter.ViewHolder>() {

    private var items = listOf<ClonedApp>()

    fun submitList(newItems: List<ClonedApp>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemClonedAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvAppName.text = item.appName
            tvPackageName.text = item.packageName
            
            root.setOnClickListener { onLaunch(item) }
            btnDelete.setOnClickListener { onDelete(item) }
            
            // In a real app we'd load the icon via PackageManager
            try {
                val icon = root.context.packageManager.getApplicationIcon(item.packageName)
                ivAppIcon.setImageDrawable(icon)
            } catch (e: Exception) {
                ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemClonedAppBinding) : RecyclerView.ViewHolder(binding.root)
}
