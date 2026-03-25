package com.stealthvault.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.database.SQLiteDatabase

@HiltAndroidApp
class StealthVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize SQLCipher for encrypted database support
        try {
            SQLiteDatabase.loadLibs(this)
        } catch (t: Throwable) {
            // Ignore fatal UnsatifiedLinkErrors caused by OS decompression bugs
            android.util.Log.e("StealthVaultApp", "Failed to load SQLCipher libs natively", t)
        }
    }
}
