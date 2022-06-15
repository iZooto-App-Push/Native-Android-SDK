package com.izooto;

import static com.izooto.AppConstant.APPPID;
import static com.izooto.AppConstant.FCM_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.GOOGLE_JSON_URL;
import static com.izooto.AppConstant.HUAWEI_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.TAG;
import static com.izooto.AppConstant.XIAOMI_TOKEN_FROM_JSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.huawei.hms.push.HmsMessaging;
import com.izooto.DatabaseHandler.DatabaseHandler;
import com.izooto.shortcutbadger.ShortcutBadger;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
public class iZooto {
    static Context appContext;
    private static String senderId;
    public static String mIzooToAppId;
    public static int icon;
    public static String soundID;
    private static Payload payload;
    public static boolean mUnsubscribeWhenNotificationsAreDisabled;
    protected static Listener mListener;
    protected static Handler mHandler;
    private static FirebaseAnalyticsTrack firebaseAnalyticsTrack;
    public static String inAppOption;
    @SuppressLint("StaticFieldLeak")
    static Activity curActivity;
    private static String advertisementID;
    public static boolean isHybrid = false;//check for SDK(Flutter,React native)
    public static String SDKDEF = "native";
    public static int bannerImage;
    private static boolean initCompleted;
    private static boolean canSetAppId = false;
    static TokenReceivedListener mTokenReceivedListener;
    static NotificationHelperListener mNotificationHelper;
    static NotificationWebViewListener mWebViewListener;
    static NotificationReceiveHybridListener mNotificationReceivedHybridlistener;
    private static LOG_LEVEL visualLogLevel = LOG_LEVEL.NONE;
    private static LOG_LEVEL logCatLevel = LOG_LEVEL.WARN;
    static String notificationReceivePayload = null;
    static String iZootoAppId;

    static boolean isInitCompleted() {
        return initCompleted;
    }

    private static OSTaskManager osTaskManager = new OSTaskManager();

    public static void setSenderId(String senderId) {
        iZooto.senderId = senderId;
    }

    private static void setActivity(Activity activity) {
        curActivity = activity;
    }

    public enum OSInAppDisplayOption {
        None, InAppAlert, Notification
    }

