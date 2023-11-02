package com.izooto;

import static android.graphics.Typeface.NORMAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


 class NewsHubAlert {
    private ImageView backButton;
    private NestedScrollView nestedScrollViewAlert;
    private ProgressBar loadingPBAlert, loadingPBAlert1;
    private RecyclerView recyclerViewAlert;
    private RelativeLayout toolbarLayout;
    private TextView poweredByText, toolbarText, iZootoText, izFooterText;
    private LinearLayout noDataFound, brandingVisibility;
    static ArrayList<Payload> payloadModalArrayList;
    @SuppressLint("StaticFieldLeak")
    static NewsHubAdapter newsHubAdapter;
    static PreferenceUtil preferenceUtil;
    static Payload mPayload;
    private final String className = this.getClass().getName();
    static NewsHubDBHelper newsHubDBHelper;
    static int page = 0, limit;
    private LinearLayout footer;
    private static AlertDialog alertDialog;
    static Boolean newsHubEnable = false;

    public void showAlertData(Activity context) {
        if (context != null) {
            context.runOnUiThread(() -> {
                preferenceUtil = PreferenceUtil.getInstance(context);
                newsHubDBHelper = new NewsHubDBHelper(context);
                if (preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4) {
                    limit = 4;
                } else {
                    limit = 0;
                    page = 0;
                }
                Rect displayRectangle = new Rect();
                Window window = context.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
                LayoutInflater inflater = context.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_news_hub_design, null);
                dialogView.setMinimumWidth((int) (displayRectangle.width() * 1f));
                dialogView.setMinimumHeight((int) (displayRectangle.height() * 1f));
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                mBuilder.setView(dialogView);

                defineAlertIds(dialogView);
                brandingVisibility(context);

                try {
                    getDataFromAPI(context, page, limit, recyclerViewAlert, loadingPBAlert, loadingPBAlert1, noDataFound, nestedScrollViewAlert, footer);
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                    if (!preferenceUtil.getBoolean("showAlertData")) {
                        preferenceUtil.setBooleanData("showAlertData", true);
                        Util.setException(context, e.toString(), className, "showAlertData");
                    }
                }

                SpannableString spannableString = new SpannableString("News Hub Powered by ");
                spannableString.setSpan(new StyleSpan(NORMAL), 0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                poweredByText.setText(spannableString);

                mBuilder.setCancelable(true);
                alertDialog = mBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);

                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    alertDialog.show();
                }
                setJsonDataAlert(context);

                nestedScrollViewAlert.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    // on scroll change we are checking when users scroll as bottom.
                    if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                        // in this method we are incrementing page number,
                        // making progress bar visible and calling get data method.
                        if (NewsHubAlert.preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4 && Util.isNetworkAvailable(context)) {
                            page++;
                            loadingPBAlert.setVisibility(View.VISIBLE);
                            //  footer.setVisibility(View.GONE);
                            try {
                                getDataFromAPI(context, page, limit, recyclerViewAlert, loadingPBAlert, loadingPBAlert1, noDataFound, nestedScrollViewAlert, footer);
                            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                                if (!preferenceUtil.getBoolean("showAlertData")) {
                                    preferenceUtil.setBooleanData("showAlertData", true);
                                    Util.setException(context, e.toString(), className, "showAlertData");
                                }
                            }
                        } else {
                            // footer.setVisibility(View.VISIBLE);
                            loadingPBAlert.setVisibility(View.GONE);
                        }

                    }
                });
                backButton.setOnClickListener(v -> alertDialog.dismiss());

                iZootoText.setOnClickListener(v -> {
                    String packageName = Util.getPackageName(v.getContext());
                    Uri uri = Uri.parse(AppConstant.NEWS_HUB_IZOOTO_BRANDING + packageName);
                    context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                });
            });
        }

    }

    private void defineAlertIds(View view) {
        backButton = view.findViewById(R.id.iv_toolbar_back_button_alert);
        nestedScrollViewAlert = view.findViewById(R.id.idNestedSV_alert);
        loadingPBAlert = view.findViewById(R.id.progress_bar_alert);
        loadingPBAlert1 = view.findViewById(R.id.progress_bar_alert1);
        recyclerViewAlert = view.findViewById(R.id.staticListData_alert);
        poweredByText = view.findViewById(R.id.tv_powered_by_alert);
        noDataFound = view.findViewById(R.id.ll_no_data_found_alert);
        toolbarText = view.findViewById(R.id.tv_toolbar_alert);
        iZootoText = view.findViewById(R.id.tv_izooto_alert);
        toolbarLayout = view.findViewById(R.id.nh_toolbar_alert);
        footer = view.findViewById(R.id.list_item_end);
        izFooterText = view.findViewById(R.id.iz_footer_text);
        brandingVisibility = view.findViewById(R.id.ll_powered_by);

    }

    static void getDataFromAPI(Activity activity, int page, int limit, RecyclerView recyclerView, ProgressBar progressBar, ProgressBar progressBar1, LinearLayout notFoundLayout, NestedScrollView nestedSV, LinearLayout footer) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (activity == null)
            return;
        if (page > limit) {
            progressBar.setVisibility(View.GONE);
            return;
        }
        // creating a string variable for newsHubUrl .
        String newsHubUrl = null;

        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
            String iZootoAppId = preferenceUtil.getStringData(AppConstant.APPPID);
            String shaKey = Util.toSHA1(iZootoAppId);

            if (!iZootoAppId.isEmpty()) {
                newsHubUrl = RestClient.NEWS_HUB_URL + shaKey + "/" + page + ".json";
                newsHubEnable = true;
            }
        } catch (Exception e) {
            if (!preferenceUtil.getBoolean("getDataFromAPI")) {
                preferenceUtil.setBooleanData("getDataFromAPI", true);
                Util.setException(activity, e.toString(), "NewsHubAlert", "getDataFromAPI");
            }
        }
        payloadModalArrayList = new ArrayList<>();
        RestClient.get(newsHubUrl, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                activity.runOnUiThread(() -> {
                    try {
                        //On below line we are extracting data from our json array
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
                              if (jsonObject1 != null) {
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
                                  // Button 1
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
                                  mPayload.setOfflineCampaign(jsonObject1.optString(ShortpayloadConstant.OFFLINE_CAMPAIGN));
                                  mPayload.setSound(jsonObject1.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                                  mPayload.setMaxNotification(jsonObject1.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                                  mPayload.setFallBackDomain(jsonObject1.optString(ShortpayloadConstant.FALL_BACK_DOMAIN));
                                  mPayload.setFallBackSubDomain(jsonObject1.optString(ShortpayloadConstant.FALLBACK_SUB_DOMAIN));
                                  mPayload.setFallBackPath(jsonObject1.optString(ShortpayloadConstant.FAll_BACK_PATH));
                                  mPayload.setDefaultNotificationPreview(jsonObject1.optInt(ShortpayloadConstant.TEXTOVERLAY));
                                  mPayload.setNotification_bg_color(jsonObject1.optString(ShortpayloadConstant.BGCOLOR));
                                  mPayload.setOfflineCampaign(jsonObject1.optString(ShortpayloadConstant.OFFLINE_CAMPAIGN));
                                  if (Util.getValidIdForCampaigns(mPayload)) {
                                      if (mPayload.getLink() != null && !mPayload.getLink().isEmpty()) {
                                          try {
                                              newsHubDBHelper.addNewsHubPayload(mPayload);
                                          }
                                          catch (Exception ex)
                                          {
                                              Log.e("Database issues occured","");
                                          }
                                      }
                                  }


                                  // on below line we are extracting data from our json object.

                                  // passing array list to our adapter class.
                                  if (preferenceUtil.getIntData(AppConstant.SET_PAGE_NO) < 4)
                                      preferenceUtil.setIntData(AppConstant.SET_PAGE_NO, page);
                                  if (newsHubDBHelper.fetchNewsHubData() != null && !newsHubDBHelper.fetchNewsHubData().isEmpty()) {
                                      progressBar1.setVisibility(View.GONE);
//                                    Log.e("TAG", "run: page -- " + page );
                                      payloadModalArrayList = newsHubDBHelper.fetchNewsHubData();
                                      newsHubAdapter = new NewsHubAdapter(payloadModalArrayList, activity, alertDialog);

                                      // setting layout manager to our recycler view.
                                      recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                                      // setting adapter to our recycler view.
                                      recyclerView.setAdapter(newsHubAdapter);

                                  } else {
                                      nestedSV.setVisibility(View.GONE);
                                      notFoundLayout.setVisibility(View.VISIBLE);
                                  }
                              }
                        }

                    } catch (JSONException e) {
                        if (!preferenceUtil.getBoolean("getDataFromAPI")) {
                            preferenceUtil.setBooleanData("getDataFromAPI", true);
                            Util.setException(activity, e.toString(), "NewsHubAlert", "getDataFromAPI");
                        }
                    }

                });

            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                activity.runOnUiThread(() -> {
                    if (newsHubDBHelper.fetchNewsHubData() != null && newsHubDBHelper.fetchNewsHubData().size() > 0) {
                        payloadModalArrayList = newsHubDBHelper.fetchNewsHubData();
                        progressBar.setVisibility(View.GONE);
                        progressBar1.setVisibility(View.GONE);
                        newsHubAdapter = new NewsHubAdapter(payloadModalArrayList, activity,alertDialog);
                        // setting layout manager to our recycler view.
                        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                        // setting adapter to our recycler view.
                        recyclerView.setAdapter(newsHubAdapter);
                    } else {
                        nestedSV.setVisibility(View.GONE);
                        notFoundLayout.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setJsonDataAlert(Context context) {
        if (context == null)
            return;
        try {
            String titleColor = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR);
            if (titleColor != null && !titleColor.isEmpty()) {
                toolbarText.setTextColor(Color.parseColor(Util.getColorCode(titleColor)));
            } else {
                toolbarText.setTextColor(Color.WHITE);
            }
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE).isEmpty()) {
                String headerTitle = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE);
                int headerLength = headerTitle.length();
                if (headerLength > 20) {
                    toolbarText.setText(headerTitle.substring(0, 20));
                } else {
                    toolbarText.setText(headerTitle);
                }
            } else {
                toolbarText.setText("News Hub");
            }

            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                String color = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR);
                toolbarLayout.setBackgroundColor(Color.parseColor(Util.getColorCode(color)));
            }
        } catch (Exception e) {
            if (!preferenceUtil.getBoolean("setJsonDataAlert")) {
                preferenceUtil.setBooleanData("setJsonDataAlert", true);
                Util.setException(context, e.toString(), "NewsHubAlert", "setJsonDataAlert");
            }
        }
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
        } catch (Exception e) {
            if (!preferenceUtil.getBoolean("brandingVisibility")) {
                preferenceUtil.setBooleanData("brandingVisibility", true);
                Util.setException(context, e.toString(), "NewsHubAlert", "brandingVisibility");
            }
        }
    }

}
