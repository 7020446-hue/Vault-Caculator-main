package com.stealthvault.app.ui.chat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stealthvault.app.data.local.entities.ChatContact
import com.stealthvault.app.databinding.ItemChatContactBinding

class ChatAdapter(private val onClick: (ChatContact) -> Unit) :
    ListAdapter<ChatContact, ChatAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemChatContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: ChatContact, onClick: (ChatContact) -> Unit) {
            binding.tvUsername.text = contact.username
            binding.tvLastMessage.text = contact.lastMessage
            binding.root.setOnClickListener { onClick(contact) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<ChatContact>() {
        override fun areItemsTheSame(oldItem: ChatContact, newItem: ChatContact) = oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: ChatContact, newItem: ChatContact) = oldItem == newItem
    }
}
