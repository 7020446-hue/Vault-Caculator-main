package com.stealthvault.app.ui.lock

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stealthvault.app.databinding.ActivityAppLockBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppLockBinding
    private var enteredPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reset state for new lock instance
        enteredPin = ""

        // Hidden trigger: Long-press the error icon area to show PIN pad
        binding.vUnlockTrigger.setOnLongClickListener {
            binding.llFakeCrash.visibility = android.view.View.GONE
            binding.cvPinPad.visibility = android.view.View.VISIBLE
            true
        }

        setupPinPad()

        binding.btnOk.setOnClickListener {
            // Standard behavior: Go home
            exitToHome()
        }
    }

    private fun setupPinPad() {
        val buttons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                enteredPin += (it as android.widget.Button).text
                updateDots()
                if (enteredPin.length >= 4) {
                    checkPin()
                }
            }
        }

        binding.btnClear.setOnClickListener {
            enteredPin = ""
            updateDots()
        }

        binding.btnCancel.setOnClickListener {
            binding.cvPinPad.visibility = android.view.View.GONE
            binding.llFakeCrash.visibility = android.view.View.VISIBLE
            enteredPin = ""
            updateDots()
        }
    }

    private fun updateDots() {
        val dots = "•".repeat(enteredPin.length)
        binding.tvPinDisplay.text = dots
    }

    private fun checkPin() {
        // Correct implementation: Use the SecurityPreferenceManager
        val master = com.stealthvault.app.data.local.SecurityPreferenceManager(this).masterPin
        if (enteredPin == master) {
            // Success! Unlock the app and clean up
            finish()
        } else {
            android.widget.Toast.makeText(this, "Security Violation: Incorrect PIN", android.widget.Toast.LENGTH_SHORT).show()
            enteredPin = ""
            updateDots()
            // Optional: Log intruder attempt?
        }
    }

    private fun exitToHome() {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Instead of finishing, always go HOME to stay secure
        exitToHome()
    }
}
