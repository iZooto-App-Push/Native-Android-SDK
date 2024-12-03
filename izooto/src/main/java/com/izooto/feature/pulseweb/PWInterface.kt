package com.izooto.feature.pulseweb

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView

/**
 * Create a interface for handling the creating view
 */
interface PWInterface {
    fun addConfiguration(context: Context?, pwUrl: String?)

    // for scroll view
    fun createScrollView(
                        scrollView: ScrollView,
                        layout: LinearLayout?,
                        shouldShowProgressBar: Boolean,
    )
    // for coordinate layout
    fun createCoordinateView(
        context: Context?,
        layout: NestedScrollView?,
        mainLayout: LinearLayout?,
        shouldShowProgressBar: Boolean,
    )
}
