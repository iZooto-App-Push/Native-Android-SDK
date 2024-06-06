package com.izooto;

import static android.graphics.Typeface.NORMAL;
import static com.izooto.NewsHubAlert.preferenceUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class NewsHubActivity extends AppCompatActivity {

    private ImageView backButton;
    private NestedScrollView nestedScrollViewAlert;
    private ProgressBar loadingPBAlert,loadingPBAlert1;
    private RecyclerView recyclerViewAlert;
    private LinearLayout toolbarLayout;
    private TextView poweredByText, toolbarText, iZootoText;
    private LinearLayout noDataFound;
    private Context context;
    private LinearLayout footer,brandingVisibility;
    private TextView izFooterText;


    private final String className = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newshub);

        context = NewsHubActivity.this;
        defineIds();
        brandingVisibility(context);
        preferenceUtil = PreferenceUtil.getInstance(context);
        NewsHubAlert.newsHubDBHelper = new NewsHubDBHelper(context);


        if (preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4) {
            NewsHubAlert.limit = 4;
        } else {
            NewsHubAlert.limit = 0;
            NewsHubAlert.page = 0;
        }

        try {
            NewsHubAlert.getDataFromAPI(NewsHubActivity.this, NewsHubAlert.page, NewsHubAlert.limit, recyclerViewAlert, loadingPBAlert,loadingPBAlert1, noDataFound, nestedScrollViewAlert, footer);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            Util.setException(context,e.toString(),"onCreate",className);
        }

        SpannableString spannableString = new SpannableString("News Hub Powered by ");
        spannableString.setSpan(new StyleSpan(NORMAL), 0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        poweredByText.setText(spannableString);

        setJsonData(context);


        nestedScrollViewAlert.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                // in this method we are incrementing page number,
                // making progress bar visible and calling get data method.
                if (preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4 && Util.isNetworkAvailable(context)) {
                    NewsHubAlert.page ++;
                    loadingPBAlert.setVisibility(View.VISIBLE);
                    // footer.setVisibility(View.GONE);
                    try {
                        NewsHubAlert.getDataFromAPI(NewsHubActivity.this, NewsHubAlert.page, NewsHubAlert.limit, recyclerViewAlert, loadingPBAlert,loadingPBAlert1, noDataFound, nestedScrollViewAlert, footer);
                    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                        Util.setException(context,e.toString(),"onCreate",className);
                    }
                } else {
                    // footer.setVisibility(View.VISIBLE);
                    loadingPBAlert.setVisibility(View.GONE);
                }

            }
        });

        backButton.setOnClickListener(v -> finish());

        iZootoText.setOnClickListener(v -> {
            String packageName = Util.getPackageName(v.getContext());
            Uri uri = Uri.parse(AppConstant.NEWS_HUB_IZOOTO_BRANDING+packageName);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });
    }

    private void brandingVisibility(Context context) {
        try {
            preferenceUtil = PreferenceUtil.getInstance(context);
            if (preferenceUtil != null) {
                int branding = preferenceUtil.getIntData(AppConstant.NEWS_HUB_B_KEY);
                if (branding == 1) {
                    brandingVisibility.setVisibility(View.VISIBLE);
                } else {
                    brandingVisibility.setVisibility(View.INVISIBLE);
                    brandingVisibility.setBackgroundColor(Color.TRANSPARENT);
                }

            }
        } catch(Exception e){
            if (!preferenceUtil.getBoolean("brandingVisibility")) {
                preferenceUtil.setBooleanData("brandingVisibility", true);
                Util.setException(context, e.toString(), className, "brandingVisibility");
            }
        }

    }
    private void defineIds() {

        backButton = findViewById(R.id.iv_toolbar_back_button);
        nestedScrollViewAlert = findViewById(R.id.idNestedSV_alert);
        loadingPBAlert = findViewById(R.id.progress_bar_alert);
        loadingPBAlert1 = findViewById(R.id.progress_bar_alert1);
        recyclerViewAlert = findViewById(R.id.staticListData);
        poweredByText = findViewById(R.id.tv_powered_by);
        noDataFound = findViewById(R.id.ll_no_data_found);
        toolbarText = findViewById(R.id.tv_toolbar);
        iZootoText = findViewById(R.id.tv_izooto);
        toolbarLayout = findViewById(R.id.nh_toolbar);
        footer = findViewById(R.id.list_item_end);
        izFooterText = findViewById(R.id.iz_footer_text);
        brandingVisibility = findViewById(R.id.linear_powered_by);

    }

    @SuppressLint("SetTextI18n")
    private void setJsonData(Context context) {
        if (context == null)
            return;
        try {
            String textColor = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR);
            if (textColor!=null && !textColor.isEmpty()){
                toolbarText.setTextColor(Color.parseColor(Util.getColorCode(textColor)));
            }else {
                toolbarText.setTextColor(Color.WHITE);
            }
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE).isEmpty()){
                String headerTitle = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE);
                int headerLength = headerTitle.length();
                if (headerLength > 20) {
                    toolbarText.setText(headerTitle.substring(0,20));
                }else {
                    toolbarText.setText(headerTitle);
                }
            }else {
                toolbarText.setText("News Hub");
            }
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                String color = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR);
                toolbarLayout.setBackgroundColor(Color.parseColor(Util.getColorCode(color)));
            }
        } catch (Exception e) {
                Util.handleExceptionOnce(context, e.toString(), className, "setJsonData");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}