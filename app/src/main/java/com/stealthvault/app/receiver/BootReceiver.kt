package com.stealthvault.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, Stealth Vault services should start.")
            // Accessibility service starts automatically if enabled by user.
            // Other background services could be started here.
        }
    }
}
