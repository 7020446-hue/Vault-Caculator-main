package com.stealthvault.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stealthvault.app.ui.vault.VaultActivity

class DialerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        if (phoneNumber == "*#*#1234#*#*") {
            // Cancel the call
            resultData = null
            
            // Open the vault
            val launchVault = Intent(context, VaultActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchVault)
        }
    }
}
