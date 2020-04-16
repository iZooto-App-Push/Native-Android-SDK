package com.izooto;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private String mUrl;

    public static void startActivity(Context context, String url) {
        context.startActivity(createIntent(context, url));

    }

    public static Intent createIntent(Context context, String url) {
        Intent webIntent = new Intent(context, WebViewActivity.class);
        webIntent.putExtra(AppConstant.KEY_WEB_URL, url);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return webIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initUI();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        initUI();
    }



    private void initUI() {
        getBundleData();
        mWebView = findViewById(R.id.webview);
        mProgressBar = findViewById(R.id.circular_progress_bar);
        WebSettings settings = mWebView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(false);
        settings.setLoadsImagesAutomatically(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new CustWebViewClient());
        mWebView.loadUrl(mUrl);
    }

    private void getBundleData() {
        Bundle tempBundle = getIntent().getExtras();
        if (tempBundle != null) {
            if (tempBundle.containsKey(AppConstant.KEY_WEB_URL))
                mUrl = tempBundle.getString(AppConstant.KEY_WEB_URL);
        }
    }



    class CustWebViewClient extends WebViewClient {
        public static final String TAG = AppConstant.APP_NAME_TAG;
//        boolean getSuccess;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Lg.d(TAG, AppConstant.webViewData);
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressBar.setVisibility(View.GONE);
            super.onPageFinished(view, url);


        }

    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
