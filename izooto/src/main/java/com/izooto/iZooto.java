package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.izooto.shortcutbadger.ShortcutBadger;
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

import static com.izooto.AppConstant.APPPID;
import static com.izooto.AppConstant.FCM_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.HUAWEI_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.PID;
import static com.izooto.AppConstant.TAG;
import static com.izooto.AppConstant.XIAOMI_TOKEN_FROM_JSON;
@SuppressWarnings("unchecked")
public class iZooto {
    static Context appContext;
    private static String senderId;
    public static String mIzooToAppId;
    public static Builder mBuilder;
    public static int icon;
    private static Payload payload;
    public static boolean mUnsubscribeWhenNotificationsAreDisabled;
    protected static Listener mListener;
    protected static Handler mHandler;
    private static FirebaseAnalyticsTrack firebaseAnalyticsTrack;
    @SuppressLint("StaticFieldLeak")
    static Activity curActivity;
    private static String advertisementID;
    public static boolean isHybrid = false;//check for SDK(Flutter,React native)
    public static String SDKDEF ="native";
    public static int bannerImage;
    private static boolean initCompleted;
    static boolean isInitCompleted() {
        return initCompleted;
    }
    private static OSTaskManager osTaskManager = new OSTaskManager();
    private static LOG_LEVEL visualLogLevel = LOG_LEVEL.NONE;
    private static LOG_LEVEL logCatLevel = LOG_LEVEL.WARN;

    private static int pageNumber;   // index handling for notification center data
    private static String notificationData;

    public static void setSenderId(String senderId) {
        iZooto.senderId = senderId;
    }
    private static void setActivity(Activity activity){
        curActivity = activity;
    }
    public static void setiZootoID(String iZooToAppId) {
        mIzooToAppId = iZooToAppId;
    }
    public static iZooto.Builder initialize(Context context) {
        return new iZooto.Builder(context);
    }

