package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentIntruderLogsBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.ui.vault.adapters.IntruderLogAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IntruderLogsFragment : Fragment(R.layout.fragment_intruder_logs) {

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentIntruderLogsBinding.bind(view)

        val adapter = IntruderLogAdapter()
        binding.rvIntruderLogs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.intruderLogs.collectLatest { logs ->
                adapter.submitList(logs)
                binding.tvNoLogs.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
