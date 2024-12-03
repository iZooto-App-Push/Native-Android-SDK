package com.izooto;

import static com.izooto.AppConstant.APP_NAME_TAG;

import android.content.Context;
import android.util.Log;

import com.izooto.core.Utilities;
import com.izooto.feature.pulseweb.Documents;
import com.izooto.feature.pulseweb.Article;
import com.izooto.pulseconfig.OutbrainAdsConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class PulseFetchData {
    private static final String IZ_CLASS_NAME = "PulseFetchData";
    private static int pageNumber;  // index handling for notification feed data
    private static final Set<String> uniqueRids = new HashSet<>();
    public static Documents documentData = null;
    public static OutbrainAdsConfig outbrainAdsConfig = null;
    public static String pulseMainUrl = null;

    public static List<Object> returnPulseResponse(Context context, String url, boolean isPagination) {
        try {
            if (context == null) {
                return null;
            }

            List<Object> feedData;
            if (!isPagination) {
                pageNumber = 1;
                uniqueRids.clear();
                feedData = fetchAndWaitForFeedData(context, url, pageNumber);
            } else {
                try {
                    pageNumber++;
                    if (pageNumber <= 5) {
                        feedData = fetchAndWaitForFeedData(context, url, pageNumber);
                    } else {
                        return Collections.emptyList();
                    }

                } catch (Exception e) {
                    Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "returnFeedResponse");
                    return Collections.emptyList();
                }
            }
            return feedData;
        } catch (Exception ex) {
            return Collections.emptyList();

        }
    }


    private static List<Object> fetchAndWaitForFeedData(Context context, String url, int pageNumber) {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Object> result = new ArrayList<>();

        try {
            fetchFeedData(context, url, pageNumber, new FetchFeedCallback() {
                @Override
                public void onSuccess(List<Object> data) {
                    result.addAll(data);
                    latch.countDown();
                }

                @Override
                public void onFailure(String errorMessage) {
                    latch.countDown();
                }

                @Override
                public void onFeedItemCount(int count) {
                    latch.countDown();
                }
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }

        try {
            latch.await();  // Wait until the fetch operation completes
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }
        return result;
    }

    private static void fetchFeedData(Context context, String url, int index, FetchFeedCallback callback) {
        if (context == null) {
            callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
            return;
        }

        final List<Object> feedItem = new ArrayList<>();
        try {

            if (url.contains(AppConstant.P_MACROS)) {
                url = url.replace(AppConstant.P_MACROS, String.valueOf(index));
            }

            RestClient.get(url, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);

                    if (Utilities.isNullOrEmpty(response)) {
                        callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
                        return;
                    }
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        JSONArray itemsArray = responseObject.getJSONArray("items");
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject itemObject = itemsArray.getJSONObject(i);
                            String id = itemObject.optString("id");

                            if (!uniqueRids.contains(id)) {
                                uniqueRids.add(id);
                                Article menuItem = new Article(
                                        itemObject.optString("hl"), // title
                                        itemObject.optString("wu"), // link
                                        itemObject.optString("imageid"),//banner image
                                        itemObject.optString("dl"),//time
                                        itemObject.optString("pn"),//publisherName
                                        itemObject.optString("pnu"),//publisherIcon
                                        false
                                );

                                feedItem.add(menuItem);
                            }
                        }
                        callback.onSuccess(feedItem);
                        callback.onFeedItemCount(itemsArray.length());

                    } catch (Exception e) {
                        callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
                        Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "fetchFeedData");
                    }
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
                }
            });
        } catch (Exception e) {
            callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "fetchFeedData");
        }
    }

    public static int fetchPageIndex() {
        return pageNumber;
    }

    private interface FetchFeedCallback {

        void onSuccess(List<Object> data);

        void onFailure(String errorMessage);

        void onFeedItemCount(int count);
    }


    void processOutbrainPulse(Context context, PreferenceUtil preferenceUtil, JSONObject topJson) {

        try {
            if (context == null || preferenceUtil == null || topJson == null || !topJson.has(AppConstant.IZ_PULSE)) {
                return;
            }
            JSONObject pulseObject;
            String optedPulse = topJson.optString(AppConstant.IZ_PULSE);
            if (Utilities.isNullOrEmpty(optedPulse)) {
                Log.d(APP_NAME_TAG, "Empty or null pulse configuration found");
                return;
            }
            pulseObject = new JSONObject(optedPulse);
            if (pulseObject.has("url")){
                pulseMainUrl = pulseObject.optString("url");
            }
            boolean pulseStatus = pulseObject.optBoolean(AppConstant.isPulseEnable);
            preferenceUtil.setBooleanData(AppConstant.PW_STATUS, pulseStatus);
            if (pulseStatus) {
                JSONObject adConfigObject = pulseObject.optJSONObject("adConf");
                if (adConfigObject == null) {
                    return;
                }
                // setup outbrain configuration
                if (adConfigObject.optJSONObject(AppConstant.iZ_OUTBRAIN) != null) {
                    JSONObject outbrainConfigObject = adConfigObject.optJSONObject(AppConstant.iZ_OUTBRAIN);
                    if (outbrainConfigObject == null) {
                        return;
                    }
                    outbrainAdsConfig = new OutbrainAdsConfig(
                            outbrainConfigObject.optBoolean("status", false),
                            outbrainConfigObject.optString("url"),
                            outbrainConfigObject.optInt("position", 5),
                            outbrainConfigObject.optBoolean("imp", true)

                    );
                }

                if (outbrainAdsConfig != null && outbrainAdsConfig.getStatus() && !Utilities.isNullOrEmpty(outbrainAdsConfig.getUrl()) && Util.isReachableApi(outbrainAdsConfig.getUrl())) {
                    RestClient.get(outbrainAdsConfig.getUrl(), new RestClient.ResponseHandler() {
                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            try {
                                if (response == null) {
                                    return;
                                }
                                JSONObject jsonObject = new JSONObject(response);
                                JSONObject responseObject = jsonObject.optJSONObject("response");
                                if (responseObject == null) {
                                    return;
                                }
                                JSONObject documents = responseObject.optJSONObject("documents");
                                if (documents == null) {
                                    return;
                                }
                                JSONArray doc = documents.optJSONArray("doc");
                                JSONObject jsonData;
                                if (doc != null) {
                                    for (int i = 0; i < doc.length(); i++) {
                                        jsonData = doc.getJSONObject(i);
                                        JSONObject thumbnailObject = new JSONObject(jsonData.toString());
                                        JSONObject thumbnail = thumbnailObject.optJSONObject("thumbnail");
                                        if (thumbnail == null) {
                                            return;
                                        }
                                        // Create Documents object by extracting data from the JSON
                                        documentData = new Documents(
                                                jsonData.optString("on-viewed","Unknown"),
                                                jsonData.optString("source_name", "Unknown"),
                                                jsonData.optString("content", "Unknown"),
                                                jsonData.optString("url", "Unknown"),
                                                jsonData.optString("orig_url", "Unknown"),
                                                jsonData.optString("adv_name", "Unknown"),
                                                thumbnail.optString("url", "")
                                        );

                                    }
                                    if (!Utilities.isNullOrEmpty(pulseMainUrl) && Util.isReachableApi(pulseMainUrl)){
                                        iZooto.initializePulse(context, pulseObject);
                                        outbrainOnViewed(documentData);
                                    }else {
                                        Log.i(APP_NAME_TAG, "Not reachable pulse url!");
                                    }
                                }

                            } catch (Exception e) {
                                Log.e(APP_NAME_TAG, e.toString());
                            }

                        }

                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                            Log.e(APP_NAME_TAG,"Outbrain failed:-> "+ response);
                        }
                    });
                } else {
                    if (!Utilities.isNullOrEmpty(pulseMainUrl) && Util.isReachableApi(pulseMainUrl)){
                        iZooto.initializePulse(context, pulseObject);
                    }else {
                        Log.i(APP_NAME_TAG, "Not reachable pulse url!");
                    }
                }
            } else {
                Log.i(APP_NAME_TAG, "Are you sure pulse is enabled?");
            }
        } catch (Exception e) {
            Log.e(APP_NAME_TAG, e.toString());
        }
    }

    private void outbrainOnViewed(Documents documentData) {
            try {
                if (outbrainAdsConfig == null || !outbrainAdsConfig.getImpression()){return;}
                if (documentData != null && !Utilities.isNullOrEmpty(documentData.getOnViewed())) {
                    String onViewedUrl = null;
                    JSONArray jsonArray = new JSONArray(documentData.getOnViewed());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        onViewedUrl = jsonArray.optString(i);
                    }
                    if (onViewedUrl == null || onViewedUrl.isEmpty() || !Util.isReachableApi(onViewedUrl)){return;}
                    RestClient.get(onViewedUrl, new RestClient.ResponseHandler() {
                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            Log.i(APP_NAME_TAG,"[onViewed]:-> "+ response);
                        }

                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }
                    });
                }
            }catch (Exception e){
                Log.e(APP_NAME_TAG, e.toString());
            }

    }

}
