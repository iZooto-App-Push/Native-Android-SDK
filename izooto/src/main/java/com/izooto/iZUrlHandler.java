package com.izooto;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;

public class iZUrlHandler {

    /**
     * Opens a given URL either in a Chrome Custom Tab or an external browser.
     *
     * @param context The application context.
     * @param mUrl    The URL to open.
     * @param inApp   Determines if the URL should be opened in a Custom Tab (2) or a browser.
     */
    public static void openUrl(Context context, String mUrl, int inApp) {
        if (mUrl == null || mUrl.isEmpty()) {
            return; // Exit if URL is null or empty
        }

        // Ensure the URL starts with "http://" or "https://"
        String url = (mUrl.startsWith("http://") || mUrl.startsWith("https://")) ? mUrl : "https://" + mUrl;

        if (inApp == 2) {

            try {
                if(Util.checkCustomTabLibrary(context)) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(context, Uri.parse(url));
                    return; // Successfully opened in a Custom Tab
                }

            } catch (Exception e) {
                logException(context, e, mUrl, "Exception");
            }
        }


        // Fallback to opening in an external browser
        openInBrowser(context, url);
    }


    /**
     * Opens a URL in the default browser as a fallback.
     *
     * @param context The application context.
     * @param url     The URL to open.
     */
    private static void openInBrowser(Context context, String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Uri referrerUri = Uri.parse("android-app://" + context.getPackageName());
            browserIntent.putExtra(Intent.EXTRA_REFERRER_NAME, referrerUri);
            context.startActivity(browserIntent);
        } catch (Exception e) {
            logException(context, e, url, "BrowserException");
        }
    }

    /**
     * Logs and handles exceptions to prevent crashes.
     *
     * @param context The application context.
     * @param e       The exception thrown.
     * @param url     The URL that caused the issue.
     * @param tag     A tag to identify the exception type.
     */
    private static void logException(Context context, Exception e, String url, String tag) {
        Util.handleExceptionOnce(context, e.toString() + url, AppConstant.APPName_3, tag);
    }
}