    private static void init(Builder builder) {
        final Context context = builder.mContext;
        appContext = context.getApplicationContext();
        mBuilder = builder;
        builder.mContext = null;
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            if (bundle != null) {
                if (bundle.containsKey(AppConstant.IZOOTO_APP_ID)) {
                    mIzooToAppId = bundle.getString(AppConstant.IZOOTO_APP_ID);
                    preferenceUtil.setStringData(AppConstant.ENCRYPTED_PID,mIzooToAppId);
                }
                if (mIzooToAppId =="") {
                    Lg.e(AppConstant.APP_NAME_TAG, AppConstant.MISSINGID);
                }
                else {
                    Lg.i(AppConstant.APP_NAME_TAG, mIzooToAppId + "");
                    RestClient.get(RestClient.P_GOOGLE_JSON_URL + mIzooToAppId +".dat", new RestClient.ResponseHandler() {
                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            if (!response.isEmpty() && response.length() > 20 && response != null) {
                                try {

                                    final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                                    JSONObject jsonObject = new JSONObject(Objects.requireNonNull(Util.decrypt(AppConstant.SECRETKEY, response)));
                                    senderId = jsonObject.getString(AppConstant.SENDERID);
                                    String appId = jsonObject.optString(AppConstant.APPID);
                                    String apiKey = jsonObject.optString(AppConstant.APIKEY);
                                    String mKey =jsonObject.optString(AppConstant.MIAPIKEY);
                                    String mId =jsonObject.optString(AppConstant.MIAPPID);
                                    String hms_appId =jsonObject.optString(AppConstant.HMS_APP_ID);
                                    mIzooToAppId = jsonObject.optString(APPPID);
                                    preferenceUtil.setiZootoID(APPPID, mIzooToAppId);
                                    trackAdvertisingId();

                                    if(!mKey.isEmpty() && !mId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_XIAOMI_TOKEN)){
                                        XiaomiSDKHandler xiaomiSDKHandler = new XiaomiSDKHandler(iZooto.appContext, mId, mKey);
                                        xiaomiSDKHandler.onMIToken();
                                    }
                                    if (!hms_appId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Huawei")  && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_HUAWEI_TOKEN)) {
                                        initHmsService(appContext);
                                    }
                                    if (senderId != null && !senderId.isEmpty()) {
                                        init(context, apiKey, appId);
                                    } else {
                                        Lg.e(AppConstant.APP_NAME_TAG, appContext.getString(R.string.something_wrong_fcm_sender_id));
                                    }
                                    if ( mIzooToAppId!= null && preferenceUtil.getBoolean(AppConstant.IS_CONSENT_STORED)) {
                                        preferenceUtil.setIntData(AppConstant.CAN_STORED_QUEUE, 1);
                                    }
                                    if (iZooto.isHybrid)
                                        preferenceUtil.setBooleanData(AppConstant.IS_HYBRID_SDK, iZooto.isHybrid);
                                } catch (JSONException e) {
                                    if (context != null) {
                                        DebugFileManager.createExternalStoragePublic(context,e.toString(),"[Log.e]-->init");
                                        Util.setException(context, e.toString(), "init", AppConstant.APP_NAME_TAG);
                                    }
                                }
                            }
                            else
                            {
                                DebugFileManager.createExternalStoragePublic(context,AppConstant.ACCOUNT_ID_EXCEPTION,"[Log.e]-->");
                            }
                        }


                    });
                }
            } else {
                DebugFileManager.createExternalStoragePublic(context,AppConstant.MESSAGE,"[Log.e]-->");
            }


        } catch (Throwable t) {
            DebugFileManager.createExternalStoragePublic(context,t.toString(),"[Log.e]-->initBuilder");
            Util.setException(appContext, t.toString(), AppConstant.APP_NAME_TAG, "initBuilder");
        }

    }
    /* HMS Integration */
    private static void initHmsService(final Context context){
        if (context == null)
            return;

        HMSTokenGenerator hmsTokenGenerator = new HMSTokenGenerator();
        hmsTokenGenerator.getHMSToken(context, new HMSTokenListener.HMSTokenGeneratorHandler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void complete(String id) {
                Log.i(AppConstant.APP_NAME_TAG, "HMS Token - " + id);
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (id != null && !id.isEmpty()) {
                    if (!preferenceUtil.getBoolean(AppConstant.IS_UPDATED_HMS_TOKEN)) {
                        preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                        preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                    }
                    iZooto.registerToken();
                }
            }

            @Override
            public void failure(String errorMessage) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext,errorMessage,"[Log.v]->");
                Lg.v(AppConstant.APP_NAME_TAG, errorMessage);
            }
        });
    }

    /* start */
    private static void init(final Context context, String apiKey, String appId) {
        if(context==null)
            return;
        FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
        fcmTokenGenerator.getToken(context, senderId, apiKey, appId, new TokenGenerator.TokenGenerationHandler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void complete(String id) {
                Util util = new Util();
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (util.isInitializationValid()) {
                    Lg.i(AppConstant.APP_NAME_TAG, AppConstant.DEVICETOKEN  + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    registerToken();
                    ActivityLifecycleListener.registerActivity((Application)appContext);
                    setCurActivity(context);
                    areNotificationsEnabledForSubscribedState(appContext);
                    if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack())
                        firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                    initCompleted = true;
                    osTaskManager.startPendingTasks();
                }
            }

            @Override
            public void failure(String errorMsg) {
                Lg.e(AppConstant.APP_NAME_TAG, errorMsg);
            }
        });

    }
    private static void trackAdvertisingId(){
        if(appContext!=null) {
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
    public static void  setNotificationSound(String soundName)
    {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        preferenceUtil.setStringData(AppConstant.NOTIFICATION_SOUND_NAME, soundName);

    }

    protected void start(final Context context, final Listener listener) {
        if (listener == null) {
            Log.v(AppConstant.APP_NAME_TAG, "getAdvertisingId - Error: null listener, dropping call");
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
                        invokeFinish(preferenceUtil.getStringData(AppConstant.ADVERTISING_ID),preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));
                    }
                }).start();
            }
        }
    }


    public interface Listener {

        void idsAvailable(String adverID,String registrationID);

        void onAdvertisingIdClientFail(Exception exception);
    }

    protected static void invokeFinish(final String advertisementID, final String registrationID) {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mListener != null) {
                    mListener.idsAvailable(advertisementID,registrationID);
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

    static void registerToken() {
        if(appContext!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getiZootoID(APPPID) != null && !preferenceUtil.getiZootoID(APPPID).isEmpty()) {
                if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
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
                        mapData.put(PID, mIzooToAppId);
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                        mapData.put(AppConstant.APPVERSION,"" + Util.getAppVersion(iZooto.appContext));
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
                        mapData.put(AppConstant.H_PLUGIN_VERSION,preferenceUtil.getStringData(AppConstant.HYBRID_PLUGIN_VERSION));

                        RestClient.postRequest(RestClient.BASE_URL, mapData, null, new RestClient.ResponseHandler() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                lastVisitApi(appContext);
                                if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                JSONObject jsonObject = new JSONObject();
                                                jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                                jsonObject.put(XIAOMI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.XiaomiToken));
                                                jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                                mBuilder.mTokenReceivedListener.onTokenReceived(jsonObject.toString());
                                            } catch (Exception ex) {
                                                Util.setException(appContext,ex.toString(),AppConstant.APP_NAME_TAG,"RegisterToken");
                                                DebugFileManager.createExternalStoragePublic(appContext,ex.toString(),"[Log.e]->RegisterToken->");
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
                            }
                        });

                    } catch (Exception exception) {
                        Util.setException(appContext, exception.toString(), AppConstant.APP_NAME_TAG, "registerToken");
                    }
                } else {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            lastVisitApi(appContext);
                        }
                        areNotificationsEnabledForSubscribedState(appContext);
                        if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                jsonObject.put(XIAOMI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.XiaomiToken));
                                jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                mBuilder.mTokenReceivedListener.onTokenReceived(jsonObject.toString());

                            } catch (Exception ex) {
                                DebugFileManager.createExternalStoragePublic(appContext,ex.toString(),"[Log.e]->RegisterTokenFailure->");
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
                                mapData.put(PID, mIzooToAppId);
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
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Missing PID -> " , "[Log.e]->");

            }
        }
    }
    static void onActivityResumed(Activity activity){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        setActivity(activity);
        if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.influenceOpenTrack();
                preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED,true);
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


    public static void processNotificationReceived(Context context,Payload payload) {
        if(payload!=null) {
            NotificationEventManager.manageNotification(payload);
        }
        if(context!=null) {
            sendOfflineDataToServer(context);
        }

    }

    public static void notificationView(Payload payload)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if(payload!=null)
        {
            if(mBuilder!=null && mBuilder.mNotificationHelper!=null)
            {
                mBuilder.mNotificationHelper.onNotificationReceived(payload);
            }
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK))
            {
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
    public static void notificationActionHandler(String data)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);

        if (!data.isEmpty()) {
            if (mBuilder != null && mBuilder.mNotificationHelper != null) {
                mBuilder.mNotificationHelper.onNotificationOpened(data);
            }
        }
        if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
            firebaseAnalyticsTrack.openedEventTrack();
        }
        else {
            if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                firebaseAnalyticsTrack.openedEventTrack();
            }
        }
        try {
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)-1);
            ShortcutBadger.applyCountOrThrow(appContext, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void notificationInAppAction(Context context, String url) {
        if (context!=null && !url.isEmpty()) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                Util.sleepTime(2000);
                if (!preferenceUtil.getBoolean(AppConstant.DEFAULT_WEB_VIEW)){
                    if (mBuilder != null && mBuilder.mWebViewListener != null){
                        mBuilder.mWebViewListener.onWebView(url);
                    }else {
                        Log.i("notification...","builder null");
                    }
                } else {
                    iZootoWebViewActivity.startActivity(context, url);
                }
            } else if (mBuilder != null && mBuilder.mWebViewListener != null) {
                mBuilder.mWebViewListener.onWebView(url);
            } else {
                iZootoWebViewActivity.startActivity(context, url);
            }
        }

    }

    /*
      Handle the Hybrid Web_View Listener
     */
    public static void notificationWebView(NotificationWebViewListener notificationWebViewListener){
        mBuilder.mWebViewListener = notificationWebViewListener;
        if(mBuilder.mWebViewListener!=null)
        {
            runNotificationWebViewCallback();
        }
    }

