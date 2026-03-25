package com.stealthvault.app.ui.vault

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.stealthvault.app.R
import com.stealthvault.app.databinding.ActivityVaultBinding
import com.stealthvault.app.ui.chat.ChatViewModel
import com.stealthvault.app.utils.ShakeDetector
import com.stealthvault.app.data.local.SecurityPreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VaultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVaultBinding
    private val viewModel: VaultViewModel by viewModels()
    private lateinit var navController: NavController
    private var isDecoyMode = false
    private var pausedAt: Long = 0L

    @Inject
    lateinit var securityPrefs: SecurityPreferenceManager
    
    private val shakeDetector = ShakeDetector {
        // Quick Hide!
        finishAndRemoveTask()
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.importFile(this, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isDecoyMode = intent.getBooleanExtra("IS_DECOY", false)
        viewModel.setDecoyMode(isDecoyMode)

        if (isDecoyMode) {
            Toast.makeText(this, "Safe Mode Active", Toast.LENGTH_SHORT).show()
        }

        setupNavigation()
        setupFab()
        shakeDetector.start(this)
    }

    override fun onPause() {
        super.onPause()
        pausedAt = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        val timeout = securityPrefs.autoLockTimeoutMs
        if (timeout != SecurityPreferenceManager.TIMEOUT_NEVER && pausedAt > 0) {
            val elapsed = System.currentTimeMillis() - pausedAt
            if (elapsed > timeout) {
                // Lock the vault: go back to calculator
                finish()
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.stop()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // Make FAB context aware
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mediaFragment -> binding.fabAdd.show()
                else -> binding.fabAdd.hide()
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
            val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_add, null)
            bottomSheet.setContentView(sheetView)

            sheetView.findViewById<android.view.View>(R.id.btnHidePhotos).setOnClickListener {
                bottomSheet.dismiss()
                importLauncher.launch("image/*")
            }
            sheetView.findViewById<android.view.View>(R.id.btnHideVideos).setOnClickListener {
                bottomSheet.dismiss()
                importLauncher.launch("video/*")
            }
            sheetView.findViewById<android.view.View>(R.id.btnHideNotes).setOnClickListener {
                bottomSheet.dismiss()
                navController.navigate(R.id.noteEditFragment)
            }
            bottomSheet.show()
        }
    }
}
