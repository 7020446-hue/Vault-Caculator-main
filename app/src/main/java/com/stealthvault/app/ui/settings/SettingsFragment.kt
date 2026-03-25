package com.stealthvault.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import javax.inject.Inject
import com.stealthvault.app.data.local.SecurityPreferenceManager

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var securityPrefs: SecurityPreferenceManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use basic findViewById since we rewrote the layout to not use the exact old binding structure
        view.findViewById<View>(R.id.btnChangePin).setOnClickListener {
            // Security Reset: Wipe stored PINs and require immediate setup
            securityPrefs.masterPin = null
            securityPrefs.decoyPin = null
            securityPrefs.isSetupComplete = false
            
            Toast.makeText(requireContext(), "Auth credentials reset. Please select a new Master PIN.", Toast.LENGTH_LONG).show()
            requireActivity().finishAffinity()
        }

        view.findViewById<View>(R.id.btnManageApps).setOnClickListener {
            androidx.navigation.fragment.NavHostFragment.findNavController(this)
                .navigate(R.id.appLockerFragment)
        }

        view.findViewById<View>(R.id.btnViewIntruderLogs).setOnClickListener {
            androidx.navigation.fragment.NavHostFragment.findNavController(this)
                .navigate(R.id.intruderLogsFragment)
        }
        view.findViewById<View>(R.id.btnChangeIcon)?.setOnClickListener {
            val options = arrayOf("Calculator", "Notes")
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Fake Icon")
                .setItems(options) { _, which ->
                    val pm = requireContext().packageManager
                    val currentPackage = requireContext().packageName

                    val calcComponent = android.content.ComponentName(currentPackage, "$currentPackage.CalculatorAlias")
                    val notesComponent = android.content.ComponentName(currentPackage, "$currentPackage.NotesAlias")

                    if (which == 0) {
                        pm.setComponentEnabledSetting(notesComponent, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                        pm.setComponentEnabledSetting(calcComponent, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                    } else {
                        pm.setComponentEnabledSetting(calcComponent, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                        pm.setComponentEnabledSetting(notesComponent, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                    }
                    
                    Toast.makeText(requireContext(), "Icon updated! Changes may take a few seconds to appear.", Toast.LENGTH_LONG).show()
                }
                .show()
        }
        
    }
}