//    private static void runNotificationWebViewCallback() {
//        runOnMainUIThread(new Runnable() {
//            public void run() {
//                try {
//                    if (!NotificationActionReceiver.WebViewClick.isEmpty()) {
//                        iZooto.mBuilder.mWebViewListener.onWebView(NotificationActionReceiver.WebViewClick);//
//                        NotificationActionReceiver.WebViewClick = "";
//                    }
////                    if (!TargetActivity.mWebViewClick.isEmpty()) {
////                        iZooto.mBuilder.mWebViewListener.onWebView(TargetActivity.mWebViewClick);//
////                        TargetActivity.mWebViewClick = "";
////
////                    }
//
//
//                }catch (Exception ex){
//                    Log.e("Exception ex",ex.toString());
//                }
//            }
//
//        });
//    }

    private static void runNotificationWebViewCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                try {
                    if (!NotificationActionReceiver.WebViewClick.isEmpty()) {
                        iZooto.mBuilder.mWebViewListener.onWebView(NotificationActionReceiver.WebViewClick);
                        NotificationActionReceiver.WebViewClick = "";
                    }

                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                    if (!TargetActivity.mWebViewClick.isEmpty() && preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                        iZooto.mBuilder.mWebViewListener.onWebView(TargetActivity.mWebViewClick);
                        TargetActivity.mWebViewClick = "";
                    }
                } catch (Exception var2) {
                    Log.v("Exception ex", var2.toString());
                }

            }
        });
    }


    public static void notificationClick(NotificationHelperListener notificationOpenedListener)
    {
        mBuilder.mNotificationHelper = notificationOpenedListener;
        if(mBuilder.mNotificationHelper!=null) {
            runNotificationOpenedCallback();
        }
    }

