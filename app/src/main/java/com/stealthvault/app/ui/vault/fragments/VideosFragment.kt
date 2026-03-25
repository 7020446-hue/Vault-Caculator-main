package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentVaultListBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.ui.vault.adapters.VaultFileAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideosFragment : Fragment(R.layout.fragment_vault_list) {

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentVaultListBinding.bind(view)

        val adapter = VaultFileAdapter { file ->
            val bundle = Bundle().apply {
                putString("filePath", file.encryptedPath)
                putString("fileType", "Video")
            }
            findNavController().navigate(R.id.mediaDetailFragment, bundle)
        }

        binding.rvFiles.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videos.collectLatest { videos ->
                adapter.submitList(videos)
            }
        }
    }
}
