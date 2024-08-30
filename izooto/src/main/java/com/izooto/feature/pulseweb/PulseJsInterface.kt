package com.izooto.feature.pulseweb

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import com.izooto.AppConstant
import com.izooto.PreferenceUtil
import com.izooto.Util

internal class PulseJsInterface(
    private val context: Context?,
    private val webView: WebView?,
    private val cardLayout: CardView?,
    private val scrollView: ScrollView?
) {
    @JavascriptInterface
    fun onButtonClick(clickURL: String?, data: Int) {
        if (context == null || webView == null || clickURL.isNullOrEmpty()) {
            return
        }
        if (data == 0) {
            try {
                val displayMetrics = Resources.getSystem().displayMetrics
                val heightPixels = displayMetrics.heightPixels
                val preferenceUtil = PreferenceUtil.getInstance(context)
                preferenceUtil.setBooleanData(AppConstant.PW_EVENTS, false)

                webView.post {
                    try {
                        val layoutParams = webView.layoutParams as ViewGroup.LayoutParams
                        layoutParams.height =
                            heightPixels // or ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, etc.
                        webView.layoutParams = layoutParams
                    } catch (ex: Exception) {
                        Util.handleExceptionOnce(
                            context,
                            ex.toString(),
                            "PulseJsInterface",
                            "onButtonClick-webView"
                        )
                    }
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
    fun onClicked() {
        if (context == null || webView == null || cardLayout == null || scrollView == null) {
            return
        }

        try {
            val preferenceUtil = PreferenceUtil.getInstance(context)
            scrollView.scrollTo(0, preferenceUtil.getIntData("scrollEvents"))
            cardLayout.post {
                try {
                    val layoutParams = cardLayout.layoutParams as ViewGroup.LayoutParams
                    layoutParams.height =
                        ViewGroup.LayoutParams.WRAP_CONTENT // or another valid size
                    cardLayout.layoutParams = layoutParams
                } catch (e: Exception) {
                    Util.handleExceptionOnce(
                        context,
                        e.toString(),
                        "PulseJsInterface",
                        "onClicked-cardLayout"
                    )
                }
            }
        } catch (e: Exception) {
            Util.handleExceptionOnce(
                context,
                e.toString(),
                "PulseJsInterface",
                "onClicked"
            )
        }
    }

}
