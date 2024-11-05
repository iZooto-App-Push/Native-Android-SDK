package com.izooto;

import static com.izooto.AppConstant.ANDROID_ID;
import static com.izooto.AppConstant.APPPID;
import static com.izooto.AppConstant.APP_NAME_TAG;
import static com.izooto.AppConstant.FCM_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.HUAWEI_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.TAG;
import static com.izooto.NewsHubAlert.newsHubDBHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.comscore.Analytics;
import com.comscore.PublisherConfiguration;
import com.comscore.UsagePropertiesAutoUpdateMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import com.izooto.core.Utilities;
import com.izooto.feature.pulseweb.PWInterface;
import com.izooto.feature.pulseweb.PulseManager;
import com.izooto.feature.pulseweb.PulseURLManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unchecked")
public class iZooto {
    private static String serverClientId;
    public static Context appContext;
    static String senderId;
    public static String iZootoAppId;
    @SuppressLint("StaticFieldLeak")
    public static Builder mBuilder;
    public static int icon;
    private static Payload payload;
    public static boolean mUnsubscribeWhenNotificationsAreDisabled;
    protected static Listener mListener;
    protected static Handler mHandler = new Handler(Looper.getMainLooper());
    public static final long activeMode = 2000;
    @SuppressLint("StaticFieldLeak")
    private static FirebaseAnalyticsTrack firebaseAnalyticsTrack;
    @SuppressLint("StaticFieldLeak")
    static Activity curActivity, newsHubContext;
    public static boolean isHybrid = false;//check for SDK(Flutter,React native)
    public static String SDK_DEF = "native";
    public static int bannerImage;
    private static boolean initCompleted;
    static boolean isXmlParse = false;
    static boolean isEDGestureUiMode = false;

    // pulse web feature
    private static String pw_Url = "";
    private static String pw_Rid = "";
    private static String pw_Cid = "";
    private static String pw_Hash = "";
    private static String feedSrc = "";
    private static String pulseTitle = "";
    private static String titleColor = "";
    private static int titleSize;
    private static String titlePosition = "left";
    private static int titleMargin;
    private static String titleEnable;
    static String pulseHeader = "";

    static boolean isInitCompleted() {
        return initCompleted;
    }

    private static final OSTaskManager osTaskManager = new OSTaskManager();
    private static boolean iZootoInitialized = false;
    static String hms_appId;


    public static void setSenderId(String senderId) {
        iZooto.senderId = senderId;
    }

    private static void setActivity(Activity activity) {
        curActivity = activity;
    }


    public static iZooto.Builder initialize(Context context) {
        return new iZooto.Builder(context);
    }