    public enum LOG_LEVEL {
        NONE, FATAL, ERROR, WARN, INFO, DEBUG, VERBOSE
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void init(Context context){
        if (context == null)
            return;
        if (canSetAppId)
            return;
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            canSetAppId = true;

            if (iZootoAppId != null && !iZootoAppId.isEmpty()) {
                preferenceUtil.setStringData(AppConstant.ENCRYPTED_PID, iZootoAppId);
                iZooto.Log(LOG_LEVEL.INFO, iZootoAppId + "");
                RestClient.get(GOOGLE_JSON_URL+iZootoAppId +".dat", new RestClient.ResponseHandler() {
                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                        iZooto.Log(LOG_LEVEL.WARN, AppConstant.INIT_FAILURE);
                    }

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        if (!response.isEmpty() && response.length() > 20 && response != null) {
                            try {
                                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(Util.decrypt(AppConstant.SECRETKEY, response)));
                                senderId = jsonObject.getString(AppConstant.SENDERID);
                                String appId = jsonObject.optString(AppConstant.APPID);
                                String apiKey = jsonObject.optString(AppConstant.APIKEY);
                                String mKey = jsonObject.optString(AppConstant.MIAPIKEY);
                                String mId = jsonObject.optString(AppConstant.MIAPPID);
                                boolean izBranding = jsonObject.optBoolean(AppConstant.IZ_G_BRANDING);
                                String hms_appId = jsonObject.optString(AppConstant.HMS_APP_ID);
                                mIzooToAppId = jsonObject.optString(APPPID);
                                preferenceUtil.setiZootoID(APPPID, mIzooToAppId);
                                preferenceUtil.setBooleanData(AppConstant.JSON_NEWS_HUB_BRANDING,izBranding);
                                String newsHub = jsonObject.optString(AppConstant.JSON_NEWS_HUB);
                                trackAdvertisingId();
                                if (!preferenceUtil.getBoolean(AppConstant.SET_JSON_NEWS_HUB))
                                    fetchNewsHubData(context, newsHub);
                                if (!mKey.isEmpty() && !mId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_XIAOMI_TOKEN)) {
                                    XiaomiSDKHandler xiaomiSDKHandler = new XiaomiSDKHandler(context, mId, mKey);
                                    xiaomiSDKHandler.onMIToken();
                                }
                                if (!hms_appId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Huawei") && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_HUAWEI_TOKEN)) {
                                    initHmsService(context);

                                }
                                preferenceUtil.setStringData(AppConstant._HMS_APPID, hms_appId);
                                if (senderId != null && !senderId.isEmpty()) {
                                    init(context, apiKey, appId);
                                } else {
                                    Lg.e(AppConstant.APP_NAME_TAG, appContext.getString(R.string.something_wrong_fcm_sender_id));
                                }
                                if (mIzooToAppId != null && preferenceUtil.getBoolean(AppConstant.IS_CONSENT_STORED)) {
                                    preferenceUtil.setIntData(AppConstant.CAN_STORED_QUEUE, 1);
                                }
                                if (iZooto.isHybrid)
                                    preferenceUtil.setBooleanData(AppConstant.IS_HYBRID_SDK, iZooto.isHybrid);
                            } catch (JSONException e) {
                                if (context != null) {
                                    DebugFileManager.createExternalStoragePublic(context, e.toString(), "[Log.e]-->init");
                                    Util.setException(context, e.toString(), "init", AppConstant.APP_NAME_TAG);
                                }
                            }
                        } else {
                            DebugFileManager.createExternalStoragePublic(context, "iZooto App id is not sync properly on panel", "[Log.e]-->");
                        }
                    }

                });
            } else {
                iZooto.Log(LOG_LEVEL.WARN, AppConstant.MISSINGID + " Please add your iZooto App id like" + AppConstant.IZOOTO_APP_ID_INTEGRATION);
                DebugFileManager.createExternalStoragePublic(context, AppConstant.MESSAGE, "[Log.e]-->");
            }

        } catch (Throwable t) {
            DebugFileManager.createExternalStoragePublic(context, t.toString(), "[Log.e]-->initBuilder");
            Util.setException(context, t.toString(), AppConstant.APP_NAME_TAG, "initBuilder");
        }
    }

    private static void initHmsService(final Context context) {
        if (context == null)
            return;

        HMSTokenGenerator hmsTokenGenerator = new HMSTokenGenerator();
        hmsTokenGenerator.getHMSToken(context, new HMSTokenListener.HMSTokenGeneratorHandler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void complete(String id) {
                iZooto.Log(LOG_LEVEL.INFO, "HMS Token - " + id);
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (id != null && !id.isEmpty()) {
                    if (!preferenceUtil.getBoolean(AppConstant.IS_UPDATED_HMS_TOKEN)) {
                        preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                        preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                    }
                    iZooto.registerToken();
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void failure(String errorMessage) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, errorMessage, "[Log.v]->");
                iZooto.Log(LOG_LEVEL.WARN, errorMessage);
            }
        });
    }

    private static void init(final Context context, String apiKey, String appId) {
        if (context == null)
            return;

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_FCM_TOKEN) || Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
            initHandler(context);
        } else {
            FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
            fcmTokenGenerator.getToken(context, senderId, apiKey, appId, new TokenGenerator.TokenGenerationHandler() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void complete(String id) {
                    initHandler(context);
                }

                @Override
                public void failure(String errorMsg) {
                    Lg.e(AppConstant.APP_NAME_TAG, errorMsg);
                }
            });
        }

    }

    private static void initHandler(final Context context) {
        if (context == null)
            return;
        setCurActivity(context);
        Util util = new Util();
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (util.isInitializationValid()) {
            iZooto.Log(LOG_LEVEL.INFO, AppConstant.DEVICETOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
            registerToken();
            ActivityLifecycleListener.registerActivity((Application) context);
            setCurActivity(context);


            if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack())
                firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(context);

            initCompleted = true;
            osTaskManager.startPendingTasks();

        }
    }

    public static List<Payload> getNotificationList(Context context) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        if (databaseHandler.isTableExists(true)) {
            List<Payload> data = databaseHandler.getAllNotification();

            return data;
        } else {
            return null;

        }
    }

    private static void trackAdvertisingId() {
        if (appContext != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            AdvertisingIdClient.getAdvertisingId(appContext, new AdvertisingIdClient.Listener() {
                @Override
                public void onAdvertisingIdClientFinish(AdvertisingIdClient.AdInfo adInfo) {
                    advertisementID = adInfo.getId();
                    preferenceUtil.setStringData(AppConstant.ADVERTISING_ID, advertisementID);
                    invokeFinish(advertisementID, preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));

                }

                @Override
                public void onAdvertisingIdClientFail(Exception exception) {
                    invokeFail(new Exception(TAG + " - Error: context null"));
                }
            });
        }
    }

    public static synchronized void idsAvailable(Context context, Listener listener) {
        new iZooto().start(context, listener);
    }

    public static void setNotificationSound(String soundName) {
        soundID = soundName;
    }

    protected void start(final Context context, final Listener listener) {
        if (listener == null) {
            iZooto.Log(LOG_LEVEL.WARN, "getAdvertisingId - Error: null listener, dropping call");
        } else {
            mHandler = new Handler(Looper.getMainLooper());
            mListener = listener;
            if (context == null) {
                invokeFail(new Exception(TAG + " - Error: context null"));
            } else {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                        invokeFinish(preferenceUtil.getStringData(AppConstant.ADVERTISING_ID), preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));
                    }
                }).start();
            }
        }
    }


    public interface Listener {

        void idsAvailable(String adverID, String registrationID);

        void onAdvertisingIdClientFail(Exception exception);
    }

    protected static void invokeFinish(final String advertisementID, final String registrationID) {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mListener != null) {
                    mListener.idsAvailable(advertisementID, registrationID);
                }
            }
        });
    }

    protected static void invokeFail(final Exception exception) {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onAdvertisingIdClientFail(exception);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static void registerToken() {
        if (appContext != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getiZootoID(APPPID) != null && !preferenceUtil.getiZootoID(APPPID).isEmpty()) {
                if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {

                        if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                            preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 3);
                        } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                            preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
                        } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                            preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
                        } else {
                            preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 1);
                        }
                        try {
                            if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty())
                                preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                            if (!preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty())
                                preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_XIAOMI_TOKEN, true);
                            Map<String, String> mapData = new HashMap<>();
                            mapData.put(AppConstant.ADDURL, "" + AppConstant.STYPE);
                            mapData.put(AppConstant.PID, mIzooToAppId);
                            mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                            mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                            mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                            mapData.put(AppConstant.APPVERSION, "" + Util.getAppVersion(iZooto.appContext));
                            mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                            mapData.put(AppConstant.ALLOWED_, "" + AppConstant.ALLOWED);
                            mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                            mapData.put(AppConstant.CHECKSDKVERSION, "" + Util.getSDKVersion(appContext));
                            mapData.put(AppConstant.LANGUAGE, "" + Util.getDeviceLanguage());
                            mapData.put(AppConstant.QSDK_VERSION, "" + AppConstant.SDKVERSION);
                            mapData.put(AppConstant.TOKEN, "" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                            mapData.put(AppConstant.ADVERTISEMENTID, "" + preferenceUtil.getStringData(AppConstant.ADVERTISING_ID));
                            mapData.put(AppConstant.XIAOMITOKEN, "" + preferenceUtil.getStringData(AppConstant.XiaomiToken));
                            mapData.put(AppConstant.PACKAGE_NAME, "" + appContext.getPackageName());
                            mapData.put(AppConstant.SDKTYPE, "" + SDKDEF);
                            mapData.put(AppConstant.KEY_HMS, "" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                            mapData.put(AppConstant.ANDROIDVERSION, "" + Build.VERSION.RELEASE);
                            mapData.put(AppConstant.DEVICENAME, "" + Util.getDeviceName());

                            RestClient.postRequest(RestClient.BASE_URL, mapData, null, new RestClient.ResponseHandler() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                void onSuccess(final String response) {
                                    super.onSuccess(response);
                                    lastVisitApi(appContext);
                                    if (mTokenReceivedListener != null) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    JSONObject jsonObject = new JSONObject();
                                                    jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                                    jsonObject.put(XIAOMI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.XiaomiToken));
                                                    jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                                    mTokenReceivedListener.onTokenReceived(jsonObject.toString());
                                                } catch (Exception ex) {
                                                    Util.setException(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "RegisterToken");
                                                    DebugFileManager.createExternalStoragePublic(appContext, ex.toString(), "[Log.e]->RegisterToken->");
                                                }

                                            }
                                        });
                                    }
                                    preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, true);
                                    preferenceUtil.setLongData(AppConstant.DEVICE_REGISTRATION_TIMESTAMP, System.currentTimeMillis());
                                    areNotificationsEnabledForSubscribedState(appContext);
                                    try {
                                        preferenceUtil.setBooleanData(AppConstant.IS_CONSENT_STORED, true);
                                        preferenceUtil.setIntData(AppConstant.CAN_STORED_QUEUE, 1);

                                        if (!preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA).isEmpty()) {
                                            Util.sleepTime(5000);
                                            JSONObject json = new JSONObject(preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA));
                                            addUserProperty(Util.toMap(json));
                                        }
                                        if (!preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN).isEmpty() && !preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV).isEmpty()) {
                                            JSONObject json = new JSONObject(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV));
                                            addEvent(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN), Util.toMap(json));
                                        }
                                        if (preferenceUtil.getBoolean(AppConstant.IS_SET_SUBSCRIPTION_METHOD))
                                            setSubscription(preferenceUtil.getBoolean(AppConstant.SET_SUBSCRITION_LOCAL_DATA));


                                        if (!preferenceUtil.getStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE).isEmpty()) {
                                            JSONArray jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE));
                                            topicApi(AppConstant.ADD_TOPIC, (List) Util.toList(jsonArray));
                                        }
                                        if (!preferenceUtil.getStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE).isEmpty()) {
                                            JSONArray jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE));
                                            topicApi(AppConstant.REMOVE_TOPIC, (List) Util.toList(jsonArray));
                                        }

                                    } catch (Exception e) {
                                        Util.setException(appContext, e.toString(), "registerToken1", AppConstant.APP_NAME_TAG);
                                    }
                                }

                                @Override
                                void onFailure(int statusCode, String response, Throwable throwable) {
                                    super.onFailure(statusCode, response, throwable);
                                    DebugFileManager.createExternalStoragePublic(appContext, response.toString(), "[Log.e]->RegisterTokenFailure->");

                                }
                            });

                        } catch (Exception exception) {
                            Util.setException(appContext, exception.toString(), AppConstant.APP_NAME_TAG, "registerToken");
                        }
                    }
                } else {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            lastVisitApi(appContext);
                        }
                        areNotificationsEnabledForSubscribedState(appContext);
                        if (mTokenReceivedListener != null) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                jsonObject.put(XIAOMI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.XiaomiToken));
                                jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                mTokenReceivedListener.onTokenReceived(jsonObject.toString());

                            } catch (Exception ex) {
                                DebugFileManager.createExternalStoragePublic(appContext, ex.toString(), "[Log.e]->RegisterTokenFailure->");
                                Util.setException(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "registerToken");

                            }
                        }
                        if (!preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA).isEmpty()) {
                            JSONObject json = new JSONObject(preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA));
                            addUserProperty(Util.toMap(json));
                        }
                        if (!preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN).isEmpty() && !preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV).isEmpty()) {
                            JSONObject json = new JSONObject(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV));
                            addEvent(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN), Util.toMap(json));
                        }
                        if (preferenceUtil.getBoolean(AppConstant.IS_SET_SUBSCRIPTION_METHOD))
                            setSubscription(preferenceUtil.getBoolean(AppConstant.SET_SUBSCRITION_LOCAL_DATA));
                        if (!preferenceUtil.getStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE).isEmpty()) {
                            JSONArray jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE));
                            topicApi(AppConstant.ADD_TOPIC, (List) Util.toList(jsonArray));
                        }
                        if (!preferenceUtil.getStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE).isEmpty()) {
                            JSONArray jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE));
                            topicApi(AppConstant.REMOVE_TOPIC, (List) Util.toList(jsonArray));
                        }
                        if (!preferenceUtil.getBoolean(AppConstant.FILE_EXIST)) {
                            try {

                                Map<String, String> mapData = new HashMap<>();
                                mapData.put(AppConstant.ADDURL, "" + AppConstant.STYPE);
                                mapData.put(AppConstant.PID, mIzooToAppId);
                                mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                                mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                                mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                                mapData.put(AppConstant.APPVERSION, "" + Util.getSDKVersion(iZooto.appContext));
                                mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                                mapData.put(AppConstant.ALLOWED_, "" + AppConstant.ALLOWED);
                                mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                                mapData.put(AppConstant.CHECKSDKVERSION, "" + Util.getSDKVersion(appContext));
                                mapData.put(AppConstant.LANGUAGE, "" + Util.getDeviceLanguage());
                                mapData.put(AppConstant.QSDK_VERSION, "" + AppConstant.SDKVERSION);
                                mapData.put(AppConstant.TOKEN, "" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                mapData.put(AppConstant.ADVERTISEMENTID, "" + preferenceUtil.getStringData(AppConstant.ADVERTISING_ID));
                                mapData.put(AppConstant.XIAOMITOKEN, "" + preferenceUtil.getStringData(AppConstant.XiaomiToken));
                                mapData.put(AppConstant.PACKAGE_NAME, "" + appContext.getPackageName());
                                mapData.put(AppConstant.SDKTYPE, "" + SDKDEF);
                                mapData.put(AppConstant.KEY_HMS, "" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                mapData.put(AppConstant.ANDROIDVERSION, "" + Build.VERSION.RELEASE);
                                mapData.put(AppConstant.DEVICENAME, "" + Util.getDeviceName());
                                DebugFileManager.createExternalStoragePublic(iZooto.appContext, mapData.toString(), "RegisterToken");

                            } catch (Exception exception) {
                                DebugFileManager.createExternalStoragePublic(iZooto.appContext, "RegisterToken -> " + exception.toString(), "[Log.e]->");
                                Util.setException(appContext, exception.toString(), "registerToken", "iZooto");

                            }
                        }


                    } catch (Exception e) {
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "RegisterToken -> " + e.toString(), "[Log.e]->");
                        Util.setException(appContext, e.toString(), "registerToken", "iZooto");
                    }
                }
            } else {
                Util.setException(iZooto.appContext, "Missing pid", AppConstant.APP_NAME_TAG, "Register Token");
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Missing PID -> ", "[Log.e]->");

            }
        }
    }

    static void onActivityResumed(Activity activity) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        setActivity(activity);
        if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.influenceOpenTrack();
                preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, true);
            }
        }

        try {
            ShortcutBadger.applyCountOrThrow(appContext, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setCurActivity(Context context) {
        boolean foreground = isContextActivity(context);
        if (foreground) {
            iZooto.curActivity = (Activity) context;
        }
    }

    private static boolean isContextActivity(Context context) {
        return context instanceof Activity;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void processNotificationReceived(Context context, Payload payload) {
        if (payload != null) {
            NotificationEventManager.manageNotification(payload);
        }
        if (context != null) {
            sendOfflineDataToServer(context);
        }

    }

     static void notificationView(Payload payload) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (payload != null) {
            if (mNotificationHelper != null && payload != null) {
                mNotificationHelper.onNotificationReceived(payload);
            }
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            }

            if (payload.getId() != null && !payload.getId().isEmpty()) {
                if (!payload.getId().equals(preferenceUtil.getStringData(AppConstant.TRACK_NOTIFICATION_ID))) {
                    preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, false);
                }
                preferenceUtil.setStringData(AppConstant.TRACK_NOTIFICATION_ID, payload.getId());
            }

        }
    }

    public static void notificationActionHandler(String data) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);

        if (!data.isEmpty()) {
            if (mNotificationHelper != null) {
                mNotificationHelper.onNotificationOpened(data);
            }
        }
        if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
            firebaseAnalyticsTrack.openedEventTrack();
        } else {
            if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                firebaseAnalyticsTrack.openedEventTrack();
            }
        }
        try {
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT) - 1);
            ShortcutBadger.applyCountOrThrow(appContext, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void notificationInAppAction(String url) {
        if (mWebViewListener != null)
            mWebViewListener.onWebView(url);
    }

    /*
      Handle the Hybrid Web_View Listener
     */
    private static void runNotificationWebViewCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                if (!NotificationActionReceiver.WebViewClick.isEmpty()) {
                    iZooto.mWebViewListener.onWebView(NotificationActionReceiver.WebViewClick);
                    NotificationActionReceiver.WebViewClick = "";
                }
            }
        });
    }

    /*
      Handle the Hybrid Notification Opened Listener
     */
    private static void runNotificationOpenedCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                if (!NotificationActionReceiver.notificationClick.isEmpty()) {
                    iZooto.mNotificationHelper.onNotificationOpened(NotificationActionReceiver.notificationClick);
                    NotificationActionReceiver.notificationClick = "";
                }
            }
        });
    }

    // handle the execution
    static void runOnMainUIThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(runnable);
        }

    }

    public static void notificationViewHybrid(String payloadList, Payload payload) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (payload != null) {
            if (mNotificationReceivedHybridlistener != null) {
                mNotificationReceivedHybridlistener.onNotificationReceivedHybrid(payloadList);
            }
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            } else {
                if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                    firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                    firebaseAnalyticsTrack.receivedEventTrack(payload);
                }
            }
            if (payload.getId() != null && !payload.getId().isEmpty()) {
                if (!payload.getId().equals(preferenceUtil.getStringData(AppConstant.TRACK_NOTIFICATION_ID))) {
                    preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, false);
                }
                preferenceUtil.setStringData(AppConstant.TRACK_NOTIFICATION_ID, payload.getId());
            }
        }
    }

    private static void runNotificationReceivedCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (NotificationEventManager.iZootoReceivedPayload != null) {
                    iZooto.mNotificationReceivedHybridlistener.onNotificationReceivedHybrid(NotificationEventManager.iZootoReceivedPayload);
                }
                if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
                    if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                        firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                        firebaseAnalyticsTrack.influenceOpenTrack();
                        preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, true);
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void areNotificationsEnabledForSubscribedState(Context context) {
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.UNSUBSCRIBE_WHEN_NOTIFICATIONS_ARE_DISABLED) && context == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    iZooto.Log(LOG_LEVEL.DEBUG, "unsubscribeWhenNotificationsAreDisabled(): operation from pending task queue.");
                    areNotificationsEnabledForSubscribedState(context);
                }
            });
            return;
        }
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (context != null) {
            int value = 0;
            if (mUnsubscribeWhenNotificationsAreDisabled) {
                boolean isChecked = Util.isNotificationEnabled(context);
                if (!isChecked) {
                    value = 2;
                }
            }
            if (value == 0 && preferenceUtil.getIntData(AppConstant.GET_NOTIFICATION_ENABLED) == 0) {
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_ENABLED, 1);
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_DISABLED, 0);
                getNotificationAPI(context, value);

            } else if (value == 2 && preferenceUtil.getIntData(AppConstant.GET_NOTIFICATION_DISABLED) == 0) {
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_DISABLED, 1);
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_ENABLED, 0);
                getNotificationAPI(context, value);

            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setDefaultTemplate(int templateID) {
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.SET_CUSTOM_TEMPLATE) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    iZooto.Log(LOG_LEVEL.DEBUG, "setDefaultTemplate(): operation from pending task queue.");
                    setDefaultTemplate(templateID);
                }
            });
            return;
        }
        if (PushTemplate.DEFAULT == templateID || PushTemplate.TEXT_OVERLAY == templateID) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_PREVIEW, templateID);
        } else {
            Util.setException(appContext, "Template id is not matched" + templateID, AppConstant.APP_NAME_TAG, "setDefaultTemplate");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void getNotificationAPI(Context context, int value) {

        if (context != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            try {
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(context));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.APPVERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.ACTION, "" + value);

                        RestClient.postRequest(RestClient.SUBSCRIPTION_API, mapData, null, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }
                        });
                    }
                }
            } catch (Exception ex) {
                Util.setException(iZooto.appContext, ex.toString(), AppConstant.APP_NAME_TAG, "getNotificationAPI");
            }
        }

    }

    public static void setInAppNotificationBehaviour(OSInAppDisplayOption displayOption) {
        inAppOption = displayOption.toString();
    }

    /*
     * Through this we will be able to send events with event name and event data
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void addEvent(String eventName, HashMap<String, Object> data) {
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.ADD_EVENT) && appContext == null) {
            String finalEventName = eventName;
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    Log.d(AppConstant.APP_NAME_TAG, "addEvent(): operation from pending task queue.");
                    addEvent(finalEventName, data);
                }
            });
            return;
        }

        if (data != null && eventName != null && eventName.length() > 0 && data.size() > 0) {
            eventName = eventName.substring(0, Math.min(eventName.length(), 32)).replace(" ", "_");
            HashMap<String, Object> newListEvent = new HashMap<String, Object>();
            for (Map.Entry<String, Object> refineEntry : data.entrySet()) {
                if (refineEntry.getKey() != null && !refineEntry.getKey().isEmpty()) {
                    String newKey = refineEntry.getKey().toLowerCase();
                    newListEvent.put(newKey, refineEntry.getValue());
                }
            }
            if (newListEvent.size() > 0)
                addEventAPI(eventName, newListEvent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void addEventAPI(String eventName, HashMap<String, Object> data) {
        if (appContext != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            HashMap<String, Object> filterEventData = checkValidationEvent(data, 1);
            if (filterEventData.size() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(filterEventData);

                    if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                        if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                            Map<String, String> mapData = new HashMap<>();
                            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                            mapData.put(AppConstant.ACT, eventName);
                            mapData.put(AppConstant.ET_, "evt");
                            mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                            mapData.put(AppConstant.VAL, "" + jsonObject.toString());

                            RestClient.postRequest(RestClient.EVENT_URL, mapData, null, new RestClient.ResponseHandler() {
                                @Override
                                void onSuccess(final String response) {
                                    super.onSuccess(response);
                                    preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, null);
                                    preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, null);
                                }

                                @Override
                                void onFailure(int statusCode, String response, Throwable throwable) {
                                    super.onFailure(statusCode, response, throwable);
                                    JSONObject jsonObjectLocal = new JSONObject(data);
                                    preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, eventName);
                                    preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, jsonObjectLocal.toString());
                                }
                            });
                        } else {
                            JSONObject jsonObjectLocal = new JSONObject(data);
                            preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, eventName);
                            preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, jsonObjectLocal.toString());
                        }
                    } else {
                        JSONObject jsonObjectLocal = new JSONObject(data);
                        preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, eventName);
                        preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, jsonObjectLocal.toString());
                    }
                } catch (Exception ex) {
                    Util.setException(appContext, ex.toString(), "iZooto", "add Event");
                }
            } else {
                Util.setException(appContext, "Event length more than 32", AppConstant.APP_NAME_TAG, "AdEvent");
            }
        }
    }

    private static HashMap<String, Object> checkValidationEvent(HashMap<String, Object> data, int index) {
        HashMap<String, Object> newList = new HashMap<String, Object>();
        for (HashMap.Entry<String, Object> array : data.entrySet()) {
            if (index <= 16) {
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String) {
                    if (array.getValue().toString().length() > 0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 64));
                        newList.put(newKey, newValue);
                        index++;
                    }
                } else if (!(array.getValue() instanceof String) && array.getValue() != null) {
                    newList.put(newKey, (array.getValue()));
                    index++;
                }
            }
        }
        return newList;
    }

    /*
     * You can use user properties to capture attributes such as demographic information or interests.
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void addUserProperty(HashMap<String, Object> object) {

        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.ADD_USERPROPERTY) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext, "addUserProperty(): operation from pending task queue.", "[Log.d]->addUserProperty->");
                    addUserProperty(object);
                }
            });
            return;
        }

        if (object != null && object.size() > 0) {
            try {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                HashMap<String, Object> newListUserProfile = new HashMap<String, Object>();
                for (Map.Entry<String, Object> refineEntry : object.entrySet()) {
                    if (refineEntry.getKey() != null && !refineEntry.getKey().isEmpty()) {
                        String newKey = refineEntry.getKey().toLowerCase();
                        newListUserProfile.put(newKey, refineEntry.getValue());
                    }
                }
                if (newListUserProfile.size() > 0) {
                    HashMap<String, Object> filterUserPropertyData = checkValidationUserProfile(newListUserProfile, 1);
                    if (filterUserPropertyData.size() > 0) {
                        JSONObject jsonObject = new JSONObject(filterUserPropertyData);
                        if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                            if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                                Map<String, String> mapData = new HashMap<>();
                                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                                mapData.put(AppConstant.ACT, "add");
                                mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                                mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                                mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                                RestClient.postRequest(RestClient.PROPERTIES_URL, mapData, null, new RestClient.ResponseHandler() {
                                    @Override
                                    void onSuccess(final String response) {
                                        super.onSuccess(response);
                                        preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, null);
                                    }

                                    @Override
                                    void onFailure(int statusCode, String response, Throwable throwable) {
                                        super.onFailure(statusCode, response, throwable);
                                        JSONObject jsonObjectLocal = new JSONObject(object);
                                        preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, jsonObjectLocal.toString());

                                    }
                                });
                            } else {
                                JSONObject jsonObjectLocal = new JSONObject(object);
                                preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, jsonObjectLocal.toString());
                            }
                        } else {
                            JSONObject jsonObjectLocal = new JSONObject(object);
                            preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, jsonObjectLocal.toString());
                        }
                    } else {
                        Util.setException(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");

                    }

                } else {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");

                    Util.setException(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");

                }

            } catch (Exception e) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");

                Util.setException(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");
            }
        } else {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");

            Util.setException(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");

        }

    }

    private static HashMap<String, Object> checkValidationUserProfile(HashMap<String, Object> data, int index) {
        HashMap<String, Object> newList = new HashMap<String, Object>();
        int indexForValue = 1;
        for (HashMap.Entry<String, Object> array : data.entrySet()) {
            if (index <= 64) {
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String) {
                    if (array.getValue().toString().length() > 0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 64));
                        newList.put(newKey, newValue);
                        index++;
                    }
                } else if (array.getValue() instanceof List) {
                    List<Object> newvalueListDta = (List<Object>) array.getValue();
                    List<Object> newvalueList = new ArrayList<Object>();
                    for (Object obj : newvalueListDta) {
                        if (indexForValue <= 64) {
                            if (obj instanceof String) {
                                String ListData = obj.toString();
                                if (indexForValue <= 64 && ListData.length() > 0) {
                                    String newListValue = ListData.substring(0, Math.min(ListData.length(), 64));
                                    newvalueList.add(newListValue);
                                    indexForValue++;
                                }
                            } else if (!(obj instanceof String) && obj != null) {
                                newvalueList.add(obj);
                                indexForValue++;
                            }
                        }
                    }
                    newList.put(newKey, newvalueList);
                    index++;
                } else if (!(array.getValue() instanceof String) && !(array.getValue() instanceof List) && array.getValue() != null) {
                    newList.put(newKey, (array.getValue()));
                    index++;
                }
            }
        }
        return newList;
    }

    /*
     * Customize your notification's badge to reflect your brand logo.
     * */
    public static void setIcon(int icon1) {
        icon = icon1;
    }

    /*
     * Disables iZooto from sending notifications to the current device.
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setSubscription(Boolean enable) {
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.SET_SUBSCRIPTION) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    setSubscription(enable);
                }
            });
            return;
        }

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);

        try {
            int value = 2;
            if (enable != null) {
                if (enable) {
                    value = 0;
                }

                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.APPVERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.ACTION, "" + value);
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "setSubscription" + mapData.toString(), "[Log.d]->setSubscription->");

                        RestClient.postRequest(RestClient.SUBSCRIPTION_API, mapData, null, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, false);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                                preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, true);
                                preferenceUtil.setBooleanData(AppConstant.SET_SUBSCRITION_LOCAL_DATA, enable);
                            }
                        });
                    } else {
                        preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, true);
                        preferenceUtil.setBooleanData(AppConstant.SET_SUBSCRITION_LOCAL_DATA, enable);
                    }
                } else {
                    preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, true);
                    preferenceUtil.setBooleanData(AppConstant.SET_SUBSCRITION_LOCAL_DATA, enable);
                }

            } else {
                Util.setException(appContext, "Value should not be null", AppConstant.APP_NAME_TAG, "setSubscription");

            }
        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, "setSubscription" + e.toString(), "[Log.e]->Exception->");

            Util.setException(appContext, e.toString(), AppConstant.APP_NAME_TAG, "setSubscription");
        }

    }

    /*
     * Send events to Google Analytics to track your campaign performance.
     * */
    public static void setFirebaseAnalytics(boolean isSet) {
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.SET_FIREBASE_ANALYTICS) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    Log.d(AppConstant.APP_NAME_TAG, "setFirebaseAnalytics(): operation from pending task queue.");
                    setFirebaseAnalytics(isSet);
                }
            });
            return;
        }
        if (appContext != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            preferenceUtil.setBooleanData(AppConstant.FIREBASE_ANALYTICS_TRACK, isSet);
        }
    }

    /*
     * Define a default banner image to be used when using the custom template.
     * */
    public static void setDefaultNotificationBanner(int setBanner) {
        bannerImage = setBanner;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void iZootoHandleNotification(Context context, final Map<String, String> data) {
        iZooto.Log(LOG_LEVEL.DEBUG, AppConstant.NOTIFICATIONRECEIVED);
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);

            if (data.get(AppConstant.AD_NETWORK) != null || data.get(AppConstant.GLOBAL) != null || data.get(AppConstant.GLOBAL_PUBLIC_KEY) != null) {
                if (data.get(AppConstant.GLOBAL_PUBLIC_KEY) != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(data.get(AppConstant.GLOBAL)));
                        String urlData = data.get(AppConstant.GLOBAL_PUBLIC_KEY);
                        jsonObject.toString();
                        if (urlData != null && !urlData.isEmpty()) {
                            String cid = jsonObject.optString(ShortpayloadConstant.ID);
                            String rid = jsonObject.optString(ShortpayloadConstant.RID);
                            NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, "FCM");
                            AdMediation.getMediationGPL(context, jsonObject, urlData);
                        } else {
                            NotificationEventManager.handleNotificationError("Payload Error", data.toString(), "MessagingSevices", "HandleNow");
                        }
                    } catch (Exception ex) {
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Payload" + ex.toString() + data.toString(), "[Log.e]->Exception->");

                        Util.setException(context, ex.toString() + "PayloadError" + data.toString(), "DATBMessagingService", "handleNow");
                    }

                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(data.get(AppConstant.GLOBAL));
                        String cid = jsonObject.optString(ShortpayloadConstant.ID);
                        String rid = jsonObject.optString(ShortpayloadConstant.RID);
                        NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, "FCM");
                        JSONObject jsonObject1 = new JSONObject(data.toString());
                        AdMediation.getMediationData(context, jsonObject1, "fcm", "");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);
                    } catch (Exception ex) {
                        Util.setException(context, ex.toString() + "PayloadError" + data.toString(), "DATBMessagingService", "handleNow");
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Payload Error" + ex.toString() + data.toString(), "[Log.e]->Exception->");

                    }
                }
            } else {
                preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                JSONObject payloadObj = new JSONObject(data);
                if (payloadObj.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(iZooto.appContext).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    payload.setCreated_Time(payloadObj.optString(ShortpayloadConstant.CREATEDON));
                    payload.setFetchURL(payloadObj.optString(ShortpayloadConstant.FETCHURL));
                    payload.setKey(payloadObj.optString(ShortpayloadConstant.KEY));
                    payload.setId(payloadObj.optString(ShortpayloadConstant.ID));
                    payload.setRid(payloadObj.optString(ShortpayloadConstant.RID));
                    payload.setLink(payloadObj.optString(ShortpayloadConstant.LINK));
                    payload.setTitle(payloadObj.optString(ShortpayloadConstant.TITLE));
                    payload.setMessage(payloadObj.optString(ShortpayloadConstant.NMESSAGE));
                    payload.setIcon(payloadObj.optString(ShortpayloadConstant.ICON));
                    payload.setReqInt(payloadObj.optInt(ShortpayloadConstant.REQINT));
                    payload.setTag(payloadObj.optString(ShortpayloadConstant.TAG));
                    payload.setBanner(payloadObj.optString(ShortpayloadConstant.BANNER));
                    payload.setAct_num(payloadObj.optInt(ShortpayloadConstant.ACTNUM));
                    payload.setBadgeicon(payloadObj.optString(ShortpayloadConstant.BADGE_ICON));
                    payload.setBadgecolor(payloadObj.optString(ShortpayloadConstant.BADGE_COLOR));
                    payload.setSubTitle(payloadObj.optString(ShortpayloadConstant.SUBTITLE));
                    payload.setGroup(payloadObj.optInt(ShortpayloadConstant.GROUP));
                    payload.setBadgeCount(payloadObj.optInt(ShortpayloadConstant.BADGE_COUNT));
                    // Button 2
                    payload.setAct1name(payloadObj.optString(ShortpayloadConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(ShortpayloadConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(ShortpayloadConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(ShortpayloadConstant.ACT1ID));
                    // Button 2
                    payload.setAct2name(payloadObj.optString(ShortpayloadConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(ShortpayloadConstant.ACT2LINK));
                    payload.setAct2icon(payloadObj.optString(ShortpayloadConstant.ACT2ICON));
                    payload.setAct2ID(payloadObj.optString(ShortpayloadConstant.ACT2ID));

                    payload.setInapp(payloadObj.optInt(ShortpayloadConstant.INAPP));
                    payload.setTrayicon(payloadObj.optString(ShortpayloadConstant.TARYICON));
                    payload.setSmallIconAccentColor(payloadObj.optString(ShortpayloadConstant.ICONCOLOR));
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.SOUND));
                    payload.setLedColor(payloadObj.optString(ShortpayloadConstant.LEDCOLOR));
                    payload.setLockScreenVisibility(payloadObj.optInt(ShortpayloadConstant.VISIBILITY));
                    payload.setGroupKey(payloadObj.optString(ShortpayloadConstant.GKEY));
                    payload.setGroupMessage(payloadObj.optString(ShortpayloadConstant.GMESSAGE));
                    payload.setFromProjectNumber(payloadObj.optString(ShortpayloadConstant.PROJECTNUMBER));
                    payload.setCollapseId(payloadObj.optString(ShortpayloadConstant.COLLAPSEID));
                    payload.setPriority(payloadObj.optInt(ShortpayloadConstant.PRIORITY));
                    payload.setRawPayload(payloadObj.optString(ShortpayloadConstant.RAWDATA));
                    payload.setAp(payloadObj.optString(ShortpayloadConstant.ADDITIONALPARAM));
                    payload.setCfg(payloadObj.optInt(ShortpayloadConstant.CFG));
                    payload.setTime_to_live(payloadObj.optString(ShortpayloadConstant.TIME_TO_LIVE));
                    payload.setPush_type(AppConstant.PUSH_FCM);
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortpayloadConstant.MAX_NOTIFICATION));

                } else {
                    String updateDaily = NotificationEventManager.getDailyTime(context);
                    if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                        preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                        NotificationEventManager.handleNotificationError("Payload Error" + payloadObj.optString("t"), payloadObj.toString(), "iz_db_clientside_handle_servcie", "handleNow()");
                    }
                    return;
                }
                if (iZooto.appContext == null)
                    iZooto.appContext = context;
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        NotificationEventManager.handleImpressionAPI(payload, AppConstant.PUSH_FCM);
                        iZooto.processNotificationReceived(context, payload);
                    } // This is your code
                };
                mainHandler.post(myRunnable);

            }
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, data.toString(), "payloadData");

        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Payload Error" + e.toString() + data.toString(), "[Log.e]->Exception->");

            Util.setException(context, e.toString(), AppConstant.APP_NAME_TAG, "handleNotification");
        }
    }

    /*
     * Use this method to tag a users to a specific value.
     * addTag method with FCM, Xiaomi and HMS topic push support
     * */
    public static void addTag(final List<String> topicName) {
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.ADD_TAG) && appContext == null) {
            osTaskManager.addTaskToQueue(() -> {
                Log.d(AppConstant.APP_NAME_TAG, "addTag(): operation from pending task queue.");
                addTag(topicName);
            });
            return;
        }
        if (topicName != null && !topicName.isEmpty()) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getStringData(AppConstant.SENDERID) != null || !preferenceUtil.getStringData(AppConstant._HMS_APPID).isEmpty()) {

                if (preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                    Log.e("izooto", "enter in Firebase Option: " + preferenceUtil.getStringData(AppConstant.SENDERID));
                    FirebaseOptions firebaseOptions =
                            new FirebaseOptions.Builder()
                                    .setGcmSenderId(preferenceUtil.getStringData(AppConstant.SENDERID)) //senderID
                                    .setApplicationId(get_App_ID()) //application ID
                                    .setApiKey(getAPI_KEY()) //Application Key
                                    .setProjectId(get_Project_ID()) //Project ID
                                    .build();
                    try {
                        FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCMDEFAULT);
                        if (firebaseApp == null) {
                            FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                        }
                    } catch (IllegalStateException ex) {
                        FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                    }
                }
                List<String> topicList = new ArrayList<>();
                for (final String filterTopicName : topicName) {
                    if (filterTopicName != null && !filterTopicName.isEmpty()) {
                        if (Util.isMatchedString(filterTopicName)) {
                            try {
                                if (preferenceUtil.getStringData(AppConstant.HMS_TOKEN) != null && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                                    HmsMessaging.getInstance(appContext).subscribe(filterTopicName);
                                    Log.i(TAG, "HMS subscribe topic successfully");
                                } else {
                                    if (preferenceUtil.getStringData(AppConstant.XiaomiToken) != null && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                                        MiPushClient.subscribe(appContext, filterTopicName, null);
                                        Log.i(TAG, "MI subscribe topic successfully");
                                    }
                                    FirebaseMessaging.getInstance().subscribeToTopic(filterTopicName);
                                    Log.i(TAG, "FCM subscribe topic successfully");
                                }

                                preferenceUtil.setStringData(AppConstant.GET_TOPIC_NAME, filterTopicName);
                                topicList.add(filterTopicName);

                            } catch (Exception e) {
                                Util.setException(iZooto.appContext, e.toString(), "iZooto", "addTag");

                            }
                        }
                    }
                }
                topicApi(AppConstant.ADD_TOPIC, topicList);
            }
        } else {
            Util.setException(iZooto.appContext, "Topic list should not be blank", AppConstant.APP_NAME_TAG, "AddTag");
        }
    }

    /*
     * User this method to remove already tagged users from a value.
     * removeTag method with FCM, Xiaomi and HMS topic push support
     * */
    public static void removeTag(final List<String> topicName) {

        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.REMOVE_TAG) && appContext == null) {
            osTaskManager.addTaskToQueue(() -> {
                Log.d(AppConstant.APP_NAME_TAG, "removeTag(): operation from pending task queue.");
                removeTag(topicName);
            });
            return;
        }
        if (topicName != null && !topicName.isEmpty()) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getStringData(AppConstant.SENDERID) != null || !preferenceUtil.getStringData(AppConstant._HMS_APPID).isEmpty()) {
                if (preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                    FirebaseOptions firebaseOptions =
                            new FirebaseOptions.Builder()
                                    .setGcmSenderId(preferenceUtil.getStringData(AppConstant.SENDERID)) //senderID
                                    .setApplicationId(get_App_ID()) //application ID
                                    .setApiKey(getAPI_KEY()) //Application Key
                                    .setProjectId(get_Project_ID()) //Project ID
                                    .build();
                    try {
                        FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCMDEFAULT);
                        if (firebaseApp == null) {
                            FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                        }
                    } catch (IllegalStateException ex) {
                        FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                    }
                }
                List<String> topicList = new ArrayList<>();
                for (final String filterTopicName : topicName) {
                    if (filterTopicName != null && !filterTopicName.isEmpty()) {
                        if (Util.isMatchedString(filterTopicName)) {
                            try {
                                if (preferenceUtil.getStringData(AppConstant.HMS_TOKEN) != null && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                                    HmsMessaging.getInstance(appContext).unsubscribe(filterTopicName);
                                } else {
                                    if (preferenceUtil.getStringData(AppConstant.XiaomiToken) != null && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                                        MiPushClient.unsubscribe(appContext, filterTopicName, null);
                                    }
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(filterTopicName);
                                }
                                preferenceUtil.setStringData(AppConstant.REMOVE_TOPIC_NAME, filterTopicName);
                                topicList.add(filterTopicName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                topicApi(AppConstant.REMOVE_TOPIC, topicList);
            }
        } else {
            Util.setException(iZooto.appContext, "Topic list should not be blank", AppConstant.APP_NAME_TAG, "RemoveTag");

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void topicApi(String action, List<String> topic) {
        if (appContext == null)
            return;

        try {
            if (topic.size() > 0) {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        HashMap<String, List<String>> data = new HashMap<>();
                        data.put(AppConstant.TOPIC, topic);
                        JSONObject jsonObject = new JSONObject(data);
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ACT, action);
                        mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                        mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                        mapData.put(AppConstant.TOKEN, "" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.XIAOMITOKEN, "" + preferenceUtil.getStringData(AppConstant.XiaomiToken));
                        mapData.put(AppConstant.KEY_HMS, "" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                        RestClient.postRequest(RestClient.PROPERTIES_URL, mapData, null, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                if (action.equalsIgnoreCase(AppConstant.ADD_TOPIC))
                                    preferenceUtil.setStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE, null);
                                else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC))
                                    preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, null);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                                JSONArray jsonArray = new JSONArray(topic);
                                if (action.equalsIgnoreCase(AppConstant.ADD_TOPIC)) {
                                    preferenceUtil.setStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE, jsonArray.toString());
                                } else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC)) {
                                    preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, jsonArray.toString());
                                }
                            }
                        });
                    } else {

                        JSONArray jsonArray = new JSONArray(topic);
                        if (action.equalsIgnoreCase(AppConstant.ADD_TOPIC)) {
                            preferenceUtil.setStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE, jsonArray.toString());
                        } else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC)) {
                            preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, jsonArray.toString());
                        }
                    }
                } else {
                    JSONArray jsonArray = new JSONArray(topic);
                    if (action.equalsIgnoreCase(AppConstant.ADD_TOPIC)) {
                        preferenceUtil.setStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE, jsonArray.toString());
                    } else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC)) {
                        preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, jsonArray.toString());
                    }
                }
            }
        } catch (Exception e) {
            Util.setException(appContext, e.toString(), "topicApi", AppConstant.APP_NAME_TAG);
        }
    }

    private static String getAPI_KEY() {
        try {
            String apiKey = FirebaseOptions.fromResource(iZooto.appContext).getApiKey();
            if (apiKey != null)
                return apiKey;
        } catch (Exception e) {
            return "";//new String(Base64.decode(FCM_DEFAULT_API_KEY_BASE64, Base64.DEFAULT));

        }
        return "";


    }

    private static String get_App_ID() {
        try {
            String application_id = FirebaseOptions.fromResource(iZooto.appContext).getApplicationId();
            if (application_id != null)
                return application_id;
        } catch (Exception ex) {
            return "";//FCM_DEFAULT_APP_ID;

        }
        return "";

    }

    private static String get_Project_ID() {
        try {
            String project_id = FirebaseOptions.fromResource(iZooto.appContext).getProjectId();
            if (project_id != null)
                return project_id;
        } catch (Exception exception) {
            return "";

        }
        return "";

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static void lastVisitApi(Context context) {
        if (context != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String time = preferenceUtil.getStringData(AppConstant.CURRENT_DATE);
            if (!time.equalsIgnoreCase(getTime())) {
                preferenceUtil.setStringData(AppConstant.CURRENT_DATE, getTime());
                try {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(AppConstant.LAST_WEBSITE_VISIT, true);
                    data.put(AppConstant.LANG_, Util.getDeviceLanguageTag());
                    JSONObject jsonObject = new JSONObject(data);

                    Map<String, String> mapData = new HashMap<>();
                    mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                    mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                    mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                    mapData.put(AppConstant.ACT, "add");
                    mapData.put(AppConstant.ISID_, "1");
                    mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                    RestClient.postRequest(RestClient.LASTVISITURL, mapData, null, new RestClient.ResponseHandler() {
                        @Override
                        void onSuccess(final String response) {
                            super.onSuccess(response);
                        }

                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }
                    });
                } catch (Exception ex) {
                    Util.setException(context, ex.toString(), AppConstant.APP_NAME_TAG, "lastVisitAPI");


                }
            }
        }
    }

    private static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }

    public static boolean isJSONValid(String targetJson) {
        try {
            new JSONObject(targetJson);
        } catch (JSONException ex) {
            try {
                new JSONArray(targetJson);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void sendOfflineDataToServer(Context context) {
        if (context == null)
            return;


        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty()) {
                JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                for (int i = 0; i < jsonArrayOffline.length(); i++) {
                    JSONObject c = jsonArrayOffline.getJSONObject(i);
                    NotificationActionReceiver.notificationClickAPI(context, c.optString("apiURL"), c.optString("cid"), c.optString("rid"), c.optInt("click"), i, "fcm");
                }
            }

            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE).isEmpty()) {
                JSONArray lciJsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE));
                for (int i = 0; i < lciJsonArrayOffline.length(); i++) {
                    JSONObject c = lciJsonArrayOffline.getJSONObject(i);
                    NotificationActionReceiver.lastClickAPI(context, c.optString("apiURL"), c.optString("rid"), i);
                }
            }

            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_VIEW_OFFLINE).isEmpty()) {
                JSONArray viewJsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_VIEW_OFFLINE));
                for (int i = 0; i < viewJsonArrayOffline.length(); i++) {
                    JSONObject c = viewJsonArrayOffline.getJSONObject(i);
                    NotificationEventManager.impressionNotification(c.optString("apiURL"), c.optString("cid"), c.optString("rid"), i, "fcm");
                }
            }

            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_VIEW_OFFLINE).isEmpty()) {
                JSONArray viewJsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_VIEW_OFFLINE));
                for (int i = 0; i < viewJsonArrayOffline.length(); i++) {
                    JSONObject c = viewJsonArrayOffline.getJSONObject(i);
                    NotificationEventManager.lastViewNotification(c.optString("apiURL"), c.optString("rid"), c.optString("cid"), i);
                }
            }
            if (!preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS).isEmpty()) {
                JSONArray mediationRecords = new JSONArray(preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS));
                for (int i = 0; i < mediationRecords.length(); i++) {
                    JSONObject jsonObject = mediationRecords.getJSONObject(i);
                    if (jsonObject.getString(AppConstant.STORE_MED_API).equals(AppConstant.MED_IMPRESION)) {
                        String jsonData = jsonObject.getString(AppConstant.STORE_MED_DATA);
                        AdMediation.mediationImpression(jsonData, i);
                    }
                    if (jsonObject.getString(AppConstant.STORE_MED_API).equals(AppConstant.MED_CLICK)) {
                        String jsonData = jsonObject.getString(AppConstant.STORE_MED_DATA);
                        NotificationActionReceiver.callMediationClicks(context, jsonData, i);
                    }
                }

            }

        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, "SendOfflineDataToServerException", "[Log.V]->SendOfflineDataToServerException->");
            Util.setException(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "sendOfflineDataToServer");

        }


    }

    /*
    To create local directory inside your file manager.
    */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void createDirectory(Context context) {
        DebugFileManager.createPublicDirectory(context);
    }

    /*
    To delete local directory from your file manager.
    */
    public static void deleteDirectory(Context context) {
        DebugFileManager.deletePublicDirectory(context);
    }

    /*
    To share local directory from your file manager.
    */
    public static void shareFile(Context context, String name, String emailID) {
        DebugFileManager.shareDebuginfo(context, name, emailID);
    }

    /*
     * Initialise iZooto SDK with context
     * Sets the global shared ApplicationContext for iZooto
     * */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void initWithContext(@Nullable Context context) {
        if (context == null) {
            iZooto.Log(LOG_LEVEL.WARN, AppConstant.INIT_WITH_CONTEXT_NULL);
        } else {
            ActivityLifecycleListener.registerActivity((Application) context);
            appContext = context.getApplicationContext();
            if (iZootoAppId == null) {
                String oldAppId = getSavedAppId();
                if (oldAppId == null) {
                    iZooto.Log(LOG_LEVEL.WARN, AppConstant.INIT_WITH_CONTEXT_OLD_ID_NULL);
                } else {
                    iZooto.Log(LOG_LEVEL.VERBOSE, AppConstant.INIT_WITH_CONTEXT_OLD_ID_NOT_NULL + oldAppId);
                    setAppId(oldAppId);
                    // inAppMessaging(appContext);
                }
            } else {
                iZooto.Log(LOG_LEVEL.VERBOSE, "initWithContext called with: " + context);
                init(context);
            }

        }
    }

    static void addActivity(Activity context)
    {
        if(context==null)
            return;
        // setNewsHub(context,null);


    }
    /*
     * Sets the app id iZooto should use in the application
     * newAppId - String app id associated with the iZooto dashboard app
     * */
    public static void setAppId(@NonNull String newAppId) {
        if (newAppId != null && !newAppId.isEmpty()) {

            if (!newAppId.equals(iZootoAppId)) {
                // Pre-check on app id to make sure init of SDK is performed properly
                //     Usually when the app id is changed during runtime so that SDK is reinitialized properly
                canSetAppId = false;
                iZooto.Log(LOG_LEVEL.VERBOSE, "setlogger.verbose(AppId called with id: " + newAppId + " changing id from: " + iZootoAppId);
            }

            iZootoAppId = newAppId;

            if (appContext == null) {
                iZooto.Log(LOG_LEVEL.WARN, AppConstant.SET_APP_ID_WITH_NULL_CONTEXT);
            } else {
                init(appContext);
            }
        } else {
            iZooto.Log(LOG_LEVEL.WARN, "setAppId called with id: " + newAppId + ", ignoring!");
        }
    }

    static String getSavedAppId() {
        return getSavedAppId(appContext);
    }

    private static String getSavedAppId(Context mContext) {
        String appId = null;

        if (mContext != null) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(mContext);
            appId = preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID);
            return appId;
        }
        return appId;
    }

    /*
     * Token callback: To get device token from iZooto
     * */
    public static void setTokenReceivedListener(TokenReceivedListener listener) {
        mTokenReceivedListener = listener;
    }

    /*
     * Notification Receive & Deeplink callback: To get iZooto notification payload & to open specific page/activity of your Application
     * */
    public static void setNotificationReceiveListener(NotificationHelperListener notificationHelper) {
        mNotificationHelper = notificationHelper;
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.NOTIFICATION_HELPER_LISTENER) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    iZooto.Log(LOG_LEVEL.VERBOSE, "setNotificationReceiveListener(): operation from pending task queue.");
                    setNotificationReceiveListener(notificationHelper);
                }
            });
            return;
        }

        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
            runNotificationOpenedCallback();
        }
    }

    /*
     * Notification WebView callback: To open landing URL inside your WebView Activity
     * */
    public static void setLandingURLListener(NotificationWebViewListener mNotificationWebViewListener) {
        mWebViewListener = mNotificationWebViewListener;
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.NOTIFICATION_WEBVIEW_LISTENER) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    iZooto.Log(LOG_LEVEL.VERBOSE, "setLandingURLListener(): operation from pending task queue.");
                    setLandingURLListener(mNotificationWebViewListener);
                }
            });
            return;
        }

        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
            runNotificationWebViewCallback();
        }
    }

    /*
     * Notification Receive Hybrid callback: To receive payload in React Native & Flutter Plugin
     * */
    public static void setNotificationReceiveHybridListener(NotificationReceiveHybridListener notificationReceivedHybrid) {
        mNotificationReceivedHybridlistener = notificationReceivedHybrid;
        if (mNotificationReceivedHybridlistener != null) {
            runNotificationReceivedCallback();
        }
    }

    /*
     * iZooto setAppId() method with your token(token should be in jsonString form).
     * */
    public static void setAppId(@NonNull String newAppId, String tokenJson) {
        if (newAppId != null && !newAppId.isEmpty()) {

            if (!newAppId.equals(iZootoAppId)) {
                // Pre-check on app id to make sure init of SDK is performed properly
                //     Usually when the app id is changed during runtime so that SDK is reinitialized properly
                canSetAppId = false;
                iZooto.Log(LOG_LEVEL.VERBOSE, "setlogger.verbose(AppId called with id: " + newAppId + " changing id from: " + iZootoAppId);
            }
            iZootoAppId = newAppId;

            if (appContext == null) {
                iZooto.Log(LOG_LEVEL.WARN, AppConstant.SET_APP_ID_WITH_NULL_CONTEXT);
            } else {

                try {
                    if (tokenJson != null && !tokenJson.isEmpty()) {
                        if (isJSONValid(tokenJson)) {

                            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                            JSONObject data = new JSONObject(tokenJson);
                            String fcmToken = data.optString(AppConstant.FCM_TOKEN_FROM_JSON);
                            preferenceUtil.setBooleanData(AppConstant.CAN_GENERATE_FCM_TOKEN, true);
                            if (data.has(AppConstant.FCM_TOKEN_FROM_JSON)) {
                                if (!fcmToken.isEmpty()) {
                                    if (!fcmToken.equals(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN))) {
                                        preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                                        preferenceUtil.setStringData(AppConstant.FCM_DEVICE_TOKEN, fcmToken);
                                    }
                                } else {
                                    Util.setException(appContext, "Please input the fcm token...", "initialize", AppConstant.APP_NAME_TAG);
                                }
                            }
                        } else {
                            iZooto.Log(LOG_LEVEL.WARN, AppConstant.GIVEN_STRING_NOT_JSON_STRING);
                        }
                    } else {
                        iZooto.Log(LOG_LEVEL.WARN, "setAppId: tokenJson is null");
                    }
                } catch (Exception e) {
                    Util.setException(appContext, e.toString(), "initialize", AppConstant.APP_NAME_TAG);
                    e.printStackTrace();
                }

                init(appContext);
            }
        } else {
            iZooto.Log(LOG_LEVEL.WARN, "setAppId called with id: " + newAppId + ", ignoring!");
        }
    }

    static void Log(@NonNull LOG_LEVEL level, @NonNull String message) {
        Log(level, message, null);
    }

    static void Log(@NonNull final LOG_LEVEL level, @NonNull String message, @Nullable Throwable throwable) {

        if (level.compareTo(logCatLevel) < 1) {
            if (level == LOG_LEVEL.VERBOSE)
                Log.v(AppConstant.APP_NAME_TAG, message, throwable);
            else if (level == LOG_LEVEL.DEBUG)
                Log.d(AppConstant.APP_NAME_TAG, message, throwable);
            else if (level == LOG_LEVEL.INFO)
                Log.i(AppConstant.APP_NAME_TAG, message, throwable);
            else if (level == LOG_LEVEL.WARN)
                Log.w(AppConstant.APP_NAME_TAG, message, throwable);
            else if (level == LOG_LEVEL.ERROR || level == LOG_LEVEL.FATAL)
                Log.e(AppConstant.APP_NAME_TAG, message, throwable);
        }
    }

    public static void setLogLevel(LOG_LEVEL inLogCatLevel, LOG_LEVEL inVisualLogLevel) {
        logCatLevel = inLogCatLevel;
        visualLogLevel = inVisualLogLevel;
    }

    public static void unsubscribeWhenNotificationsAreDisabled(boolean set) {
        mUnsubscribeWhenNotificationsAreDisabled = set;
        areNotificationsEnabledForSubscribedState(appContext);
    }

    public static void izootoLog(iZooto.LOG_LEVEL level, String message) {
        Log(level, message);
    }

    public static void setLogLevel(int inLogCatLevel, int inVisualLogLevel) {
        setLogLevel(getLogLevel(inLogCatLevel), getLogLevel(inVisualLogLevel));
    }

    private static iZooto.LOG_LEVEL getLogLevel(int level) {
        switch (level) {
            case 0:
                return iZooto.LOG_LEVEL.NONE;
            case 1:
                return iZooto.LOG_LEVEL.FATAL;
            case 2:
                return iZooto.LOG_LEVEL.ERROR;
            case 3:
                return iZooto.LOG_LEVEL.WARN;
            case 4:
                return iZooto.LOG_LEVEL.INFO;
            case 5:
                return iZooto.LOG_LEVEL.DEBUG;
            case 6:
                return iZooto.LOG_LEVEL.VERBOSE;
        }

        if (level < 0)
            return iZooto.LOG_LEVEL.NONE;
        return iZooto.LOG_LEVEL.VERBOSE;
    }



    // newsHub
