package com.izooto;

import static android.graphics.Typeface.BOLD;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.izooto.DatabaseHandler.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsHubActivity extends AppCompatActivity {
    private ImageView backButton;
    private NestedScrollView nestedScrollViewAlert;
    private ProgressBar loadingPBAlert,progress_bar_alert1;
    private RecyclerView recyclerViewAlert;
    private RelativeLayout toolbarLayout;
    private TextView poweredByText, toolbarText, iZootoText;
    private LinearLayout noDataFound;
    private Context context;
    private LinearLayout brandingLayout;
//    int page = 0, limit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newshub);
        context = NewsHubActivity.this;
        defineIds();
        NewsHubAlert.preferenceUtil = PreferenceUtil.getInstance(context);
        NewsHubAlert.newsHubDBHelper = new NewsHubDBHelper(context);
        loadingPBAlert.setVisibility(View.GONE);
        if (NewsHubAlert.preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4)
            NewsHubAlert.limit = 4;
        else
            NewsHubAlert.limit = 0;

        NewsHubAlert.getDataFromAPI(NewsHubActivity.this, NewsHubAlert.page, NewsHubAlert.limit, recyclerViewAlert, progress_bar_alert1, noDataFound, nestedScrollViewAlert);
        SpannableString spannableString = new SpannableString("News Hub Powered by ");
        spannableString.setSpan(new StyleSpan(BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        poweredByText.setText(spannableString);

        setJsonData(context);

        nestedScrollViewAlert.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // on scroll change we are checking when users scroll as bottom.
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    if (NewsHubAlert.preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4 && Util.isNetworkAvailable(context)) {
                        NewsHubAlert.page++;
                        loadingPBAlert.setVisibility(View.VISIBLE);
                        NewsHubAlert.getDataFromAPI(NewsHubActivity.this, NewsHubAlert.page, NewsHubAlert.limit, recyclerViewAlert, loadingPBAlert, noDataFound, nestedScrollViewAlert);
                    } else {
                        loadingPBAlert.setVisibility(View.GONE);
                    }

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        iZootoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.izooto.com";
                Uri uri = Uri.parse(url);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
    }


    private void defineIds() {
        backButton =  findViewById(R.id.iv_toolbar_back_button);
        nestedScrollViewAlert =  findViewById(R.id.idNestedSV_alert);
        loadingPBAlert =  findViewById(R.id.progress_bar_alert);
        progress_bar_alert1 =findViewById(R.id.progress_bar_alert1);
        recyclerViewAlert =  findViewById(R.id.staticListData);
        poweredByText =  findViewById(R.id.tv_powered_by);
        noDataFound =  findViewById(R.id.ll_no_data_found);
        toolbarText =  findViewById(R.id.tv_toolbar);
        iZootoText =  findViewById(R.id.tv_izooto);
        toolbarLayout =  findViewById(R.id.nh_toolbar);
        brandingLayout=findViewById(R.id.brandingLayout);
    }

    private void setJsonData(Context context) {
        if (context == null)
            return;
        try {
            if (!NewsHubAlert.preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE).isEmpty())
                toolbarText.setText(NewsHubAlert.preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE));
            if(!NewsHubAlert.preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR).isEmpty()) {
                int titleColor = Color.parseColor(NewsHubAlert.preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR));
                toolbarText.setTextColor(titleColor);
            }
            if (!NewsHubAlert.preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                int color = Color.parseColor(NewsHubAlert.preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
                toolbarLayout.setBackgroundColor(color);
            }
            if(NewsHubAlert.preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_BRANDING))
            {
                brandingLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                brandingLayout.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            Util.setException(context,""+e.toString(),"NewsHUb Activity","setJsonData");
        }
    }
}