    private static void init(Builder builder) {
        try {
            final Context context = builder.mContext;
            appContext = context.getApplicationContext();
            ActivityLifecycleListener.registerActivity((Application) context);
            mBuilder = builder;
            builder.mContext = null;
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = appInfo.metaData;

                if (bundle != null) {
                    if (bundle.containsKey(AppConstant.IZOOTO_APP_ID)) {
                        iZootoAppId = bundle.getString(AppConstant.IZOOTO_APP_ID);
                        preferenceUtil.setStringData(AppConstant.ENCRYPTED_PID, iZootoAppId);
                        preferenceUtil.setStringData(AppConstant.APP_ID, iZootoAppId);
                    }

                    if (iZootoAppId == null || iZootoAppId.isEmpty()) {
                        Lg.e(AppConstant.APP_NAME_TAG, AppConstant.MISSINGID);
                    } else {
                        Lg.i(AppConstant.APP_NAME_TAG, iZootoAppId);
                        RestClient.get(RestClient.P_GOOGLE_JSON_URL + iZootoAppId + ".dat", new RestClient.ResponseHandler() {
                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }

                            @Override
                            void onSuccess(String response) {
                                super.onSuccess(response);
                                try {
                                    if (!Utilities.isNullOrEmpty(response) && response.length() > 20) {
                                        processResponse(context, response, preferenceUtil);
                                    } else {
                                        Util.handleExceptionOnce(appContext, AppConstant.ACCOUNT_ID_EXCEPTION, AppConstant.APP_NAME_TAG, "init_onSuccess");
                                    }
                                } catch (Exception ex) {
                                    Util.handleExceptionOnce(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "init_onSuccess");
                                }
                            }
                        });
                    }
                } else {
                    DebugFileManager.createExternalStoragePublic(context, AppConstant.MESSAGE, "[Log.e]-->");
                }

            } catch (Throwable t) {
                Util.handleExceptionOnce(appContext, t.toString(), AppConstant.APP_NAME_TAG, "initBuilder");
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "initBuilder");
        }
    }

    private static void processResponse(Context context, String response, PreferenceUtil preferenceUtil) {
        if (context == null || preferenceUtil == null) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(Util.decrypt(context, AppConstant.SECRETKEY, response)));
            if (jsonObject.has(AppConstant.SENDERID)) {
                senderId = jsonObject.optString(AppConstant.SENDERID);
            } else {
                senderId = Util.getSenderId();
            }
            String pid = jsonObject.optString(AppConstant.APPPID);
            preferenceUtil.setStringData(AppConstant.APPPID, pid);
            serverClientId = jsonObject.optString(AppConstant.SERVER_CLIENT_ID);
            hms_appId = jsonObject.optString(AppConstant.HMS_APP_ID);
            String newsHub = jsonObject.optString(AppConstant.JSON_NEWS_HUB);
            boolean isPrompt = jsonObject.optBoolean(AppConstant.PROMPT_ENABLE);
            preferenceUtil.setBooleanData(AppConstant.PROMPT_ENABLE, isPrompt);

            try {
                if (!preferenceUtil.getBoolean(AppConstant.CHECK_SUBSCRIPTION) && preferenceUtil.getEnableState(AppConstant.NOTIFICATION_ENABLE_DISABLE)) {
                    setSubscription(true);
                    preferenceUtil.setBooleanData(AppConstant.NOTIFICATION_ENABLE_DISABLE, true);
                    preferenceUtil.setBooleanData(AppConstant.CHECK_SUBSCRIPTION, true);
                } else {
                    setSubscription(preferenceUtil.getBoolean(AppConstant.NOTIFICATION_ENABLE_DISABLE));
                }
            } catch (Exception e) {
                Log.e(APP_NAME_TAG, e.toString());
            }

            checkAndExecuteOneTapRecord(context, preferenceUtil);
            checkAndExecutePulse(context, preferenceUtil, jsonObject);

            try {
                String csId = jsonObject.optString(AppConstant.CAM_SCORE_ID);
                if (!Utilities.isNullOrEmpty(csId)) {
                    checkCamScoreSDK(context, csId);
                }
            } catch (Exception ex) {
                Log.e(AppConstant.APP_NAME_TAG, ex.toString());
            }

            try {
                int brand_key = jsonObject.optInt(AppConstant.NEWS_HUB_B_KEY);
                preferenceUtil.setIntData(AppConstant.NEWS_HUB_B_KEY, brand_key);
            } catch (Exception e) {
                Log.e("branding", "branding is null or empty!");
            }

            if (!preferenceUtil.getBoolean(AppConstant.SET_JSON_NEWS_HUB)) {
                fetchNewsHubData(context, newsHub);
            }

            try {
                if (preferenceUtil.getStringData(AppConstant.ADVERTISING_ID).isEmpty()) {
                    trackAdvertisingId();
                }
            } catch (Exception ex) {
                Log.e(AppConstant.APP_NAME_TAG, ex.toString());
            }

            if (Util.getPid(context) != null && preferenceUtil.getBoolean(AppConstant.IS_CONSENT_STORED)) {
                preferenceUtil.setIntData(AppConstant.CAN_STORED_QUEUE, 1);
            }

            if (iZooto.isHybrid) {
                preferenceUtil.setBooleanData(AppConstant.IS_HYBRID_SDK, true);
            }
            Log.d(APP_NAME_TAG, "android_id --> " + Util.getAndroidId(context));
        } catch (Exception ex) {
            Log.e(APP_NAME_TAG, "Kindly verify the izooto_app_id");
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.APP_NAME_TAG, "processResponse");
        }
    }

    // check and Execute pulse feature
    private static void checkAndExecutePulse(Context context, PreferenceUtil preferenceUtil, JSONObject jsonObject) {
        if (context == null || jsonObject == null || !jsonObject.has(AppConstant.IZ_PULSE)) {
            return;
        }
        try {
            String pulseConfig = jsonObject.optString(AppConstant.IZ_PULSE);
            if (Utilities.isNullOrEmpty(pulseConfig)) {
                Log.d(AppConstant.APP_NAME_TAG, "Empty or null pulse configuration found");
                return;
            }
            JSONObject pulseObject = new JSONObject(pulseConfig);
            initializePulse(context, preferenceUtil, pulseObject);
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.APP_NAME_TAG, "checkAndExecutePulse");
        }
    }


    // To initialize pulse feature
    private static void initializePulse(Context context, PreferenceUtil preferenceUtil, final JSONObject pulseObject) {
        if (context == null || pulseObject == null || pulseObject.length() == 0) {
            Log.d(APP_NAME_TAG, "Failed to initialize pulse feature");
            return;
        }

        try {
            String status = pulseObject.optString(AppConstant.isPulseEnable);
            preferenceUtil.setStringData(AppConstant.PW_STATUS, status);
            if (!status.equals("1")) {
                Log.d(APP_NAME_TAG, "Pulse feature is not enabled");
                return;
            }
            iZooto.pw_Rid = pulseObject.optString(AppConstant.RID_);
            iZooto.pw_Cid = pulseObject.optString(AppConstant.CID_);
            iZooto.pw_Hash = pulseObject.optString(AppConstant.PW_HASH);
            String feedSrc = pulseObject.optString(AppConstant.PW_FEED_SRC);
            iZooto.feedSrc = PulseURLManager.encodeFeedSource(feedSrc);
            String ads = pulseObject.optString(AppConstant.PW_ADS);
            String url = pulseObject.optString(AppConstant.PW_URL);
            iZooto.pulseHeader = pulseObject.optString(AppConstant.PULSE_HEADER);
            JSONObject labelObject = pulseObject.optJSONObject("label");
            if (labelObject != null && labelObject.length() > 0) {
                iZooto.pulseTitle = labelObject.optString("title", "Latest News");  // !=null ? labelObject.optString("title") : "Latest News";
                iZooto.titleSize = labelObject.optInt("size", 14); // Provide a default value of 0 if not present
                iZooto.titlePosition = labelObject.optString("position", "left");
                iZooto.titleMargin = labelObject.optInt("margin", 10);
                iZooto.titleEnable = labelObject.optString("status", "0");
                iZooto.titleColor = labelObject.optString("color", "#000000");// != null ? labelObject.optString("color") : "#000000";
            } else {
                iZooto.pulseTitle = "Latest News";
                iZooto.titleSize = 14;
                iZooto.titleColor = "#000000";
                iZooto.titlePosition = "left";
                iZooto.titleMargin = 10;
                iZooto.titleEnable = "0";
            }
            if (Utilities.isNullOrEmpty(url)) {
                Log.d(APP_NAME_TAG, "Missing pulse URL");
                return;
            }
            if (url.contains("?")) {
                iZooto.pw_Url = url + "&pid=" + Util.getPid(context) + "&bKey=" + Util.getAndroidId(appContext) + "&cid=" + iZooto.pw_Cid + "&rid=" + iZooto.pw_Rid + "&rfiIdHash=" + iZooto.pw_Hash + "&feedSrc=" + iZooto.feedSrc + "&ads="+ads+ "&sth="+ iZooto.pulseHeader;
            } else {
                iZooto.pw_Url = url + "?pid=" + Util.getPid(context) + "&bKey=" + Util.getAndroidId(appContext) + "&cid=" + iZooto.pw_Cid + "&rid=" + iZooto.pw_Rid + "&rfiIdHash=" + iZooto.pw_Hash + "&feedSrc=" + iZooto.feedSrc + "&ads="+ads+ "&sth="+ iZooto.pulseHeader;
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.APP_NAME_TAG, "initializePulse");
        }
    }

    private static void checkAndExecuteOneTapRecord(Context context, PreferenceUtil preferenceUtil) {
        if (context == null) {
            return;
        }
        try {
            boolean isSyncFailure = preferenceUtil.getBoolean(AppConstant.OT_SYNC_FAILURE);
            boolean isEmailNotNull = !Utilities.isNullOrEmpty(preferenceUtil.getStringData(AppConstant.IZ_EMAIL));
            boolean isAlreadySignIn = preferenceUtil.getBoolean(AppConstant.OT_SIGN_IN);

            if (!isSyncFailure || !isEmailNotNull || !isAlreadySignIn) {
                return;
            }
            String email = preferenceUtil.getStringData(AppConstant.IZ_EMAIL);
            String firstName = preferenceUtil.getStringData(AppConstant.IZ_FIRST_NAME);
            String lastName = preferenceUtil.getStringData(AppConstant.IZ_LAST_NAME);
            OneTapSignInManager.syncUserDetails(context, email, firstName, lastName);

        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.APP_NAME_TAG, "checkAndExecuteOneTapRecord");
        }
    }

    /* HMS Integration */
    static void initHmsService(final Context context) {
        if (context == null)
            return;

        try {
            HMSTokenGenerator hmsTokenGenerator = new HMSTokenGenerator();
            hmsTokenGenerator.getHMSToken(context, new HMSTokenListener.HMSTokenGeneratorHandler() {
                @Override
                public void complete(String id) {
                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                    if (id != null && !id.isEmpty()) {
                        if (!preferenceUtil.getBoolean(AppConstant.IS_UPDATED_HMS_TOKEN)) {
                            preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                            preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                        }
                        registerToken();
                    }
                }

                @Override
                public void failure(String errorMessage) {
                    Util.handleExceptionOnce(iZooto.appContext, errorMessage, APP_NAME_TAG, "initHmsService");
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "initHmsService");
        }
    }

    /* start */
    static void init(final Context context, String senderId) {
        if (context == null) {
            return;
        }
        try {
            FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
            fcmTokenGenerator.getToken(context, senderId, new TokenGenerator.TokenGenerationHandler() {
                @Override
                public void complete(String id) {
                    try {
                        Util util = new Util();
                        if (util.isInitializationValid()) {
                            if (id != null && !id.isEmpty()) {
                                registerToken();
                                ActivityLifecycleListener.registerActivity((Application) appContext.getApplicationContext());
                                setCurActivity(context);
                                if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack())
                                    firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                                initCompleted = true;
                                osTaskManager.startPendingTasks();
                            }
                        }
                    } catch (Exception ex) {
                        Util.handleExceptionOnce(iZooto.appContext, ex.toString(), APP_NAME_TAG, "initToken");
                    }
                }

                @Override
                public void failure(String errorMsg) {
                    Lg.e(AppConstant.APP_NAME_TAG, errorMsg);
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "initToken");
        }
    }

    public static void trackAdvertisingId() {
        if (appContext == null) {
            return;
        }
        try {
            new Thread(() -> {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                AdvertisingIdClient.getAdvertisingId(appContext, new AdvertisingIdClient.Listener() {
                    @Override
                    public void onAdvertisingIdClientFinish(AdvertisingIdClient.AdInfo adInfo) {
                        String advertisementID = adInfo.getId();
                        preferenceUtil.setStringData(AppConstant.ADVERTISING_ID, advertisementID);
                        invokeFinish(advertisementID, preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));
                    }

                    @Override
                    public void onAdvertisingIdClientFail(Exception exception) {
                        invokeFail(exception);
                    }
                });
            }).start();

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "trackAdvertisingId");
        }
    }

    public static synchronized void idsAvailable(Context context, Listener listener) {
        new iZooto().start(context, listener);
    }

    public static void setNotificationSound(String soundName) {
        try {
            if (soundName == null || soundName.isEmpty()) {
                return;
            }
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            preferenceUtil.setStringData(AppConstant.NOTIFICATION_SOUND_NAME, soundName);

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "setNotificationSound");
        }

    }

    protected void start(final Context context, final Listener listener) {
        if (listener == null) {
            return;
        }

        mHandler = new Handler(Looper.getMainLooper());
        mListener = listener;

        if (context == null) {
            invokeFail(new Exception(TAG + " - Error: context null"));
            return;
        }

        // Run the long-running operation in a separate thread to avoid blocking the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context); // Use the passed context instead of appContext
                    final String advertisingId = preferenceUtil.getStringData(AppConstant.ADVERTISING_ID);
                    final String encryptedPid = preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID);
                    // Handle the results on the main thread
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invokeFinish(advertisingId, encryptedPid);
                        }
                    });
                } catch (Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invokeFail(e);
                        }
                    });
                }
            }
        }).start();
    }


    public interface Listener {
        void idsAvailable(String adVerID, String registrationID);

        void onAdvertisingIdClientFail(Exception exception);
    }


    protected static void invokeFinish(final String advertisementID, final String registrationID) {
        mHandler.post(() -> {
            synchronized (iZooto.class) {
                if (mListener != null) {
                    mListener.idsAvailable(advertisementID, registrationID);
                }
            }
        });
    }

    protected static void invokeFail(final Exception exception) {
        mHandler.post(() -> {
            synchronized (iZooto.class) {
                if (mListener != null) {
                    mListener.onAdvertisingIdClientFail(exception);
                }
            }
        });
    }

    static void registerToken() {
        if (appContext == null) {
            return;
        }
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                    if (preferenceUtil.getiZootoID(APPPID) != null && !preferenceUtil.getiZootoID(APPPID).isEmpty()) {
                        if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
                            if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 3);

                            } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
                            } else {
                                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 1);
                            }
                            try {
                                if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty())
                                    preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);

                                Map<String, String> mapData = new HashMap<>();
                                mapData.put(AppConstant.ADDURL, "" + AppConstant.STYPE);
                                mapData.put(AppConstant.PID, Util.getPid(appContext));
                                mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                                mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                                mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                                mapData.put(AppConstant.APPVERSION, Util.getAppVersion(iZooto.appContext));
                                mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                                mapData.put(AppConstant.ALLOWED_, "" + AppConstant.ALLOWED);
                                mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(appContext));
                                mapData.put(AppConstant.CHECKSDKVERSION, Util.getSDKVersion(appContext));
                                mapData.put(AppConstant.LANGUAGE, Util.getDeviceLanguage());
                                mapData.put(AppConstant.QSDK_VERSION, AppConstant.SDKVERSION);
                                mapData.put(AppConstant.TOKEN, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                mapData.put(AppConstant.ADVERTISEMENTID, preferenceUtil.getStringData(AppConstant.ADVERTISING_ID));
                                mapData.put(AppConstant.PACKAGE_NAME, appContext.getPackageName());
                                mapData.put(AppConstant.SDKTYPE, SDK_DEF);
                                mapData.put(AppConstant.KEY_HMS, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                mapData.put(AppConstant.ANDROIDVERSION, Build.VERSION.RELEASE);
                                mapData.put(AppConstant.DEVICE_NAME, Util.getDeviceName());
                                mapData.put(AppConstant.H_PLUGIN_VERSION, preferenceUtil.getStringData(AppConstant.HYBRID_PLUGIN_VERSION));

                                RestClient.postRequest(RestClient.BASE_URL, mapData, null, new RestClient.ResponseHandler() {
                                    @Override
                                    void onSuccess(final String response) {
                                        super.onSuccess(response);
                                        lastVisitApi(appContext);
                                        if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                try {
                                                    JSONObject jsonObject = new JSONObject();
                                                    jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                                    jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                                    if (!jsonObject.optString(FCM_TOKEN_FROM_JSON).isEmpty() || !jsonObject.optString(HUAWEI_TOKEN_FROM_JSON).isEmpty()) {
                                                        mBuilder.mTokenReceivedListener.onTokenReceived(jsonObject.toString());
                                                        Lg.d(AppConstant.APP_NAME_TAG, AppConstant.DEVICE_TOKEN + jsonObject);
                                                    }
                                                } catch (Exception ex) {
                                                    Util.handleExceptionOnce(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "RegisterToken");
                                                }

                                            });
                                        }
                                        preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, true);
                                        preferenceUtil.setLongData(AppConstant.DEVICE_REGISTRATION_TIMESTAMP, System.currentTimeMillis());
                                        try {
                                            preferenceUtil.setBooleanData(AppConstant.IS_CONSENT_STORED, true);
                                            preferenceUtil.setIntData(AppConstant.CAN_STORED_QUEUE, 1);

                                            if (!preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA).isEmpty()) {
                                                JSONObject json = new JSONObject(preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA));
                                                addUserProperty(Util.toMap(json));
                                            }
                                            if (!preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN).isEmpty() && !preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV).isEmpty()) {
                                                JSONObject json = new JSONObject(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV));
                                                addEvent(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN), Util.toMap(json));
                                            }
                                        } catch (Exception e) {
                                            Util.handleExceptionOnce(appContext, e.toString(), "registerToken1", AppConstant.APP_NAME_TAG);
                                        }
                                    }

                                    @Override
                                    void onFailure(int statusCode, String response, Throwable throwable) {
                                        super.onFailure(statusCode, response, throwable);
                                    }
                                });

                            } catch (Exception exception) {
                                Util.handleExceptionOnce(appContext, exception.toString(), AppConstant.APP_NAME_TAG, "registerToken");
                            }
                        } else {
                            try {
                                lastVisitApi(appContext);
                                if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                        jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                        if (!jsonObject.optString(FCM_TOKEN_FROM_JSON).isEmpty() || !jsonObject.optString(HUAWEI_TOKEN_FROM_JSON).isEmpty()) {
                                            mBuilder.mTokenReceivedListener.onTokenReceived(jsonObject.toString());
                                            Lg.d(AppConstant.APP_NAME_TAG, AppConstant.DEVICE_TOKEN + jsonObject);
                                        }

                                    } catch (Exception ex) {
                                        Util.handleExceptionOnce(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "registerToken");

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
                                if (!preferenceUtil.getBoolean(AppConstant.FILE_EXIST)) {
                                    try {
                                        Map<String, String> mapData = new HashMap<>();
                                        mapData.put(AppConstant.ADDURL, "" + AppConstant.STYPE);
                                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                                        mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                                        mapData.put(AppConstant.APPVERSION, Util.getSDKVersion(iZooto.appContext));
                                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                                        mapData.put(AppConstant.ALLOWED_, "" + AppConstant.ALLOWED);
                                        mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(appContext));
                                        mapData.put(AppConstant.CHECKSDKVERSION, Util.getSDKVersion(appContext));
                                        mapData.put(AppConstant.LANGUAGE, Util.getDeviceLanguage());
                                        mapData.put(AppConstant.QSDK_VERSION, AppConstant.SDKVERSION);
                                        mapData.put(AppConstant.TOKEN, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                        mapData.put(AppConstant.ADVERTISEMENTID, preferenceUtil.getStringData(AppConstant.ADVERTISING_ID));
                                        mapData.put(AppConstant.PACKAGE_NAME, appContext.getPackageName());
                                        mapData.put(AppConstant.SDKTYPE, SDK_DEF);
                                        mapData.put(AppConstant.KEY_HMS, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                        mapData.put(AppConstant.ANDROIDVERSION, Build.VERSION.RELEASE);
                                        mapData.put(AppConstant.DEVICE_NAME, Util.getDeviceName());
                                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, mapData.toString(), "RegisterToken");

                                    } catch (Exception exception) {
                                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "RegisterToken -> " + exception, "[Log.e]->");
                                        Util.handleExceptionOnce(appContext, exception.toString(), "registerToken", APP_NAME_TAG);

                                    }
                                }
                            } catch (Exception e) {
                                DebugFileManager.createExternalStoragePublic(iZooto.appContext, "RegisterToken -> " + e, "[Log.e]->");
                                Util.handleExceptionOnce(appContext, e.toString(), "registerToken", APP_NAME_TAG);
                            }
                        }
                    } else {
                        Util.handleExceptionOnce(iZooto.appContext, "Missing pid", AppConstant.APP_NAME_TAG, "Register Token");
                    }
                }
            });

            executorService.shutdown();

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "registerToken");
        }

    }

    static void onActivityResumed(Activity activity) {
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            setActivity(activity);
            if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
                if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                    firebaseAnalyticsTrack.influenceOpenTrack();
                    preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, true);
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "onActivityResumed");
        }
    }

    private static void setCurActivity(Context context) {
        if (context == null) {
            return;
        }
        boolean foreground = isContextActivity(context);
        if (foreground) {
            iZooto.curActivity = (Activity) context;
        }
    }

    private static boolean isContextActivity(Context context) {
        return context instanceof Activity;
    }


    public static void processNotificationReceived(Context context, Payload payload) {
        try {
            if (payload != null) {
                NotificationEventManager.manageNotification(payload);
            }
            if (context != null) {
                sendOfflineDataToServer(context);
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "processNotificationReceived");
        }
    }

    public static void notificationView(Payload payload) {
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (payload == null) {
                return;
            }
            if (mBuilder == null || mBuilder.mNotificationHelper == null) {
                return;
            }
            try {
                if (payload.getRid() != null && !payload.getRid().isEmpty()) {
                    preferenceUtil.setIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN, Util.getValidIdForCampaigns(payload));
                } else {
                    Log.w("notificationV...", "rid null or empty!");
                }
                int campaigns = preferenceUtil.getIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN);
                if (campaigns == AppConstant.CAMPAIGN_SI || campaigns == AppConstant.CAMPAIGN_SE) {
                    Log.w("notificationV...", "...");
                } else {
                    payload.setLink(Util.updateUrlParameter(payload.getLink()));
                    payload.setAct1link(Util.updateUrlParameter(payload.getAct1link()));
                    payload.setAct2link(Util.updateUrlParameter(payload.getAct2link()));
                    mBuilder.mNotificationHelper.onNotificationReceived(payload);
                }

            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "notificationView");
            }

            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "notificationView");

        }
    }

    public static void notificationActionHandler(String data, String pulseData, boolean isPulseEnable) {
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (!Utilities.isNullOrEmpty(data)) {
                if (mBuilder != null && mBuilder.mNotificationHelper != null) {
                    if (isPulseEnable) {
                        String activityName = preferenceUtil.getStringData(AppConstant.PW_ACTIVITY_NAME);
                        if (!Utilities.isNullOrEmpty(activityName)) {
                            try {
                                if (!activityName.contains(".")) {
                                    activityName = iZooto.appContext.getPackageName() + "." + activityName;
                                }
                                Class<?> activityClass = Class.forName(activityName);
                                Intent intent = new Intent(iZooto.appContext, activityClass);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                iZooto.appContext.startActivity(intent);
                                PulseURLManager.updatePulseURL(pulseData);
                            } catch (Exception e) {
                                Log.v(APP_NAME_TAG, "Activity class not found");
                            }
                        } else {
                            Log.i(APP_NAME_TAG, "Activity name is null or empty");
                        }
                    } else {
                        mBuilder.mNotificationHelper.onNotificationOpened(data);
                    }

                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "notificationActionHandler");
        }
    }

    public static void notificationInAppAction(Context context, String url) {
        try {
            if (context == null || url == null || url.isEmpty()) {
                return;
            }
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                if (!preferenceUtil.getBoolean(AppConstant.IZ_DEFAULT_WEB_VIEW)) {
                    if (mBuilder != null && mBuilder.mWebViewListener != null) {
                        mBuilder.mWebViewListener.onWebView(url);
                    } else {
                        Log.i("notification...", "builder null");
                    }
                } else {
                    iZootoWebViewActivity.startActivity(context, url);
                }
            } else if (mBuilder != null && mBuilder.mWebViewListener != null) {
                mBuilder.mWebViewListener.onWebView(url);
            } else {
                iZootoWebViewActivity.startActivity(context, url);
            }


        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "notificationInAppAction");
        }
    }

    /*
      PulseManager the Hybrid Web_View Listener
     */
    public static void notificationWebView(NotificationWebViewListener notificationWebViewListener) {
        mBuilder.mWebViewListener = notificationWebViewListener;
        if (mBuilder.mWebViewListener != null) {
            runNotificationWebViewCallback();
        }
    }

    private static void runNotificationWebViewCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                try {
                    if (NotificationActionReceiver.WebViewClick != null && !NotificationActionReceiver.WebViewClick.isEmpty()) {
                        iZooto.mBuilder.mWebViewListener.onWebView(NotificationActionReceiver.WebViewClick);
                        NotificationActionReceiver.WebViewClick = "";
                    }

                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                    if (TargetActivity.mWebViewClick != null && !TargetActivity.mWebViewClick.isEmpty() && preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                        iZooto.mBuilder.mWebViewListener.onWebView(TargetActivity.mWebViewClick);
                        TargetActivity.mWebViewClick = "";
                    }
                } catch (Exception var2) {
                    Util.handleExceptionOnce(iZooto.appContext, var2.toString(), APP_NAME_TAG, "runNotificationWebViewCallback");
                }
            }
        });
    }

    public static void notificationClick(NotificationHelperListener notificationOpenedListener) {
        mBuilder.mNotificationHelper = notificationOpenedListener;
        if (mBuilder.mNotificationHelper != null) {
            runNotificationOpenedCallback();
        }
    }


    private static void runNotificationOpenedCallback() {
        runOnMainUIThread(() -> {
            try {
                if (NotificationActionReceiver.notificationClick != null && !NotificationActionReceiver.notificationClick.isEmpty()) {
                    iZooto.mBuilder.mNotificationHelper.onNotificationOpened(NotificationActionReceiver.notificationClick);
                    NotificationActionReceiver.notificationClick = "";
                }
            } catch (Exception ex) {
                Util.handleExceptionOnce(iZooto.appContext, ex.toString(), APP_NAME_TAG, "runNotificationOpenedCallback");

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
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), APP_NAME_TAG, "runOnMainUIThread");
        }

    }

    public static void notificationViewHybrid(String payloadList, Payload payload) {
        if (payload == null) {
            return;
        }
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (mBuilder != null && mBuilder.mNotificationReceivedHybridListener != null) {
            try {
                if (payload.getRid() != null && !payload.getRid().isEmpty()) {
                    preferenceUtil.setIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN, Util.getValidIdForCampaigns(payload));
                } else {
                    Log.w("notificationV...", "rid null or empty!");
                }
                int campaigns = preferenceUtil.getIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN);
                if (campaigns == AppConstant.CAMPAIGN_SI || campaigns == AppConstant.CAMPAIGN_SE) {
                    Log.w("notificationV...", "...");
                } else {
                    if (payloadList == null || payloadList.isEmpty()) {
                        return;
                    }
                    mBuilder.mNotificationReceivedHybridListener.onNotificationReceivedHybrid(payloadList);
                }

            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "notificationViewHybrid");
            }


        }
        if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
            firebaseAnalyticsTrack.receivedEventTrack(payload);
        } else {
            if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            }
        }
        if (payload.getId() == null || payload.getId().isEmpty()) {
            return;
        }
        if (!payload.getId().equals(preferenceUtil.getStringData(AppConstant.TRACK_NOTIFICATION_ID))) {
            preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, false);
        }
        preferenceUtil.setStringData(AppConstant.TRACK_NOTIFICATION_ID, payload.getId());


    }

    public static void notificationReceivedCallback(NotificationReceiveHybridListener notificationReceivedHybridListener) {
        mBuilder.mNotificationReceivedHybridListener = notificationReceivedHybridListener;
        if (mBuilder.mNotificationReceivedHybridListener != null) {
            runNotificationReceivedCallback();
        }
    }

    private static void runNotificationReceivedCallback() {
        runOnMainUIThread(() -> {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (NotificationEventManager.iZootoReceivedPayload == null) {
                return;
            }
            try {
                if (payload.getRid() != null && !payload.getRid().isEmpty()) {
                    preferenceUtil.setIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN, Util.getValidIdForCampaigns(payload));
                } else {
                    Log.w("notificationV...", "rid null or empty!");
                }
                int campaigns = preferenceUtil.getIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN);
                if (campaigns == AppConstant.CAMPAIGN_SI || campaigns == AppConstant.CAMPAIGN_SE) {
                    Log.w("notificationV...", "...");
                } else {
                    iZooto.mBuilder.mNotificationReceivedHybridListener.onNotificationReceivedHybrid(NotificationEventManager.iZootoReceivedPayload);
                }

            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "runNotificationReceivedCallback");
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, e.toString(), "[Log.e]->RID");
            }

            if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
                if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                    firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                    firebaseAnalyticsTrack.influenceOpenTrack();
                    preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, true);
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
        public NotificationReceiveHybridListener mNotificationReceivedHybridListener;

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

        public Builder setLandingURLListener(NotificationWebViewListener mNotificationWebViewListener) {
            mWebViewListener = mNotificationWebViewListener;
            return this;

        }

        public Builder setNotificationReceiveHybridListener(NotificationReceiveHybridListener notificationReceivedHybrid) {
            mNotificationReceivedHybridListener = notificationReceivedHybrid;
            return this;
        }

        public Builder unsubscribeWhenNotificationsAreDisabled(boolean set) {
            mUnsubscribeWhenNotificationsAreDisabled = set;
            return this;
        }

        public void build() {
            try {
                iZooto.init(this);
                iZootoInitialized = true;
            } catch (Exception e) {
                iZootoInitialized = false;
            }
        }

    }

    public static boolean initialized() {
        return !iZootoInitialized;
    }

    public static void setDefaultTemplate(int templateID) {
        try {
            if (PushTemplate.DEFAULT == templateID || PushTemplate.TEXT_OVERLAY == templateID) {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                preferenceUtil.setIntData(AppConstant.NOTIFICATION_PREVIEW, templateID);
            } else {
                Util.handleExceptionOnce(appContext, "Template id is not matched" + templateID, AppConstant.APP_NAME_TAG, "setDefaultTemplate");
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(appContext, "Template id is not matched" + templateID, AppConstant.APP_NAME_TAG, "setDefaultTemplate");
        }
    }


    // send events  with event name and event data
    public static void addEvent(String eventName, HashMap<String, Object> data) {
        try {
            if (data != null && eventName != null && !eventName.isEmpty() && !data.isEmpty()) {
                eventName = eventName.substring(0, Math.min(eventName.length(), 32)).replace(" ", "_");
                HashMap<String, Object> newListEvent = new HashMap<String, Object>();
                for (Map.Entry<String, Object> refineEntry : data.entrySet()) {
                    if (refineEntry.getKey() != null && !refineEntry.getKey().isEmpty()) {
                        String newKey = refineEntry.getKey().toLowerCase();
                        newListEvent.put(newKey, refineEntry.getValue());
                    }
                }
                if (!newListEvent.isEmpty())
                    addEventAPI(eventName, newListEvent);
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "addEvent");
        }
    }

    private static void addEventAPI(String eventName, HashMap<String, Object> data) {
        if (appContext == null) {
            return;
        }
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        HashMap<String, Object> filterEventData = checkValidationEvent(data, 1);
        if (filterEventData.size() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(filterEventData);

                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ACT, eventName);
                        mapData.put(AppConstant.ET_, "evt");
                        mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(appContext));
                        mapData.put(AppConstant.VAL, "" + jsonObject);
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
                Util.handleExceptionOnce(appContext, ex.toString(), APP_NAME_TAG, "add Event");
            }
        } else {
            Util.handleExceptionOnce(appContext, "Event length more than 32", AppConstant.APP_NAME_TAG, "AdEvent");
        }

    }

    private static HashMap<String, Object> checkValidationEvent(HashMap<String, Object> data, int index) {
        try {
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
        } catch (Exception e) {
            Util.handleExceptionOnce(appContext, e.toString(), AppConstant.APP_NAME_TAG, "checkValidationEvent");
            return null;
        }
    }

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
                            if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                                Map<String, String> mapData = new HashMap<>();
                                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                                mapData.put(AppConstant.ACT, "add");
                                mapData.put(AppConstant.ET_, AppConstant.USERP_);
                                mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(appContext));
                                mapData.put(AppConstant.VAL, "" + jsonObject);
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
                        Util.handleExceptionOnce(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");

                    }

                } else {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");
                    Util.handleExceptionOnce(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");

                }

            } catch (Exception e) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");
                Util.handleExceptionOnce(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");
            }
        } else {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Blank user properties", "[Log.d]->addUserProperty->");
            Util.handleExceptionOnce(appContext, "Blank user properties", AppConstant.APP_NAME_TAG, "addUserProperty");

        }
    }

    private static HashMap<String, Object> checkValidationUserProfile(HashMap<String, Object> data, int index) {
        try {
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
                        List<Object> newValueListDta = (List<Object>) array.getValue();
                        List<Object> newvalueList = new ArrayList<Object>();
                        for (Object obj : newValueListDta) {
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
        } catch (Exception e) {
            Util.handleExceptionOnce(appContext, e.toString(), AppConstant.APP_NAME_TAG, "checkValidationUserProfile");
            return null;
        }
    }

    /* Allow user to send messages to a specific set of devices identified by their subscribed device */
    public static void setSubscription(Boolean enable) {
        try {
            if (iZooto.appContext == null) {
                return;
            }
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (!enable) {
                if (senderId == null || senderId.isEmpty()) {
                    return;
                }

                FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
                String token = preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN);
                if (token != null && !token.isEmpty()) {
                    fcmTokenGenerator.removeDeviceAddress(iZooto.appContext, senderId);
                    preferenceUtil.setBooleanData(AppConstant.NOTIFICATION_ENABLE_DISABLE, false);
                }

            } else {
                if (senderId == null || senderId.isEmpty()) {
                    Lg.e(AppConstant.APP_NAME_TAG, iZooto.appContext.getString(R.string.something_wrong_with_fcm_sender_id));
                    return;
                }
                checkNotificationPermission(iZooto.appContext, Util.getChannelId(), senderId);
                preferenceUtil.setBooleanData(AppConstant.NOTIFICATION_ENABLE_DISABLE, true);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "setSubscription");
        }

    }

    public static void setFirebaseAnalytics(boolean isSet) {

        if (appContext == null) {
            return;
        }
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        preferenceUtil.setBooleanData(AppConstant.FIREBASE_ANALYTICS_TRACK, isSet);

    }

    public static void setDefaultNotificationBanner(int setBanner) {
        bannerImage = setBanner;
    }


    // This method has been deprecated.
    public static void iZootoHandleNotification(final Context context, final Map<String, String> data) {
        Log.d(AppConstant.APP_NAME_TAG, AppConstant.NOTIFICATIONRECEIVED);
        try {
            if (iZooto.initialized()) {
                return;
            }
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (data.get(AppConstant.AD_NETWORK) != null || data.get(AppConstant.GLOBAL) != null || data.get(AppConstant.GLOBAL_PUBLIC_KEY) != null) {
                if (data.get(AppConstant.GLOBAL_PUBLIC_KEY) != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(data.get(AppConstant.GLOBAL)));
                        String urlData = data.get(AppConstant.GLOBAL_PUBLIC_KEY);
                        if (jsonObject.toString() != null && urlData != null && !urlData.isEmpty()) {
                            String cid = jsonObject.optString(ShortPayloadConstant.ID);
                            String rid = jsonObject.optString(ShortPayloadConstant.RID);
                            NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, "FCM");
                            AdMediation.mediationGPL(context, jsonObject, urlData);
                        } else {
                            NotificationEventManager.handleNotificationError("Payload Error", data.toString(), APP_NAME_TAG, "iZootoHandleNotification");
                        }
                    } catch (Exception ex) {
                        Util.handleExceptionOnce(context, ex + "PayloadError" + data, "iZooto", "handleNow" + " gpl");
                    }

                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(data.get(AppConstant.GLOBAL)));
                        String cid = jsonObject.optString(ShortPayloadConstant.ID);
                        String rid = jsonObject.optString(ShortPayloadConstant.RID);
                        NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, "FCM");
                        JSONObject jsonObject1 = new JSONObject(data.toString());
                        AdMediation.getMediationData(context, jsonObject1, "fcm", "");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);
                    } catch (Exception ex) {
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext, "Payload Error" + ex + data, "[Log.e]->Exception->");
                    }
                }
            } else {
                preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                JSONObject payloadObj = new JSONObject(data);
                if (payloadObj.optLong(ShortPayloadConstant.CREATEDON) > PreferenceUtil.getInstance(iZooto.appContext).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    payload.setCreated_Time(payloadObj.optString(ShortPayloadConstant.CREATEDON));
                    payload.setFetchURL(payloadObj.optString(ShortPayloadConstant.FETCHURL));
                    payload.setKey(payloadObj.optString(ShortPayloadConstant.KEY));
                    payload.setId(payloadObj.optString(ShortPayloadConstant.ID).replace("['", "").replace("']", "").replace("~", ""));
                    payload.setRid(payloadObj.optString(ShortPayloadConstant.RID));
                    payload.setLink(payloadObj.optString(ShortPayloadConstant.LINK).replace("['", "").replace("']", ""));
                    payload.setTitle(payloadObj.optString(ShortPayloadConstant.TITLE).replace("['", "").replace("']", ""));
                    payload.setMessage(payloadObj.optString(ShortPayloadConstant.NMESSAGE).replace("['", "").replace("']", ""));
                    payload.setIcon(payloadObj.optString(ShortPayloadConstant.ICON).replace("['", "").replace("']", ""));
                    payload.setReqInt(payloadObj.optInt(ShortPayloadConstant.REQINT));
                    payload.setTag(payloadObj.optString(ShortPayloadConstant.TAG));
                    payload.setTl(payloadObj.optString(ShortPayloadConstant.TIME_TO_LIVE));
                    payload.setBanner(payloadObj.optString(ShortPayloadConstant.BANNER).replace("['", "").replace("']", ""));
                    payload.setAct_num(payloadObj.optInt(ShortPayloadConstant.ACTNUM));
                    payload.setBadgeicon(payloadObj.optString(ShortPayloadConstant.BADGE_ICON).replace("['", "").replace("']", ""));
                    payload.setBadgecolor(payloadObj.optString(ShortPayloadConstant.BADGE_COLOR));
                    payload.setSubTitle(payloadObj.optString(ShortPayloadConstant.SUBTITLE));
                    payload.setGroup(payloadObj.optInt(ShortPayloadConstant.GROUP));
                    payload.setBadgeCount(payloadObj.optInt(ShortPayloadConstant.BADGE_COUNT));
                    // Button 2
                    payload.setAct1name(payloadObj.optString(ShortPayloadConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(ShortPayloadConstant.ACT1LINK).replace("['", "").replace("']", ""));
                    payload.setAct1icon(payloadObj.optString(ShortPayloadConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(ShortPayloadConstant.ACT1ID));
                    // Button 2
                    payload.setAct2name(payloadObj.optString(ShortPayloadConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(ShortPayloadConstant.ACT2LINK).replace("['", "").replace("']", ""));
                    payload.setAct2icon(payloadObj.optString(ShortPayloadConstant.ACT2ICON));
                    payload.setAct2ID(payloadObj.optString(ShortPayloadConstant.ACT2ID));

                    payload.setInapp(payloadObj.optInt(ShortPayloadConstant.INAPP));
                    payload.setTrayicon(payloadObj.optString(ShortPayloadConstant.TARYICON));
                    payload.setSmallIconAccentColor(payloadObj.optString(ShortPayloadConstant.ICONCOLOR));
                    payload.setLedColor(payloadObj.optString(ShortPayloadConstant.LEDCOLOR));
                    payload.setLockScreenVisibility(payloadObj.optInt(ShortPayloadConstant.VISIBILITY));
                    payload.setGroupKey(payloadObj.optString(ShortPayloadConstant.GKEY));
                    payload.setGroupMessage(payloadObj.optString(ShortPayloadConstant.GMESSAGE));
                    payload.setFromProjectNumber(payloadObj.optString(ShortPayloadConstant.PROJECTNUMBER));
                    payload.setCollapseId(payloadObj.optString(ShortPayloadConstant.COLLAPSEID));
                    payload.setPriority(payloadObj.optInt(ShortPayloadConstant.PRIORITY));
                    payload.setRawPayload(payloadObj.optString(ShortPayloadConstant.RAWDATA));
                    payload.setRc(payloadObj.optString(ShortPayloadConstant.RC));
                    payload.setRv(payloadObj.optString(ShortPayloadConstant.RV));
                    payload.setAp(payloadObj.optString(ShortPayloadConstant.ADDITIONALPARAM));
                    payload.setCfg(payloadObj.optInt(ShortPayloadConstant.CFG));
                    payload.setPush_type(String.valueOf(PushType.fcm));
                    // Notification Channel .............
                    payload.setChannel(payloadObj.optString(ShortPayloadConstant.NOTIFICATION_CHANNEL));
                    payload.setVibration(payloadObj.optString(ShortPayloadConstant.VIBRATION));
                    payload.setBadge(payloadObj.optInt(ShortPayloadConstant.BADGE));
                    payload.setOtherChannel(payloadObj.optString(ShortPayloadConstant.OTHER_CHANNEL));
                    payload.setSound(payloadObj.optString(ShortPayloadConstant.SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortPayloadConstant.MAX_NOTIFICATION));
                    payload.setFallBackDomain(payloadObj.optString(ShortPayloadConstant.FALL_BACK_DOMAIN));
                    payload.setFallBackSubDomain(payloadObj.optString(ShortPayloadConstant.FALLBACK_SUB_DOMAIN));
                    payload.setFallBackPath(payloadObj.optString(ShortPayloadConstant.FAll_BACK_PATH));
                    payload.setDefaultNotificationPreview(payloadObj.optInt(ShortPayloadConstant.TEXTOVERLAY));
                    payload.setNotification_bg_color(payloadObj.optString(ShortPayloadConstant.BGCOLOR));
                    payload.setOfflineCampaign(payloadObj.optString(ShortPayloadConstant.OFFLINE_CAMPAIGN));
                    payload.setExpiryTimerValue(payloadObj.optString(ShortPayloadConstant.EXPIRY_TIMER_VALUE));
                    payload.setMakeStickyNotification(payloadObj.optString(ShortPayloadConstant.MAKE_STICKY_NOTIFICATION));

                    try {
                        if (payload.getRid() != null && !payload.getRid().isEmpty()) {
                            preferenceUtil.setIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN, Util.getValidIdForCampaigns(payload));
                        } else {
                            Log.d(APP_NAME_TAG, "rid null or empty!");
                        }
                        if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                            int campaigns = preferenceUtil.getIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN);
                            if (campaigns == AppConstant.CAMPAIGN_SI || campaigns == AppConstant.CAMPAIGN_SE) {
                                Log.d(AppConstant.APP_NAME_TAG, "Offline campaign");
                            } else {
                                newsHubDBHelper.addNewsHubPayload(payload);
                            }
                        }
                    } catch (Exception e) {
                        Log.d(APP_NAME_TAG, e.toString());
                    }

                } else {
                    String updateDaily = NotificationEventManager.getDailyTime(context);
                    if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                        preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                        NotificationEventManager.handleNotificationError("Payload Error" + payloadObj.optString("t"), payloadObj.toString(), "iz_db_clientside_handle_service", "handleNow()");
                    }
                    return;
                }

                if (iZooto.appContext == null)
                    iZooto.appContext = context;

                final Handler mainHandler = new Handler(Looper.getMainLooper());
                final Runnable myRunnable = () -> {
                    NotificationEventManager.handleImpressionAPI(payload, AppConstant.PUSH_FCM);
                    iZooto.processNotificationReceived(context, payload);
                };

                try {
                    NotificationExecutorService notificationExecutorService = new NotificationExecutorService(context);
                    notificationExecutorService.executeNotification(mainHandler, myRunnable, payload);
                } catch (Exception e) {
                    Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "notificationExecutorService");
                }
            }
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, data.toString(), "payloadData");

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "handleNotification");
        }
    }

    public static void addTag(final List<String> topicName) {
        try {

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
                        FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCM_DEFAULT);
                        if (firebaseApp == null) {
                            FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCM_DEFAULT);
                        }
                    } catch (IllegalStateException ex) {
                        FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCM_DEFAULT);
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
                                    Util.handleExceptionOnce(iZooto.appContext, e.toString(), APP_NAME_TAG, "addTag");

                                }
                            }
                        }
                    }
                    topicApi(AppConstant.ADD_TOPIC, topicList);
                }
            } else {
                Util.handleExceptionOnce(iZooto.appContext, "Topic list should not be  blank", AppConstant.APP_NAME_TAG, "AddTag");
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, "Topic list should not be  blank", AppConstant.APP_NAME_TAG, "AddTag");
        }
    }

    public static void removeTag(final List<String> topicName) {
        try {
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
                        FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCM_DEFAULT);
                        if (firebaseApp == null) {
                            FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCM_DEFAULT);
                        }
                    } catch (IllegalStateException ex) {
                        FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCM_DEFAULT);
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
                                    Util.handleExceptionOnce(iZooto.appContext, "Topic list should not be  blank", AppConstant.APP_NAME_TAG, "RemoveTag");
                                }
                            }
                        }
                    }
                    topicApi(AppConstant.REMOVE_TOPIC, topicList);
                }
            } else {
                Util.handleExceptionOnce(iZooto.appContext, "Topic list should not be  blank", AppConstant.APP_NAME_TAG, "RemoveTag");
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "RemoveTag");
        }
    }

    private static void topicApi(String action, List<String> topic) {
        if (appContext == null)
            return;

        try {
            if (topic.size() > 0) {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                        HashMap<String, List<String>> data = new HashMap<>();
                        data.put(AppConstant.TOPIC, topic);
                        JSONObject jsonObject = new JSONObject(data);
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ACT, action);
                        mapData.put(AppConstant.ET_, AppConstant.USERP_);
                        mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(appContext));
                        mapData.put(AppConstant.VAL, "" + jsonObject);
                        mapData.put(AppConstant.TOKEN, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
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
            Util.handleExceptionOnce(appContext, e.toString(), "topicApi", AppConstant.APP_NAME_TAG);
        }
    }

    private static String getAPI_KEY() {
        try {
            String apiKey = Objects.requireNonNull(FirebaseOptions.fromResource(iZooto.appContext)).getApiKey();
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

    @SuppressLint("NewApi")
    static void lastVisitApi(Context context) {
        if (context == null) {
            return;
        }
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
                mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(appContext));
                mapData.put(AppConstant.VAL, "" + jsonObject);
                mapData.put(AppConstant.ACT, "add");
                mapData.put(AppConstant.ISID_, "1");
                mapData.put(AppConstant.ET_, AppConstant.USERP_);
                RestClient.postRequest(RestClient.LAST_VISIT_URL, mapData, null, new RestClient.ResponseHandler() {
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
                Util.handleExceptionOnce(context, ex.toString(), AppConstant.APP_NAME_TAG, "lastVisitAPI");
            }
        }
    }

    private static String getTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            String currentDate = sdf.format(new Date());
            return currentDate;
        } catch (Exception ex) {
            return "";
        }
    }

    public static iZooto.Builder initialize(Context context, String tokenJson) {
        if (context == null)
            return null;

        try {
            if (tokenJson != null && !tokenJson.isEmpty()) {
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
                } else {
                    Log.e(AppConstant.APP_NAME_TAG, "Given String is Not Valid JSON String");
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "initialize", AppConstant.APP_NAME_TAG);
        }
        return null;
    }

    public static boolean isJSONValid(String targetJson) {
        try {
            new JSONObject(targetJson);
        } catch (Exception ex) {
            try {
                new JSONArray(targetJson);
            } catch (Exception ex1) {
                Util.handleExceptionOnce(iZooto.appContext, ex1.toString(), AppConstant.APP_NAME_TAG, "isJSONValid");
                return false;
            }
        }
        return true;
    }

    private static void sendOfflineDataToServer(Context context) {
        if (context == null)
            return;

        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty()) {
                JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                for (int i = 0; i < jsonArrayOffline.length(); i++) {
                    JSONObject c = jsonArrayOffline.getJSONObject(i);
                    NotificationActionReceiver.notificationClickAPI(context, c.optString("apiURL"), c.optString("cid"), c.optString("rid"), c.optInt("click"), i, "fcm");
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
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "sendOfflineDataToServer");

        }
    }

    public static void createDirectory(Context context) {
        DebugFileManager.createPublicDirectory(context);
    }

    public static void deleteDirectory(Context context) {
        DebugFileManager.deletePublicDirectory(context);
    }

    public static void shareFile(Context context, String name, String emailID) {

        DebugFileManager.shareDebuginfo(context);
    }

    public static void setPluginVersion(String pluginVersion) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            preferenceUtil.setStringData(AppConstant.HYBRID_PLUGIN_VERSION, Objects.requireNonNullElse(pluginVersion, ""));
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, "pluginVersion", APP_NAME_TAG, "setPluginVersion");
        }
    }

    public enum LOG_LEVEL {
        NONE, FATAL, ERROR, WARN, INFO, DEBUG, VERBOSE
    }

    public static void promptForPushNotifications() {
        if (iZooto.appContext == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                Intent intent = new Intent(iZooto.appContext, NotificationPermission.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                iZooto.appContext.startActivity(intent);
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "", "");
            }
        }

    }

    public static void setNewsHub(Context context, RelativeLayout view) {
        if (context == null) {
            return;
        }
        iZooto.isXmlParse = false;
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (view != null) {
                Thread.sleep(activeMode);
                String appId = preferenceUtil.getiZootoID(AppConstant.APPPID);
                if (appId != null && !appId.isEmpty()) {
                    int newsHubStatus = preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_STATUS);
                    int designType = preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_DESIGN_TYPE);
                    if (newsHubStatus == 1) {
                        if (designType == 1) {
                            setFloatingButton(context, view);
                            setNewsHubImpressionApi(context, designType);
                        } else if (designType == 2) {
                            setStickyButton(context, view);
                            setNewsHubImpressionApi(context, designType);
                        } else
                            Log.e(AppConstant.APP_NAME_TAG, "No widget type is defined!");
                    } else {
                        Log.e(AppConstant.APP_NAME_TAG, "NewsHub disabled!");
                    }
                } else {
                    Log.e(AppConstant.APP_NAME_TAG, APP_NAME_TAG + " initialization failed!");
                }

            } else {
                int newsHubStatus = preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_STATUS);
                if (newsHubStatus == 1) {
                    Intent intent = new Intent(context, NewsHubActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    setNewsHubOpenApi(context, 0);
                }
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setNewsHub");
        }

    }

    private static void setNewsHubImpressionApi(Context context, int wt) {

        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            String app_id = preferenceUtil.getiZootoID(AppConstant.APPPID);
            HashMap<String, String> newsHubData = new HashMap<>();
            newsHubData.put(APPPID, app_id);
            newsHubData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
            newsHubData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
            newsHubData.put(ANDROID_ID, Util.getAndroidId(context));
            newsHubData.put(AppConstant.OP, AppConstant.NEWS_HUB_VIEW);
            newsHubData.put(AppConstant.SDK_VER, AppConstant.SDKVERSION);
            newsHubData.put(AppConstant.WIDGET_TYPE, String.valueOf(wt));
            newsHubData.put(AppConstant.MST, "1");

            RestClient.postRequest(RestClient.NEWS_HUB_IMPRESSION_URL, newsHubData, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setNewsHubImpressionApi");

        }


    }

    private static void setNewsHubOpenApi(Context context, int wt) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            String app_id = preferenceUtil.getiZootoID(AppConstant.APPPID);
            HashMap<String, String> newsHubData = new HashMap<>();
            newsHubData.put(APPPID, app_id);
            newsHubData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
            newsHubData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
            newsHubData.put(ANDROID_ID, Util.getAndroidId(context));
            newsHubData.put(AppConstant.OP, AppConstant.OPEN);
            newsHubData.put(AppConstant.SDK_VER, AppConstant.SDKVERSION);
            newsHubData.put(AppConstant.WIDGET_TYPE, String.valueOf(wt));
            newsHubData.put(AppConstant.MST, "1");

            RestClient.postRequest(RestClient.NEWS_HUB_OPEN_URL, newsHubData, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setNewsHubOpenApi");

        }


    }

    public static void setNewsHub(RelativeLayout view, String jsonString) {
        final Activity context = iZooto.newsHubContext;
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (jsonString != null && !jsonString.isEmpty()) {
                JSONObject jsonObject = new JSONObject(jsonString);
                fetchNewsHubData(context, jsonObject.optString(AppConstant.JSON_NEWS_HUB));
                preferenceUtil.setBooleanData(AppConstant.SET_JSON_NEWS_HUB, true);
                setFloatingButton(context, view);
            } else {
                Log.e("setNewsHub", "Your json string is null");
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setNewsHub");
        }

    }

    private static void fetchNewsHubData(Context context, String newsHubJsonData) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (newsHubJsonData != null && !newsHubJsonData.isEmpty()) {
                JSONObject jsonObject = new JSONObject(newsHubJsonData);
                preferenceUtil.setIntData(AppConstant.JSON_NEWS_HUB_DESIGN_TYPE, jsonObject.optInt("designType"));
                preferenceUtil.setIntData(AppConstant.JSON_NEWS_HUB_STATUS, jsonObject.optInt("status"));
                preferenceUtil.setBooleanData(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN, jsonObject.optBoolean("isFullScreen"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_COLOR, jsonObject.optString("mainColor"));
                preferenceUtil.setIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE, jsonObject.optInt("iconType"));
                preferenceUtil.setBooleanData(AppConstant.JSON_NEWS_HUB_IS_DESCRIPTION, jsonObject.optBoolean("isDescription"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_TITLE, jsonObject.optString("title"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR, jsonObject.optString("titleColor"));
                preferenceUtil.setBooleanData(AppConstant.JSON_NEWS_HUB_BRANDING, jsonObject.optBoolean("branding"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_WIDGET, jsonObject.optString("widget"));
                preferenceUtil.setStringData(AppConstant.JSON_NEWS_HUB_FALLBACK_IMAGE_URL, jsonObject.optString("fallbackImageURL"));
                preferenceUtil.setPlacement(AppConstant.JSON_NEWS_HUB_PLACEMENT, jsonObject.getJSONArray("placement"));

            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "fetchNewsHubData");

        }

    }

    private static void setFloatingButton(Context context, RelativeLayout view) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                setHybridFloatingButton(context, view);
            } else {
                @SuppressLint("InflateParams")
                View itemView = LayoutInflater.from(context).inflate(R.layout.news_hub_floating_button, null, false);
                FloatingActionButton floatingActionButton = null;
                try {
                    JSONArray jsonArray = null;
                    String placement = preferenceUtil.getPlacement(AppConstant.JSON_NEWS_HUB_PLACEMENT);
                    if (placement != null && !placement.isEmpty()) {
                        jsonArray = new JSONArray(placement);

                        if (((int) jsonArray.get(0)) == 1 && ((int) jsonArray.get(1)) == 0) {
                            floatingActionButton = itemView.findViewById(R.id.nh_floating_bleft);
                            floatingActionButton.setScaleType(ImageView.ScaleType.CENTER);
                            floatingActionButton.setVisibility(View.VISIBLE);
                        } else if (((int) jsonArray.get(0)) == 1 && ((int) jsonArray.get(1)) == 1) {
                            floatingActionButton = itemView.findViewById(R.id.nh_floating_bright);
                            floatingActionButton.setScaleType(ImageView.ScaleType.CENTER);
                            floatingActionButton.setVisibility(View.VISIBLE);
                        } else if (((int) jsonArray.get(0)) == 0 && ((int) jsonArray.get(1)) == 0) {
                            floatingActionButton = itemView.findViewById(R.id.nh_floating_tleft);
                            floatingActionButton.setScaleType(ImageView.ScaleType.CENTER);
                            floatingActionButton.setVisibility(View.VISIBLE);
                        } else if (((int) jsonArray.get(0)) == 0 && ((int) jsonArray.get(1)) == 1) {
                            floatingActionButton = itemView.findViewById(R.id.nh_floating_tright);
                            floatingActionButton.setScaleType(ImageView.ScaleType.CENTER);
                            floatingActionButton.setVisibility(View.VISIBLE);
                        } else {
                            floatingActionButton = itemView.findViewById(R.id.nh_floating_bright);
                            floatingActionButton.setScaleType(ImageView.ScaleType.CENTER);
                            floatingActionButton.setVisibility(View.VISIBLE);
                        }

                    } else {
                        floatingActionButton = itemView.findViewById(R.id.nh_floating_bright);
                        floatingActionButton.setScaleType(ImageView.ScaleType.CENTER);
                    }


                    changeFloatingActionDynamically(context, floatingActionButton);
                } catch (Exception e) {
                    if (!preferenceUtil.getBoolean("setFloatingButton")) {
                        preferenceUtil.setBooleanData("setFloatingButton", true);
                        Util.setException(context, e.toString(), AppConstant.APP_NAME_TAG, "setFloatingButton");
                    }
                }
                if (floatingActionButton != null) {
                    floatingActionButton.setOnClickListener(view1 -> {
                        boolean isFullScreen = preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN);
                        if (isFullScreen) {
                            Intent intent = new Intent(context, NewsHubActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } else {
                            NewsHubAlert newsHubAlert = new NewsHubAlert();
                            newsHubAlert.showAlertData((Activity) context);
                        }
                        setNewsHubOpenApi(context, 1);
                    });
                }

                view.addView(itemView);// floating button

            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setFloatingButton");

        }
    }

    private static void setHybridFloatingButton(Context context, RelativeLayout view) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            @SuppressLint("InflateParams")
            View itemView = LayoutInflater.from(context).inflate(R.layout.news_hub_hybrid_floating_button, null, false);
            FrameLayout floatingActionButton = null;
            try {
                JSONArray jsonArray = null;
                String placement = preferenceUtil.getPlacement(AppConstant.JSON_NEWS_HUB_PLACEMENT);
                if (placement != null && !placement.isEmpty()) {
                    jsonArray = new JSONArray(placement);

                    if (((int) jsonArray.get(0)) == 1 && ((int) jsonArray.get(1)) == 0) {
                        floatingActionButton = itemView.findViewById(R.id.nh_hy_floating_bleft);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    } else if (((int) jsonArray.get(0)) == 1 && ((int) jsonArray.get(1)) == 1) {
                        floatingActionButton = itemView.findViewById(R.id.nh_hy_floating_bright);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    } else if (((int) jsonArray.get(0)) == 0 && ((int) jsonArray.get(1)) == 0) {
                        floatingActionButton = itemView.findViewById(R.id.nh_hy_floating_tleft);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    } else if (((int) jsonArray.get(0)) == 0 && ((int) jsonArray.get(1)) == 1) {
                        floatingActionButton = itemView.findViewById(R.id.nh_hy_floating_tright);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    floatingActionButton = itemView.findViewById(R.id.nh_hy_floating_bright);
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
                changeHybridFloatingActionDynamically(context, itemView);
            } catch (Exception e) {
                if (!preferenceUtil.getBoolean("setHybridFloatingButton")) {
                    preferenceUtil.setBooleanData("setHybridFloatingButton", true);
                    Util.setException(context, e.toString(), AppConstant.APP_NAME_TAG, "setHybridFloatingButton");
                }
            }
            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(view1 -> {
                    boolean isFullScreen = preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN);
                    if (isFullScreen) {
                        Intent intent = new Intent(context, NewsHubActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        NewsHubAlert newsHubAlert = new NewsHubAlert();
                        newsHubAlert.showAlertData((Activity) context);
                    }

                    setNewsHubOpenApi(context, 1);
                });
            }

            view.addView(itemView);// floating button
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setHybridFloatingButton");

        }
    }


    @SuppressLint({"NewApi"})
    private static void changeHybridFloatingActionDynamically(Context context, View itemView) {
        if (context == null || itemView == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            FrameLayout frameLayoutBLeft = itemView.findViewById(R.id.nh_hy_floating_bleft);
            FrameLayout frameLayoutBright = itemView.findViewById(R.id.nh_hy_floating_bright);
            FrameLayout frameLayoutTLeft = itemView.findViewById(R.id.nh_hy_floating_tleft);
            FrameLayout frameLayoutTRight = itemView.findViewById(R.id.nh_hy_floating_tright);
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
                frameLayoutBLeft.setBackgroundTintList(ColorStateList.valueOf(color));
                frameLayoutBright.setBackgroundTintList(ColorStateList.valueOf(color));
                frameLayoutTLeft.setBackgroundTintList(ColorStateList.valueOf(color));
                frameLayoutTRight.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            ImageView iconBLeft = itemView.findViewById(R.id.nh_floating_icon_bleft);
            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
                case 2:
                    iconBLeft.setImageResource(R.drawable.ic_iz_bell_ring);
                    iconBLeft.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    iconBLeft.setImageResource(R.drawable.ic_iz_lighting);
                    iconBLeft.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    iconBLeft.setImageResource(R.drawable.ic_iz_shout_out);
                    iconBLeft.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    iconBLeft.setImageResource(R.drawable.ic_iz_megaphone);
                    iconBLeft.setColorFilter(Color.WHITE);
                    break;
                default:
                    iconBLeft.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    iconBLeft.setColorFilter(Color.WHITE);
                    break;
            }

            ImageView iconBright = itemView.findViewById(R.id.nh_floating_icon_bright);
            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
                case 2:
                    iconBright.setImageResource(R.drawable.ic_iz_bell_ring);
                    iconBright.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    iconBright.setImageResource(R.drawable.ic_iz_lighting);
                    iconBright.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    iconBright.setImageResource(R.drawable.ic_iz_shout_out);
                    iconBright.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    iconBright.setImageResource(R.drawable.ic_iz_megaphone);
                    iconBright.setColorFilter(Color.WHITE);
                    break;
                default:
                    iconBright.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    iconBright.setColorFilter(Color.WHITE);
                    break;
            }

            ImageView iconTLeft = itemView.findViewById(R.id.nh_floating_icon_tleft);
            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
                case 2:
                    iconTLeft.setImageResource(R.drawable.ic_iz_bell_ring);
                    iconTLeft.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    iconTLeft.setImageResource(R.drawable.ic_iz_lighting);
                    iconTLeft.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    iconTLeft.setImageResource(R.drawable.ic_iz_shout_out);
                    iconTLeft.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    iconTLeft.setImageResource(R.drawable.ic_iz_megaphone);
                    iconTLeft.setColorFilter(Color.WHITE);
                    break;
                default:
                    iconTLeft.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    iconTLeft.setColorFilter(Color.WHITE);
                    break;
            }

            ImageView iconTRight = itemView.findViewById(R.id.nh_floating_icon_tright);
            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
                case 2:
                    iconTRight.setImageResource(R.drawable.ic_iz_bell_ring);
                    iconTRight.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    iconTRight.setImageResource(R.drawable.ic_iz_lighting);
                    iconTRight.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    iconTRight.setImageResource(R.drawable.ic_iz_shout_out);
                    iconTRight.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    iconTRight.setImageResource(R.drawable.ic_iz_megaphone);
                    iconTRight.setColorFilter(Color.WHITE);
                    break;
                default:
                    iconTRight.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    iconTRight.setColorFilter(Color.WHITE);
                    break;
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "changeHybridFloatingActionDynamically");

        }

    }

    private static void changeFloatingActionDynamically(Context context, FloatingActionButton floatingActionButton) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {
                case 2:
                    floatingActionButton.setImageResource(R.drawable.ic_iz_bell_ring);
                    floatingActionButton.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    floatingActionButton.setImageResource(R.drawable.ic_iz_lighting);
                    floatingActionButton.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    floatingActionButton.setImageResource(R.drawable.ic_iz_shout_out);
                    floatingActionButton.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    floatingActionButton.setImageResource(R.drawable.ic_iz_megaphone);
                    floatingActionButton.setColorFilter(Color.WHITE);
                    break;
                default:
                    floatingActionButton.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    floatingActionButton.setColorFilter(Color.WHITE);
                    break;
            }
        } catch (Exception e) {

            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "changeFloatingActionDynamically");

        }

    }

    private static void setStickyButton(Context context, RelativeLayout view) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        @SuppressLint("InflateParams")
        View itemView = LayoutInflater.from(context).inflate(R.layout.nh_sticky_bar, null, false);
        changeDynamicStickyBar(context, itemView);
        FrameLayout stickyBarRight = itemView.findViewById(R.id.sticky_barRight);
        FrameLayout stickyBarLeft = itemView.findViewById(R.id.sticky_barLeft);

        try {
            JSONArray jsonArray = null;
            String placement = preferenceUtil.getPlacement(AppConstant.JSON_NEWS_HUB_PLACEMENT);
            if (placement != null && !placement.isEmpty()) {
                jsonArray = new JSONArray(placement);

                if (((int) jsonArray.get(0)) == 1 && ((int) jsonArray.get(1)) == 0) {
                    stickyBarLeft.setVisibility(View.VISIBLE);
                    stickyBarRight.setVisibility(View.GONE);

                    stickyBarLeft.setOnClickListener(v -> {
                        boolean isFullScreen = preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN);
                        if (isFullScreen) {
                            Intent intent = new Intent(context, NewsHubActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } else {
                            NewsHubAlert newsHubAlert = new NewsHubAlert();
                            newsHubAlert.showAlertData((Activity) context);
                        }
                        setNewsHubOpenApi(context, 2);
                    });
                } else if (((int) jsonArray.get(0)) == 1 && ((int) jsonArray.get(1)) == 1) {

                    stickyBarLeft.setVisibility(View.GONE);
                    stickyBarRight.setVisibility(View.VISIBLE);

                    stickyBarRight.setOnClickListener(v -> {
                        boolean isFullScreen = preferenceUtil.getBoolean(AppConstant.JSON_NEWS_HUB_IS_FULL_SCREEN);
                        if (isFullScreen) {
                            Intent intent = new Intent(context, NewsHubActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } else {
                            NewsHubAlert newsHubAlert = new NewsHubAlert();
                            newsHubAlert.showAlertData((Activity) context);
                        }
                        setNewsHubOpenApi(context, 2);
                    });
                }

            } else {
                stickyBarLeft.setVisibility(View.GONE);
                stickyBarRight.setVisibility(View.VISIBLE);
            }

            view.addView(itemView);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "setStickyButton");

        }


    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    private static void changeDynamicStickyBar(Context context, View itemView) {

        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            FrameLayout frameLayout = itemView.findViewById(R.id.sticky_barLeft);
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
                frameLayout.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            ImageView icon_newHub = itemView.findViewById(R.id.newsHub_icon);
            TextView title = itemView.findViewById(R.id.news_hub_title);

            String textTitle = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE);
            String textColor = preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_TITLE_COLOR);
            if (textColor != null && !textColor.isEmpty()) {
                title.setTextColor(Color.parseColor(Util.getColorCode(textColor)));
            } else {
                title.setTextColor(Color.WHITE);
            }
            if (textTitle != null && !textTitle.isEmpty()) {
                int headerLength = textTitle.length();
                if (headerLength > 20) {
                    title.setText(textTitle.substring(0, 20));
                } else {
                    title.setText(textTitle);
                }
            } else {
                title.setText("News Hub");
            }

            ViewTreeObserver vto1 = frameLayout.getViewTreeObserver();
            vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    RelativeLayout mainScreen = itemView.findViewById(R.id.mainLayout);
                    int layoutHeight = frameLayout.getMeasuredHeight();
                    int mainHeight = mainScreen.getMeasuredHeight();
                    if (textTitle != null && textTitle.length() < 20) {
                        frameLayout.setY((mainHeight / 2f) - (layoutHeight * 1.9f));
                    } else {
                        frameLayout.setY((mainHeight / 2f) - (layoutHeight * 1.4f));
                    }
                }
            });

            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {

                case 2:
                    icon_newHub.setImageResource(R.drawable.ic_iz_bell_ring);
                    icon_newHub.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    icon_newHub.setImageResource(R.drawable.ic_iz_lighting);
                    icon_newHub.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    icon_newHub.setImageResource(R.drawable.ic_iz_shout_out);
                    icon_newHub.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    icon_newHub.setImageResource(R.drawable.ic_iz_megaphone);
                    icon_newHub.setColorFilter(Color.WHITE);
                    break;
                default:
                    icon_newHub.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    icon_newHub.setColorFilter(Color.WHITE);
                    break;
            }
            FrameLayout frameLayout1 = itemView.findViewById(R.id.sticky_barRight);

            ViewTreeObserver vto = frameLayout1.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    frameLayout1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    RelativeLayout mainScreen = itemView.findViewById(R.id.mainLayout);
                    int layoutHeight = frameLayout1.getMeasuredHeight();
                    int mainWidth = mainScreen.getMeasuredWidth();
                    int mainHeight = mainScreen.getMeasuredHeight();
                    frameLayout1.setX(mainWidth - layoutHeight);
                    if (textTitle != null && textTitle.length() < 20) {
                        frameLayout1.setY((mainHeight / 2f) - (layoutHeight * 1.9f));
                    } else {
                        frameLayout1.setY((mainHeight / 2f) - (layoutHeight * 1.4f));
                    }

                }
            });
            if (!preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR).isEmpty()) {
                int color = Color.parseColor(preferenceUtil.getStringData(AppConstant.JSON_NEWS_HUB_COLOR));
                frameLayout1.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            ImageView icon_newHub1 = itemView.findViewById(R.id.newsHub_icon1);
            TextView title1 = itemView.findViewById(R.id.news_hub_title1);

            if (textColor != null && !textColor.isEmpty()) {
                title1.setTextColor(Color.parseColor(Util.getColorCode(textColor)));
            } else {
                title1.setTextColor(Color.WHITE);
            }
            if (textTitle != null && !textTitle.isEmpty()) {
                int headerLength = textTitle.length();
                if (headerLength > 20) {
                    title1.setText(textTitle.substring(0, 20));
                } else {
                    title1.setText(textTitle);
                }
            } else {
                title1.setText("News Hub");
            }

            switch (preferenceUtil.getIntData(AppConstant.JSON_NEWS_HUB_ICON_TYPE)) {

                case 2:
                    icon_newHub1.setImageResource(R.drawable.ic_iz_bell_ring);
                    icon_newHub1.setColorFilter(Color.WHITE);
                    break;
                case 3:
                    icon_newHub1.setImageResource(R.drawable.ic_iz_lighting);
                    icon_newHub1.setColorFilter(Color.WHITE);
                    break;
                case 4:
                    icon_newHub1.setImageResource(R.drawable.ic_iz_shout_out);
                    icon_newHub1.setColorFilter(Color.WHITE);
                    break;
                case 5:
                    icon_newHub1.setImageResource(R.drawable.ic_iz_megaphone);
                    icon_newHub1.setColorFilter(Color.WHITE);
                    break;
                default:
                    icon_newHub1.setImageResource(R.drawable.ic_iz_defualt_newshub);
                    icon_newHub1.setColorFilter(Color.WHITE);
                    break;
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.APP_NAME_TAG, "changeDynamicStickyBar");

        }

    }

    private static void setNewsHubActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        newsHubContext = activity;
    }

    public static void navigateToSettings(Activity activity) {
        try {
            if (activity == null) {
                return;
            }
            Intent settingsIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, Util.getPackageName(activity));
            }
            activity.startActivity(settingsIntent);
        } catch (Exception ex) {
            Util.handleExceptionOnce(activity, ex.toString(), AppConstant.APP_NAME_TAG, "navigateToSettings");
        }
    }

    public static void setNotificationChannelName(String channelName) {
        if (iZooto.appContext == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        if (channelName != null && !channelName.isEmpty()) {
            preferenceUtil.setStringData(AppConstant.iZ_STORE_CHANNEL_NAME, channelName);
        } else {
            preferenceUtil.setStringData(AppConstant.iZ_STORE_CHANNEL_NAME, Util.getApplicationName(iZooto.appContext) + " Notification");
        }
    }

    // Notification Feed API Response
    public static String getNotificationFeed(boolean isPagination) {
        final Context context = iZooto.appContext;
        try {
            if (context == null) {
                Log.d(AppConstant.APP_NAME_TAG, "Context is null. Unable to fetch notifications feed data.");
                return AppConstant.IZ_NO_MORE_DATA;
            }

            if (!Utilities.hasUserRegistered(context)) {
                return AppConstant.IZ_ERROR_MESSAGE;
            }
            return NotificationFeedManager.returnFeedResponse(context, isPagination);

        } catch (Exception ex) {
            return AppConstant.IZ_NO_MORE_DATA;
        }
    }

    // Sign-In with Google One Tap (Credential Manager API)
    public static void requestOneTapActivity(Context context, OneTapCallback callback) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (context != null && serverClientId != null && !serverClientId.trim().isEmpty()) {
                        OneTapSignInManager.manageSignInRequest(context, serverClientId, callback);
                    } else {
                        Log.d(AppConstant.APP_NAME_TAG, "server-client-id should not be null or empty!");
                    }
                }
            }, 2000);

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "requestOneTapActivity");

        }
    }

    // Generalized method to get User details (OneTap Feature)

    /**
     * @param context   Pass the current object
     * @param email     Pass the email id
     * @param firstName Pass the firstName
     * @param lastName  Pass the last Name
     */
    public static void syncUserDetailsEmail(Context context, String email, String firstName, String lastName) {
        try {
            if (context != null && email != null && !email.trim().isEmpty()) {
                OneTapSignInManager.syncUserDetails(context, email, firstName, lastName);
            } else {
                Log.d(AppConstant.APP_NAME_TAG, "context and email should not be null or empty");
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), AppConstant.APP_NAME_TAG, "syncUserDetails");

        }
    }


    /**
     * @param context   Current object
     * @param channelId Pass the channel id ,its working above android 8
     * @param senderId  Fetch the sender id from google_service.json file
     */
    static void checkNotificationPermission(Context context, String channelId, String senderId) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);

            if (Util.areNotificationsEnabled(context, channelId)) {
                if (hms_appId != null && !hms_appId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Huawei") && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_HUAWEI_TOKEN)) {
                    initHmsService(appContext);
                }
                if (senderId != null && !senderId.isEmpty()) {
                    init(context, senderId);
                } else {
                    Lg.e(AppConstant.APP_NAME_TAG, appContext.getString(R.string.something_wrong_with_fcm_sender_id));
                }
            } else {
                FCMTokenGenerator generator = new FCMTokenGenerator();
                if (senderId != null && !senderId.isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                    generator.removeDeviceAddress(context, senderId);
                }
                boolean promptEnable = PreferenceUtil.getInstance(context).getBoolean("isPrompt");
                if (promptEnable) {
                    promptForPushNotifications();
                }
                Log.e(APP_NAME_TAG, "Notification permission disabled!");

            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), APP_NAME_TAG, "checkNotificationPermission");
        }

    }

    public static void enablePulse(Context context, CoordinatorLayout layout, Boolean shouldShowProgressBar) {
        if (context == null || layout == null) {
            Log.i(APP_NAME_TAG, "Cannot proceed: Context or layout is null");
            return;
        }

        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    String status = preferenceUtil.getStringData(AppConstant.PW_STATUS);
                    String pURL = iZooto.pw_Url;
                    if (!Utilities.hasUserPid(context) || Utilities.isNullOrEmpty(pURL) || !status.equals("1")) {
                        return;
                    }

                    if (!Utilities.isNullOrEmpty(PulseURLManager.encodedURL)) {
                        pURL = pURL + "&article=" + PulseURLManager.encodedURL;
                    }

                    PWInterface pwInterface = PulseManager.INSTANCE.getInstance();
                    pwInterface.addConfiguration(context, pURL);
                    pwInterface.createWebView(layout, shouldShowProgressBar, iZooto.pulseTitle, iZooto.titleEnable, iZooto.titleColor, iZooto.titleMargin, iZooto.titleSize, iZooto.titlePosition, pURL + "&df=0");
                    registerPulseActivityName(context);

                } catch (Exception e) {
                    Util.handleExceptionOnce(context, e.toString(), APP_NAME_TAG, "enablePulse");
                }
            }, 1000);
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), APP_NAME_TAG, "enablePulse");
        }
    }

    // overloading the pulse feature
    public static void enablePulse(Context context, ScrollView scrollViewId, LinearLayout layout, Boolean shouldShowProgressBar) {
        if (context == null && layout == null && scrollViewId == null) {
            Log.i(APP_NAME_TAG, "Cannot proceed: Context or layout is null");
            return;
        }

        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    String status = preferenceUtil.getStringData(AppConstant.PW_STATUS);
                    String pURL = iZooto.pw_Url;
                    if (!Utilities.hasUserPid(context) || Utilities.isNullOrEmpty(pURL) || !status.equals("1")) {
                        return;
                    }

                    if (!Utilities.isNullOrEmpty(PulseURLManager.encodedURL)) {
                        pURL = pURL + "&article=" + PulseURLManager.encodedURL;
                    }
                    PWInterface pwInterface = PulseManager.INSTANCE.getInstance();
                    pwInterface.addConfiguration(context, pURL);
                    pwInterface.createWebView(scrollViewId, layout, shouldShowProgressBar, iZooto.pulseTitle, iZooto.titleEnable, iZooto.titleColor, iZooto.titleMargin, iZooto.titleSize, iZooto.titlePosition, pURL + "&df=1");
                    registerPulseActivityName(context);

                } catch (Exception e) {
                    Util.handleExceptionOnce(context, e.toString(), APP_NAME_TAG, "enablePulse");
                }
            }, 1000);
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), APP_NAME_TAG, "enablePulse");
        }
    }

    private static void registerPulseActivityName(Context context) {
        if (context == null) {
            return;
        }
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (context instanceof Activity) {
                String activityName = context.getClass().getName();
                preferenceUtil.setStringData(AppConstant.PW_ACTIVITY_NAME, activityName);
            } else {
                preferenceUtil.setStringData(AppConstant.PW_ACTIVITY_NAME, "");
            }
        } catch (Exception ex) {
            preferenceUtil.setStringData(AppConstant.PW_ACTIVITY_NAME, "");
            Util.handleExceptionOnce(context, ex.toString(), APP_NAME_TAG, "registerPulseActivityName");
        }
    }

    /*
    context = passing the current object
    csId = comscore publisherId
 */
    private static void checkCamScoreSDK(Context context, String csId) {
        if (context == null || csId == null) {
            return;
        }
        try {
            if (!Utilities.isNullOrEmpty(csId)) {
                enableAnalyticsWithComScore(context, csId, true);
            } else {
                Log.i(APP_NAME_TAG, AppConstant.CHECK_COM_ID);
            }
        } catch (Exception ex) {
            Log.e(APP_NAME_TAG, AppConstant.CHECK_COM_SDK);
        }
    }

    /*
      context - pass the current object
      publisherId = pass the comscore publisher Id
      isDebug = true /false
    */
    static void enableAnalyticsWithComScore(Context context, String publisherId, boolean isDebug) {
        try {
            if (context == null)
                return;

            if (Util.hasComScoreLibrary()) {
                PublisherConfiguration configuration = new PublisherConfiguration.Builder()
                        .publisherId(publisherId)
                        // .persistentLabels(labels)
                        .build();
                Analytics.getConfiguration().addClient(configuration);
                Analytics.getConfiguration().setUsagePropertiesAutoUpdateMode(
                        UsagePropertiesAutoUpdateMode.FOREGROUND_AND_BACKGROUND);
                Analytics.getConfiguration().enableChildDirectedApplicationMode();
                if (isDebug) {
                    Analytics.getConfiguration().enableImplementationValidationMode();
                }
                Analytics.start(context);
            } else {
                Lg.d(AppConstant.APP_NAME_TAG, AppConstant.CHECK_COM_SDK);
            }
        } catch (Exception ex) {
            Log.e(AppConstant.APP_NAME_TAG, ex.toString());
        }
    }
}
