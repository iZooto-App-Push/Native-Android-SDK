package com.izooto;

import static android.graphics.Typeface.BOLD;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsHubAlert {
    private ImageView backButton;
    private NestedScrollView nestedScrollViewAlert;
    private ProgressBar loadingPBAlert;
    private RecyclerView recyclerViewAlert;
    private RelativeLayout toolbarLayout;
    private TextView poweredByText, toolbarText, iZootoText;
    private LinearLayout noDataFound,ll_powered_by;
    static ArrayList<Payload> payloadModalArrayList;
    static NewsHubAdapter newsHubAdapter;
    static PreferenceUtil preferenceUtil;
    static Payload mPayload;
    static NewsHubDBHelper newsHubDBHelper;

    static int page = 0, limit;

    public void showAlertData(final Activity context) {

        if (context != null) {
            context.runOnUiThread(new Runnable() {
                @RequiresApi(
                        api = 19
                )
                public void run() {

                    preferenceUtil = PreferenceUtil.getInstance(context);
                    newsHubDBHelper = new NewsHubDBHelper(context);

                    if (preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4)
                        limit = 4;
                    else {
                        limit = 0;
                        page = 0;
                    }

                    Rect displayRectangle = new Rect();
                    Window window = context.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context, R.style.NH_ALERT_DIALOG);
                    LayoutInflater inflater = context.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alert_news_hub_design, (ViewGroup)null);

                    dialogView.setMinimumWidth((int)(displayRectangle.width() * 1f));
                    dialogView.setMinimumHeight((int)(displayRectangle.height() * 1f));
                    mBuilder.setView(dialogView);

                    defineAlertIds(dialogView);

                    getDataFromAPI(context, page, limit, recyclerViewAlert, loadingPBAlert, noDataFound, nestedScrollViewAlert);

                    SpannableString spannableString = new SpannableString("News Hub Powered by ");
                    spannableString.setSpan(new StyleSpan(BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    poweredByText.setText(spannableString);
                    ll_powered_by.setVisibility(View.VISIBLE);

                    mBuilder.setCancelable(true);
                    final AlertDialog alertDialog = mBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();

                    setJsonDataAlert(context);

                    nestedScrollViewAlert.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            // on scroll change we are checking when users scroll as bottom.
                            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                                // in this method we are incrementing page number,
                                // making progress bar visible and calling get data method.
                                if (NewsHubAlert.preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4 && Util.isNetworkAvailable(context)) {
                                    page++;
                                    loadingPBAlert.setVisibility(View.VISIBLE);
                                    getDataFromAPI(context, page, limit, recyclerViewAlert, loadingPBAlert, noDataFound, nestedScrollViewAlert);
                                } else {
                                    loadingPBAlert.setVisibility(View.GONE);
                                }

                            }
                        }
                    });
                    backButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    iZootoText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = "https://www.izooto.com";
                            Uri uri = Uri.parse(url);
                            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    });
                }
            });
        }

    }

    private void defineAlertIds(View view) {
        backButton =  view.findViewById(R.id.iv_toolbar_back_button_alert);
        nestedScrollViewAlert =  view.findViewById(R.id.idNestedSV_alert);
        loadingPBAlert =  view.findViewById(R.id.progress_bar_alert);
        recyclerViewAlert =  view.findViewById(R.id.staticListData_alert);
        poweredByText =  view.findViewById(R.id.tv_powered_by_alert);
        noDataFound =  view.findViewById(R.id.ll_no_data_found_alert);
        toolbarText =  view.findViewById(R.id.tv_toolbar_alert);
        iZootoText =  view.findViewById(R.id.tv_izooto_alert);
        toolbarLayout =  view.findViewById(R.id.nh_toolbar_alert);
        ll_powered_by=view.findViewById(R.id.ll_powered_by);

    }

    static void getDataFromAPI(Activity activity, int page, int limit, RecyclerView recyclerView, ProgressBar progressBar, LinearLayout notFoundLayout, NestedScrollView nestedSV) {
        if (activity == null)
            return;

        if (page > limit) {

            progressBar.setVisibility(View.GONE);
            return;
        }
        String url = "https://nh.iz.do/nh/a588d2fb1061d6931af41496660ef3b70fbcf708/" + page + ".json";

        payloadModalArrayList = new ArrayList<>();
        RestClient.get(url, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            /*
                             * On below line we are extracting data from our json array*/
                            JSONArray dataArray = new JSONArray(response);
                            JSONObject jsonObject1;

                            // passing data from our json array in our array list.
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject jsonObject = dataArray.getJSONObject(i);
                                jsonObject1 = jsonObject.optJSONObject("p");

                                if (jsonObject1 == null) {
                                    jsonObject1 = jsonObject.optJSONObject("a");
                                }
                                mPayload = new Payload();

                                mPayload.setCreated_Time(jsonObject1.optString(ShortpayloadConstant.CREATEDON));
                                mPayload.setFetchURL(jsonObject1.optString(ShortpayloadConstant.FETCHURL));
                                mPayload.setKey(jsonObject1.optString(ShortpayloadConstant.KEY));
                                mPayload.setId(jsonObject1.optString(ShortpayloadConstant.ID));
                                mPayload.setRid(jsonObject1.optString(ShortpayloadConstant.RID));
                                mPayload.setLink(jsonObject1.optString(ShortpayloadConstant.LINK));
                                mPayload.setTitle(jsonObject1.optString(ShortpayloadConstant.TITLE));
                                mPayload.setMessage(jsonObject1.optString(ShortpayloadConstant.NMESSAGE));
                                mPayload.setIcon(jsonObject1.optString(ShortpayloadConstant.ICON));
                                mPayload.setReqInt(jsonObject1.optInt(ShortpayloadConstant.REQINT));
                                mPayload.setTag(jsonObject1.optString(ShortpayloadConstant.TAG));
                                mPayload.setBanner(jsonObject1.optString(ShortpayloadConstant.BANNER));
                                mPayload.setAct_num(jsonObject1.optInt(ShortpayloadConstant.ACTNUM));
                                mPayload.setBadgeicon(jsonObject1.optString(ShortpayloadConstant.BADGE_ICON));
                                mPayload.setBadgecolor(jsonObject1.optString(ShortpayloadConstant.BADGE_COLOR));
                                mPayload.setSubTitle(jsonObject1.optString(ShortpayloadConstant.SUBTITLE));
                                mPayload.setGroup(jsonObject1.optInt(ShortpayloadConstant.GROUP));
                                mPayload.setBadgeCount(jsonObject1.optInt(ShortpayloadConstant.BADGE_COUNT));
                                // Button 2
                                mPayload.setAct1name(jsonObject1.optString(ShortpayloadConstant.ACT1NAME));
                                mPayload.setAct1link(jsonObject1.optString(ShortpayloadConstant.ACT1LINK));
                                mPayload.setAct1icon(jsonObject1.optString(ShortpayloadConstant.ACT1ICON));
                                mPayload.setAct1ID(jsonObject1.optString(ShortpayloadConstant.ACT1ID));
                                // Button 2
                                mPayload.setAct2name(jsonObject1.optString(ShortpayloadConstant.ACT2NAME));
                                mPayload.setAct2link(jsonObject1.optString(ShortpayloadConstant.ACT2LINK));
                                mPayload.setAct2icon(jsonObject1.optString(ShortpayloadConstant.ACT2ICON));
                                mPayload.setAct2ID(jsonObject1.optString(ShortpayloadConstant.ACT2ID));

                                mPayload.setInapp(jsonObject1.optInt(ShortpayloadConstant.INAPP));
                                mPayload.setTrayicon(jsonObject1.optString(ShortpayloadConstant.TARYICON));
                                mPayload.setSmallIconAccentColor(jsonObject1.optString(ShortpayloadConstant.ICONCOLOR));
                                mPayload.setSound(jsonObject1.optString(ShortpayloadConstant.SOUND));
                                mPayload.setLedColor(jsonObject1.optString(ShortpayloadConstant.LEDCOLOR));
                                mPayload.setLockScreenVisibility(jsonObject1.optInt(ShortpayloadConstant.VISIBILITY));
                                mPayload.setGroupKey(jsonObject1.optString(ShortpayloadConstant.GKEY));
                                mPayload.setGroupMessage(jsonObject1.optString(ShortpayloadConstant.GMESSAGE));
                                mPayload.setFromProjectNumber(jsonObject1.optString(ShortpayloadConstant.PROJECTNUMBER));
                                mPayload.setCollapseId(jsonObject1.optString(ShortpayloadConstant.COLLAPSEID));
                                mPayload.setPriority(jsonObject1.optInt(ShortpayloadConstant.PRIORITY));
                                mPayload.setRawPayload(jsonObject1.optString(ShortpayloadConstant.RAWDATA));
                                mPayload.setAp(jsonObject1.optString(ShortpayloadConstant.ADDITIONALPARAM));
                                mPayload.setCfg(jsonObject1.optInt(ShortpayloadConstant.CFG));
                                mPayload.setPush_type(AppConstant.PUSH_FCM);
                                mPayload.setSound(jsonObject1.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                                mPayload.setMaxNotification(jsonObject1.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                                mPayload.setFallBackDomain(jsonObject1.optString(ShortpayloadConstant.FALL_BACK_DOMAIN));
                                mPayload.setFallBackSubDomain(jsonObject1.optString(ShortpayloadConstant.FALLBACK_SUB_DOMAIN));
                                mPayload.setFallBackPath(jsonObject1.optString(ShortpayloadConstant.FAll_BACK_PATH));
                                mPayload.setDefaultNotificationPreview(jsonObject1.optInt(ShortpayloadConstant.TEXTOVERLAY));
                                mPayload.setNotification_bg_color(jsonObject1.optString(ShortpayloadConstant.BGCOLOR));
                                newsHubDBHelper.addNewsHubPayload(mPayload);

                                if (preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4)
                                    preferenceUtil.setIntData(AppConstant.SET_PAGE_NO, page);

                                if (newsHubDBHelper.fetchNewsHubData() != null) {
//                                    Log.e("TAG", "run: page -- " + page );
                                    payloadModalArrayList = newsHubDBHelper.fetchNewsHubData();
                                    newsHubAdapter = new NewsHubAdapter(payloadModalArrayList, activity);

                                    // setting layout manager to our recycler view.
                                    recyclerView.setLayoutManager(new LinearLayoutManager(activity));

                                    // setting adapter to our recycler view.
                                    recyclerView.setAdapter(newsHubAdapter);
                                } else {
//                                    Log.e("TAG", "Data not found");
                                    nestedSV.setVisibility(View.GONE);
                                    notFoundLayout.setVisibility(View.VISIBLE);
                                }
                                progressBar.setVisibility(View.GONE);


                            }
                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);

                            e.printStackTrace();

                        }

                    }
                });

            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                progressBar.setVisibility(View.GONE);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newsHubDBHelper.fetchNewsHubData() != null && newsHubDBHelper.fetchNewsHubData().size() > 0) {
                            payloadModalArrayList = newsHubDBHelper.fetchNewsHubData();
                            newsHubAdapter = new NewsHubAdapter(payloadModalArrayList, activity);

                            // setting layout manager to our recycler view.
                            recyclerView.setLayoutManager(new LinearLayoutManager(activity));

                            // setting adapter to our recycler view.
                            recyclerView.setAdapter(newsHubAdapter);
//                            Log.e("TAG", "run: ------ " + payloadModalArrayList);
                        } else {
                            nestedSV.setVisibility(View.GONE);
                            notFoundLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });
    }

    private void setJsonDataAlert(Context context) {
        if (context == null)
            return;
        try {
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE).isEmpty())
                toolbarText.setText(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE));

            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
                toolbarLayout.setBackgroundColor(color);
            }
            if(preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_BRANDING))
            {
                ll_powered_by.setVisibility(View.VISIBLE);
            }
            else
            {
                ll_powered_by.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

}
