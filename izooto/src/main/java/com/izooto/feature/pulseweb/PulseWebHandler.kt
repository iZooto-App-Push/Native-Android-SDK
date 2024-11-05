package com.izooto.feature.pulseweb

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.AppBarLayout
import com.izooto.AppConstant
import com.izooto.PreferenceUtil
import com.izooto.Util
import kotlin.math.abs


internal class PulseWebHandler : PWInterface {
    private var progressBar: ProgressBar? = null
    private var context: Context? = null
    private var url: String? = null
    private var initFirst: Boolean = true

    override fun addConfiguration(context: Context?, pwUrl: String?) {
        requireNotNull(context) { "Context cannot be null" }
        requireNotNull(pwUrl) { "URL cannot be null" }
        this.context = context
        this.url = pwUrl
    }

    /**
    layout - Linear layout
    shouldShowProgressBar - boolean value true/false
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun createWebView(
        scrollView: ScrollView?,
        layout: LinearLayout?,
        shouldShowProgressBar: Boolean,
        pulseTitle: String,
        titleEnable: String,
        titleColor: String,
        titleMargin: Int,
        titleSize: Int,
        titlePosition: String,
        pulseURL: String?
    ) {
        if (context == null || pulseURL == null || layout == null || scrollView == null) {
            return
        }
        try {
            val label = createAndReturnLabel(
                context, pulseTitle, titleEnable, titleColor, titleMargin, titleSize, titlePosition
            )
            if (label != null) {
                layout.addView(label)
            }

            // Create and configure the CardView
            val cardView = CardView(context!!).apply {
                try {
                    radius = 16f // Set corner radius
                    setCardBackgroundColor(Color.WHITE) // Set background color for the CardView
                    cardElevation = 8f // Set elevation for shadow effect
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(titleMargin, titleMargin, titleMargin, titleMargin) // Set margins around the CardView
                    }
                } catch (ex: Exception) {
                    Log.d(AppConstant.APP_NAME_TAG, ex.toString())
                }
            }


            // Create and configure the WebView
            val webView = WebView(context!!).apply {
                try {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(topMargin, topMargin, topMargin, topMargin) // Set margins around the CardView
                    }
                    handleWebSettings(this)
                    clearWebViewSessionAndCookies(this)
                    webChromeClient = WebChromeClient()
                    evaluateJavascript(
                        "(function() { localStorage.removeItem('__lsv__'); })();", null
                    )
                    webViewClient = PulseWebViewClient(context)
                     loadUrl(pulseURL)
                    addJavascriptInterface(
                        PulseJsInterface(context, this, cardView, scrollView), "Android"
                    )
                } catch (ex: Exception) {
                    Log.d(AppConstant.APP_NAME_TAG, ex.toString())
                }
            }

            webView.clearFocus()
            webView.isFocusable = false
            webView.isFocusableInTouchMode = false
            if(hasAdMobLibrary()){
                 CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
                webView.getSettings().mediaPlaybackRequiresUserGesture = false
                MobileAds.registerWebView(webView)
            }

            // Optionally, add a progress bar if needed
            if (shouldShowProgressBar) {
                progressBar = createAndReturnProgressBar(context)
                webView.webChromeClient = progressBar?.let { PulseWebChromeClient(context!!, it) }
            }

            // Add the WebView to the CardView
            cardView.addView(webView)

            // Finally, add the CardView to your layout
            applyConfigAndAddView(layout, cardView, progressBar, shouldShowProgressBar)
            layout.addView(cardView)
            val preferenceUtils = PreferenceUtil.getInstance(context)
            scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                try {
                    val location = IntArray(2)
                    cardView.getLocationOnScreen(location)
                    val cardViewTopOnScreen = location[1]

                    // Get the location of the ScrollView's top
                    val scrollViewLocation = IntArray(2)
                    scrollView.getLocationOnScreen(scrollViewLocation)
                    val scrollViewTopOnScreen = scrollViewLocation[1]

                    // Define a threshold for detection accuracy
                    val threshold = 10 // You can adjust this value

                    // Check if CardView top is within the threshold of the ScrollView top
                    if (abs(cardViewTopOnScreen - scrollViewTopOnScreen) <= threshold) {
                        val value = scrollY - 100
                        preferenceUtils.setIntData("scrollEvents", value)
                    }
                } catch (ex: Exception) {
                    Log.d(AppConstant.APP_NAME_TAG, ex.toString())
                }
            }
        } catch (e: Exception) {
            Util.handleExceptionOnce(context, e.toString(), "PulseWebHandler", "createWebView")
        }
    }

    /**
    layout - CoordinateLayout
    shouldShowProgressBar - boolean value true/false
     */
    override fun createWebView(
        layout: CoordinatorLayout?,
        shouldShowProgressBar: Boolean,
        pulseTitle: String,
        titleEnable: String,
        titleColor: String,
        titleMargin: Int,
        titleSize: Int,
        titlePosition: String,
        pulseURL: String?
    ) {
        if (context == null || url == null || layout == null || pulseURL == null) {
            return
        }
        try {
            val displayMetrics = Resources.getSystem().displayMetrics
            val heightPixels = displayMetrics.heightPixels
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightPixels + 10
            )

            val webView = WebView(context!!)
            webView.layoutParams = layoutParams
            handleWebSettings(webView)
            clearWebViewSessionAndCookies(webView)
            webView.webChromeClient = WebChromeClient()
            webView.evaluateJavascript("(function() { localStorage.removeItem('__lsv__'); })();", null)
            webView.webViewClient = PulseWebViewClient(context)
            webView.loadUrl(pulseURL.toString())

            webView.addJavascriptInterface(
                PulseJsInterface(context, webView, null, null), "Android"
            )
            if(hasAdMobLibrary()){
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
                webView.getSettings().mediaPlaybackRequiresUserGesture = false
                MobileAds.registerWebView(webView)
            }
            if (shouldShowProgressBar) {
                progressBar = createAndReturnProgressBar(context)
                webView.webChromeClient = progressBar?.let { PulseWebChromeClient(context!!, it) }
            }
            applyConfigAndAddView(
                layout,
                webView,
                progressBar,
                shouldShowProgressBar,
                pulseTitle,
                titleEnable,
                titleColor,
                titleMargin,
                titleSize,
                titlePosition,
                pulseURL
            )
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
                context, ex.toString(), "PulseWebHandler", "clearWebViewSessionAndCookies"
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
            webView.overScrollMode = View.OVER_SCROLL_NEVER
            webView.clearFocus()
            webView.isVerticalScrollBarEnabled = false
            webView.isFocusable = false
            webView.isFocusableInTouchMode = false
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

    private fun createAndReturnLabel(
        context: Context?,
        pulseTitle: String,
        titleEnable: String,
        titleColor: String,
        titleMargin: Int,
        titleSize: Int,
        titlePosition: String
    ): TextView? {
        return if (context == null || titleEnable != "1") {
            null
        } else try {
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = when (titlePosition) {
                        "right" -> Gravity.END
                        "center" -> Gravity.CENTER
                        else -> Gravity.START
                    }
                }
                visibility = View.VISIBLE
                text = pulseTitle
                textSize = titleSize.toFloat()
                setTypeface(null, Typeface.BOLD)

                // Parse color with fallback to default
                val defaultColor = "#000000"
                setTextColor(runCatching {
                    Color.parseColor(titleColor)
                }.getOrElse {
                    Color.parseColor(defaultColor)
                })
                setPadding(titleMargin, titleMargin, titleMargin, titleMargin)
            }
            textView
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
        shouldShowProgressBar: Boolean,
        pulseTitle: String,
        titleEnable: String,
        titleColor: String,
        titleMargin: Int,
        titleSize: Int,
        titlePosition: String,
        pulseURL: String?
    ) {
        if (context == null || layout == null || webView == null) {
            return
        }
        try {
            val label = createAndReturnLabel(
                context, pulseTitle, titleEnable, titleColor, titleMargin, titleSize, titlePosition
            )

            val linearParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            val linearLayout = LinearLayout(context)
            linearLayout.layoutParams = linearParams
            linearLayout.orientation = LinearLayout.VERTICAL

            if (label != null) {
                linearLayout.addView(label)
            }

            if (shouldShowProgressBar && progressBar != null) {
                linearLayout.addView(progressBar)
            }

            linearLayout.addView(webView)
            val nestedScrollView = createAndReturnNestedScrollView(context, webView, linearLayout)
            if (nestedScrollView != null) {
                layout.addView(nestedScrollView)
            }
        } catch (e: Exception) {
            Util.handleExceptionOnce(
                context, e.toString(), "PulseWebHandler", "applyConfigAndAddView"
            )
        }
    }

