package com.stealthvault.app.ui.vault.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentMediaDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MediaDetailFragment : Fragment(R.layout.fragment_media_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMediaDetailBinding.bind(view)

        val filePath = arguments?.getString("filePath") ?: return
        val fileType = arguments?.getString("fileType") ?: "Photo"

        if (fileType == "Photo") {
            binding.ivFullMedia.visibility = View.VISIBLE
            Glide.with(this)
                .load(File(filePath))
                .into(binding.ivFullMedia)
        } else {
            binding.vvMedia.visibility = View.VISIBLE
            binding.vvMedia.setVideoURI(Uri.fromFile(File(filePath)))
            binding.vvMedia.start()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
