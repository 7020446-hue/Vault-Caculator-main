package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentNoteEditBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteEditFragment : Fragment(R.layout.fragment_note_edit) {

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNoteEditBinding.bind(view)

        val noteId = arguments?.getLong("noteId") ?: 0L
        
        if (noteId != 0L) {
            val note = viewModel.notes.value.find { it.id == noteId }
            note?.let {
                binding.etNoteTitle.setText(it.title)
                binding.etNoteContent.setText(it.content)
                when (it.category) {
                    "Password" -> binding.chipPassword.isChecked = true
                    "Finance" -> binding.chipFinance.isChecked = true
                    else -> binding.chipGeneral.isChecked = true
                }
            }
        }

        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString()
            val content = binding.etNoteContent.text.toString()
            val category = when (binding.cgCategory.checkedChipId) {
                R.id.chipPassword -> "Password"
                R.id.chipFinance -> "Finance"
                else -> "General"
            }

            if (title.isNotEmpty() && content.isNotEmpty()) {
                viewModel.saveNote(title, content, category, noteId)
                Toast.makeText(requireContext(), "Security Note Saved!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
