package com.izooto;


import static com.izooto.Util.openURLInBrowserIntent;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NotificationActionReceiver extends BroadcastReceiver {

    private String mUrl = "";
    private int inApp;
    private String rid;
    private String cid;
    private int btnCount;
    private static String additionalData;
    private String phoneNumber;
    private String act1ID;
    private String act2ID;
    private static String landingURL;
    private String act2URL;
    private String act1URL;
    private String btn1Title;
    private String btn2Title;
    private String clickIndex = "0";
    private String lastClickIndex = "0";
    public static String notificationClick = "";
    public static String WebViewClick = "";
    public static String medClick = "";
    private String pushType;
    private int cfg;
    private static String notificationTitle = "";
    private static String notificationMessage = "";
    private static String notificationBannerImage = "";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        try {
            String GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE = "15";
            Intent it = new Intent(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
            context.sendBroadcast(it);
            getBundleData(context, intent);
            if (mUrl != null) {
                mUrl.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(context).getStringData(AppConstant.FCM_DEVICE_TOKEN));
            }
            getBundleData(context, intent);
            try {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (clickIndex.equalsIgnoreCase("1")) {
                    String clkURL;
                    int dataCfg = Util.getBinaryToDecimal(cfg);
                    if (dataCfg > 0) {
                        clkURL = "https://clk" + dataCfg + ".izooto.com/clk" + dataCfg;
                    } else {
                        clkURL = RestClient.NOTIFICATION_CLICK;
                    }
                    notificationClickAPI(context, clkURL, cid, rid, btnCount, -1, pushType);
                    String lastEighthIndex;
                    String lastTenthIndex;
                    String dataInBinary = Util.getIntegerToBinary(cfg);
                    if (!dataInBinary.isEmpty()) {
                        lastEighthIndex = String.valueOf(dataInBinary.charAt(dataInBinary.length() - 8));
                        lastTenthIndex = String.valueOf(dataInBinary.charAt(dataInBinary.length() - 10));
                    } else {
                        lastEighthIndex = "0";
                        lastTenthIndex = "0";
                    }

                    if (lastClickIndex.equalsIgnoreCase("1") || lastEighthIndex.equalsIgnoreCase("1")) {
                        String dayDiff1 = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK_WEEKLY));
                        String updateWeekly = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK_WEEKLY);
                        String updateDaily = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK_DAILY);
                        String time = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK);
                        String lciURL;


                        if (dataCfg > 0) {
                            lciURL = "https://lci" + dataCfg + ".izooto.com/lci" + dataCfg;
                        } else
                            lciURL = RestClient.LAST_NOTIFICATION_CLICK_URL;
                        if (lastEighthIndex.equalsIgnoreCase("1")) {
                            if (lastTenthIndex.equalsIgnoreCase("1")) {
                                if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK_DAILY, Util.getTime());
                                    lastClickAPI(context, lciURL, rid, -1);
                                }
                            } else {
                                if (updateWeekly.isEmpty() || Integer.parseInt(dayDiff1) >= 7) {
                                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK_WEEKLY, Util.getTime());
                                    lastClickAPI(context, lciURL, rid, -1);
                                }
                            }
                        } else if (lastClickIndex.equalsIgnoreCase("1") && lastEighthIndex.equalsIgnoreCase("0")) {
                            String dayDiff = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK));
                            if (time.isEmpty() || Integer.parseInt(dayDiff) >= 7) {
                                preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK, Util.getTime());
                                lastClickAPI(context, lciURL, rid, -1);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Util.handleExceptionOnce(context, e.toString(), "NotificationActionReceiver", "onReceive");
            }
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                if (AdMediation.clicksData != null && !AdMediation.clicksData.isEmpty()) {
                    for (int i = 0; i < AdMediation.clicksData.size(); i++) {
                        AdMediation.clicksData.size();
                        try {
                            NotificationEventManager.callRandomClick(AdMediation.clicksData.get(i));
                        } catch (Exception e) {
                            Log.e("onReceive", e.toString());
                        }
                    }
                }
            }
            if (!Objects.equals(preferenceUtil.getStringData("MEDIATIONCLICKDATA"), "")) {
                String medClickData = preferenceUtil.getStringData("MEDIATIONCLICKDATA");
                callMediationClicks(context, medClickData, 0);
            }
            try {
                if (additionalData.equalsIgnoreCase("")) {
                    additionalData = "1";
                }
            } catch (Exception e) {
                Log.e("onReceive", e.toString());
            }
            if (!additionalData.equalsIgnoreCase("1") && inApp >= 0) {
                try {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(AppConstant.BUTTON_ID_1, act1ID);
                    hashMap.put(AppConstant.BUTTON_TITLE_1, btn1Title);
                    hashMap.put(AppConstant.BUTTON_URL_1, act1URL);
                    hashMap.put(AppConstant.ADDITIONAL_DATA, additionalData);
                    hashMap.put(AppConstant.LANDING_URL, landingURL);
                    hashMap.put(AppConstant.BUTTON_ID_2, act2ID);
                    hashMap.put(AppConstant.BUTTON_TITLE_2, btn2Title);
                    hashMap.put(AppConstant.BUTTON_URL_2, act2URL);
                    hashMap.put(AppConstant.IZ_NOTIFICATION_TITLE_KEY_NAME, notificationTitle);
                    hashMap.put(AppConstant.ACTION_TYPE, String.valueOf(btnCount));
                    JSONObject jsonObject = new JSONObject(hashMap);

                    HashMap<String, String> pulseMap = new HashMap<>();
                    pulseMap.put(AppConstant.P_TITLE, notificationTitle);
                    pulseMap.put(AppConstant.P_MESSAGE, notificationMessage);
                    pulseMap.put(AppConstant.P_BANNER_IMAGE, notificationBannerImage);
                    pulseMap.put(AppConstant.P_LANDING_URL, landingURL);
                    JSONObject pulseObject = new JSONObject(pulseMap);
                    if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK))
                        iZooto.notificationActionHandler(jsonObject.toString(), pulseObject.toString(), Util.isPulseDeepLink(cfg));
                    else {
                        if (Util.isAppInForeground(context)) {
                            iZooto.notificationActionHandler(jsonObject.toString(), "", false);
                        } else if (isAppBackground(context)) {
                            if (preferenceUtil.getBoolean("Android8")) {
                                launchApp(context);
                                notificationClick = jsonObject.toString();
                                iZooto.notificationActionHandler(jsonObject.toString(), "", false);
                            } else {
                                launchApp(context);
                                notificationClick = jsonObject.toString();
                                iZooto.notificationActionHandler(jsonObject.toString(), "", false);
                            }
                        } else {
                            notificationClick = jsonObject.toString();
                            launchApp(context);
                        }

                    }
                } catch (Exception e) {
                    Log.e("onReceive", e.toString());
                }

            } else {
                try {
                    if (inApp == 1 && phoneNumber.equalsIgnoreCase(AppConstant.NO) && !Objects.equals(landingURL, "") && !landingURL.isEmpty()) {
                        if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                            iZooto.notificationInAppAction(context, mUrl);
                        } else if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                            if (Util.isAppInForeground(context)) {
                                iZooto.notificationInAppAction(context, mUrl);
                            } else if (isAppBackground(context)) {
                                launchApp(context);
                                WebViewClick = mUrl;
                                iZooto.notificationInAppAction(context, WebViewClick);
                            } else {
                                launchApp(context);
                                WebViewClick = mUrl;
                                iZooto.notificationInAppAction(context, WebViewClick);
                            }
                        } else {
                            iZootoWebViewActivity.startActivity(context, mUrl);
                        }

                    } else {
                        try {
                            if (phoneNumber.equalsIgnoreCase(AppConstant.NO)) {
                                if (mUrl != null && !mUrl.isEmpty()) {
                                    openURLInBrowser(context, mUrl);
                                } else {
                                    if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                                        Util.sleepTime(2000);
                                        launchApp(context);
                                    } else {
                                        launchApp(context);
                                    }
                                }
                            } else {
                                Intent browserIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
                                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(browserIntent);
                            }


                        } catch (Exception ex) {
                            Util.handleExceptionOnce(context, ex.toString(), "notification action", "landing url issues" + mUrl);
                        }
                    }
                } catch (Exception e) {
                    Log.e("onReceive", e.toString());
                }
            }

        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), "NotificationActionReceiver", "onReceive");
        }

    }

    public static boolean isAppBackground(Context context) {
        boolean isBackground = true;

        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isBackground = false;
                        }
                    }
                }
            }
            return isBackground;
        } catch (Exception ex) {
            return isBackground;
        }
    }


    static void openURLInBrowser(Context context, @NonNull String url) {
        openURLInBrowser(context, Uri.parse(url.trim()));
    }

    private static void openURLInBrowser(Context context, @NonNull Uri uri) {
        try {
            Intent intent = openURLInBrowserIntent(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "NotificationActionReceiver", "openURLInBrowser");
        }
    }


    static void lastClickAPI(Context context, String lciURL, String rid, int i) {
        if (context == null)
            return;
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK, Util.getTime());
            HashMap<String, Object> data = new HashMap<>();
            data.put(AppConstant.LAST_NOTIFICAION_CLICKED, true);
            JSONObject jsonObject = new JSONObject(data);
            Map<String, String> mapData = new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
            mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
            mapData.put(AppConstant.VAL, "" + jsonObject);
            mapData.put(AppConstant.ACT, "add");
            mapData.put(AppConstant.ISID_, "1");
            mapData.put(AppConstant.ET_, AppConstant.USERP_);


            RestClient.postRequest(lciURL, mapData, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);
                    try {
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE).isEmpty() && i >= 0) {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE));
                            jsonArrayOffline.remove(i);
                            preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE, null);
                        }
                    } catch (Exception e) {
                        Util.handleExceptionOnce(context, e.toString(), "NotificationActionReceiver", "lastClickAPI");
                    }
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    try {
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE).isEmpty()) {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE));
                            if (!Util.ridExists(jsonArrayOffline, rid)) {
                                Util.trackClickOffline(context, lciURL, AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE, rid, "0", 0);
                            }
                        } else
                            Util.trackClickOffline(context, lciURL, AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE, rid, "0", 0);
                    } catch (Exception e) {
                        Util.handleExceptionOnce(context, e.toString(), "NotificationActionReceiver", "lastClickAPI");
                    }
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "NotificationActionReceiver", "lastClickAPI");
        }

    }


    static void callMediationClicks(Context context, final String medClick, int cNUmber) {
        try {
            if (medClick != null && !medClick.isEmpty()) {
                DebugFileManager.createExternalStoragePublic(context, medClick, "mediationClick");
                JSONObject jsonObject = new JSONObject(medClick);
                RestClient.postRequest(RestClient.MEDIATION_CLICKS, null, jsonObject, new RestClient.ResponseHandler() {
                    @SuppressLint("NewApi")
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                        if (!preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS).isEmpty() && cNUmber >= 0) {
                            try {
                                JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS));
                                jsonArrayOffline.remove(cNUmber);
                                preferenceUtil.setStringData(AppConstant.STORE_MEDIATION_RECORDS, null);
                            } catch (Exception ex) {
                                DebugFileManager.createExternalStoragePublic(context, "MediationCLick" + ex, "[Log.V]->");
                            }
                        } else {
                            preferenceUtil.setStringData("MEDIATIONCLICKDATA", "");
                        }
                    }

                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                        Util.trackMediation_Impression_Click(context, AppConstant.MED_CLICK, medClick);
                    }
                });
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), "NotificationActionReceiver", "callMediationClicks");

        }
    }


    private void getBundleData(Context context, Intent intent) {
        try {
            Bundle tempBundle = intent.getExtras();
            if (tempBundle == null) {
                return;
            }
            if (tempBundle.containsKey(AppConstant.KEY_WEB_URL))
                mUrl = tempBundle.getString(AppConstant.KEY_WEB_URL);
            if (tempBundle.containsKey(AppConstant.KEY_IN_APP))
                inApp = tempBundle.getInt(AppConstant.KEY_IN_APP);
            if (tempBundle.containsKey(AppConstant.KEY_IN_RID))
                rid = tempBundle.getString(AppConstant.KEY_IN_RID);
            if (tempBundle.containsKey(AppConstant.KEY_IN_CID))
                cid = tempBundle.getString(AppConstant.KEY_IN_CID);
            if (tempBundle.containsKey(AppConstant.KEY_IN_BUTOON))
                btnCount = tempBundle.getInt(AppConstant.KEY_IN_BUTOON);
            if (tempBundle.containsKey(AppConstant.KEY_IN_ADDITIONALDATA))
                additionalData = tempBundle.getString(AppConstant.KEY_IN_ADDITIONALDATA);
            if (tempBundle.containsKey(AppConstant.KEY_IN_PHONE))
                phoneNumber = tempBundle.getString(AppConstant.KEY_IN_PHONE);
            if (tempBundle.containsKey(AppConstant.KEY_IN_ACT1ID))
                act1ID = tempBundle.getString(AppConstant.KEY_IN_ACT1ID);
            if (tempBundle.containsKey(AppConstant.KEY_IN_ACT2ID))
                act2ID = tempBundle.getString(AppConstant.KEY_IN_ACT2ID);
            if (tempBundle.containsKey(AppConstant.LANDINGURL))
                landingURL = tempBundle.getString(AppConstant.LANDINGURL);
            if (tempBundle.containsKey(AppConstant.ACT1URL))
                act1URL = tempBundle.getString(AppConstant.ACT1URL);
            if (tempBundle.containsKey(AppConstant.ACT2URL))
                act2URL = tempBundle.getString(AppConstant.ACT2URL);
            if (tempBundle.containsKey(AppConstant.ACT1TITLE))
                btn1Title = tempBundle.getString(AppConstant.ACT1TITLE);
            if (tempBundle.containsKey(AppConstant.ACT2TITLE))
                btn2Title = tempBundle.getString(AppConstant.ACT2TITLE);
            if (tempBundle.containsKey(AppConstant.CLICKINDEX))
                clickIndex = tempBundle.getString(AppConstant.CLICKINDEX);
            if (tempBundle.containsKey(AppConstant.LASTCLICKINDEX))
                lastClickIndex = tempBundle.getString(AppConstant.LASTCLICKINDEX);
            if (tempBundle.containsKey(AppConstant.PUSH))
                pushType = tempBundle.getString(AppConstant.PUSH);
            if (tempBundle.containsKey(AppConstant.CFGFORDOMAIN))
                cfg = tempBundle.getInt(AppConstant.CFGFORDOMAIN);
            if (tempBundle.containsKey(AppConstant.IZ_NOTIFICATION_TITLE_KEY_NAME))
                notificationTitle = tempBundle.getString(AppConstant.IZ_NOTIFICATION_TITLE_KEY_NAME);
            if (tempBundle.containsKey(AppConstant.P_MESSAGE))
                notificationMessage = tempBundle.getString(AppConstant.P_MESSAGE);
            if (tempBundle.containsKey(AppConstant.P_BANNER_IMAGE))
                notificationBannerImage = tempBundle.getString(AppConstant.P_BANNER_IMAGE);

            if (tempBundle.containsKey(AppConstant.KEY_NOTIFICITON_ID)) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(tempBundle.getInt(AppConstant.KEY_NOTIFICITON_ID));
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "notificationClickAPI", "getBundleData");
        }
    }

    static void notificationClickAPI(Context context, String clkURL, String cid, String rid, int btnCount, int i, String pushType) {
        if (context == null)
            return;
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            Map<String, String> mapData = new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
            mapData.put(AppConstant.CID_, cid);
            mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
            mapData.put(AppConstant.RID_, rid);
            mapData.put(AppConstant.PUSH, pushType);
            mapData.put("op", "click");
            mapData.put(AppConstant.IZ_LANDING_URL, landingURL);
            mapData.put(AppConstant.IZ_DEEPLINK_URL, additionalData);
            mapData.put(AppConstant.IZ_NOTIFICATION_TITLE_KEY_NAME, notificationTitle);
            if (btnCount != 0)
                mapData.put("btn", "" + btnCount);
            DebugFileManager.createExternalStoragePublic(context, mapData.toString(), "clickData");
            RestClient.postRequest(clkURL, mapData, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);
                    try {
                        JSONArray jsonArrayOffline;
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty() && i >= 0) {
                            jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                            jsonArrayOffline.remove(i);
                            preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE, null);
                        }
                    } catch (Exception e) {
                        DebugFileManager.createExternalStoragePublic(context, mapData.toString(), "clickData");
                    }
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    try {
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty()) {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                            if (!Util.ridExists(jsonArrayOffline, rid)) {
                                Util.trackClickOffline(context, clkURL, AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE, rid, cid, btnCount);
                            }
                        } else {
                            Util.trackClickOffline(context, clkURL, AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE, rid, cid, btnCount);
                        }
                    } catch (Exception e) {
                        Util.handleExceptionOnce(context, e.toString(), "notificationClickAPI", "NotificationActionReceiver");

                    }
                }
            });
                }
            });
            executorService.shutdown();

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "notificationClickAPI", "NotificationActionReceiver");
        }
    }


    private static void launchApp(Context context) {
        if (context == null) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        Intent launchIntent;
        String name;
        try {
            if (pm != null && !Util.isAppInForeground(context)) {
                ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                name = (String) pm.getApplicationLabel(app);
                launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
                Intent intentAppLaunch = launchIntent; // new Intent();
                if (intentAppLaunch != null) {
                    intentAppLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intentAppLaunch);
                    Log.d(AppConstant.APP_NAME_TAG + "Found it:", name);
                }


            } else {
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (pm != null && preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                    ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                    name = (String) pm.getApplicationLabel(app);
                    launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
                    Intent intentAppLaunch = launchIntent; // new Intent();
                    if (intentAppLaunch != null) {
                        intentAppLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intentAppLaunch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intentAppLaunch);
                        Log.d(AppConstant.APP_NAME_TAG + "Found it:", name);
                    }

                } else {
                    if (pm == null) {
                        return;
                    }
                    ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                    name = (String) pm.getApplicationLabel(app);
                    launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
                    Intent intentAppLaunch = launchIntent; // new Intent();
                    if (intentAppLaunch != null) {
                        intentAppLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intentAppLaunch);
                        Log.d(AppConstant.APP_NAME_TAG + "Found it:", name);
                    }

                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APPName_3, "launch App");
        }

    }
}

