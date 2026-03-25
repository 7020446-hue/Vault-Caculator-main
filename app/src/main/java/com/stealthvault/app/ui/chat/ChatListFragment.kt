package com.stealthvault.app.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentChatListBinding
import com.stealthvault.app.ui.chat.adapters.ChatAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : Fragment(R.layout.fragment_chat_list) {

    private val viewModel: ChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChatListBinding.bind(view)

        val adapter = ChatAdapter { contact ->
            val bundle = Bundle().apply {
                putString("partnerId", contact.userId)
                putString("partnerName", contact.username)
                putString("partnerPubKey", contact.publicKey)
            }
            findNavController().navigate(R.id.chatScreenFragment, bundle)
        }

        binding.rvChats.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contacts.collectLatest { contacts ->
                adapter.submitList(contacts)
                binding.tvNoChats.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.btnAddContact.setOnClickListener {
            findNavController().navigate(R.id.contactsFragment)
        }

        // Start listening for new messages on relay
        viewModel.startListening()
    }
}
