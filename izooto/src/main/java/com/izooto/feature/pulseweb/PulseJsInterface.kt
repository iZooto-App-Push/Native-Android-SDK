package com.izooto.feature.pulseweb

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.izooto.AppConstant
import com.izooto.PreferenceUtil
import com.izooto.Util
import kotlin.concurrent.thread


internal class PulseJsInterface(private val context: Context?, val webView: WebView) {
    @JavascriptInterface
    fun onButtonClick(clickURL: String?, data: Int) {
        if (context == null || clickURL.isNullOrEmpty()) {
            return
        }
        if(data == 0) {
            try {
                val displayMetrics = Resources.getSystem().displayMetrics
                val heightPixels = displayMetrics.heightPixels
                val preferenceUtil = PreferenceUtil.getInstance(context)
                preferenceUtil.setBooleanData(AppConstant.PW_EVENTS, false)
                webView.post {
                    val layoutParams = webView.layoutParams as LinearLayout.LayoutParams
                    layoutParams.height =
                        heightPixels // or ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, etc.
                    webView.layoutParams = layoutParams
                }
            } catch (ex: Exception) {
                Util.handleExceptionOnce(
                    context,
                    ex.toString(),
                    "PulseJsInterface",
                    "onButtonClick"
                )
            }
        }

    }
    @JavascriptInterface
    fun onClicked()
    {
        try {
            thread {
                webView.post {
                    val layoutParams = webView.layoutParams as LinearLayout.LayoutParams
                    layoutParams.height = 0
                    webView.layoutParams = layoutParams
                }
            }

            Thread.sleep(50)
            webView.post {
                val layoutParams = webView.layoutParams as LinearLayout.LayoutParams
                layoutParams.height =
                    ViewGroup.LayoutParams.WRAP_CONTENT // or ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, etc.
                webView.layoutParams = layoutParams
            }

        }
        catch (ex: Exception) {
            Util.handleExceptionOnce(context, ex.toString(), "PulseJsInterface", "onButtonClick")

        }
    }

}
