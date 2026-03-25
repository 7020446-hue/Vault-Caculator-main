package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentVaultListBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.ui.vault.adapters.VaultFileAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilesFragment : Fragment(R.layout.fragment_vault_list) {

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentVaultListBinding.bind(view)

        val adapter = VaultFileAdapter { file ->
            viewModel.restoreFile(file)
        }

        binding.rvFiles.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.files.collectLatest { files ->
                adapter.submitList(files)
                binding.tvEmpty.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
