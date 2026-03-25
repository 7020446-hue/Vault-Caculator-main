package com.stealthvault.app.ui.fake

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.stealthvault.app.data.local.SecurityPreferenceManager
import com.stealthvault.app.databinding.ActivityCalculatorBinding
import com.stealthvault.app.ui.vault.VaultActivity
import com.stealthvault.app.utils.CameraHelper
import dagger.hilt.android.AndroidEntryPoint
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private var currentInput = ""
    
    @Inject
    lateinit var securityPrefs: SecurityPreferenceManager
    @Inject
    lateinit var cameraHelper: CameraHelper

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            binding = ActivityCalculatorBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupBiometric()
            setupButtons()
            
            // Check for Camera Permission on first run to ensure Intruder Selfie works
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)
            }

            // Show initial setup prompt if needed
            if (!securityPrefs.isSetupComplete) {
                binding.tvDisplay.text = "0"
                binding.tvHistory.text = "CREATE MASTER PIN"
            }

            binding.tvHistory.setOnLongClickListener {
                if (securityPrefs.isSetupComplete) {
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    Toast.makeText(this, "Complete Setup First", Toast.LENGTH_SHORT).show()
                }
                true
            }
        } catch (t: Throwable) {
            val tv = android.widget.TextView(this).apply {
                text = "CRASH: " + android.util.Log.getStackTraceString(t)
                setTextColor(android.graphics.Color.RED)
                textSize = 14f
                setPadding(32, 32, 32, 32)
            }
            // Remove previous content view and show crash
            val scrollView = android.widget.ScrollView(this).apply {
                addView(tv)
                setBackgroundColor(android.graphics.Color.BLACK)
            }
            setContentView(scrollView)
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    launchVault(false)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vault Authentication")
            .setSubtitle("Authenticate to access your secure vault")
            .setNegativeButtonText("Use PIN")
            .build()
    }

    private fun setupButtons() {
        // ... (previous button setup logic)
        val allButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9,
            binding.btnDot, binding.btnAdd, binding.btnSub, binding.btnMul, binding.btnDiv
        )

        allButtons.forEach { button ->
            button.setOnClickListener {
                currentInput += (it as Button).text
                binding.tvDisplay.text = currentInput
            }
        }

        binding.btnClear.setOnClickListener {
            currentInput = ""
            binding.tvDisplay.text = "0"
        }

        binding.btnBackspace.setOnClickListener {
            if (currentInput.isNotEmpty()) {
                currentInput = currentInput.dropLast(1)
                binding.tvDisplay.text = if (currentInput.isEmpty()) "0" else currentInput
            }
        }

        binding.btnEquals.setOnClickListener {
            checkUnlock()
        }
    }

    private fun checkUnlock() {
        if (!securityPrefs.isSetupComplete) {
            setupPins()
            return
        }

        // 🔒 Lockout check: too many wrong attempts
        if (securityPrefs.isLockedOut) {
            binding.tvHistory.text = "TOO MANY ATTEMPTS"
            binding.tvDisplay.text = "⛔"
            Toast.makeText(this, "Vault locked. Wait 30 seconds.", Toast.LENGTH_LONG).show()
            currentInput = ""
            // Auto-release after 30 seconds
            binding.root.postDelayed({
                securityPrefs.isLockedOut = false
                securityPrefs.failedPinAttempts = 0
                binding.tvHistory.text = "ENTER PIN"
                binding.tvDisplay.text = "0"
            }, 30_000)
            return
        }

        val master = securityPrefs.masterPin
        val decoy = securityPrefs.decoyPin

        when (currentInput) {
            master -> {
                // ✅ Correct: reset counters, record unlock time
                securityPrefs.failedPinAttempts = 0
                securityPrefs.lastUnlockTime = System.currentTimeMillis()
                launchVault(isDecoy = false)
            }
            decoy -> {
                securityPrefs.failedPinAttempts = 0
                securityPrefs.lastUnlockTime = System.currentTimeMillis()
                launchVault(isDecoy = true)
            }
            else -> {
                // ❌ Wrong PIN
                if (currentInput.length >= 4 && !currentInput.contains("[+×÷−]".toRegex())) {
                    val attempts = securityPrefs.failedPinAttempts + 1
                    securityPrefs.failedPinAttempts = attempts
                    val maxAttempts = securityPrefs.maxFailedAttempts
                    val remaining = maxAttempts - attempts
                    
                    cameraHelper.takeIntruderPhoto(this)

                    if (attempts >= maxAttempts) {
                        securityPrefs.isLockedOut = true
                        binding.tvHistory.text = "VAULT LOCKED"
                        Toast.makeText(this, "Too many wrong attempts! Vault locked for 30s.", Toast.LENGTH_LONG).show()
                    } else {
                        binding.tvHistory.text = "WRONG ($remaining attempts left)"
                        Toast.makeText(this, "Wrong PIN. $remaining attempts remaining.", Toast.LENGTH_SHORT).show()
                    }
                    binding.tvDisplay.text = "0"
                } else {
                    performMath()
                }
            }
        }
        currentInput = ""
    }

    private fun setupPins() {
        val input = currentInput
        if (input.isEmpty() || input.length < 4) {
            Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
            return
        }

        if (securityPrefs.masterPin == null) {
            securityPrefs.masterPin = input
            binding.tvHistory.text = "CREATE DECOY PIN"
            Toast.makeText(this, "Master PIN Saved!", Toast.LENGTH_SHORT).show()
        } else if (securityPrefs.decoyPin == null) {
            if (input == securityPrefs.masterPin) {
                Toast.makeText(this, "Decoy must be different", Toast.LENGTH_SHORT).show()
            } else {
                securityPrefs.decoyPin = input
                securityPrefs.isSetupComplete = true
                binding.tvHistory.text = "VAULT SECURED"
                Toast.makeText(this, "Setup Complete!", Toast.LENGTH_LONG).show()
            }
        }
        currentInput = ""
        binding.tvDisplay.text = "0"
    }

    private fun performMath() {
        try {
            val expression = ExpressionBuilder(currentInput
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
            ).build()
            
            val result = expression.evaluate()
            binding.tvHistory.text = currentInput
            binding.tvDisplay.text = result.toString()
            currentInput = result.toString()
        } catch (e: Exception) {
            binding.tvDisplay.text = "Error"
            cameraHelper.takeIntruderPhoto(this)
        }
    }

    private fun launchVault(isDecoy: Boolean) {
        val intent = Intent(this, VaultActivity::class.java).apply {
            putExtra("IS_DECOY", isDecoy)
        }
        startActivity(intent)
        finish()
    }
}
