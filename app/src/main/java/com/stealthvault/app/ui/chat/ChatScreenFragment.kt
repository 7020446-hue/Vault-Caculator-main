package com.stealthvault.app.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentChatScreenBinding
import com.stealthvault.app.ui.chat.adapters.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatScreenFragment : Fragment(R.layout.fragment_chat_screen) {

    private val viewModel: ChatViewModel by viewModels()
    private var partnerId: String = ""
    private var partnerName: String = ""
    private var partnerPubKey: String = ""
    private var selfDestructSec: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChatScreenBinding.bind(view)

        partnerId = arguments?.getString("partnerId") ?: return
        partnerName = arguments?.getString("partnerName") ?: "Secure Chat"
        partnerPubKey = arguments?.getString("partnerPubKey") ?: ""

        binding.tvChatPartner.text = partnerName

        val adapter = MessageAdapter()
        binding.rvMessages.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getMessages(partnerId).collectLatest { messages ->
                adapter.submitList(messages)
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(partnerId, partnerPubKey, text, selfDestructSec)
                binding.etMessage.setText("")
            }
        }

        binding.btnSelfDestruct.setOnClickListener {
            // cycle: 0 -> 30s -> 60s -> 5m -> 0
            selfDestructSec = when (selfDestructSec) {
                0 -> 30
                30 -> 60
                60 -> 300
                else -> 0
            }
            binding.btnSelfDestruct.setImageResource(if (selfDestructSec > 0) R.drawable.ic_timer else R.drawable.ic_lock)
            // Note: Visual indicator of timer
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }
}
