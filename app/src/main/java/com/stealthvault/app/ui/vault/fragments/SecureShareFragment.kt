package com.stealthvault.app.ui.vault.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.stealthvault.app.R
import com.stealthvault.app.data.local.entities.VaultFile
import com.stealthvault.app.databinding.FragmentSecureShareBinding
import com.stealthvault.app.ui.vault.VaultViewModel
import com.stealthvault.app.utils.QrHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class SecureShareFragment : Fragment(R.layout.fragment_secure_share) {

    private val args: SecureShareFragmentArgs by navArgs()
    private val viewModel: VaultViewModel by activityViewModels()
    private var shareTimer: CountDownTimer? = null
    private var tempShareFile: File? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSecureShareBinding.bind(view)

        // Mock VaultFile from args
        val vaultFile = VaultFile(
            fileName = args.fileName,
            encryptedPath = args.filePath,
            originalPath = "",
            fileType = "Photo",
            size = 0
        )

        // Prepare temporary share
        tempShareFile = viewModel.prepareSecureShare(requireContext(), vaultFile)
        
        tempShareFile?.let { file ->
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            val shareText = uri.toString()
            
            // Generate QR Code
            val qrBitmap = QrHelper.generateQrCode(shareText, 512)
            binding.ivQrCode.setImageBitmap(qrBitmap)

            // Setup standard share
            binding.btnShareLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Securely Share File"))
            }
        }

        // Start 10-minute timer
        startExpiryTimer(binding)
    }

    private fun startExpiryTimer(binding: FragmentSecureShareBinding) {
        shareTimer = object : CountDownTimer(10 * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = millisUntilFinished / 1000 / 60
                val sec = (millisUntilFinished / 1000) % 60
                binding.tvExpiryInfo.text = String.format("Expires in %02d:%02d", min, sec)
            }

            override fun onFinish() {
                binding.tvExpiryInfo.text = "Link Expired & Deleted"
                binding.btnShareLink.isEnabled = false
                binding.ivQrCode.alpha = 0.2f
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shareTimer?.cancel()
    }
}
