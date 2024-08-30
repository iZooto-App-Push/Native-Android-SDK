package com.izooto.feature.pulseweb

import android.content.Context
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import com.izooto.Util

internal class PulseWebChromeClient(
    private val context: Context, private val progressBar: ProgressBar
) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        try {
            progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
        } catch (e: Exception) {
            Util.handleExceptionOnce(
                context, e.toString(), "PulseWebChromeClient", "onProgressChanged"
            )
        }
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        return true
    }
}
