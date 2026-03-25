package com.stealthvault.app.ui.vault.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.stealthvault.app.R
import com.stealthvault.app.databinding.FragmentBrowserBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowserFragment : Fragment(R.layout.fragment_browser) {

    private var _binding: FragmentBrowserBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBrowserBinding.bind(view)

        setupWebView()

        binding.etUrl.setOnEditorActionListener { _, _, _ ->
            val url = binding.etUrl.text.toString()
            if (url.isNotEmpty()) {
                val fullUrl = if (url.startsWith("http")) url else "https://www.google.com/search?q=$url"
                binding.webView.loadUrl(fullUrl)
            }
            true
        }
    }

    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                domStorageEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Purge everything for ultimate stealth
        binding.webView.apply {
            clearHistory()
            clearCache(true)
            clearFormData()
        }
        _binding = null
    }
}
