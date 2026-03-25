package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentMediaBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.ui.vault.adapters.VaultFileAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MediaFragment : Fragment(R.layout.fragment_media) {

    private val viewModel: VaultViewModel by activityViewModels()
    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VaultFileAdapter

    @javax.inject.Inject
    lateinit var encryptionManager: com.stealthvault.app.data.security.EncryptionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMediaBinding.bind(view)

        setupRecyclerView()
        setupTabs()
        observeViewModel(0) // Default to Photos
    }

    private fun setupRecyclerView() {
        adapter = VaultFileAdapter(encryptionManager) { file ->
            val bundle = Bundle().apply {
                putString("filePath", file.encryptedPath)
                putString("fileType", file.fileType)
            }
            findNavController().navigate(R.id.mediaDetailFragment, bundle)
        }
        
        // Calculate dynamic span count
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val spanCount = (dpWidth / 180).toInt().coerceAtLeast(2) // 180dp per item

        binding.rvMedia.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), spanCount)
            adapter = this@MediaFragment.adapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                observeViewModel(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnEmptyImport.setOnClickListener {
            // Forward call to activity's FAB trigger
            (activity as? com.stealthvault.app.ui.vault.VaultActivity)?.findViewById<android.view.View>(com.stealthvault.app.R.id.fabAdd)?.performClick()
        }
    }

    private fun observeViewModel(position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (position == 0) {
                viewModel.photos.collectLatest { list ->
                    adapter.submitList(list)
                    binding.llEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            } else {
                viewModel.videos.collectLatest { list ->
                    adapter.submitList(list)
                    binding.llEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
