package com.stealthvault.app.ui.chat.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stealthvault.app.data.local.entities.SecureMessage
import com.stealthvault.app.databinding.ItemMessageReceivedBinding
import com.stealthvault.app.databinding.ItemMessageSentBinding
import java.util.*

class MessageAdapter : ListAdapter<SecureMessage, RecyclerView.ViewHolder>(DiffCallback) {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromMe) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: SecureMessage) {
            binding.tvMessage.text = message.content
            binding.tvTime.text = DateFormat.format("HH:mm", Calendar.getInstance().apply { timeInMillis = message.timestamp })
        }
    }

    class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: SecureMessage) {
            binding.tvMessage.text = message.content
            binding.tvTime.text = DateFormat.format("HH:mm", Calendar.getInstance().apply { timeInMillis = message.timestamp })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentViewHolder) holder.bind(message)
        else if (holder is ReceivedViewHolder) holder.bind(message)
    }

    object DiffCallback : DiffUtil.ItemCallback<SecureMessage>() {
        override fun areItemsTheSame(oldItem: SecureMessage, newItem: SecureMessage) = oldItem.messageId == newItem.messageId
        override fun areContentsTheSame(oldItem: SecureMessage, newItem: SecureMessage) = oldItem == newItem
    }
}