//    private static void setNewsHub(Activity context, RelativeLayout view,int isShow) {
//        if (context == null)
//            return;
//
//        if(isShow ==0)
//          //  setFloatingButton(context, view);
//        else
//           // setStickyButton(context,view);
//
//
//        // setFloatingButton(context, view);
//    }

    private static void setNewsHub(Activity context, RelativeLayout view, String jsonString) {
        if (context == null)
            return;
        try {

            if (jsonString != null && !jsonString.isEmpty()) {
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                JSONObject jsonObject = new JSONObject(jsonString);
                fetchNewsHubData(context, jsonObject.optString(AppConstant.JSON_NEWS_HUB));
                preferenceUtil.setBooleanData(AppConstant.SET_JSON_NEWS_HUB, true);

               // setFloatingButton(context, view);
               // setStickyButton(context,view);
            }
            else
                Log.w(AppConstant.APP_NAME_TAG, "Your json string is null");


        } catch (Exception e) {
            Log.w(AppConstant.APP_NAME_TAG, e.toString());
        }
    }

//    private static void setStickyButton(Activity context, RelativeLayout view) {
//        if(context ==null)
//            return;
//        if(view!=null) {
//            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
//
//            View itemView = LayoutInflater.from(context).inflate(R.layout.nh_sticy_layout, null, false);
//            changeDynamicSticyBar(context, itemView);
//            FrameLayout frameLayout = itemView.findViewById(R.id.news_hub);
//            frameLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN)) {
//                        context.startActivity(new Intent(context, NewsHubActivity.class));
//                    } else {
//                        // NewsHubAlert newsHubAlert = new NewsHubAlert();
//                        // newsHubAlert.showAlertData(context);
//                        context.startActivity(new Intent(context, NewsHubActivity.class));
//
//                    }
//                }
//            });
//            view.addView(itemView);
//        }
//        else
//        {
//            context.startActivity(new Intent(context, NewsHubActivity.class));
//        }
//
//    }

