package com.izooto.feature.pulseweb

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout

interface PWInterface {
    fun addConfiguration(context: Context?, pwUrl: String?)
    fun createWebView(
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
    )

    fun createWebView(
        layout: CoordinatorLayout?,
        shouldShowProgressBar: Boolean,
        pulseTitle: String,
        titleEnable: String,
        titleColor: String,
        titleMargin: Int,
        titleSize: Int,
        titlePosition: String,
        pulseURL: String?
    )

}
