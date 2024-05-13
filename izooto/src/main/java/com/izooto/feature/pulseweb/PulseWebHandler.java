package com.izooto.feature.pulseweb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.izooto.Util;

public class PulseWebHandler {
    private final String IZ_CLASSNAME = "PulseWebHandler";
    private ProgressBar progressBar;
    private final Context context;
    private final String url;

    private PulseWebHandler(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    public static PulseWebHandler addConfiguration(Context context, String url) {
        if (context == null || url == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        return new PulseWebHandler(context, url);
    }

    public void createWebView(LinearLayout layout, Boolean shouldShowProgressBar) {
        if (context == null || url == null || layout == null) {
            return;
        }
        try {
            WebView webView = new WebView(context);
            handleWebSettings(webView);
            webView.setVerticalScrollBarEnabled(true);
            webView.clearCache(true);
            webView.clearHistory();
            webView.setWebViewClient(new PulseWebViewClient(context));

            if (shouldShowProgressBar) {
                progressBar = createAndReturnProgressBar(context);
                webView.setWebChromeClient(new PulseWebChromeClient(context, progressBar));
            }

            webView.addJavascriptInterface(new PulseJsInterface(context), "Android");
            webView.loadUrl(url);
            applyConfigAndAddView(layout, webView, progressBar, shouldShowProgressBar);

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "createWebView");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void handleWebSettings(final WebView webView) {
        if (webView == null) {
            return;
        }
        try {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setDomStorageEnabled(true);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "handleWebSettings");
        }
    }

    private ProgressBar createAndReturnProgressBar(Context context) {
        if (context == null) {
            return null;
        }
        try {
            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(0xFFCCCCCC, android.graphics.PorterDuff.Mode.MULTIPLY);
            progressBar.setVisibility(View.GONE);
            return progressBar;
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "createAndReturnProgressBar");
            return null;
        }
    }

    private void applyConfigAndAddView(LinearLayout layout, WebView webView, ProgressBar progressBar, Boolean shouldShowProgressBar) {
        if (layout == null || webView == null) {
            return;
        }
        try {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(layoutParams);
            layout.setOrientation(LinearLayout.VERTICAL);

            if (shouldShowProgressBar && progressBar != null) {
                layout.addView(progressBar);
            }

            layout.addView(webView);

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "applyConfigAndAddView");
        }
    }

}

