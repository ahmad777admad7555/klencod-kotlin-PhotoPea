package com.ahmedaldheeb.photopea

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: TextView

    private val homeUrl = "https://www.photopea.com/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // حاوية رئيسية
        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor("#1A1A1A"))

        // WebView
        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = false
                displayZoomControls = false
                mediaPlaybackRequiresUserGesture = false
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            }

            // قبول الكوكيز (Photopea يحتاجها)
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            // عميل التصفح
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        progressBar.visibility = View.GONE
                        showError(error?.description?.toString() ?: "Unknown error")
                    }
                }
            }

            // شريط التحميل
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.progress = newProgress
                    progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }

            loadUrl(homeUrl)
        }

        // شريط تقدم أفقي أعلى
        progressBar = ProgressBar(
            this, null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            visibility = View.VISIBLE
        }
        val progressLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8
        )
        progressLp.gravity = android.view.Gravity.TOP
        root.addView(progressBar, progressLp)

        // شاشة الخطأ (مخفية افتراضياً)
        errorView = TextView(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            visibility = View.GONE
            setPadding(48, 48, 48, 48)
            setOnClickListener {
                visibility = View.GONE
                webView.visibility = View.VISIBLE
                webView.reload()
            }
        }
        root.addView(
            errorView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // WebView يملأ الشاشة
        root.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    private fun showError(msg: String) {
        webView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        errorView.text = "تعذّر تحميل الصفحة\n\n$msg\n\nاضغط لإعادة المحاولة"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}