//    @SuppressLint("NewApi")
//    private static void changeDynamicSticyBar(Context context, View itemView) {
//        if (context == null)
//            return;
//        try {
//            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
//
//            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
//                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
//                FrameLayout frameLayout =itemView.findViewById(R.id.news_hub);
//                frameLayout.setBackgroundTintList(ColorStateList.valueOf(color));
//                ImageView icon_newHub=itemView.findViewById(R.id.icon_newHub);
//                switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
//
//                    case 3:
//                        icon_newHub.setImageResource(R.drawable.ic_iz_lighting);
//                        break;
//                    case 4:
//                        icon_newHub.setImageResource(R.drawable.ic_iz_shout_out);
//                        break;
//                    case 5:
//                        icon_newHub.setImageResource(R.drawable.ic_izmegaphone);
//                        break;
//                    case 2:
//                    default:
//                        icon_newHub.setImageResource(R.drawable.ic_iz_lighting);
//
//                        // icon_newHub.setImageResource(R.drawable.ic_iz_bell);
//                        break;
//                }
//            }
//        }
//        catch (Exception ex)
//        {
//            Log.e("Error",ex.toString());
//        }
//
//    }

    private static void fetchNewsHubData(Context context, String newsHubJsonData) {
        if (context == null)
            return;

        try {
            if (!newsHubJsonData.isEmpty()) {
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                JSONObject jsonObject = new JSONObject(newsHubJsonData);
                preferenceUtil.setIntData(AppConstant.JSON_NEWS_HUB_STATUS, jsonObject.optInt("status"));
                preferenceUtil.setBooleanData(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN, jsonObject.optBoolean("isFullScreen"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_COLOR,jsonObject.optString("mainColor"));
                preferenceUtil.setIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE, jsonObject.optInt("iconType"));
                preferenceUtil.setBooleanData(AppConstant.JSON_NEWS_HUB_IS_DESCRIPTION, jsonObject.optBoolean("isDescription"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_TITLE,"Notification Alerts");//jsonObject.optString("title"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_WIDGET, jsonObject.optString("widget"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_FALLBACK_IMAGE_URL, jsonObject.optString("fallbackImageURL"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR,jsonObject.optString("titleColor"));
            }
        } catch (Exception e) {
            Log.w(AppConstant.APP_NAME_TAG, e.toString());
        }
    }

//    private static void changeFloatingActionDynamically(Context context, FloatingActionButton floatingActionButton) {
//        if (context == null)
//            return;
//
//        try {
//            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
//
//            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
//                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
//                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(color));
//            }
//
//            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
//
//                case 3:
//                    floatingActionButton.setImageResource(R.drawable.ic_iz_lighting);
//                    break;
//                case 4:
//                    floatingActionButton.setImageResource(R.drawable.ic_iz_shout_out);
//                    break;
//                case 5:
//                    floatingActionButton.setImageResource(R.drawable.ic_izmegaphone);
//                    break;
//                case 2:
//                default:
//                    floatingActionButton.setImageResource(R.drawable.ic_iz_bell);
//                    break;
//            }
//        } catch (Exception e) {
//            Log.w(AppConstant.APP_NAME_TAG, e.toString());
//        }
//    }

//    private static void setFloatingButton(Activity context, RelativeLayout view) {
//        if (context == null)
//            return;
//        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
//
//        View  itemView = LayoutInflater.from(context).inflate(R.layout.nh_floating_layout,null, false);
//        FloatingActionButton floatingActionButton = itemView.findViewById(R.id.fab);
//        // FrameLayout frameLayout = itemView.findViewById(R.id.news_hub);
//        changeFloatingActionDynamically(context, floatingActionButton);
//        // frameLayout.setBackgroundColor(Color.RED);
//        // changeDynamicSticyBar(context,itemView);
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if (preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN)) {
//                    context.startActivity(new Intent(context, NewsHubActivity.class));
//                }
//                else {
//                    NewsHubAlert newsHubAlert = new NewsHubAlert();
//                    newsHubAlert.showAlertData(context);
//                }
//            }
//        });
//        view.addView(itemView);
//
////        try {
////            if (view != null) {
////                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
////                FloatingActionButton floatingActionButton = new FloatingActionButton(context);
////                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
////                layoutParams.setMargins(32, 32, 32, 32);
////                view.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
////
////                floatingActionButton.setLayoutParams(layoutParams);
////                changeFloatingActionDynamically(context, floatingActionButton);
////                floatingActionButton.setOnClickListener(new View.OnClickListener() {
////                    public void onClick(View view) {
////                        if (preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN)) {
////                            context.startActivity(new Intent(context, NewsHubActivity.class));
////                        }
////                        else {
////                            NewsHubAlert newsHubAlert = new NewsHubAlert();
////                            newsHubAlert.showAlertData(context);
////                        }
////                    }
////                });
////
////                view.addView(floatingActionButton);
////
////            } else {
////                context.startActivity(new Intent(context, NewsHubActivity.class));
////            }
////        } catch (Exception e) {
////            Log.w("Warning", e.toString());
////        }
//    }

    //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    public static void inAppMessaging(Context context) {
//        new InAppMessagingCustomDialog().getDialog(context);
//        boolean preferences = PreferenceUtil.getInstance(context).getBoolean(AppConstant.FIRST_TIME);
//        if(!preferences) {
//
//            PreferenceUtil.getInstance(appContext).setBooleanData(AppConstant.FIRST_TIME, true);
//        } else {
//            Log.e(TAG, "Not show again dialog after show one time");
//        }
//    }
}
