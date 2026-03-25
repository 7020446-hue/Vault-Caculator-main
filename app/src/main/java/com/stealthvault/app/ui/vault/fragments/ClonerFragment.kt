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

    @javax.inject.Inject
    lateinit var clonerManager: com.stealthvault.app.utils.ClonerManager

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
                // ALWAYS Check for In-App Web Version First (to keep user inside the vault)
                val webUrl = com.stealthvault.app.ui.vault.AppWebActivity.WEB_APP_MAP[app.packageName]

                if (webUrl != null) {
                    // ✅ Open IMMEDIATELY inside vault (seamless experience)
                    val intent = android.content.Intent(requireContext(), com.stealthvault.app.ui.vault.AppWebActivity::class.java).apply {
                        putExtra(com.stealthvault.app.ui.vault.AppWebActivity.EXTRA_URL, webUrl)
                        putExtra(com.stealthvault.app.ui.vault.AppWebActivity.EXTRA_TITLE, app.appName)
                    }
                    startActivity(intent)
                } else if (clonerManager.hasManagedProfile()) {
                    // 🔄 Fallback to Sandbox Profile if it exists
                    clonerManager.launchAppInWorkProfile(app.packageName)
                } else {
                    // 🔄 Last resort: Normal Native Launch
                    val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        requireContext().startActivity(launchIntent)
                    }
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

    private fun showManagedProfileSetupDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bypass Sandbox Setup")
            .setMessage("To install apps *inside* the vault, we create a private 'Personal Sandbox'. This bypassed version won't show the usual IT Admin error.")
            .setPositiveButton("Setup Sandbox") { _, _ ->
                clonerManager.createManagedProfile()
            }
            .setNegativeButton("Add Shortcut Only") { _, _ ->
                showAppPicker() 
            }
            .show()
    }

    private fun showAppPicker() {
        val pm = requireContext().packageManager
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
            .setTitle("Select App")
            .setItems(appNames) { _, which ->
                val selectedApp = apps[which]
                val name = selectedApp.loadLabel(pm).toString()
                viewModel.cloneApp(selectedApp.packageName, name)
                
                if (clonerManager.hasManagedProfile()) {
                    clonerManager.enableAppInWorkProfile(selectedApp.packageName)
                }
                
                Toast.makeText(context, "\"$name\" added to vault.", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Open Market") { _, _ ->
                clonerManager.openPlayStoreInWorkProfile()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
