package com.stealthvault.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.stealthvault.app.R
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import javax.inject.Inject
import com.stealthvault.app.data.local.SecurityPreferenceManager
import com.stealthvault.app.utils.SmartLockManager

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var securityPrefs: SecurityPreferenceManager

    @Inject
    lateinit var smartLockManager: SmartLockManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Smart Security ---
        val tvAutoLock = view.findViewById<android.widget.TextView>(R.id.tvAutoLockStatus)
        val tvMaxAttempts = view.findViewById<android.widget.TextView>(R.id.tvMaxAttemptsStatus)

        fun refreshSmartStatus() {
            val timeoutLabel = when (securityPrefs.autoLockTimeoutMs) {
                SecurityPreferenceManager.TIMEOUT_IMMEDIATE -> "Immediately (most secure)"
                SecurityPreferenceManager.TIMEOUT_30S      -> "After 30 seconds"
                SecurityPreferenceManager.TIMEOUT_1MIN     -> "After 1 minute"
                SecurityPreferenceManager.TIMEOUT_5MIN     -> "After 5 minutes"
                else                                       -> "Never (least secure)"
            }
            tvAutoLock?.text = "Currently: $timeoutLabel"
            tvMaxAttempts?.text = "Currently: lock after ${securityPrefs.maxFailedAttempts} wrong PINs"
        }
        refreshSmartStatus()

        view.findViewById<View>(R.id.btnAutoLock)?.setOnClickListener {
            val labels = arrayOf("Immediately", "After 30 seconds", "After 1 minute", "After 5 minutes", "Never")
            val values = longArrayOf(
                SecurityPreferenceManager.TIMEOUT_IMMEDIATE,
                SecurityPreferenceManager.TIMEOUT_30S,
                SecurityPreferenceManager.TIMEOUT_1MIN,
                SecurityPreferenceManager.TIMEOUT_5MIN,
                SecurityPreferenceManager.TIMEOUT_NEVER
            )
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Auto-Lock Timer")
                .setItems(labels) { _, which ->
                    securityPrefs.autoLockTimeoutMs = values[which]
                    refreshSmartStatus()
                    Toast.makeText(requireContext(), "Auto-Lock set to: ${labels[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        view.findViewById<View>(R.id.btnMaxAttempts)?.setOnClickListener {
            val options = arrayOf("3 attempts", "5 attempts", "10 attempts", "Unlimited (off)")
            val numbers = intArrayOf(3, 5, 10, Int.MAX_VALUE)
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Wrong PIN Lockout")
                .setItems(options) { _, which ->
                    securityPrefs.maxFailedAttempts = numbers[which]
                    securityPrefs.failedPinAttempts = 0 // Reset counter
                    securityPrefs.isLockedOut = false
                    refreshSmartStatus()
                    Toast.makeText(requireContext(), "Lockout set to: ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        view.findViewById<View>(R.id.btnChangePin).setOnClickListener {
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
                    Toast.makeText(requireContext(), "Icon updated! Changes may take a few seconds.", Toast.LENGTH_LONG).show()
                }
                .show()
        }

        // Smart Lock Wi-Fi Setup
        view.findViewById<View>(R.id.btnSmartLock)?.setOnClickListener {
            val currentSsid = smartLockManager.getCurrentSsid()
            val trustedSsid = securityPrefs.trustedSsid

            val message = buildString {
                if (currentSsid != null) append("Current network: \"$currentSsid\"\n") else append("Not connected to Wi-Fi.\n")
                if (trustedSsid != null) append("Trusted home: \"$trustedSsid\"") else append("No trusted network set.")
            }

            val options = buildList {
                if (currentSsid != null) add("Set \"$currentSsid\" as trusted home")
                if (trustedSsid != null) add("Clear trusted network")
            }.toTypedArray()

            if (options.isEmpty()) {
                Toast.makeText(requireContext(), "Connect to Wi-Fi first to set up Smart Lock.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Smart Lock")
                .setMessage(message)
                .setItems(options) { _, which ->
                    val selected = options[which]
                    if (selected.startsWith("Set")) {
                        currentSsid?.let {
                            smartLockManager.setTrustedSsid(it)
                            Toast.makeText(requireContext(), "✅ Smart Lock enabled for \"$it\"", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        smartLockManager.clearTrustedSsid()
                        Toast.makeText(requireContext(), "Smart Lock disabled.", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }
    }
}