//    private static void runNotificationOpenedCallback() {
//        runOnMainUIThread(new Runnable() {
//            public void run() {
//                if (!NotificationActionReceiver.notificationClick.isEmpty()) {
//                    iZooto.mBuilder.mNotificationHelper.onNotificationOpened(NotificationActionReceiver.notificationClick);
//                    NotificationActionReceiver.notificationClick = "";
//                }
////                else
////                {
////                    if(!TargetActivity.mNotificationClick.isEmpty())
////                    {
////                        iZooto.mBuilder.mNotificationHelper.onNotificationOpened(TargetActivity.mNotificationClick);
////                        TargetActivity.mNotificationClick = "";
////                    }
////                }
//            }
//        });
//    }
private static void runNotificationOpenedCallback() {
    runOnMainUIThread(new Runnable() {
        public void run() {
            try {
                if (!NotificationActionReceiver.notificationClick.isEmpty()) {
                    iZooto.mBuilder.mNotificationHelper.onNotificationOpened(NotificationActionReceiver.notificationClick);
                    NotificationActionReceiver.notificationClick = "";
                }

//                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
//                if (!TargetActivity.mNotificationClick.isEmpty() && preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
//                    iZooto.mBuilder.mNotificationHelper.onNotificationOpened(TargetActivity.mNotificationClick);
//                    TargetActivity.mNotificationClick = "";
//                }
            }catch (Exception ex)
            {

            }

        }
    });
}

    // handle the execution
    static void runOnMainUIThread(Runnable runnable) {
        try {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(runnable);
            }
        }catch (Exception ex){
            Log.e("Exception x",ex.toString());
        }

    }
    public static void notificationViewHybrid(String payloadList, Payload payload)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if(payload!=null)
        {
            if(mBuilder!=null && mBuilder.mNotificationReceivedHybridlistener !=null)
            {
                mBuilder.mNotificationReceivedHybridlistener.onNotificationReceivedHybrid(payloadList);
            }
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            }else {
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
    public static void notificationReceivedCallback(NotificationReceiveHybridListener notificationReceivedHybridListener){
        mBuilder.mNotificationReceivedHybridlistener = notificationReceivedHybridListener;
        if(mBuilder.mNotificationReceivedHybridlistener != null) {
            runNotificationReceivedCallback();
        }
    }
    private static void runNotificationReceivedCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (NotificationEventManager.iZootoReceivedPayload != null) {
                    iZooto.mBuilder.mNotificationReceivedHybridlistener.onNotificationReceivedHybrid(NotificationEventManager.iZootoReceivedPayload);
//                    NotificationEventManager.iZootoReceivedPayload;
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

    public static class Builder {
        Context mContext;
        private TokenReceivedListener mTokenReceivedListener;
        public NotificationHelperListener mNotificationHelper;
        public NotificationWebViewListener mWebViewListener;
       // OSInAppDisplayOption mDisplayOption;
        public NotificationReceiveHybridListener mNotificationReceivedHybridlistener;
        private Builder(Context context) {
            mContext = context;
        }

        public Builder setTokenReceivedListener(TokenReceivedListener listener) {
            mTokenReceivedListener = listener;
            return this;
        }
        public Builder setNotificationReceiveListener(NotificationHelperListener notificationHelper) {
            mNotificationHelper = notificationHelper;
            return this;
        }

//        public Builder inAppNotificationBehaviour(OSInAppDisplayOption displayOption) {
//            mDisplayOption = displayOption;
//            inAppOption = displayOption.toString();
//            return this;
//        }
        public Builder setLandingURLListener(NotificationWebViewListener mNotificationWebViewListener){
            mWebViewListener = mNotificationWebViewListener;
            return this;

        }
        public Builder setNotificationReceiveHybridListener(NotificationReceiveHybridListener notificationReceivedHybrid) {
            mNotificationReceivedHybridlistener = notificationReceivedHybrid;
            return this;
        }


        public Builder unsubscribeWhenNotificationsAreDisabled(boolean set){
            mUnsubscribeWhenNotificationsAreDisabled = set;
            /*if (set){
                areNotificationsEnabledForSubscribedState(mContext);
            }*/
            return this;
        }


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void build() {
            iZooto.init(this);
        }

    }

    private static void areNotificationsEnabledForSubscribedState(Context context){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (context!=null) {
            int value = 0;
            if (mUnsubscribeWhenNotificationsAreDisabled) {
                boolean isChecked = Util.isNotificationEnabled(context);
                if (!isChecked) {
                    value = 2;
                }
            }
            if (value==0 && preferenceUtil.getIntData(AppConstant.GET_NOTIFICATION_ENABLED)==0){
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_ENABLED,1);
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_DISABLED,0);
                getNotificationAPI(context,value);

            }else if (value==2 && preferenceUtil.getIntData(AppConstant.GET_NOTIFICATION_DISABLED)==0) {
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_DISABLED, 1);
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_ENABLED, 0);
                getNotificationAPI(context, value);

            }
        }

    }
    public static void setDefaultTemplate(int templateID){
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.SET_CUSTOM_TEMPLATE) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    Log.d(AppConstant.APP_NAME_TAG, "setCustomTemplate(): operation from pending task queue.");
                    setDefaultTemplate(templateID);
                }
            });
            return;
        }
        if(PushTemplate.DEFAULT == templateID || PushTemplate.TEXT_OVERLAY == templateID) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_PREVIEW, templateID);
        }
        else
        {
            Util.setException(appContext,"Template id is not matched"+templateID,AppConstant.APP_NAME_TAG,"setDefaultTemplate");
        }

    }

    private static void getNotificationAPI(Context context, int value){

        if(context!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            try {
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() &&   preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(context));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.APPVERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.ACTION, "" + value);

                        RestClient.postRequest(RestClient.SUBSCRIPTION_API, mapData,null, new RestClient.ResponseHandler() {
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
    // send events  with event name and event data
    public static void addEvent(String eventName, HashMap<String,Object> data) {
        if (data != null && eventName != null&&eventName.length()>0&&data.size()>0) {
            eventName = eventName.substring(0, Math.min(eventName.length(), 32)).replace(" ","_");
            HashMap<String, Object>  newListEvent= new HashMap<String, Object>();
            for (Map.Entry<String,Object> refineEntry : data.entrySet()) {
                if (refineEntry.getKey()!=null&&!refineEntry.getKey().isEmpty()){
                    String newKey = refineEntry.getKey().toLowerCase();
                    newListEvent.put(newKey,refineEntry.getValue());
                }
            }
            if (newListEvent.size()>0)
                addEventAPI(eventName,newListEvent);
        }
    }
    private static void addEventAPI(String eventName,HashMap<String,Object> data){
        if(appContext!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            HashMap<String, Object> filterEventData = checkValidationEvent(data, 1);
            if (filterEventData.size() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(filterEventData);

                    if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty()  && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                        if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                            Map<String, String> mapData = new HashMap<>();
                            mapData.put(PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                            mapData.put(AppConstant.ACT, eventName);
                            mapData.put(AppConstant.ET_, "evt");
                            mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                            mapData.put(AppConstant.VAL, "" + jsonObject.toString());

                            RestClient.postRequest(RestClient.EVENT_URL, mapData,null, new RestClient.ResponseHandler() {
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
                }
                catch (Exception ex)
                {
                    Util.setException(appContext,ex.toString(),"iZooto","add Event");
                }
            }  else {
                Util.setException(appContext,"Event length more than 32",AppConstant.APP_NAME_TAG,"AdEvent");
            }
        }
    }

    private static HashMap<String, Object> checkValidationEvent(HashMap<String, Object> data,int index){
        HashMap<String, Object>  newList= new HashMap<String, Object>();
        for (HashMap.Entry<String,Object> array:data.entrySet()) {
            if (index<=16){
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String){
                    if (array.getValue().toString().length()>0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 64));
                        newList.put(newKey, newValue);
                        index++;
                    }
                } else if (!(array.getValue() instanceof String)&&array.getValue()!=null){
                    newList.put(newKey, ( array.getValue()));
                    index ++;
                }
            }
        }
        return newList;
    }
    public static void addUserProperty(HashMap<String, Object> object) {

        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.ADD_USERPROPERTY) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext,"addUserProperty(): operation from pending task queue.","[Log.d]->addUserProperty->");
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
                        if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty()  && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                            if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                                Map<String, String> mapData = new HashMap<>();
                                mapData.put(PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
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
                    }
                    else
                    {
                        Util.setException(appContext, "Blank user properties",AppConstant.APP_NAME_TAG, "addUserProperty");
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Blank user properties","[Log.d]->addUserProperty->");

                    }

                }
                else {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Blank user properties","[Log.d]->addUserProperty->");

                    Util.setException(appContext, "Blank user properties",AppConstant.APP_NAME_TAG, "addUserProperty");

                }

            } catch (Exception e) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Blank user properties","[Log.d]->addUserProperty->");

                Util.setException(appContext, "Blank user properties",AppConstant.APP_NAME_TAG, "addUserProperty");
            }
        }
        else
        {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Blank user properties","[Log.d]->addUserProperty->");

            Util.setException(appContext, "Blank user properties",AppConstant.APP_NAME_TAG, "addUserProperty");

        }

    }

    private static HashMap<String, Object> checkValidationUserProfile(HashMap<String, Object> data,int index){
        HashMap<String, Object>  newList= new HashMap<String, Object>();
        int indexForValue = 1;
        for (HashMap.Entry<String,Object> array:data.entrySet()) {
            if (index<=64){
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String){
                    if (array.getValue().toString().length()>0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 64));
                        newList.put(newKey, newValue);
                        index++;
                    }
                } else if (array.getValue() instanceof List) {
                    List<Object> newvalueListDta = (List<Object>) array.getValue();
                    List<Object> newvalueList = new ArrayList<Object>();
                    for(Object obj: newvalueListDta) {
                        if (indexForValue<=64){
                            if (obj instanceof String){
                                String ListData = obj.toString();
                                if (indexForValue<=64&&ListData.length()>0){
                                    String newListValue = ListData.substring(0, Math.min(ListData.length(), 64));
                                    newvalueList.add(newListValue);
                                    indexForValue ++;
                                }
                            }else if (!(obj instanceof String)&&obj!=null){
                                newvalueList.add(obj);
                                indexForValue ++;
                            }
                        }
                    }
                    newList.put(newKey, newvalueList);
                    index ++;
                }else if (!(array.getValue() instanceof String)&&!(array.getValue() instanceof List)&&array.getValue()!=null){
                    newList.put(newKey, ( array.getValue()));
                    index ++;
                }
            }
        }
        return newList;
    }

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
        preferenceUtil.setBooleanData(AppConstant.NOTIFICATION_ENABLE_DISABLE, enable);
        try {
            int value = 2;
            if (enable != null) {
                if (enable) {
                    value = 0;
                }

                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty()  && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.APPVERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.ACTION, "" + value);
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"setSubscription"+mapData.toString(),"[Log.d]->setSubscription->");

                        RestClient.postRequest(RestClient.SUBSCRIPTION_API, mapData,null, new RestClient.ResponseHandler() {
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

            }
            else
            {
                Util.setException(appContext, "Value should not be null",AppConstant.APP_NAME_TAG, "setSubscription");

            }
        }catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,"setSubscription"+e.toString(),"[Log.e]->Exception->");

            Util.setException(appContext, e.toString(),  AppConstant.APP_NAME_TAG,"setSubscription");
        }

    }

    public static void setFirebaseAnalytics(boolean isSet){
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
        if(appContext !=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            preferenceUtil.setBooleanData(AppConstant.FIREBASE_ANALYTICS_TRACK, isSet);
        }
    }
    public static void setDefaultNotificationBanner(int setBanner){
        bannerImage = setBanner;
    }
    public static void iZootoHandleNotification(Context context, final Map<String,String> data)
    {
        Log.d(AppConstant.APP_NAME_TAG, AppConstant.NOTIFICATIONRECEIVED);
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);

            if(data.get(AppConstant.AD_NETWORK) !=null || data.get(AppConstant.GLOBAL)!=null || data.get(AppConstant.GLOBAL_PUBLIC_KEY)!=null)
            {
                if(data.get(AppConstant.GLOBAL_PUBLIC_KEY)!=null)
                {
                    try
                    {
                        JSONObject jsonObject=new JSONObject(Objects.requireNonNull(data.get(AppConstant.GLOBAL)));
                        String urlData=data.get(AppConstant.GLOBAL_PUBLIC_KEY);
                        if(jsonObject.toString()!=null && urlData!=null && !urlData.isEmpty()) {
                            String cid = jsonObject.optString(ShortpayloadConstant.ID);
                            String rid = jsonObject.optString(ShortpayloadConstant.RID);
                            NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL,cid,rid,-1,"FCM");
                            AdMediation.getMediationGPL(context, jsonObject, urlData);
                        }
                        else
                        {
                            NotificationEventManager.handleNotificationError("Payload Error",data.toString(),"MessagingSevices","HandleNow");
                        }
                    }
                    catch (Exception ex)
                    {
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Payload"+ex.toString()+data.toString(),"[Log.e]->Exception->");

                        Util.setException(context,ex.toString()+"PayloadError"+data.toString(),"DATBMessagingService","handleNow");
                    }

                }
                else {
                    try {
                        JSONObject jsonObject = new JSONObject(data.get(AppConstant.GLOBAL));
                        String cid = jsonObject.optString(ShortpayloadConstant.ID);
                        String rid = jsonObject.optString(ShortpayloadConstant.RID);
                        NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1,"FCM");
                        JSONObject jsonObject1=new JSONObject(data.toString());
                        AdMediation.getMediationData(context, jsonObject1,"fcm","");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);
                    }
                    catch (Exception ex)
                    {
                        Util.setException(context,ex.toString()+"PayloadError"+data.toString(),"DATBMessagingService","handleNow");
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Payload Error"+ex.toString()+data.toString(),"[Log.e]->Exception->");

                    }
                }
            }
            else {
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
                    String updateDaily=NotificationEventManager.getDailyTime(context);
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
                        NotificationEventManager.handleImpressionAPI(payload,AppConstant.PUSH_FCM);
                        iZooto.processNotificationReceived(context,payload);
                    } // This is your code
                };
                mainHandler.post(myRunnable);

            }
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,data.toString(),"payloadData");

        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,"Payload Error"+e.toString()+data.toString(),"[Log.e]->Exception->");

            Util.setException(context,e.toString(),AppConstant.APP_NAME_TAG,"handleNotification");
        }
    }
    public static void addTag(final List<String> topicName){
        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.ADD_TAG) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    Log.d(AppConstant.APP_NAME_TAG, "addTag(): operation from pending task queue.");
                    addTag(topicName);
                }
            });
            return;
        }
        if (topicName != null && !topicName.isEmpty()) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getStringData(AppConstant.SENDERID) != null) {
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
                List<String> topicList = new ArrayList<String>();
                for (final String filterTopicName : topicName) {
                    if (filterTopicName != null && !filterTopicName.isEmpty()) {
                        if (Util.isMatchedString(filterTopicName)) {
                            try {
                                FirebaseMessaging.getInstance().subscribeToTopic(filterTopicName);
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
        }
        else
        {
            Util.setException(iZooto.appContext,"Topic list should not be  blank",AppConstant.APP_NAME_TAG,"AddTag");
        }
    }

    public static void removeTag(final List<String> topicName){

        if (osTaskManager.shouldQueueTaskForInit(OSTaskManager.REMOVE_TAG) && appContext == null) {
            osTaskManager.addTaskToQueue(new Runnable() {
                @Override
                public void run() {
                    Log.d(AppConstant.APP_NAME_TAG, "removeTag(): operation from pending task queue.");
                    removeTag(topicName);
                }
            });
            return;
        }
        if (topicName != null && !topicName.isEmpty()) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getStringData(AppConstant.SENDERID) != null ) {

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
                List<String> topicList = new ArrayList<String>();
                for (final String filterTopicName : topicName) {
                    if (filterTopicName != null && !filterTopicName.isEmpty()) {
                        if (Util.isMatchedString(filterTopicName)) {
                            try {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(filterTopicName);
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
        }
        else
        {
            Util.setException(iZooto.appContext,"Topic list should not be  blank",AppConstant.APP_NAME_TAG,"RemoveTag");

        }
    }

    private static void topicApi(String action, List<String> topic){
        if (appContext == null)
            return;

        try {
            if (topic.size() > 0){
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty()  && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() ) {
                        HashMap<String, List<String>> data = new HashMap<>();
                        data.put(AppConstant.TOPIC, topic);
                        JSONObject jsonObject = new JSONObject(data);
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ACT, action);
                        mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                        mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                        mapData.put(AppConstant.TOKEN, "" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        RestClient.postRequest(RestClient.PROPERTIES_URL, mapData, null,new RestClient.ResponseHandler() {
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
                                }
                                else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC)) {
                                    preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, jsonArray.toString());
                                }
                            }
                        });
                    } else {

                        JSONArray jsonArray = new JSONArray(topic);
                        if (action.equalsIgnoreCase(AppConstant.ADD_TOPIC)) {
                            preferenceUtil.setStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE, jsonArray.toString());
                        }
                        else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC)) {
                            preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, jsonArray.toString());
                        }
                    }
                } else {
                    JSONArray jsonArray = new JSONArray(topic);
                    if (action.equalsIgnoreCase(AppConstant.ADD_TOPIC)) {
                        preferenceUtil.setStringData(AppConstant.IZ_ADD_TOPIC_OFFLINE, jsonArray.toString());
                    }
                    else if (action.equalsIgnoreCase(AppConstant.REMOVE_TOPIC)) {
                        preferenceUtil.setStringData(AppConstant.IZ_REMOVE_TOPIC_OFFLINE, jsonArray.toString());
                    }
                }
            }
        }catch (Exception e) {
            Util.setException(appContext, e.toString(), "topicApi", AppConstant.APP_NAME_TAG);
        }
    }

    private static String  getAPI_KEY()
    {
        try {
            String apiKey = FirebaseOptions.fromResource(iZooto.appContext).getApiKey();
            if (apiKey != null)
                return apiKey;
        }
        catch (Exception e)
        {
            return "";//new String(Base64.decode(FCM_DEFAULT_API_KEY_BASE64, Base64.DEFAULT));

        }
        return "";


    }
    private  static String get_App_ID() {
        try {
            String application_id = FirebaseOptions.fromResource(iZooto.appContext).getApplicationId();
            if (application_id!=null)
                return application_id;
        }
        catch (Exception ex)
        {
            return "";//FCM_DEFAULT_APP_ID;

        }
        return "";

    }
    private  static String get_Project_ID()
    {
        try {
            String project_id = FirebaseOptions.fromResource(iZooto.appContext).getProjectId();
            if(project_id!=null)
                return project_id;
        }
        catch (Exception exception)
        {
            return "";

        }
        return "";

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    static void lastVisitApi(Context context){
        if(context!=null) {
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
                    mapData.put(PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                    mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                    mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                    mapData.put(AppConstant.ACT, "add");
                    mapData.put(AppConstant.ISID_, "1");
                    mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                    RestClient.postRequest(RestClient.LASTVISITURL, mapData,null, new RestClient.ResponseHandler() {
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
    public static iZooto.Builder initialize(Context context, String tokenJson) {
        if (context == null)
            return null;

        try {
            if (tokenJson !=null && !tokenJson.isEmpty()) {
                if (isJSONValid(tokenJson)) {

                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
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
                            Util.setException(context, "Please input the fcm token...", "initialize", AppConstant.APP_NAME_TAG);
                        }
                    }


                    return new iZooto.Builder(context);

                }
                else
                {
                    Log.e(AppConstant.APP_NAME_TAG,"Given String is Not Valid JSON String");

                }

            }

        } catch (Exception e) {
            Util.setException(context, e.toString(), "initialize", AppConstant.APP_NAME_TAG);
            e.printStackTrace();
        }
        return null;

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

    private static void sendOfflineDataToServer(Context context) {
        if (context == null)
            return;


        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty()) {
                JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                for (int i = 0; i < jsonArrayOffline.length(); i++) {
                    JSONObject c = jsonArrayOffline.getJSONObject(i);
                    NotificationActionReceiver.notificationClickAPI(context, c.optString("apiURL"), c.optString("cid"), c.optString("rid"), c.optInt("click"), i,"fcm");
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
                    NotificationEventManager.impressionNotification(c.optString("apiURL"), c.optString("cid"), c.optString("rid"), i,"fcm");
                }
            }

            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_VIEW_OFFLINE).isEmpty()) {
                JSONArray viewJsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_VIEW_OFFLINE));
                for (int i = 0; i < viewJsonArrayOffline.length(); i++) {
                    JSONObject c = viewJsonArrayOffline.getJSONObject(i);
                    NotificationEventManager.lastViewNotification(c.optString("apiURL"), c.optString("rid"), c.optString("cid"), i);
                }
            }
            if(!preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS).isEmpty())
            {
                JSONArray mediationRecords = new JSONArray(preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS));
                for(int i=0;i<mediationRecords.length();i++)
                {
                    JSONObject jsonObject=mediationRecords.getJSONObject(i);
                    if(jsonObject.getString(AppConstant.STORE_MED_API).equals(AppConstant.MED_IMPRESION))
                    {
                        String jsonData= jsonObject.getString(AppConstant.STORE_MED_DATA);
                        AdMediation.mediationImpression(jsonData,i);
                    }
                    if(jsonObject.getString(AppConstant.STORE_MED_API).equals(AppConstant.MED_CLICK))
                    {
                        String jsonData= jsonObject.getString(AppConstant.STORE_MED_DATA);
                        NotificationActionReceiver.callMediationClicks(context,jsonData,i);
                    }
                }

            }

        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,"SendOfflineDataToServerException","[Log.V]->SendOfflineDataToServerException->");
            Util.setException(iZooto.appContext,e.toString(),AppConstant.APP_NAME_TAG,"sendOfflineDataToServer");

        }


    }
    public static void  createDirectory(Context context)
    {
        DebugFileManager.createPublicDirectory(context);
    }
    public static void deleteDirectory(Context context)
    {
        DebugFileManager.deletePublicDirectory(context);
    }
    public static void shareFile(Context context,String name,String emailID)
    {

        DebugFileManager.shareDebuginfo(context,name,emailID);
    }
    public static void setPluginVersion(String pluginVersion)
    {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        if(pluginVersion!=null)
        {
            preferenceUtil.setStringData(AppConstant.HYBRID_PLUGIN_VERSION,pluginVersion);
        }
        else
        {
            preferenceUtil.setStringData(AppConstant.HYBRID_PLUGIN_VERSION,"");
        }
    }
    public enum LOG_LEVEL {
        NONE, FATAL, ERROR, WARN, INFO, DEBUG, VERBOSE
    }
    public static void promptForPushNotifications() {
        if(iZooto.appContext!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {

                    Intent intent = new Intent(iZooto.appContext, NotificationPermission.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    iZooto.appContext.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**     setNotificationChannelName      */
    public static void setNotificationChannelName(String channelName) {
        if(iZooto.appContext!= null) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (channelName != null && !channelName.isEmpty()){
                if(channelName.charAt(0)!= ' '){
                    if(channelName.length()<=30){
                        preferenceUtil.setStringData(AppConstant.iZ_STORE_CHANNEL_NAME, channelName);
                    }
                    else{
                        preferenceUtil.setStringData(AppConstant.iZ_STORE_CHANNEL_NAME, Util.getApplicationName(iZooto.appContext) + " Notification");
                    }
                }else{
                    Log.e(AppConstant.APP_NAME_TAG,"Channel Name not allowed with whitespace at first index");
                    preferenceUtil.setStringData(AppConstant.iZ_STORE_CHANNEL_NAME, Util.getApplicationName(iZooto.appContext) + " Notification");
                }
            }else {
                preferenceUtil.setStringData(AppConstant.iZ_STORE_CHANNEL_NAME, Util.getApplicationName(iZooto.appContext) + " Notification");
            }
        }
    }

    /** navigate to Notification settings */

    public static  void navigateToSettings(Activity activity)
    {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, Util.getPackageName(activity));
                activity.startActivity(settingsIntent);
            }
            else {
                Log.e(AppConstant.APP_NAME_TAG,"Method require API level 26 or Above");
            }
        }catch (Exception ex)
        {
            Log.e("Exception ex",ex.toString());
        }
    }

    /*  get the notification data */
    public static String getNotificationFeed(boolean isPagination){
        Context context = iZooto.appContext;
        String token;
        if(context != null){
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String pid = preferenceUtil.getStringData(PID);

            if(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN)!= null && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()){
                token = preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN);
            }
            else if (preferenceUtil.getStringData(AppConstant.HMS_TOKEN)!= null && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                token = preferenceUtil.getStringData(AppConstant.HMS_TOKEN);
            }
            else if (preferenceUtil.getStringData(AppConstant.XiaomiToken)!= null && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                token = preferenceUtil.getStringData(AppConstant.XiaomiToken);
            }
            else{
                token = null;
            }

            if(pid!= null && !pid.isEmpty() && token!= null && !token.isEmpty()){

                if(!isPagination){
                    pageNumber = 0;
                    fetchNotificationData(context, pageNumber);
                    Util.sleepTime(2000);
                    notificationData = preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_DATA);
                } else {
                    try{
                        JSONArray array = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_DATA));
                        if (array.length() >= 15) {
                            pageNumber++;
                            if(pageNumber < 5){
                                fetchNotificationData(context, pageNumber);
                                Util.sleepTime(2000);
                                notificationData = preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_DATA);
                            } else{
                                return AppConstant.IZ_NO_MORE_DATA;
                            }

                        } else{
                            return AppConstant.IZ_NO_MORE_DATA;
                        }

                    }catch (Exception e){
                        Log.d(AppConstant.APP_NAME_TAG, e.toString());
                    }
                }

            }else{
                return AppConstant.IZ_ERROR_MESSAGE;
            }

            if(notificationData != null && !notificationData.isEmpty()){
                return notificationData;
            }else{
                return AppConstant.IZ_NO_MORE_DATA;
            }
        }
        return AppConstant.IZ_NO_MORE_DATA;
    }

    private static void fetchNotificationData(Context context, int index) {
        if(context != null){
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            ArrayList<JSONObject> dataList = new ArrayList<>();

            preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_DATA, null);
            try{
                String encrypted_pid = Util.toSHA1(preferenceUtil.getStringData(PID));
                RestClient.get("https://nh.iz.do/nh/" + encrypted_pid + "/" + index + ".json", new RestClient.ResponseHandler() {
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        if(response != null && !response.isEmpty()){
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = null;
                                JSONObject jsonObject1 = null;
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    jsonObject = jsonArray.optJSONObject(i).optJSONObject(ShortpayloadConstant.NOTIFICATION_PAYLOAD);
                                    jsonObject1 = new JSONObject();
                                    jsonObject1.put(AppConstant.IZ_TITLE_INFO, jsonObject.optString(ShortpayloadConstant.TITLE));
                                    jsonObject1.put(AppConstant.IZ_MESSAGE_INFO, jsonObject.optString(ShortpayloadConstant.NMESSAGE));
                                    jsonObject1.put(AppConstant.IZ_BANNER_INFO, jsonObject.optString(ShortpayloadConstant.BANNER));
                                    jsonObject1.put(AppConstant.IZ_LANDING_URL_INFO, jsonObject.optString(ShortpayloadConstant.LINK));
                                    jsonObject1.put(AppConstant.IZ_TIME_STAMP_INFO, jsonObject.optString(ShortpayloadConstant.CREATEDON));
                                    dataList.add(jsonObject1);
                                }
                                preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_DATA, dataList.toString());

                            } catch (Exception e) {
                                preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_DATA, null);
                                if(!preferenceUtil.getBoolean(AppConstant.IZ_NOTIFICATION_FETCH_EXCEPTION)) {
                                    preferenceUtil.setBooleanData(AppConstant.IZ_NOTIFICATION_FETCH_EXCEPTION,true);
                                    Util.setException(context, e.toString(), AppConstant.APP_NAME_TAG, AppConstant.IZ_NOTIFICATION_FETCH_EXCEPTION);
                                }
                            }
                        }else {
                            preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_DATA, null);

                        }
                    }

                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                        preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_DATA, null);
                    }
                });
            }catch (Exception e){
                if(!preferenceUtil.getBoolean(AppConstant.IZ_NOTIFICATION_FETCH_EXCEPTION)) {
                    preferenceUtil.setBooleanData(AppConstant.IZ_NOTIFICATION_FETCH_EXCEPTION,true);
                    Util.setException(context, e.toString(), AppConstant.APP_NAME_TAG, AppConstant.IZ_NOTIFICATION_FETCH_EXCEPTION);
                }
            }
        }
    }
}

