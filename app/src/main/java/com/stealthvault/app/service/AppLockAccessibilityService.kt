package com.stealthvault.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.stealthvault.app.data.repository.VaultRepository
import com.stealthvault.app.ui.lock.AppLockActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.stealthvault.app.utils.SmartLockManager

@AndroidEntryPoint
class AppLockAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var repository: VaultRepository
    
    @Inject
    lateinit var smartLockManager: SmartLockManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return
            if (pkgName == packageName) return

            // Smart Lock: Skip locking apps when user is on trusted home Wi-Fi
            if (smartLockManager.isAtHome()) return

            checkAppLock(pkgName)
        }
    }


    private fun checkAppLock(pkgName: String) {
        serviceScope.launch {
            if (repository.isAppLocked(pkgName)) {
                launchLockOverlay(pkgName)
            }
        }
    }

    private fun launchLockOverlay(pkgName: String) {
        val intent = Intent(this, AppLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("LOCKED_PACKAGE", pkgName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Handle interruption
    }
}
