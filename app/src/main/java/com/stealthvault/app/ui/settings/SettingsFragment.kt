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
    }
}
