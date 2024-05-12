package com.izooto.feature.pulseweb;

import android.content.Context;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.izooto.Util;

public class PulseWebChromeClient extends WebChromeClient {
    private final String IZ_CLASSNAME = "PulseWebChromeClient";
    private final Context context;
    private final ProgressBar progressBar;

    protected PulseWebChromeClient(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        try {
            progressBar.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "onProgressChanged");
        }
    }

}
