package com.izooto.feature.pulseweb;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import androidx.browser.customtabs.CustomTabsIntent;

import com.izooto.Util;

public class PulseJsInterface {
    private final String IZ_CLASSNAME = "PulseJsInterface";
    private final Context context;

    protected PulseJsInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void onButtonClick(String clickURL, int data) {
        if (context == null || clickURL == null || clickURL.isEmpty()) {
            return;
        }
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            customTabsIntent.launchUrl(context, Uri.parse(clickURL));
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString() + " pulse webURL is not exits or error", IZ_CLASSNAME, "onButtonClick");
        }
    }

}
