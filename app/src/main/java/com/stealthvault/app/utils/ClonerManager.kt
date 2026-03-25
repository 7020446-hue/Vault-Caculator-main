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
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
        
        val profiles = userManager.userProfiles
        val workProfile = profiles.find { it != android.os.Process.myUserHandle() }
        
        if (workProfile != null) {
            val activities = launcherApps.getActivityList(packageName, workProfile)
            if (activities.isNotEmpty()) {
                launcherApps.startMainActivity(activities[0].componentName, workProfile, null, null)
            } else {
                // If not found, try enabling it (in case it was hidden/disabled)
                enableAppInWorkProfile(packageName)
            }
        }
    }

    /**
     * Unhide/Enable an app in the work profile.
     */
    fun enableAppInWorkProfile(packageName: String) {
        val admin = ComponentName(context, AdminReceiver::class.java)
        // Note: For full control, we must be the Profile Owner.
        // We can also try opening the app's settings in the work profile.
        try {
            devicePolicyManager.setApplicationHidden(admin, packageName, false)
        } catch (e: Exception) {
            // Might fail if not profile owner yet or app not found
        }
    /**
     * Open the Play Store inside the work profile to allow installing new apps there.
     */
    fun openPlayStoreInWorkProfile() {
        val profiles = userManager.userProfiles
        val workProfile = profiles.find { it != android.os.Process.myUserHandle() }
        
        if (workProfile != null) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MARKET)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent, null) // Note: startActivity might automatically pick the profile-aware intent if handled correctly
            // Alternatively, use a cross-profile intent or find the market activity in the profile.
        }
    }
}
