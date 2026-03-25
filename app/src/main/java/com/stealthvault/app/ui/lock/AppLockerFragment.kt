package com.stealthvault.app.ui.lock

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentAppLockerBinding
import com.stealthvault.app.ui.lock.adapters.AppInfo
import com.stealthvault.app.ui.lock.adapters.AppListAdapter
import com.stealthvault.app.ui.vault.VaultViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AppLockerFragment : Fragment(R.layout.fragment_app_locker) {

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAppLockerBinding.bind(view)

        val adapter = AppListAdapter { app, isLocked ->
            if (isLocked) {
                viewModel.lockApp(app.packageName, app.name)
            } else {
                viewModel.unlockApp(app.packageName, app.name)
            }
        }

        binding.rvApps.adapter = adapter
        
        checkAccessibilityService()

        lifecycleScope.launch {
            viewModel.lockedApps.collectLatest { lockedList ->
                val lockedPkgs = lockedList.map { it.packageName }.toSet()
                
                binding.progressBar.visibility = View.VISIBLE
                val allApps = withContext(Dispatchers.IO) {
                    getInstalledApps(lockedPkgs)
                }
                binding.progressBar.visibility = View.GONE
                adapter.submitList(allApps)
            }
        }
    }

    private fun getInstalledApps(lockedPkgs: Set<String>): List<AppInfo> {
        val pm = requireContext().packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        return pm.queryIntentActivities(mainIntent, 0).map { resolveInfo ->
            val pkgName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            AppInfo(appName, pkgName, icon, lockedPkgs.contains(pkgName))
        }.sortedBy { it.name }
    }

    private fun checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled(requireContext())) {
            // Suggest enabling the service in a real app with a message/dialog
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            // startActivity(intent)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${com.stealthvault.app.service.AppLockAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabled?.contains(service) == true
    }
}
