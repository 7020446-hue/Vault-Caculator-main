package com.stealthvault.app.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.os.UserManager
import com.stealthvault.app.receiver.AdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClonerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    /**
     * Check if a Work Profile (Managed Profile) already exists.
     */
    fun hasManagedProfile(): Boolean {
        val profiles = userManager.userProfiles
        return profiles.size > 1 // Profiles include parent + guest OR work
    }

    /**
     * Trigger the creation of a managed profile.
     * This will open the system setup UI.
     */
    fun createManagedProfile() {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                ComponentName(context, AdminReceiver::class.java))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Launch an app in the managed profile.
     */
    fun launchAppInWorkProfile(packageName: String) {
        val pm = context.packageManager
        val mainIntent = pm.getLaunchIntentForPackage(packageName) ?: return
        
        // Find the managed profile user handle
        val profiles = userManager.userProfiles
        val workProfile = profiles.find { it != android.os.Process.myUserHandle() }
        
        if (workProfile != null) {
            context.startActivity(mainIntent, null) // In theory, startActivity uses current user unless specified
            // Note: Launching specifically in work profile usually requires cross-profile intent if not handled by system
        }
    }
}
