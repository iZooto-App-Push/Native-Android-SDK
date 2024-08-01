package com.izooto.feature.pulseweb

import android.content.Context
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

interface PWInterface {
    fun addConfiguration(context: Context?, pwUrl: String?)
    fun createWebView(layout: CoordinatorLayout?, shouldShowProgressBar: Boolean)
    fun createWebView(layout: LinearLayout?, shouldShowProgressBar: Boolean)
}
