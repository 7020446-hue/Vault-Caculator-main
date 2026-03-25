package com.stealthvault.app.ui.vault.fragments

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentClonerBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.utils.ClonerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClonerFragment : Fragment(R.layout.fragment_cloner) {

    private var _binding: FragmentClonerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VaultViewModel by viewModels()
    private lateinit var adapter: ClonedAppAdapter

    @Inject
    lateinit var clonerManager: ClonerManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClonerBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        binding.fabClone.setOnClickListener {
            if (!clonerManager.hasManagedProfile()) {
                showManagedProfileSetupDialog()
            } else {
                showAppPicker()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ClonedAppAdapter(
            onLaunch = { app ->
                clonerManager.launchAppInWorkProfile(app.packageName)
            },
            onDelete = { app ->
                viewModel.deleteClonedApp(app)
            }
        )
        binding.rvClones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClones.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.clonedApps.collect { clones ->
                adapter.submitList(clones)
                binding.tvEmpty.visibility = if (clones.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showManagedProfileSetupDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Initialize Sandbox")
            .setMessage("App Cloning requires creating a secure 'Work Profile' on your device. This profile provides isolated storage and separate accounts for your cloned apps.")
            .setPositiveButton("Setup") { _, _ ->
                clonerManager.createManagedProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAppPicker() {
        val pm = requireContext().packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .sortedBy { it.loadLabel(pm).toString() }

        val appNames = apps.map { it.loadLabel(pm).toString() }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select App to Clone")
            .setItems(appNames) { _, which ->
                val selectedApp = apps[which]
                viewModel.cloneApp(selectedApp.packageName, selectedApp.loadLabel(pm).toString())
                Toast.makeText(context, "${selectedApp.loadLabel(pm)} added to vault", Toast.LENGTH_SHORT).show()
                // Auto-enable selected app in work profile
                clonerManager.enableAppInWorkProfile(selectedApp.packageName)
            }
            .setNeutralButton("Install from Play (inside Sandbox)") { _, _ ->
                clonerManager.openPlayStoreInWorkProfile()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
