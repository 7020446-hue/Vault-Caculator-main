package com.stealthvault.app.ui.vault.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.stealthvault.app.R
import com.stealthvault.app.data.security.EncryptionManager
import com.stealthvault.app.databinding.FragmentMediaDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MediaDetailFragment : Fragment(R.layout.fragment_media_detail) {

    @Inject
    lateinit var encryptionManager: EncryptionManager
    private var tempFile: File? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMediaDetailBinding.bind(view)

        val filePath = arguments?.getString("filePath") ?: return
        val fileType = arguments?.getString("fileType") ?: "Photo"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val encryptedFile = File(filePath)
                val decryptedFile = withContext(Dispatchers.IO) {
                    val temp = File(requireContext().cacheDir, "temp_view_${encryptedFile.name}")
                    encryptionManager.decryptFile(encryptedFile, temp)
                    temp
                }
                tempFile = decryptedFile

                if (fileType == "Photo") {
                    binding.ivFullMedia.visibility = View.VISIBLE
                    Glide.with(this@MediaDetailFragment)
                        .load(decryptedFile)
                        .into(binding.ivFullMedia)
                } else {
                    binding.vvMedia.visibility = View.VISIBLE
                    binding.vvMedia.setVideoURI(Uri.fromFile(decryptedFile))
                    binding.vvMedia.start()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to decrypt media", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Securely wipe the temp file
        tempFile?.let {
            if (it.exists()) it.delete()
        }
    }
}
