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
        setupHome()

        // Restore browsing session if user switched tabs
        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
            val hasHistory = binding.webView.copyBackForwardList().size > 0
            if (hasHistory) {
                binding.homeContainer.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
                binding.etUrl.setText(binding.webView.url ?: "")
            }
        }

        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            val url = binding.etUrl.text.toString()
            if (url.isNotEmpty()) {
                performSearch(url)
            }
            // Hide keyboard
            val imm = context?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(binding.etUrl.windowToken, 0)
            true
        }

        // Smart Back Navigation
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.visibility == View.VISIBLE) {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack() // Navigate web history
                    } else {
                        // Return to Incognito Homepage
                        binding.webView.visibility = View.GONE
                        binding.webView.clearHistory()
                        binding.homeContainer.visibility = View.VISIBLE
                        binding.etUrl.setText("")
                    }
                } else {
                    // Not browsing, pass back to system
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun setupHome() {
        binding.scGoogle.setOnClickListener { performSearch("https://www.google.com") }
        binding.scDuckGo.setOnClickListener { performSearch("https://duckduckgo.com") }
        binding.scWiki.setOnClickListener { performSearch("https://wikipedia.org") }
    }

    private fun performSearch(query: String) {
        val cleanQuery = query.trim()
        val fullUrl = when {
            // Force search with \ prefix (e.g., "\google" or "\g query")
            cleanQuery.startsWith("\\") -> {
                val command = cleanQuery.substring(1).split(" ", limit = 2)
                if (command.size > 1) {
                    val searchEngine = command[0].lowercase()
                    val searchTerm = java.net.URLEncoder.encode(command[1], "UTF-8")
                    when (searchEngine) {
                        "g", "google" -> "https://www.google.com/search?q=$searchTerm"
                        "d", "duck", "ddg" -> "https://duckduckgo.com/?q=$searchTerm"
                        "w", "wiki" -> "https://en.wikipedia.org/wiki/$searchTerm"
                        else -> "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(cleanQuery.substring(1), "UTF-8")}"
                    }
                } else {
                    "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(cleanQuery.substring(1), "UTF-8")}"
                }
            }
            // Normal URL detection
            cleanQuery.startsWith("http://") || cleanQuery.startsWith("https://") -> cleanQuery
            cleanQuery.contains(".") && !cleanQuery.contains(" ") -> "https://$cleanQuery"
            // Default search
            else -> "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(cleanQuery, "UTF-8")}"
        }
        
        binding.homeContainer.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
        binding.webView.loadUrl(fullUrl)
        binding.etUrl.setText(fullUrl)
    }

    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.etUrl.setText(url)
                }
            }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.webView?.saveState(outState)
    }
}
