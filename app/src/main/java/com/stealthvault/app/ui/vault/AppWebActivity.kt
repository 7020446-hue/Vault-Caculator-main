package com.stealthvault.app.ui.vault

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.stealthvault.app.R

class AppWebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    companion object {
        const val EXTRA_URL = "APP_URL"
        const val EXTRA_TITLE = "APP_TITLE"

        // Map installed app package names to their mobile web equivalents
        val WEB_APP_MAP = mapOf(
            "com.instagram.android"         to "https://www.instagram.com",
            "com.facebook.katana"           to "https://m.facebook.com",
            "com.facebook.lite"             to "https://m.facebook.com",
            "com.twitter.android"           to "https://mobile.twitter.com",
            "com.zhiliaoapp.musically"      to "https://www.tiktok.com",
            "com.ss.android.ugc.trill"      to "https://www.tiktok.com",
            "com.whatsapp"                  to "https://web.whatsapp.com",
            "com.snapchat.android"          to "https://web.snapchat.com",
            "com.pinterest"                 to "https://www.pinterest.com",
            "com.reddit.frontpage"          to "https://m.reddit.com",
            "com.linkedin.android"          to "https://m.linkedin.com",
            "com.spotify.music"             to "https://open.spotify.com",
            "com.netflix.mediaclient"       to "https://www.netflix.com",
            "com.amazon.mShop.android.shopping" to "https://www.amazon.com",
            "com.ebay.mobile"               to "https://m.ebay.com",
            "com.google.android.youtube"    to "https://m.youtube.com",
            "com.google.android.gm"         to "https://mail.google.com",
            "com.google.android.apps.maps"  to "https://maps.google.com",
            "com.discord"                   to "https://discord.com/app",
            "com.telegram.messenger"        to "https://web.telegram.org",
            "org.telegram.messenger"        to "https://web.telegram.org",
            "com.viber.voip"                to "https://account.viber.com",
            "com.paypal.android.p2pmobile"  to "https://www.paypal.com/myaccount",
            "com.ubercab"                   to "https://m.uber.com",
            "com.airbnb.android"            to "https://www.airbnb.com",
            "com.tumblr"                    to "https://www.tumblr.com",
            "com.microsoft.teams"           to "https://teams.microsoft.com",
            "com.slack"                     to "https://app.slack.com",
            "com.dropbox.android"           to "https://www.dropbox.com",
            "com.google.android.apps.drive" to "https://drive.google.com"
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_web)

        webView = findViewById(R.id.appWebView)
        progressBar = findViewById(R.id.webProgressBar)
        val tvNoSupportContainer: android.widget.LinearLayout = findViewById(R.id.tvNoSupport)
        val tvNoSupportText: TextView = findViewById(R.id.tvNoSupportText)

        val url = intent.getStringExtra(EXTRA_URL)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "App"
        supportActionBar?.title = title

        if (url.isNullOrEmpty()) {
            webView.visibility = View.GONE
            progressBar.visibility = View.GONE
            tvNoSupportContainer.visibility = View.VISIBLE
            tvNoSupportText.text = "❌ \"$title\" doesn't have a web version.\n\nThis app requires the native Android app to run."
            return
        }

        tvNoSupportContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE

        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    view.loadUrl(request.url.toString())
                    return true
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress < 100) {
                        progressBar.visibility = View.VISIBLE
                        progressBar.progress = newProgress
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    title?.let { supportActionBar?.title = it }
                }
            }
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                // Use a mobile user agent for proper rendering
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36"
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
            loadUrl(url)
        }
    }

    // Handle back button to navigate within webview
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear session data on close for stealth
        webView.clearHistory()
        webView.clearCache(false)
    }
}
