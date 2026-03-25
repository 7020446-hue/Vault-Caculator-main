package com.stealthvault.app.ui.vault.fragments

import android.content.Intent
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClonerFragment : Fragment(R.layout.fragment_cloner) {

    private var _binding: FragmentClonerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VaultViewModel by viewModels()
    private lateinit var adapter: ClonedAppAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClonerBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        // Directly show app picker — no admin, no sandbox setup needed
        binding.fabClone.setOnClickListener {
            showAppPicker()
        }
    }

    private fun setupRecyclerView() {
        adapter = ClonedAppAdapter(
            onLaunch = { app ->
                // Launch the real installed app as a brand-new, separate task
                val launchIntent = requireContext().packageManager
                    .getLaunchIntentForPackage(app.packageName)?.apply {
                        // These flags force Android to open it as a separate app,
                        // completely independent from the vault
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    }
                if (launchIntent != null) {
                    requireContext().startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "${app.appName} is not installed on this device.", Toast.LENGTH_SHORT).show()
                }
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

    private fun showAppPicker() {
        val pm = requireContext().packageManager
        // Only show user-installed apps that have a launcher icon
        val apps = pm.getInstalledApplications(0)
            .filter { app ->
                app.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                pm.getLaunchIntentForPackage(app.packageName) != null &&
                app.packageName != requireContext().packageName
            }
            .sortedBy { it.loadLabel(pm).toString() }

        if (apps.isEmpty()) {
            Toast.makeText(context, "No installed apps found.", Toast.LENGTH_SHORT).show()
            return
        }

        val appNames = apps.map { it.loadLabel(pm).toString() }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add App Shortcut")
            .setItems(appNames) { _, which ->
                val selectedApp = apps[which]
                val name = selectedApp.loadLabel(pm).toString()
                viewModel.cloneApp(selectedApp.packageName, name)
                Toast.makeText(context, "\"$name\" added to vault shortcuts.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