    // add linear layout and WebView to layout
    private fun applyConfigAndAddView(
        layout: LinearLayout?,
        cardView: CardView?,
        progressBar: ProgressBar?,
        shouldShowProgressBar: Boolean
    ) {
        if (context == null || layout == null || cardView == null) {
            return
        }
        try {

            if (shouldShowProgressBar && progressBar != null) {
                layout.addView(progressBar)
            }
        } catch (e: Exception) {
            Util.handleExceptionOnce(
                context, e.toString(), "PulseWebHandler", "applyConfigAndAddView"
            )
        }
    }

    private fun createAndReturnNestedScrollView(
        context: Context?, webView: WebView?, linearLayout: LinearLayout?
    ): NestedScrollView? {
        if (context == null || webView == null || linearLayout == null) {
            return null
        }
        try {
            val layoutParams = CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams.behavior = AppBarLayout.ScrollingViewBehavior()
            val nestedScrollView = NestedScrollView(context)
            nestedScrollView.layoutParams = layoutParams
            nestedScrollView.addView(linearLayout)

            val preferenceUtil = PreferenceUtil.getInstance(context)
            nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                try {
                    if (v.getChildAt(0).bottom <= nestedScrollView.height + scrollY) {
                        webView.evaluateJavascript("window.Android.loadMore();", null)
                    }

                    if (preferenceUtil.getBoolean(AppConstant.PW_EVENTS) || initFirst) {
                        webView.post {
                            val webViewParams = webView.layoutParams as LinearLayout.LayoutParams
                            webViewParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                            webView.layoutParams = webViewParams
                            initFirst = false
                        }
                    }

                } catch (ex: Exception) {
                    Log.v(AppConstant.APP_NAME_TAG, "Failed to handle scroll event")
                }
            })
            return nestedScrollView
        } catch (ex: Exception) {
            Util.handleExceptionOnce(
                context, ex.toString(), "PulseWebHandler", "createAndReturnNestedScrollView"
            )
        }
        return null
    }
    fun hasAdMobLibrary(): Boolean {
        return try {
            Class.forName("com.google.android.gms.ads.MobileAds")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

}
