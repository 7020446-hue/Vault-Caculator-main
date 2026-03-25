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
            
            // Secret trigger: Long press history to use Biometrics
            binding.tvHistory.setOnLongClickListener {
                if (securityPrefs.isSetupComplete) {
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    Toast.makeText(this, "Set Master PIN first", Toast.LENGTH_SHORT).show()
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

        binding.btnEquals.setOnClickListener {
            checkUnlock()
        }
    }

    private fun checkUnlock() {
        if (!securityPrefs.isSetupComplete) {
            setupPins()
            return
        }

        val master = securityPrefs.masterPin
        val decoy = securityPrefs.decoyPin

        when (currentInput) {
            master -> launchVault(isDecoy = false)
            decoy -> launchVault(isDecoy = true)
            else -> performMath()
        }
        currentInput = ""
    }

    private fun setupPins() {
        val input = currentInput
        if (input.isEmpty()) return

        if (securityPrefs.masterPin == null) {
            securityPrefs.masterPin = input
            Toast.makeText(this, "Master PIN set. Now enter a DECOY PIN and press '='", Toast.LENGTH_LONG).show()
        } else if (securityPrefs.decoyPin == null) {
            if (input == securityPrefs.masterPin) {
                Toast.makeText(this, "Decoy PIN must be different from Master PIN", Toast.LENGTH_SHORT).show()
            } else {
                securityPrefs.decoyPin = input
                securityPrefs.isSetupComplete = true
                Toast.makeText(this, "Setup complete! Enter Master PIN to unlock vault.", Toast.LENGTH_LONG).show()
            }
        }
        currentInput = ""
        binding.tvDisplay.text = "0"
        binding.tvHistory.text = ""
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
