package com.stealthvault.app.ui.vault.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.stealthvault.app.data.local.entities.VaultFile
import com.stealthvault.app.data.security.EncryptionManager
import com.stealthvault.app.databinding.ItemVaultFileBinding
import kotlinx.coroutines.*
import java.io.File

class VaultFileAdapter(
    private val encryptionManager: EncryptionManager,
    private val onItemClick: (VaultFile) -> Unit
) : ListAdapter<VaultFile, VaultFileAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemVaultFileBinding, private val encryptionManager: EncryptionManager) :
        RecyclerView.ViewHolder(binding.root) {
            
        private var job: Job? = null

        fun bind(file: VaultFile, clickListener: (VaultFile) -> Unit) {
            binding.tvFileName.text = file.fileName
            binding.root.setOnClickListener { clickListener(file) }
            
            // Cancel any previous decode job if recycled
            job?.cancel()

            // Safe fallback icon
            val defaultIconRes = when (file.fileType) {
                "Photo" -> android.R.drawable.ic_menu_gallery
                "Video" -> android.R.drawable.ic_menu_camera
                else -> android.R.drawable.ic_menu_view
            }
            binding.ivThumbnail.setImageResource(defaultIconRes)
            binding.ivThumbnail.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE

            val thumbFile = File(file.encryptedPath + "_thumb")
            if (thumbFile.exists()) {
                job = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val stream = encryptionManager.getDecryptedStream(thumbFile)
                        val bitmap = BitmapFactory.decodeStream(stream)
                        stream.close()

                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                binding.ivThumbnail.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                                Glide.with(binding.ivThumbnail)
                                    .load(bitmap)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Security: never cache on disk
                                    .skipMemoryCache(false)
                                    .into(binding.ivThumbnail)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVaultFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, encryptionManager)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<VaultFile>() {
        override fun areItemsTheSame(oldItem: VaultFile, newItem: VaultFile) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: VaultFile, newItem: VaultFile) = oldItem == newItem
    }
}
