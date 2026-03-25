package com.stealthvault.app.utils

import android.content.Context
import android.net.wifi.WifiManager
import com.stealthvault.app.data.local.SecurityPreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityPrefs: SecurityPreferenceManager
) {

    fun isAtHome(): Boolean {
        val trustedSsid = securityPrefs.trustedSsid ?: return false
        val currentSsid = getCurrentSsid() ?: return false
        return currentSsid == trustedSsid
    }

    fun getCurrentSsid(): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ssid = wifiManager.connectionInfo.ssid?.replace("\"", "")
            if (ssid == null || ssid == "<unknown ssid>") null else ssid
        } catch (e: Exception) { null }
    }

    fun setTrustedSsid(ssid: String) {
        securityPrefs.trustedSsid = ssid
    }

    fun clearTrustedSsid() {
        securityPrefs.trustedSsid = null
    }
}
