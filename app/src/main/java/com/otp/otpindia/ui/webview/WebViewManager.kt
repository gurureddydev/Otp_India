package com.otp.otpindia.ui.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.otp.otpindia.databinding.ActivityMainBinding
import com.otp.otpindia.ui.animation.ProgressBarAnimation
import com.otp.otpindia.utils.Constants.DELAY
import com.otp.otpindia.utils.NetworkUtils
import com.otp.otpindia.utils.Utils

class WebViewManager(
    val webView: WebView,
    private val binding: ActivityMainBinding,
    private val defaultUrl: String,
    private val noInternetMessage: String
) {

    private var isFirstTimeLaunch = true

    init {
        initializeWebView()
        showInitialAnimation()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        with(webView.settings) {
            javaScriptEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }

            val isNetworkAvailable = NetworkUtils.isNetworkAvailable(webView.context)
            cacheMode =
                if (isNetworkAvailable) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK

            if (!isNetworkAvailable) {
                Utils.showSnackBar(binding.root, noInternetMessage)
            }
        }

        binding.circularPb.visibility = View.GONE
        webView.webViewClient = CustomWebViewClient()
        webView.webChromeClient = CustomWebChromeClient()
    }

    private fun showInitialAnimation() {
        if (isFirstTimeLaunch) {
            updateViews(View.VISIBLE, View.INVISIBLE)
            isFirstTimeLaunch = false

            showLogoAndProgressBar()
            webView.loadUrl(defaultUrl)
        }
    }

    fun showLogoAndProgressBar() {
        val progressBar = binding.horizontalProgressBar
        val leafImageView = binding.leafImageView
        binding.circularPb.visibility = View.GONE
        val progressListener = object : ProgressBarAnimation.OnProgressListener {
            override fun onProgress(progress: Int) {
                val progressBarWidth =
                    progressBar.width - progressBar.paddingStart - progressBar.paddingEnd
                val leafPosition = (progressBarWidth * (progress / 100f)).toInt()
                updateLeafPosition(leafImageView, leafPosition)

                if (progress >= 99) {
                    leafImageView.visibility = View.GONE
                }
            }
        }

        val progressBarAnimation =
            ProgressBarAnimation(progressBar, leafImageView, 0, 100, progressListener)
        progressBarAnimation.duration = DELAY
        progressBar.startAnimation(progressBarAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.apply {
                iconImageView.visibility = View.GONE
                frameLayout.visibility = View.GONE
                webViewContainer.visibility = View.VISIBLE
            }
        }, DELAY)
    }

    private fun updateLeafPosition(leafImageView: ImageView, leafPosition: Int) {
        val layoutParams = leafImageView.layoutParams as FrameLayout.LayoutParams
        layoutParams.marginStart = leafPosition
        leafImageView.layoutParams = layoutParams
    }

    private fun runOnUiThread(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }

    private fun handleError(errorMessage: String) {
        Utils.showSnackBar(binding.root, errorMessage)
    }

    private fun handlePageStarted() {
        if (isFirstTimeLaunch) {
            updateViews(View.VISIBLE, View.INVISIBLE)
        } else {
            updateViews(View.INVISIBLE, View.VISIBLE)
        }
    }

    private fun handlePageFinished() {
        if (isFirstTimeLaunch) {
            isFirstTimeLaunch = false
        }
        updateViews(View.INVISIBLE, View.VISIBLE)
    }

    private fun updateViews(progressVisible: Int, webVisible: Int) {
        binding.apply {
            horizontalProgressBar.visibility = progressVisible
            webView.visibility = webVisible

            if (progressVisible == View.INVISIBLE && webVisible == View.VISIBLE) {
                horizontalProgressBar.visibility = View.GONE
            }
        }
    }

    private inner class CustomWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            runOnUiThread {
                handlePageStarted()
                binding.circularPb.visibility = View.VISIBLE
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            runOnUiThread {
                handlePageFinished()
                binding.circularPb.visibility = View.GONE
            }
        }

        override fun onReceivedHttpError(
            view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            runOnUiThread {
                handleError(
                    "HTTP Error: ${errorResponse?.reasonPhrase ?: "Unknown error"}"
                )
            }
        }
    }

    private inner class CustomWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            runOnUiThread { binding.horizontalProgressBar.progress = newProgress }
        }
    }
}
