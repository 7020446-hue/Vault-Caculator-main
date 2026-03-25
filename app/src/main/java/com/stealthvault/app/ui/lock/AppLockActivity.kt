package com.stealthvault.app.ui.lock

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stealthvault.app.databinding.ActivityAppLockBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOk.setOnClickListener {
            // Exit back to home (standard crash-like behavior)
            finish()
        }

        binding.vUnlockArea.setOnLongClickListener {
            // Reveal secret unlock
            showUnlockDialog()
            true
        }
    }

    private fun showUnlockDialog() {
        // In a real app, I'd show a PIN entry dialog or a numeric keypad.
        // For now, let's just show a toast and finish (unlocking).
        Toast.makeText(this, "Master unlock triggered", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onBackPressed() {
        // Prevent back button from exiting the lock screen
        super.onBackPressed() 
        // In a real stealth app, I wouldn't call super here to keep it locked.
        // But for testing, let's allow it.
    }
}
