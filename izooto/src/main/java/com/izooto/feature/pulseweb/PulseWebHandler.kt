package com.izooto.feature.pulseweb

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.izooto.Util


internal class PulseWebHandler : PWInterface {
    private var progressBar: ProgressBar? = null
    private var context: Context? = null
    private var url: String? = null

    override fun addConfiguration(context: Context?, pwUrl: String?) {
        requireNotNull(context) { "Context cannot be null" }
        requireNotNull(pwUrl) { "URL cannot be null" }
        this.context = context
        this.url = pwUrl
    }

    override fun createWebView(layout: CoordinatorLayout?, shouldShowProgressBar: Boolean) {
        if (context == null || url == null || layout == null) {
            return
        }
        try {
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            val webView = WebView(context!!)
            webView.layoutParams = layoutParams
            handleWebSettings(webView)
            clearWebViewSessionAndCookies(webView)
            webView.webChromeClient = WebChromeClient()
            webView.evaluateJavascript(
                "(function() { localStorage.removeItem('__lsv__'); })();", null
            )
            webView.webViewClient = PulseWebViewClient(context)
            webView.loadUrl(url.toString())

            webView.addJavascriptInterface(PulseJsInterface(context, webView), "Android")
            if (shouldShowProgressBar) {
                progressBar = createAndReturnProgressBar(context)
                webView.webChromeClient = progressBar?.let { PulseWebChromeClient(context!!, it) }
            }
            applyConfigAndAddView(layout, webView, progressBar, shouldShowProgressBar)
        } catch (e: Exception) {
            Util.handleExceptionOnce(context, e.toString(), "PulseWebHandler", "createWebView")
        }
    }

    private fun clearWebViewSessionAndCookies(webView: WebView?) {
        if (webView == null) {
            return
        }
        try {
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
            webView.clearCache(true)
            webView.clearHistory()
            webView.clearFormData()
        } catch (ex: Exception) {
            Util.handleExceptionOnce(
                context,
                ex.toString(),
                "PulseWebHandler",
                "clearWebViewSessionAndCookies"
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun handleWebSettings(webView: WebView?) {
        if (webView == null) {
            return
        }
        try {
            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.loadsImagesAutomatically = true
            webSettings.mediaPlaybackRequiresUserGesture = false
        } catch (e: Exception) {
            Util.handleExceptionOnce(context, e.toString(), "PulseWebHandler", "handleWebSettings")
        }
    }

    private fun createAndReturnProgressBar(context: Context?): ProgressBar? {
        return if (context == null) {
            null
        } else try {
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val progressBar = ProgressBar(context)
            layoutParams.gravity = Gravity.CENTER
            progressBar.layoutParams = layoutParams
            progressBar.isIndeterminate = true
            progressBar.indeterminateDrawable.setColorFilter(-0x333334, PorterDuff.Mode.MULTIPLY)
            progressBar.visibility = View.GONE
            progressBar
        } catch (e: Exception) {
            Util.handleExceptionOnce(
                context, e.toString(), "PulseWebHandler", "createAndReturnProgressBar"
            )
            null
        }
    }

    private fun applyConfigAndAddView(
        layout: CoordinatorLayout?,
        webView: WebView?,
        progressBar: ProgressBar?,
        shouldShowProgressBar: Boolean
    ) {
        if (context == null || layout == null || webView == null) {
            return
        }
        try {
            val layoutParams = CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams.behavior = AppBarLayout.ScrollingViewBehavior()
            val nestedScrollView = NestedScrollView(context!!)
            nestedScrollView.layoutParams = layoutParams
            val linearParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            val linearLayout = LinearLayout(context)
            linearLayout.layoutParams = linearParams
            linearLayout.orientation = LinearLayout.VERTICAL
            if (shouldShowProgressBar && progressBar != null) {
                linearLayout.addView(progressBar)
            }
            linearLayout.addView(webView)
            nestedScrollView.addView(linearLayout)
            nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                if (v.getChildAt(0).bottom <= nestedScrollView.height + scrollY) {
                    webView.evaluateJavascript("window.Android.loadMore();", null)
                }
            })
            layout.addView(nestedScrollView)
        } catch (e: Exception) {
            Util.handleExceptionOnce(
                context, e.toString(), "PulseWebHandler", "applyConfigAndAddView"
            )
        }
    }
}
