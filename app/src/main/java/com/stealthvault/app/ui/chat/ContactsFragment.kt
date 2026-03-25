package com.stealthvault.app.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.stealthvault.app.R
import com.stealthvault.app.data.local.entities.ChatContact
import com.stealthvault.app.databinding.FragmentContactsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    private val viewModel: ChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentContactsBinding.bind(view)

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString()
            if (query.isNotEmpty()) {
                searchUser(query, binding)
            }
        }
    }

    private fun searchUser(username: String, binding: FragmentContactsBinding) {
        lifecycleScope.launch {
            try {
                val db = FirebaseDatabase.getInstance()
                // Simple username lookup
                val snap = db.getReference("users").orderByChild("username").equalTo(username).get().await()
                
                if (snap.exists()) {
                    val userSnap = snap.children.first()
                    val userId = userSnap.key ?: return@launch
                    val publicKey = userSnap.child("publicKey").value as? String ?: ""
                    
                    val contact = ChatContact(userId, username, publicKey)
                    viewModel.addContact(contact)
                    // Initial E2E Hello
                    viewModel.sendMessage(userId, publicKey, "Hello, I added you on Stealth Vault!")
                    
                    Toast.makeText(context, "Contact Added!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
