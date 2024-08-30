package com.izooto.feature.pulseweb

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.browser.customtabs.CustomTabsIntent
import com.izooto.AppConstant
import com.izooto.PreferenceUtil
import com.izooto.Util

internal class PulseWebViewClient(private val context: Context?) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (context == null) {
            return true
        }

        try {
            view?.clearFocus()
            view?.isFocusable = false
            view?.isFocusableInTouchMode = false

            val url = request?.url.toString()
            if (url.startsWith("http://") || url.startsWith("https://")) {
                try {
                    // Evaluate JavaScript safely
                    view?.evaluateJavascript(
                        "document.location.hash = '';",
                        null
                    )
                    // Post layout change, safely handle layoutParams casting
                    view?.post {
                        try {
                            val parent = view.parent
                            val layoutParams = when (parent) {
                                is LinearLayout -> view.layoutParams as LinearLayout.LayoutParams
                                is FrameLayout -> view.layoutParams as FrameLayout.LayoutParams
                                else -> view.layoutParams as ViewGroup.LayoutParams
                            }
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            view.layoutParams =layoutParams
                        } catch (e: ClassCastException) {
                            Util.handleExceptionOnce(
                                context,
                                e.toString(),
                                "PulseWebViewClient",
                                "shouldOverrideUrlLoading-layoutParams"
                            )
                        }
                    }

                    // Preference and intent handling
                    val preferenceUtil = PreferenceUtil.getInstance(context)
                    preferenceUtil.setBooleanData(AppConstant.PW_EVENTS, true)
                    val intent = CustomTabsIntent.Builder().build()
                    intent.launchUrl(context, request!!.url)
                    return true

                } catch (e: Exception) {
                    // Handle any exception that occurs within this try block
                    Util.handleExceptionOnce(
                        context,
                        e.toString(),
                        "PulseWebViewClient",
                        "shouldOverrideUrlLoading-intent"
                    )
                }
                return false
            } else {
                return true
            }

        } catch (e: Exception) {
            // Catch any exception that occurs in the outer block
            Util.handleExceptionOnce(
                context, e.toString(), "PulseWebViewClient", "shouldOverrideUrlLoading"
            )
        }
        return true
    }

    override fun onReceivedError(
        view: WebView, request: WebResourceRequest, error: WebResourceError
    ) {
        super.onReceivedError(view, request, error)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        super.onReceivedSslError(view, handler, error)
    }

    override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
        super.onUnhandledKeyEvent(view, event)
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        return super.onRenderProcessGone(view, detail)
    }

    override fun onLoadResource(view: WebView, url: String) {
        super.onLoadResource(view, url)
        try {
            view.clearFocus()
            view.isFocusable = false
            view.isFocusableInTouchMode = false
        } catch (ex: Exception) {
            Log.d(AppConstant.APP_NAME_TAG, ex.toString())
        }
    }

}
