package com.izooto;

import android.content.Context;
import android.util.Log;

import com.izooto.core.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class NotificationFeedManager {

    private static final String IZ_CLASS_NAME = "NotificationFeedManager";
    private static int pageNumber;   // index handling for notification feed data
    private static int feedItemCount;   // calculate feed item count
    private static final Set<String> uniqueRids = new HashSet<>();

    static String returnFeedResponse(Context context, boolean isPagination) {
        try {
            if (context == null) {
                return AppConstant.IZ_NO_MORE_DATA;
            }

            String feedData = "";

            if (!isPagination) {
                pageNumber = 0;
                uniqueRids.clear();
                feedData = fetchAndWaitForFeedData(context, pageNumber);
            } else {
                try {
                    // If we have 15 or more items, fetch the next page
                    if (feedItemCount >= 15) {
                        pageNumber++;
                        if (pageNumber < 5) {
                            feedData = fetchAndWaitForFeedData(context, pageNumber);
                        } else {
                            uniqueRids.clear();
                            return AppConstant.IZ_NO_MORE_DATA;
                        }
                    } else {
                        return AppConstant.IZ_NO_MORE_DATA;
                    }
                } catch (Exception e) {
                    Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "returnFeedResponse");
                    return AppConstant.IZ_NO_MORE_DATA;
                }
            }

            if (Utilities.isNullOrEmpty(feedData)) {
                return AppConstant.IZ_NO_MORE_DATA;
            }

            return feedData;
        } catch (Exception ex) {
            return AppConstant.IZ_NO_MORE_DATA;
        }
    }


    private static String fetchAndWaitForFeedData(Context context, int pageNumber) {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] result = new String[1];

        try {
            fetchFeedData(context, pageNumber, new FetchFeedCallback() {
                @Override
                public void onSuccess(String data) {
                    result[0] = data;
                    latch.countDown();
                }

                @Override
                public void onFailure(String errorMessage) {
                    result[0] = AppConstant.IZ_NO_MORE_DATA;
                    latch.countDown();
                }

                @Override
                public void onFeedItemCount(int count) {
                    feedItemCount = count;
                    latch.countDown();
                }
            });
        } catch (Exception ex) {
            return AppConstant.IZ_NO_MORE_DATA;
        }

        try {
            latch.await();  // Wait until the fetch operation completes
        } catch (InterruptedException e) {
            return AppConstant.IZ_NO_MORE_DATA;
        }
        return result[0];
    }

    private static void fetchFeedData(Context context, int index, FetchFeedCallback callback) {
        if (context == null) {
            callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
            return;
        }

        final ArrayList<JSONObject> feedItem = new ArrayList<>();
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String encrypted_pid = Util.toSHA1(preferenceUtil.getStringData(AppConstant.APPPID));
            Log.d(AppConstant.APP_NAME_TAG, "Fetching notification feed from API. Page number: " + index);

            RestClient.get("https://nh.izooto.com/nh/" + encrypted_pid + "/" + index + ".json", new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);

                    if (Utilities.isNullOrEmpty(response)) {
                        callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject;
                        JSONObject jsonObject1;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.optJSONObject(i).optJSONObject(ShortPayloadConstant.NOTIFICATION_PAYLOAD);
                            if (jsonObject == null) {
                                callback.onFailure(AppConstant.IZ_NO_MORE_DATA);
                                return;
                            }

                            String rid = jsonObject.optString(ShortPayloadConstant.RID);

                            if (!uniqueRids.contains(rid)) {
                                uniqueRids.add(rid);
                                jsonObject1 = new JSONObject();
                                jsonObject1.put(AppConstant.IZ_TITLE_INFO, jsonObject.optString(ShortPayloadConstant.TITLE));
                                jsonObject1.put(AppConstant.IZ_MESSAGE_INFO, jsonObject.optString(ShortPayloadConstant.NMESSAGE));
                                jsonObject1.put(AppConstant.IZ_BANNER_INFO, jsonObject.optString(ShortPayloadConstant.BANNER));
                                jsonObject1.put(AppConstant.IZ_LANDING_URL_INFO, jsonObject.optString(ShortPayloadConstant.LINK));
                                jsonObject1.put(AppConstant.IZ_TIME_STAMP_INFO, jsonObject.optString(ShortPayloadConstant.CREATEDON));
                                feedItem.add(jsonObject1);
                            } else {
                                Log.d(AppConstant.APP_NAME_TAG, "Duplicate notification skipped: rid = " + rid);
                            }
                        }
                        callback.onSuccess(feedItem.toString());
                        callback.onFeedItemCount(jsonArray.length());

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

    private interface FetchFeedCallback {
        void onSuccess(String data);

        void onFailure(String errorMessage);

        void onFeedItemCount(int count);
    }

}
