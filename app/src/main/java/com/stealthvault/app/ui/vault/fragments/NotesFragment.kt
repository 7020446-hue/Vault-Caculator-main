package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentNotesBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.ui.vault.adapters.VaultNoteAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes) {

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNotesBinding.bind(view)

        val adapter = VaultNoteAdapter { note ->
            // Edit existing note logic here
        }

        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.noteEditFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collectLatest { notes ->
                adapter.submitList(notes)
            }
        }
    }
}
