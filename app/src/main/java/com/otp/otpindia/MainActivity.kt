package com.otp.otpindia

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.appcompat.app.AppCompatActivity
import com.otp.otpindia.databinding.ActivityMainBinding
import com.otp.otpindia.utils.NetworkUtils
import com.otp.otpindia.utils.SnackBarUtils

class MainActivity : AppCompatActivity(), OnBackPressedDispatcherOwner {

    private val defaultUrl by lazy { getString(R.string.default_url) }
    private val noInternet by lazy { getString(R.string.no_internet) }

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeWebView()
        setupOnBackPressedCallback()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        webView = binding.webView

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = true
        }

        val isNetworkAvailable = NetworkUtils.isNetworkAvailable(this)
        webSettings.cacheMode =
            if (isNetworkAvailable) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK

        if (!isNetworkAvailable) {
            SnackBarUtils.showSnackBar(binding.root, noInternet)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                updateViews(ImageView.VISIBLE, ProgressBar.VISIBLE, WebView.INVISIBLE)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                updateViews(ImageView.GONE, ProgressBar.GONE, WebView.VISIBLE)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                if (!url.isNullOrBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return true
            }
        }

        webView.loadUrl(defaultUrl)
    }

    private fun updateViews(logoVisible: Int, progressVisible: Int, webVisible: Int) {
        binding.apply {
            splashImg.visibility = logoVisible
            progressBar.visibility = progressVisible
            webView.visibility = webVisible
        }
    }

    private fun setupOnBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}