package com.stealthvault.app.utils

import android.content.Context
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TRUSTED_SSID = "Home_Network" // Should be configurable in settings

    fun isAtHome(): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val currentSsid = info.ssid.replace("\"", "")
        
        return currentSsid == TRUSTED_SSID
    }
}
