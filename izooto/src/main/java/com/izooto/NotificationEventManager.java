package com.izooto;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.izooto.core.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NotificationEventManager {
    private static int badgeColor;
    private static int priority;
    private static boolean addCheck;
    private static String lastView_Click = "0";
    private static boolean isCheck;
    public static String iZootoReceivedPayload;


    // Manage notification - Ads and cloud push
    public static void manageNotification(Payload payload) {
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
            addCheck = false;
            try {
                allCloudPush(payload);
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "manageNotification");
            }
        } else {
            addCheck = true;
            allAdPush(payload);
        }
    }


    // Ads push
    private static void allAdPush(Payload payload) {
        if (iZooto.appContext == null || payload == null) {
            return;
        }
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (preferenceUtil.getIntData(AppConstant.CLOUD_PUSH) == 1) {
                if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                    showNotification(payload);
                } else {
                    processPayload(payload);
                }
            } else {
                try {
                    String data = preferenceUtil.getStringData(AppConstant.NOTIFICATION_DUPLICATE);
                    JSONObject jsonObject = new JSONObject();
                    if (data != null && !data.isEmpty()) {
                        JSONArray jsonArray1 = new JSONArray(data);
                        if (jsonArray1.length() > 150) {
                            long currentTime = System.currentTimeMillis(); //fetch start time
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                                if ((currentTime - (Long.parseLong(jsonObject2.getString(AppConstant.CHECK_CREATED_ON)))) > Long.parseLong(payload.getTime_to_live())) {
                                    jsonArray1.remove(i);
                                } else {
                                    if (i < 10) {
                                        jsonArray1.remove(i);
                                    }
                                }
                            }
                            preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                        } else {
                            if (jsonArray1.length() > 0) {
                                for (int index = 0; index < jsonArray1.length(); index++) {
                                    JSONObject jsonObject1 = jsonArray1.getJSONObject(index);
                                    if (jsonObject1.getString(AppConstant.CHECK_CREATED_ON).equalsIgnoreCase(payload.getCreated_Time()) && jsonObject1.getString(AppConstant.CHECK_RID).equalsIgnoreCase(payload.getRid())) {
                                        isCheck = true;
                                        if (jsonObject1.getString(AppConstant.Check_Notification).equalsIgnoreCase(AppConstant.YES)) {
                                            jsonArray1.remove(index);
                                        } else {
                                            jsonArray1.remove(index);
                                            jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                            jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                            jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                            jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_YES);
                                            jsonArray1.put(jsonObject);
                                        }
                                        break;
                                    } else {
                                        isCheck = false;
                                    }
                                }

                                if (isCheck) {
                                    preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());

                                } else {
                                    if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                                        showNotification(payload);
                                    } else {
                                        processPayload(payload);
                                    }
                                    jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                    jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                    jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                    jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                                    jsonArray1.put(jsonObject);
                                    preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                                }
                            } else {
                                jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                                jsonArray1.put(jsonObject);
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                                if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                                    showNotification(payload);
                                } else {
                                    processPayload(payload);
                                }
                            }
                        }
                    } else {
                        JSONArray jsonArray = new JSONArray();
                        jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                        jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                        jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                        jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                        jsonArray.put(jsonObject);
                        preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray.toString());
                        if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                            showNotification(payload);
                        } else {
                            processPayload(payload);
                        }
                        preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray.toString());
                    }
                } catch (Exception ex) {
                    Util.handleExceptionOnce(iZooto.appContext, ex.toString(), "NotificationEventManager", "allAdPush");
                }
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), "NotificationEventManager", "allAdPush");
        }
    }


    // cloud push
    private static void allCloudPush(Payload payload) {
        if (iZooto.appContext == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        try {
            if (preferenceUtil.getIntData(AppConstant.CLOUD_PUSH) == 1) {
                showNotification(payload);
            } else {
                String data = preferenceUtil.getStringData(AppConstant.NOTIFICATION_DUPLICATE);
                JSONObject jsonObject = new JSONObject();
                if (!data.isEmpty()) {
                    JSONArray jsonArray1 = new JSONArray(data);
                    if (jsonArray1.length() > 150) {
                        long currentTime = System.currentTimeMillis(); //fetch start time
                        for (int i = 0; i < jsonArray1.length(); i++) {
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                            if ((currentTime - (Long.parseLong(jsonObject2.getString(AppConstant.CHECK_CREATED_ON)))) > Long.parseLong(payload.getTime_to_live())) {
                                jsonArray1.remove(i);
                            } else {
                                if (i < 10) {
                                    jsonArray1.remove(i);
                                }
                            }
                        }
                        preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                    } else {
                        if (jsonArray1.length() > 0) {
                            for (int index = 0; index < jsonArray1.length(); index++) {
                                JSONObject jsonObject1 = jsonArray1.getJSONObject(index);
                                if (jsonObject1.getString(AppConstant.CHECK_CREATED_ON).equalsIgnoreCase(payload.getCreated_Time()) && jsonObject1.getString(AppConstant.CHECK_RID).equalsIgnoreCase(payload.getRid())) {
                                    isCheck = true;
                                    if (jsonObject1.getString(AppConstant.Check_Notification).equalsIgnoreCase(AppConstant.YES)) {
                                        jsonArray1.remove(index);

                                    } else {
                                        jsonArray1.remove(index);
                                        jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                        jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                        jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                        jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_YES);
                                        jsonArray1.put(jsonObject);
                                    }
                                    break;
                                } else {
                                    isCheck = false;
                                }
                            }
                            if (isCheck) {
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                            } else {
                                showNotification(payload);
                                jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                                jsonArray1.put(jsonObject);
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                            }
                        } else {
                            showNotification(payload);
                            jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                            jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                            jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                            jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                            jsonArray1.put(jsonObject);
                            preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                        }
                    }
                } else {
                    JSONArray jsonArray = new JSONArray();
                    jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                    jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                    jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                    jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                    jsonArray.put(jsonObject);
                    preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray.toString());
                    showNotification(payload);
                }
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), "NotificationEventManager", "allCloudPush");
        }
    }


    static void processPayload(final Payload payload) {
        if (payload == null) {
            return;
        }
        String fetchURL = Util.updateUrlParameter(payload.getFetchURL());
        RestClient.get(fetchURL, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response == null) {
                    return;
                }
                try {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext, response, "fetcherPayloadResponse");
                    Object json = new JSONTokener(response).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject jsonObject = new JSONObject(response);
                        parseJson(payload, jsonObject);

                    } else if (json instanceof JSONArray) {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("", jsonArray);
                        parseJson(payload, jsonObject);
                    }
                } catch (JSONException e) {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Fetcher" + e + response, "[Log.e]->");
                    String fallBackURL = AdMediation.callFallbackAPI(payload);
                    AdMediation.showFallBackResponse(fallBackURL, payload);
                }

            }


            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, response, "fetcherPayloadResponse");
                String fallBackURL = AdMediation.callFallbackAPI(payload);
                AdMediation.showFallBackResponse(fallBackURL, payload);
            }
        });

    }

    static void parseJson(Payload payload, JSONObject jsonObject) {
        try {
            if (payload == null || jsonObject == null) {
                return;
            }
            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                payload.setLink(AdMediation.getParsedValue(jsonObject, payload.getLink()));
                if (!payload.getLink().startsWith("http://") && !payload.getLink().startsWith("https://")) {
                    String url = payload.getLink();
                    url = "https://" + url;
                    payload.setLink(url);
                }
            }

            if (payload.getTitle() != null && !payload.getTitle().isEmpty()) {
                payload.setTitle(AdMediation.getParsedValue(jsonObject, payload.getTitle()));
            }
            if (payload.getMessage() != null && !payload.getMessage().isEmpty())
                payload.setMessage(AdMediation.getParsedValue(jsonObject, payload.getMessage()));
            if (payload.getBanner() != null && !payload.getBanner().isEmpty())
                payload.setBanner(AdMediation.getParsedValue(jsonObject, payload.getBanner()));
            if (payload.getIcon() != null && !payload.getIcon().isEmpty())
                payload.setIcon(AdMediation.getParsedValue(jsonObject, payload.getIcon()));
            if (payload.getAct1name() != null && !payload.getAct1name().isEmpty())
                payload.setAct1name(AdMediation.getParsedValue(jsonObject, payload.getAct1name()));
            payload.setAct1link(AdMediation.getParsedValue(jsonObject, payload.getAct1link()));
            if (!payload.getAct1link().startsWith("http://") && !payload.getAct1link().startsWith("https://")) {
                String url = payload.getAct1link();
                url = "https://" + url;
                payload.setAct1link(url);
            }
            if (payload.getAct2name() != null && !payload.getAct2name().isEmpty())
                payload.setAct2name(AdMediation.getParsedValue(jsonObject, payload.getAct2name()));
            payload.setAct2link(AdMediation.getParsedValue(jsonObject, payload.getAct2link()));
            if (!payload.getAct2link().startsWith("http://") && !payload.getAct2link().startsWith("https://")) {
                String url = payload.getAct2link();
                url = "https://" + url;
                payload.setAct2link(url);
            }
            parseRvValues(payload, jsonObject);
            parseRcValues(payload, jsonObject);
            payload.setAp("");
            payload.setInapp(0);
            if (payload.getTitle() != null && !payload.getTitle().isEmpty()) {
                notificationPreview(iZooto.appContext, payload);
                AdMediation.successList.clear();
                AdMediation.failsList.clear();
                AdMediation.showClicksAndImpressionData(payload);
            } else {
                String fallBackURL = AdMediation.callFallbackAPI(payload);
                AdMediation.showFallBackResponse(fallBackURL, payload);
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "parseJson");
        }
    }


    // handle the rc key
    private static boolean isValidJson(String jsonStr) throws JSONException {
        Object json = new JSONTokener(jsonStr).nextValue();
        return json instanceof JSONObject || json instanceof JSONArray;
    }

    static void parseRcValues(Payload payload, JSONObject jsonObject) {
        try {
            String object;
            if (payload.getRc() != null && !payload.getRc().isEmpty()) {
                JSONArray jsonArray = new JSONArray(payload.getRc());
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getString(i);
                    payload.setRc(getRcParseValues(jsonObject, object));
                    AdMediation.clicksData.add(payload.getRc());
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "parseRcValues");
        }
    }


    static String getRcParseValues(JSONObject jsonObject, String sourceString) {
        try {
            if (isValidJson(sourceString)) {
                if (sourceString.startsWith("~")) return sourceString.replace("~", "");
                else {
                    if (sourceString.contains(".")) {
                        JSONObject jsonObject1 = null;
                        String[] linkArray = sourceString.split("\\.");
                        if (linkArray.length == 2 || linkArray.length == 3) {
                            for (String s : linkArray) {
                                if (s.contains("[")) {
                                    String[] linkArray1 = s.split("\\[");
                                    jsonObject1 = jsonObject.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                                } else {
                                    jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]);
                                }
                                return jsonObject1.optString(linkArray[2]);
                            }
                        } else if (linkArray.length == 4) {
                            if (linkArray[2].contains("[")) {
                                String[] linkArray1 = linkArray[2].split("\\[");
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                                return jsonObject1.optString(linkArray[3]);
                            }
                        }
                    }
                }
            } else {
                return sourceString;
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "getRcParseValues");
        }
        return null;
    }


    static String getRvParseValues(JSONObject jsonObject, String path) throws Exception {
        try {
            String[] parts = path.split("\\.");
            JSONObject currentObject = jsonObject;
            for (String part : parts) {
                part = part.replaceAll("\\[|\\]|'", "");

                if (part.contains("[")) {
                    String key = part.substring(0, part.indexOf("["));
                    int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));

                    if (currentObject.has(key)) {
                        Object value = currentObject.get(key);
                        if (value instanceof JSONArray) {
                            JSONArray array = (JSONArray) value;
                            if (index >= 0 && index < array.length()) {
                                currentObject = array.getJSONObject(index);
                            } else {
                                throw new Exception("Index out of bounds for key '" + key + "'");
                            }
                        } else {
                            throw new Exception("Key '" + key + "' is not an array.");
                        }
                    } else {
                        throw new Exception("Key '" + key + "' not found.");
                    }
                } else {
                    if (currentObject.has(part)) {
                        Object value = currentObject.get(part);
                        if (value instanceof JSONObject) {
                            currentObject = (JSONObject) value;
                        } else {
                            return String.valueOf(value);
                        }
                    } else {
                        throw new Exception("Key '" + part + "' not found.");
                    }
                }
            }

            return currentObject.toString();
        } catch (Exception e) {
            throw new Exception("Error while fetching value: " + e.getMessage());
        }
    }

    static void parseRvValues(Payload payload, JSONObject jsonObject) {
        try {
            if (payload.getRv() != null && !payload.getRv().isEmpty()) {
                JSONArray jsonArray = new JSONArray(payload.getRv());
                for (int i = 0; i < jsonArray.length(); i++) {
                    String path = jsonArray.getString(i);
                    try {
                        if (getRvParseValues(jsonObject, path) != null && !getRvParseValues(jsonObject, path).isEmpty()) {
                            payload.setRv(getRvParseValues(jsonObject, path));
                            callRandomView(payload.getRv());
                        }
                    } catch (Exception e) {
                        Log.i("parseRvValues", e.toString());
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "parseAgainJson");
                    }

                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "parseRvValues");
        }
    }


    static void callRandomView(String rv) {
        try {
            if (rv != null && !rv.isEmpty()) {
                RestClient.get(rv, new RestClient.ResponseHandler() {
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                    }

                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }
                });
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "callRandomView");
        }
    }


    private static void showNotification(final Payload payload) {
        if (iZooto.appContext == null) return;
        notificationPreview(iZooto.appContext, payload);
    }


    //handle ads notifications
    private static void receiveAds(final Payload payload) {
        try {
            if (payload == null) {
                return;
            }
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable notificationRunnable = new Runnable() {
                @SuppressLint("LaunchActivityFromNotification")
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String clickIndex = "0";
                    String impressionIndex = "0";
                    String lastSeventhIndex = "0";
                    String lastNinthIndex = "0";
                    String data = Util.getIntegerToBinary(payload.getCfg());

                    if (!Utilities.isNullOrEmpty(data)) {
                        clickIndex = String.valueOf(data.charAt(data.length() - 2));
                        impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                        lastView_Click = String.valueOf(data.charAt(data.length() - 3));
                        lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                        lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                    }

                    badgeCountUpdate(payload.getBadgeCount());
                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);

                    // create channel and get channelId
                    NotificationManager notificationManager = (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    String channelId = iZootoNotificationChannelHandler.createNotificationChannel(iZooto.appContext, notificationManager, payload);

                    NotificationCompat.Builder notificationBuilder = null;
                    Notification summaryNotification = null;
                    int SUMMARY_ID = 0;
                    Intent intent = null;

                    badgeColor = getBadgeColor(payload.getBadgecolor());

                    // Add icon and banner image
                    Bitmap iconBitmap = payload.getIconBitmap();
                    Bitmap bannerBitmap = payload.getBannerBitmap();


                    intent = notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, clickIndex, lastView_Click, 100, 0);
                    if (intent == null) {
                        return;
                    }
                    PendingIntent pendingIntent = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getActivity(iZooto.appContext, (int) System.currentTimeMillis() /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, (int) System.currentTimeMillis() /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                    }


                    notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId).setSmallIcon(getDefaultSmallIconId()).setContentTitle(payload.getTitle()).setContentText(payload.getMessage()).setContentIntent(pendingIntent).setStyle(new NotificationCompat.BigTextStyle().bigText(payload.getMessage())).setOngoing(Util.enableSticky(payload)) /*    Notification sticky   */.setTimeoutAfter(Util.getRequiredInteraction(payload)) /*    Required Interaction   */.setAutoCancel(true);


                    try {
                        BigInteger accentColor = Util.getAccentColor();
                        if (accentColor != null)
                            notificationBuilder.setColor(accentColor.intValue());
                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "getAccentColor");
                    }


                    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                        if (payload.getPriority() == 0)
                            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                        else {
                            priority = priorityForLessOreo();
                            notificationBuilder.setPriority(priority);
                        }
                    }


                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (payload.getGroup() == 1) {
                            notificationBuilder.setGroup(payload.getGroupKey());


                            summaryNotification = new NotificationCompat.Builder(iZooto.appContext, channelId).setContentTitle(payload.getTitle()).setContentText(payload.getMessage()).setSmallIcon(getDefaultSmallIconId()).setColor(badgeColor).setStyle(new NotificationCompat.InboxStyle().addLine(payload.getMessage()).setBigContentTitle(payload.getGroupMessage())).setGroup(payload.getGroupKey()).setGroupSummary(true).setOngoing(Util.enableSticky(payload)) /*    Notification sticky   */.setTimeoutAfter(Util.getRequiredInteraction(payload)) /*    Required Interaction   */.build();


                        }
                    }


                    if (!payload.getSubTitle().contains(AppConstant.NULL) && payload.getSubTitle() != null && !payload.getSubTitle().isEmpty()) {
                        notificationBuilder.setSubText(payload.getSubTitle());
                    }


                    if (payload.getBadgecolor() != null && !payload.getBadgecolor().isEmpty()) {
                        notificationBuilder.setColor(badgeColor);
                    }


                    if (iconBitmap != null) notificationBuilder.setLargeIcon(iconBitmap);
                    else if (bannerBitmap != null) notificationBuilder.setLargeIcon(bannerBitmap);


                    if (bannerBitmap != null && !payload.getSubTitle().contains(AppConstant.NULL) && payload.getSubTitle() != null && !payload.getSubTitle().isEmpty()) {
                        notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bannerBitmap).bigLargeIcon(iconBitmap).setSummaryText(payload.getMessage()));
                    } else if (bannerBitmap != null && payload.getMessage() != null && !payload.getMessage().isEmpty()) {
                        notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bannerBitmap).bigLargeIcon(iconBitmap).setSummaryText(payload.getMessage()));


                    } else if (bannerBitmap != null && payload.getMessage().isEmpty()) {
                        notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bannerBitmap).bigLargeIcon(iconBitmap).setSummaryText(Util.makeBlackString(payload.getTitle())));
                    }

                    int notificationId;
                    if (payload.getTag() != null && !payload.getTag().isEmpty()) {
                        notificationId = Util.convertStringToDecimal(payload.getTag());
                    } else {
                        notificationId = (int) System.currentTimeMillis();
                    }

                    if (payload.getAct1name() != null && !payload.getAct1name().isEmpty()) {
                        String phone = getPhone(payload.getAct1link());
                        Intent btn1 = notificationClick(payload, payload.getAct1link(), payload.getLink(), payload.getAct2link(), phone, clickIndex, lastView_Click, notificationId, 1);
                        if (btn1 == null) {
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            btn1.setPackage(Util.getPackageName(iZooto.appContext));
                            pendingIntent = PendingIntent.getActivity(iZooto.appContext, (int) System.currentTimeMillis(), btn1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        } else {
                            pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, (int) System.currentTimeMillis(), btn1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        }

                        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, payload.getAct1name().replace("~", ""), pendingIntent).build();
                        notificationBuilder.addAction(action1);
                    }


                    if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                        String phone = getPhone(payload.getAct2link());
                        Intent btn2 = notificationClick(payload, payload.getAct2link(), payload.getLink(), payload.getAct1link(), phone, clickIndex, lastView_Click, notificationId, 2);
                        if (btn2 == null) {
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            btn2.setPackage(Util.getPackageName(iZooto.appContext));
                            pendingIntent = PendingIntent.getActivity(iZooto.appContext, (int) System.currentTimeMillis(), btn2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        } else {
                            pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, (int) System.currentTimeMillis(), btn2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        }


                        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, payload.getAct2name().replace("~", ""), pendingIntent).build();
                        notificationBuilder.addAction(action2);
                    }
                    try {
                        if (payload.getMakeStickyNotification() != null && !payload.getMakeStickyNotification().isEmpty() && payload.getMakeStickyNotification().equals("1")) {
                            preferenceUtil.setStringData(AppConstant.TP_TYPE, AppConstant.TYPE_P);
                            if (NotificationPreview.dismissedNotification(payload, notificationId) != null) {
                                Intent btn3 = NotificationPreview.dismissedNotification(payload, notificationId);
                                if (btn3 == null) {
                                    return;
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    btn3.setPackage(Util.getPackageName(iZooto.appContext));
                                    pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, notificationId, btn3, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                } else {
                                    pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, notificationId, btn3, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                }
                            }

                            NotificationCompat.Action action3 = new NotificationCompat.Action.Builder(0, iZooto.appContext.getResources().getString(R.string.iz_cta_dismissed), pendingIntent).build();
                            notificationBuilder.addAction(action3);
                        }
                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "getMakeStickyNotification");
                    }


                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (payload.getGroup() == 1) {
                            notificationManager.notify(SUMMARY_ID, summaryNotification);
                        }
                    }
                    notificationManager.notify(notificationId, notificationBuilder.build());


                    try {
                        if (lastView_Click.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")) {
                            lastViewNotificationApi(payload, lastView_Click, lastSeventhIndex, lastNinthIndex);
                        }


                        if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                            iZooto.notificationView(payload);
                        } else {
                            NotificationEventManager.onReceiveNotificationHybrid(iZooto.appContext, payload);
                            NotificationEventManager.iZootoReceivedPayload = preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY);
                            iZooto.notificationViewHybrid(NotificationEventManager.iZootoReceivedPayload, payload);
                        }

                        if (payload.getMaxNotification() != 0) {
                            getMaximumNotificationInTray(iZooto.appContext, payload.getMaxNotification());
                        }

                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "notificationView");
                    }
                }
            };

            if (payload.getFetchURL() != null && !payload.getFetchURL().isEmpty()) {
                NotificationExecutorService notificationExecutorService = new NotificationExecutorService(iZooto.appContext);
                notificationExecutorService.executeNotification(handler, notificationRunnable, payload);
            } else {
                handler.post(notificationRunnable);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "notificationView");
        }
    }


    private static void receivedNotification(final Payload payload) {
        try {
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable notificationRunnable = new Runnable() {
                @SuppressLint("LaunchActivityFromNotification")
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String clickIndex = "0";
                    String lastSeventhIndex = "0";
                    String lastNinthIndex = "0";
                    String data = Util.getIntegerToBinary(payload.getCfg());
                    if (!Utilities.isNullOrEmpty(data)) {
                        clickIndex = String.valueOf(data.charAt(data.length() - 2));
                        lastView_Click = String.valueOf(data.charAt(data.length() - 3));
                        lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                        lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                    }

                    badgeCountUpdate(payload.getBadgeCount());

                    // create channel and get channelId
                    NotificationManager notificationManager = (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    String channelId = iZootoNotificationChannelHandler.createNotificationChannel(iZooto.appContext, notificationManager, payload);


                    NotificationCompat.Builder notificationBuilder = null;
                    Notification summaryNotification = null;
                    int SUMMARY_ID = 0;
                    Intent intent = null;

                    badgeColor = getBadgeColor(payload.getBadgecolor());

                    // Add icon and banner image
                    Bitmap iconBitmap = payload.getIconBitmap();
                    Bitmap bannerBitmap = payload.getBannerBitmap();

                    intent = notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, clickIndex, lastView_Click, 100, 0);

                    if (intent == null) {
                        return;
                    }

                    PendingIntent pendingIntent = null;
                    // support Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getActivity(iZooto.appContext, (int) System.currentTimeMillis() /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, (int) System.currentTimeMillis() /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                    }


                    //-------------- RemoteView  notification layout  ---------------
                    RemoteViews collapsedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.remote_view);
                    RemoteViews expandedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.remote_view_expands);


                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        if (bannerBitmap == null && iconBitmap == null) {
                            if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 46) {
                                collapsedView.setTextViewText(R.id.tv_title, payload.getTitle());
                                collapsedView.setViewVisibility(R.id.tv_message, View.VISIBLE);
                                collapsedView.setTextViewText(R.id.tv_message, payload.getMessage());
                            } else {
                                collapsedView.setTextViewText(R.id.tv_title, payload.getTitle());
                            }


                        } else {
                            collapsedView.setViewVisibility(R.id.linear_layout_large_icon, View.VISIBLE);
                            if (iconBitmap != null)
                                collapsedView.setImageViewBitmap(R.id.iv_large_icon, Util.makeCornerRounded(iconBitmap));
                            else
                                collapsedView.setImageViewBitmap(R.id.iv_large_icon, Util.makeCornerRounded(bannerBitmap));
                            if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 40) {
                                collapsedView.setTextViewText(R.id.tv_title, payload.getTitle());
                                collapsedView.setViewVisibility(R.id.tv_message, View.VISIBLE);
                                collapsedView.setTextViewText(R.id.tv_message, payload.getMessage());
                            } else {
                                collapsedView.setTextViewText(R.id.tv_title, payload.getTitle());
                            }
                        }
                    } else {
                        if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 46) {
                            collapsedView.setTextViewText(R.id.tv_title, payload.getTitle());
                            collapsedView.setViewVisibility(R.id.tv_message, View.VISIBLE);
                            collapsedView.setTextViewText(R.id.tv_message, payload.getMessage());
                        } else collapsedView.setTextViewText(R.id.tv_title, payload.getTitle());
                    }


                    //--------------------- expanded notification ------------------
                    if (bannerBitmap == null) {
                        expandedView.setTextViewText(R.id.tv_title, payload.getTitle());
                        if (!payload.getMessage().isEmpty()) {
                            expandedView.setViewVisibility(R.id.tv_message, View.VISIBLE);
                            expandedView.setTextViewText(R.id.tv_message, payload.getMessage());
                        }
                    } else {
                        if (bannerBitmap != null) {
                            if (payload.getAct1name().isEmpty() && payload.getAct2name().isEmpty()) {
                                expandedView.setViewVisibility(R.id.tv_title_with_banner_with_button, View.INVISIBLE);
                                expandedView.setViewVisibility(R.id.iv_banner, View.VISIBLE);//0 for visible
                                expandedView.setImageViewBitmap(R.id.iv_banner, bannerBitmap);
                                if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 46) {
                                    expandedView.setViewVisibility(R.id.tv_message_with_banner, View.VISIBLE);
                                    expandedView.setTextViewText(R.id.tv_title, payload.getTitle());
                                    expandedView.setTextViewText(R.id.tv_message_with_banner, payload.getMessage());
                                } else {
                                    if (!payload.getMessage().isEmpty()) {
                                        expandedView.setViewVisibility(R.id.tv_message_with_banner_with_button, View.VISIBLE);
                                        expandedView.setTextViewText(R.id.tv_title, payload.getTitle());
                                        expandedView.setTextViewText(R.id.tv_message_with_banner_with_button, payload.getMessage());
                                    } else
                                        expandedView.setTextViewText(R.id.tv_title, payload.getTitle());
                                }
                            } else {
                                expandedView.setViewVisibility(R.id.tv_title_with_banner_with_button, View.VISIBLE);
                                expandedView.setViewVisibility(R.id.tv_title, View.INVISIBLE);//2 for gone
                                expandedView.setViewVisibility(R.id.iv_banner, View.VISIBLE);
                                expandedView.setTextViewText(R.id.tv_title_with_banner_with_button, payload.getTitle());
                                expandedView.setImageViewBitmap(R.id.iv_banner, bannerBitmap);
                                if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 46) {
                                    expandedView.setViewVisibility(R.id.tv_message_with_banner_with_button, View.VISIBLE);
                                    expandedView.setTextViewText(R.id.tv_message_with_banner_with_button, payload.getMessage());
                                }
                            }
                        }
                    }


                    notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId).setSmallIcon(getDefaultSmallIconId()).setContentTitle(payload.getTitle()).setContentText(payload.getMessage()).setContentIntent(pendingIntent).setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setCustomContentView(collapsedView).setCustomBigContentView(expandedView).setOngoing(Util.enableSticky(payload)).setTimeoutAfter(Util.getRequiredInteraction(payload)) /*    Required Interaction   */.setAutoCancel(true);


                    try {
                        BigInteger accentColor = Util.getAccentColor();
                        if (accentColor != null)
                            notificationBuilder.setColor(accentColor.intValue());
                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "getAccentColor");
                    }


                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                        notificationBuilder.setCustomHeadsUpContentView(collapsedView);
                        if (iconBitmap != null) notificationBuilder.setLargeIcon(iconBitmap);
                        else {
                            if (bannerBitmap != null)
                                notificationBuilder.setLargeIcon(bannerBitmap);
                        }
                    }


                    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                        if (payload.getPriority() == 0)
                            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                        else {
                            priority = priorityForLessOreo();
                            notificationBuilder.setPriority(priority);
                        }
                    }


                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (payload.getGroup() == 1) {
                            if (payload.getMessage().isEmpty()) {
                                notificationBuilder.setGroup(payload.getGroupKey());
                                summaryNotification = new NotificationCompat.Builder(iZooto.appContext, channelId).setContentText(Util.makeBoldString(payload.getTitle())).setSmallIcon(getDefaultSmallIconId()).setColor(badgeColor).setStyle(new NotificationCompat.InboxStyle().addLine(Util.makeBlackString(payload.getTitle())).setBigContentTitle(payload.getGroupMessage())).setGroup(payload.getGroupKey()).setGroupSummary(true).setOngoing(Util.enableSticky(payload)).setTimeoutAfter(Util.getRequiredInteraction(payload)) /*    Required Interaction   */.build();
                            } else {
                                notificationBuilder.setGroup(payload.getGroupKey());


                                summaryNotification = new NotificationCompat.Builder(iZooto.appContext, channelId).setContentTitle(payload.getTitle()).setContentText(payload.getMessage()).setSmallIcon(getDefaultSmallIconId()).setColor(badgeColor).setStyle(new NotificationCompat.InboxStyle().addLine(payload.getMessage()).setBigContentTitle(payload.getGroupMessage())).setGroup(payload.getGroupKey()).setOngoing(Util.enableSticky(payload)).setTimeoutAfter(Util.getRequiredInteraction(payload)) /*    Required Interaction   */.setGroupSummary(true).build();
                            }
                        }
                    }


                    if (!payload.getSubTitle().contains(AppConstant.NULL) && payload.getSubTitle() != null && !payload.getSubTitle().isEmpty()) {
                        notificationBuilder.setSubText(payload.getSubTitle());
                    }
                    if (payload.getBadgecolor() != null && !payload.getBadgecolor().isEmpty()) {
                        notificationBuilder.setColor(badgeColor);
                    }

                    int notificationID;
                    if (payload.getTag() != null && !payload.getTag().isEmpty()) {
                        notificationID = Util.convertStringToDecimal(payload.getTag());
                    } else {
                        notificationID = (int) System.currentTimeMillis();
                    }


                    if (payload.getAct1name() != null && !payload.getAct1name().isEmpty()) {
                        String phone = getPhone(payload.getAct1link());
                        Intent btn1 = notificationClick(payload, payload.getAct1link(), payload.getLink(), payload.getAct2link(), phone, clickIndex, lastView_Click, notificationID, 1);

                        if (btn1 == null) {
                            return;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            btn1.setPackage(Util.getPackageName(iZooto.appContext));
                            pendingIntent = PendingIntent.getActivity(iZooto.appContext, (int) System.currentTimeMillis(), btn1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        } else {
                            pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, (int) System.currentTimeMillis(), btn1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


                        }
                        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(R.drawable.transparent_image, payload.getAct1name().replace("~", ""), pendingIntent).build();
                        notificationBuilder.addAction(action1);
                    }


                    if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                        String phone = getPhone(payload.getAct2link());
                        Intent btn2 = notificationClick(payload, payload.getAct2link(), payload.getLink(), payload.getAct1link(), phone, clickIndex, lastView_Click, notificationID, 2);

                        if (btn2 == null) {
                            return;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            btn2.setPackage(Util.getPackageName(iZooto.appContext));
                            pendingIntent = PendingIntent.getActivity(iZooto.appContext, (int) System.currentTimeMillis(), btn2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        } else {
                            pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, (int) System.currentTimeMillis(), btn2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        }
                        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.drawable.transparent_image, payload.getAct2name().replace("~", ""), pendingIntent).build();
                        notificationBuilder.addAction(action2);
                    }


                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                    try {
                        if (payload.getMakeStickyNotification() != null && !payload.getMakeStickyNotification().isEmpty() && payload.getMakeStickyNotification().equals("1")) {
                            preferenceUtil.setStringData(AppConstant.TP_TYPE, AppConstant.TYPE_P);
                            if (NotificationPreview.dismissedNotification(payload, notificationID) != null) {
                                Intent cancelIntent = NotificationPreview.dismissedNotification(payload, notificationID);
                                if (cancelIntent == null) {
                                    return;
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    cancelIntent.setPackage(Util.getPackageName(iZooto.appContext));
                                    pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, notificationID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                } else {
                                    pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, notificationID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                }
                            }
                            NotificationCompat.Action action3 = new NotificationCompat.Action.Builder(R.drawable.transparent_image, iZooto.appContext.getResources().getString(R.string.iz_cta_dismissed), pendingIntent).build();
                            notificationBuilder.addAction(action3);

                        }
                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "receivedNotification");
                    }

                    // adding a new button
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (payload.getGroup() == 1) {
                            notificationManager.notify(SUMMARY_ID, summaryNotification);
                        }
                    }
                    notificationManager.notify(notificationID, notificationBuilder.build());

                    try {
                        if (lastView_Click.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")) {
                            lastViewNotificationApi(payload, lastView_Click, lastSeventhIndex, lastNinthIndex);
                        }

                        if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK))
                            iZooto.notificationView(payload);
                        else {
                            NotificationEventManager.onReceiveNotificationHybrid(iZooto.appContext, payload);
                            NotificationEventManager.iZootoReceivedPayload = preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY);
                            iZooto.notificationViewHybrid(NotificationEventManager.iZootoReceivedPayload, payload);
                        }
                        if (payload.getMaxNotification() != 0) {
                            getMaximumNotificationInTray(iZooto.appContext, payload.getMaxNotification());
                        }

                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "receivedNotification");
                    }
                }
            };


            if (payload.getFetchURL() != null && !payload.getFetchURL().isEmpty()) {
                NotificationExecutorService notificationExecutorService = new NotificationExecutorService(iZooto.appContext);
                notificationExecutorService.executeNotification(handler, notificationRunnable, payload);
            } else {
                handler.post(notificationRunnable);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "receivedNotification");
        }
    }


    private static String getFinalUrl(Payload payload) {
        byte[] data = new byte[0];
        try {
            data = payload.getLink().getBytes(AppConstant.UTF);
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "getFinalUrl");
        }
        String encodedLink = Base64.encodeToString(data, Base64.DEFAULT);
        Uri builtUri = Uri.parse(payload.getLink()).buildUpon().appendQueryParameter(AppConstant.URL_ID, payload.getId()).appendQueryParameter(AppConstant.URL_CLIENT, payload.getKey()).appendQueryParameter(AppConstant.URL_RID, payload.getRid()).appendQueryParameter(AppConstant.URL_BKEY_, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).appendQueryParameter(AppConstant.URL_FRWD___, encodedLink).build();
        return builtUri.toString();
    }


    public static String decodeURL(String url) {
        try {
            if (url.contains(AppConstant.URL_FWD)) {
                String[] arrOfStr = url.split(AppConstant.URL_FWD_);
                String[] second = arrOfStr[1].split(AppConstant.URL_BKEY);
                return new String(Base64.decode(second[0], Base64.DEFAULT));
            } else {
                return url;
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "decodeURL");
            return url;
        }
    }


    static int priorityForLessOreo() {
        return Notification.PRIORITY_HIGH;
    }


    static void badgeCountUpdate(int count) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        try {
            if (count > 0) {
                if (preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT) >= 1) {
                    preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT) + 1);
                } else {
                    preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT, 1);
                }

            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "badgeCountUpdate");
        }
    }


    // notification click intent
    static Intent notificationClick(Payload payload, String landingUrl, String btn1URL, String btn2URL, String phone, String finalClickIndex, String lastClick, int notificationId, int button) {
        String link = landingUrl;
        String link1 = payload.getAct1link();
        String link2 = payload.getAct2link();
        String lnKey = payload.getLink();
        try {
            if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
                link = Util.updateUrlParameter(link);
                link1 = Util.updateUrlParameter(link1);
                link2 = Util.updateUrlParameter(link2);
                lnKey = Util.updateUrlParameter(lnKey);
            }

            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                intent = new Intent(iZooto.appContext, TargetActivity.class);

            } else {
                intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);
            }

            link = (link == null) ? "" : link;
            link1 = (link1 == null) ? "" : link1;
            link2 = (link2 == null) ? "" : link2;
            lnKey = (lnKey == null) ? "" : lnKey;

            intent.putExtra(AppConstant.KEY_WEB_URL, link);
            intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificationId);
            intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
            intent.putExtra(AppConstant.KEY_IN_CID, payload.getId());
            intent.putExtra(AppConstant.KEY_IN_RID, payload.getRid());
            intent.putExtra(AppConstant.KEY_IN_BUTOON, button);
            intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, payload.getAp());
            intent.putExtra(AppConstant.KEY_IN_PHONE, phone);
            intent.putExtra(AppConstant.KEY_IN_ACT1ID, payload.getAct1ID());
            intent.putExtra(AppConstant.KEY_IN_ACT2ID, payload.getAct2ID());
            intent.putExtra(AppConstant.LANDINGURL, link);
            intent.putExtra(AppConstant.ACT1TITLE, payload.getAct1name());
            intent.putExtra(AppConstant.ACT2TITLE, payload.getAct2name());
            intent.putExtra(AppConstant.ACT1URL, link1);
            intent.putExtra(AppConstant.ACT2URL, link2);
            intent.putExtra(AppConstant.CLICKINDEX, finalClickIndex);
            intent.putExtra(AppConstant.LASTCLICKINDEX, lastClick);
            intent.putExtra(AppConstant.PUSH, payload.getPush_type());
            intent.putExtra(AppConstant.CFGFORDOMAIN, payload.getCfg());
            intent.putExtra(AppConstant.IZ_NOTIFICATION_TITLE_KEY_NAME, payload.getTitle());
            intent.putExtra(AppConstant.P_MESSAGE, payload.getMessage());
            intent.putExtra(AppConstant.P_BANNER_IMAGE, payload.getBanner());
            intent.putExtra(AppConstant.KEY_LN, lnKey);
            return intent;

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "notificationClick");
            return null;
        }
    }


    // Badge color
    static int getBadgeColor(String setColor) {
        int iconColor;
        if (setColor.contains("#")) {
            try {
                iconColor = Color.parseColor(setColor);
            } catch (IllegalArgumentException ex) {
                iconColor = Color.TRANSPARENT;
                ex.printStackTrace();
            }
        } else if (setColor != null && !setColor.isEmpty()) {
            try {
                iconColor = Color.parseColor("#" + setColor);
            } catch (Exception ex) { // handle your exception
                iconColor = Color.TRANSPARENT;
            }
        } else {
            iconColor = Color.TRANSPARENT;
        }
        return iconColor;
    }


    static String getPhone(String getActLink) {
        String phone;
        String checkNumber = decodeURL(getActLink);
        if (checkNumber.contains(AppConstant.TELIPHONE)) phone = checkNumber;
        else phone = AppConstant.NO;
        return phone;
    }


    static void lastViewNotificationApi(final Payload payload, String lastViewIndex, String seventhCFG, String ninthCFG) {
        if (iZooto.appContext == null) {
            return;
        }
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            String dayDiff1 = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_WEEKLY));
            String updateWeekly = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_WEEKLY);
            String updateDaily = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_DAILY);
            String time = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW);
            String limURL;
            int dataCfg = Util.getBinaryToDecimal(payload.getCfg());

            if (dataCfg > 0) {
                limURL = "https://lim" + dataCfg + ".izooto.com/lim" + dataCfg;
            } else limURL = RestClient.LAST_NOTIFICATION_VIEW_URL;


            if (seventhCFG.equalsIgnoreCase("1")) {
                if (ninthCFG.equalsIgnoreCase("1")) {
                    if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                        preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                        lastViewNotification(limURL, payload.getRid(), payload.getId(), -1);
                    }
                } else {
                    if (updateWeekly.isEmpty() || Integer.parseInt(dayDiff1) >= 7) {
                        preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_WEEKLY, Util.getTime());
                        lastViewNotification(limURL, payload.getRid(), payload.getId(), -1);
                    }
                }
            } else if (lastViewIndex.equalsIgnoreCase("1") && seventhCFG.equalsIgnoreCase("0")) {
                String dayDiff = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW));
                if (time.isEmpty() || Integer.parseInt(dayDiff) >= 7) {
                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW, Util.getTime());
                    lastViewNotification(limURL, payload.getRid(), payload.getId(), -1);
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "lastViewNotificationApi");
        }
    }


    static void lastViewNotification(String limURL, String rid, String cid, int i) {
        if (iZooto.appContext == null) return;

        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            HashMap<String, Object> data = new HashMap<>();
            data.put(AppConstant.LAST_NOTIFICAION_VIEWED, true);
            JSONObject jsonObject = new JSONObject(data);
            Map<String, String> mapData = new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
            mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(iZooto.appContext));
            mapData.put(AppConstant.VAL, "" + jsonObject);
            mapData.put(AppConstant.ACT, "add");
            mapData.put(AppConstant.ISID_, "1");
            mapData.put(AppConstant.ET_, AppConstant.USERP_);
            RestClient.postRequest(limURL, mapData, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);

                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APPName_2, "lastViewNotification");
        }
    }


    /*
     *Set Maximum notification in the tray through getMaximumNotificationInTray() method
     * */
    static void getMaximumNotificationInTray(Context context, int mn) {
        if (context == null) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationManager notificationManagerActive = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                StatusBarNotification[] notifications = notificationManagerActive.getActiveNotifications();
                SortedMap<Long, Integer> activeNotifIds = new TreeMap<>();
                for (StatusBarNotification notification : notifications) {
                    if (notification.getTag() == null) {
                        activeNotifIds.put(notification.getNotification().when, notification.getId());
                    }
                }

                int data = activeNotifIds.size() - mn;
                for (Map.Entry<Long, Integer> mapData : activeNotifIds.entrySet()) {
                    if (data <= 0) return;
                    data--;
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(mapData.getValue());
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APPName_2, "MaxNotification in Tray");
        }

    }


    static void onReceiveNotificationHybrid(Context context, Payload payload) {
        if (context == null || payload == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        JSONObject payloadJSON = new JSONObject();
        JSONArray jsonArray;
        try {
            if (!preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY).isEmpty())
                jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY));
            else jsonArray = new JSONArray();
            if (jsonArray.length() >= 10) {
                jsonArray.remove(0);
            }
            try {
                payloadJSON.put("title", payload.getTitle());
                payloadJSON.put("message", payload.getMessage());
                payloadJSON.put("banner", payload.getBanner());
                payloadJSON.put("landingURL", Util.updateUrlParameter(payload.getLink()));
            } catch (Exception e) {
                Util.handleExceptionOnce(context, e.toString(), AppConstant.APPName_2, "onReceiveNotificationHybrid");
            }
            jsonArray.put(payloadJSON);
            preferenceUtil.setStringData(AppConstant.PAYLOAD_JSONARRAY, jsonArray.toString());
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APPName_2, "onReceiveNotificationHybrid");
        }
    }


    public static String getDailyTime(Context context) {
        if (context == null) return "";
        else {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            return preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_DAILY);
        }
    }


    public static void handleImpressionAPI(Payload payload, String pushName) {
        if (payload != null && iZooto.appContext != null) {
            try {
                String impressionIndex = "0";
                String data = Util.getIntegerToBinary(payload.getCfg());
                if (data != null && !data.isEmpty()) {
                    impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                    if (impressionIndex.equalsIgnoreCase("1")) {
                        viewNotificationApi(payload, pushName);
                    }
                }
            } catch (Exception ex) {
                Util.handleExceptionOnce(iZooto.appContext, ex + "RID" + payload.getRid() + "CID" + payload.getId(), AppConstant.APPName_2, "handleImpressionAPI");
            }
        }
    }


    static void viewNotificationApi(final Payload payload, String pushName) {
        if (iZooto.appContext == null) {
            return;
        }
        String impURL;
        int dataCfg = Util.getBinaryToDecimal(payload.getCfg());
        if (dataCfg > 0) {
            impURL = "https://impr" + dataCfg + ".izooto.com/imp" + dataCfg;
        } else impURL = RestClient.IMPRESSION_URL;
        impressionNotification(impURL, payload.getId(), payload.getRid(), -1, pushName);

    }


    public static void impressionNotification(String impURL, String cid, String rid, int i, String pushName) {
        if (iZooto.appContext == null) {
            return;
        }

        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                    Map<String, String> mapData = new HashMap<>();
                    mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                    mapData.put(AppConstant.CID_, cid);
                    mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(iZooto.appContext));
                    mapData.put(AppConstant.RID_, rid);
                    mapData.put(AppConstant.NOTIFICATION_OP, "view");
                    mapData.put(AppConstant.PUSH, pushName);
                    mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
                    RestClient.postRequest(impURL, mapData, null, new RestClient.ResponseHandler() {
                        @Override
                        void onSuccess(final String response) {
                            super.onSuccess(response);
                        }


                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                            Util.handleExceptionOnce(iZooto.appContext, mapData + "Failure", AppConstant.APPName_2, "impressionNotification");
                        }
                    });

                }
            });
            executorService.shutdown();
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e + "RID" + rid + "CID" + cid, AppConstant.APPName_2, "impressionNotification");
        }
    }


    public static void handleNotificationError(String errorName, String payload, String className, String methodName) {
        if (iZooto.appContext == null) {
            return;
        }
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        try {
            HashMap<String, String> data = new HashMap<>();
            data.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            data.put(AppConstant.ANDROID_ID, Util.getAndroidId(iZooto.appContext));
            data.put("op", "view");
            data.put("fcm_token", preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
            data.put("hms_token", preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
            data.put("error", errorName);
            data.put("payloadData", payload);
            data.put("className", className);
            data.put("sdk_version", AppConstant.SDKVERSION);
            data.put("methodName", methodName);

            RestClient.postRequest(RestClient.APP_EXCEPTION_URL, data, null, new RestClient.ResponseHandler() {
                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }

                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                }
            });


        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), AppConstant.APPName_2, "handleNotificationError");
        }

    }


    // notification default icon
    private static int getDefaultSmallIconId() {
        int notificationIcon = getDrawableId();
        if (notificationIcon != 0) {
            return notificationIcon;
        }
        return android.R.drawable.ic_popup_reminder;
    }


    private static int getDrawableId() {
        return iZooto.appContext.getResources().getIdentifier("ic_stat_izooto_default", "drawable", iZooto.appContext.getPackageName());
    }


    static void notificationPreview(Context context, Payload payload) {
        if (context == null) return;

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (payload.getDefaultNotificationPreview() == 2 || preferenceUtil.getIntData(AppConstant.NOTIFICATION_PREVIEW) == PushTemplate.TEXT_OVERLAY) {
            NotificationPreview.receiveCustomNotification(payload);
        } else if (payload.getDefaultNotificationPreview() == 3 || preferenceUtil.getIntData(AppConstant.NOTIFICATION_PREVIEW) == PushTemplate.DEVICE_NOTIFICATION_OVERLAY) {
            receiveAds(payload);
        } else {
            receivedNotification(payload);
        }
    }

    static void callRandomClick(String rc) {
        if (rc != null && !rc.isEmpty()) {
            RestClient.get(rc, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }
            });
        }
    }

}
