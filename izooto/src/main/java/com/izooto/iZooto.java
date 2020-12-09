package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.izooto.shortcutbadger.ShortcutBadger;
import com.izooto.shortcutbadger.ShortcutBadgerException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.izooto.AppConstant.TAG;

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
    public static String inAppOption;
    @SuppressLint("StaticFieldLeak")
    static Activity curActivity;
    public static Class<?>  getWebActivity;


    public static void setSenderId(String senderId) {
        iZooto.senderId = senderId;
    }
    private static void setActivity(Activity activity){
        curActivity = activity;
    }
    public static void setIzooToAppId(String izooToAppId) {
        mIzooToAppId = izooToAppId;
    }
    public static iZooto.Builder initialize(Context context) {
        return new iZooto.Builder(context);
    }
    public enum OSInAppDisplayOption {
        None, InAppAlert, Notification
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

                    RestClient.get(AppConstant.GOOGLE_JSON_URL + mIzooToAppId +".dat", new RestClient.ResponseHandler() {
                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            try {
                                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(Util.decrypt(AppConstant.SECRETKEY, response)));
                                senderId =jsonObject.getString(AppConstant.SENDERID);
                                String appId = jsonObject.getString(AppConstant.APPID);
                                String apiKey = jsonObject.getString(AppConstant.APIKEY);
                                mIzooToAppId = jsonObject.getString(AppConstant.APPPID);
                                preferenceUtil.setiZootoID(AppConstant.APPPID,mIzooToAppId);
                                Log.e("JSONObject",jsonObject.toString());
                                if (senderId != null && !senderId.isEmpty()) {
                                    init(context, apiKey, appId);
                                } else {
                                    Lg.e(AppConstant.APP_NAME_TAG, appContext.getString(R.string.something_wrong_fcm_sender_id));
                                }
                            } catch (JSONException e) {
                                Lg.e(AppConstant.APP_NAME_TAG,e.toString());
                            }
                        }
                    });
                }
            } else {
                Lg.e(AppConstant.APP_NAME_TAG, AppConstant.MESSAGE);
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void init(final Context context, String apiKey, String appId) {

        FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
        fcmTokenGenerator.initFireBaseApp(senderId);
        fcmTokenGenerator.getToken(context, senderId, apiKey, appId, new TokenGenerator.TokenGenerationHandler() {

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
                    trackAdvertisingId();

                    if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack())
                        firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);

                }
            }

            @Override
            public void failure(String errorMsg) {
                Lg.e(AppConstant.APP_NAME_TAG, errorMsg);
            }
        });

    }

    private static void trackAdvertisingId(){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        AdvertisingIdClient.getAdvertisingId(appContext, new AdvertisingIdClient.Listener() {
            @Override
            public void onAdvertisingIdClientFinish(AdvertisingIdClient.AdInfo adInfo) {
                String advertisementID;
                advertisementID = adInfo.getId();
                preferenceUtil.setStringData(AppConstant.ADVERTISING_ID,advertisementID);
                invokeFinish(advertisementID,preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));

            }

            @Override
            public void onAdvertisingIdClientFail(Exception exception) {
                invokeFail(new Exception(TAG + " - Error: context null"));
            }
        });
    }

    public static synchronized void idsAvailable(Context context, Listener listener) {
        new iZooto().start(context, listener);
    }

    protected void start(final Context context, final Listener listener) {
        if (listener == null) {
            Log.e(AppConstant.APP_NAME_TAG, "getAdvertisingId - Error: null listener, dropping call");
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

    protected static void invokeFinish(final String adverID, final String registrationID) {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mListener != null) {
                    mListener.idsAvailable(adverID,registrationID);
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



    private static void initFireBaseApp(final String senderId, final String apiKey, final String appId) {

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder().setGcmSenderId(senderId).setApplicationId(appId).setApiKey(apiKey).build();

        try {
            FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCMDEFAULT);
            if (firebaseApp == null) {
                FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
            }
        } catch (IllegalStateException ex) {
            // FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);

        }
    }

    private static void registerToken() {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
            String api_url = AppConstant.ADDURL + AppConstant.STYPE + AppConstant.PID + mIzooToAppId + AppConstant.BTYPE_ + AppConstant.BTYPE + AppConstant.DTYPE_ + AppConstant.DTYPE + AppConstant.TIMEZONE + System.currentTimeMillis() + AppConstant.APPVERSION + Util.getSDKVersion() +
                    AppConstant.OS + AppConstant.SDKOS + AppConstant.ALLOWED_ + AppConstant.ALLOWED + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.CHECKSDKVERSION +Util.getSDKVersion()+AppConstant.LANGUAGE+Util.getDeviceLanguage();

            try {
                String deviceName = URLEncoder.encode(Util.getDeviceName(), AppConstant.UTF);
                String osVersion = URLEncoder.encode(Build.VERSION.RELEASE, AppConstant.UTF);
                api_url += AppConstant.ANDROIDVERSION + osVersion + AppConstant.DEVICENAME + deviceName;
            } catch (UnsupportedEncodingException e) {
                Lg.e(AppConstant.APP_NAME_TAG, AppConstant.UNEXCEPTION);
            }


            RestClient.get(api_url, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(final String response) {

                    super.onSuccess(response);


                    if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mBuilder.mTokenReceivedListener.onTokenReceived(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                            }
                        });

                    }

                    preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, true);
                    preferenceUtil.setLongData(AppConstant.DEVICE_REGISTRATION_TIMESTAMP, System.currentTimeMillis());
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);

                }
            });

        }
        else
        {
            if(mBuilder!=null && mBuilder.mTokenReceivedListener!=null) {
                mBuilder.mTokenReceivedListener.onTokenReceived(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
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


    public static void processNotificationReceived(Payload payload) {
        if(payload!=null) {
            NotificationEventManager.manageNotification(payload);
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
        if(mBuilder!=null && mBuilder.mNotificationHelper!=null)
        {
            mBuilder.mNotificationHelper.onNotificationOpened(data);
        }
        if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
            firebaseAnalyticsTrack.openedEventTrack();
        }
        try {
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)-1);
            ShortcutBadger.applyCountOrThrow(appContext, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static class Builder {
        Context mContext;
        private TokenReceivedListener mTokenReceivedListener;
        private NotificationHelperListener mNotificationHelper;
        OSInAppDisplayOption mDisplayOption;
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

        public Builder inAppNotificationBehaviour(OSInAppDisplayOption displayOption) {
            mDisplayOption = displayOption;
            inAppOption = displayOption.toString();
            return this;
        }


        public Builder unsubscribeWhenNotificationsAreDisabled(boolean set){
            mUnsubscribeWhenNotificationsAreDisabled = set;
            /*if (set){
                areNotificationsEnabledForSubscribedState(mContext);
            }*/
            return this;
        }


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

    private static void getNotificationAPI(Context context,int value){

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        String appVersion = Util.getSDKVersion();
        String api_url = AppConstant.API_PID + preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN)
                + AppConstant.BTYPE_+ AppConstant.BTYPE + AppConstant.DTYPE_ + AppConstant.DTYPE + AppConstant.APPVERSION + appVersion + AppConstant.PTE_ + AppConstant.PTE +
                AppConstant.OS +AppConstant.SDKOS  + AppConstant.PT_+ AppConstant.PT + AppConstant.GE_ + AppConstant.GE + AppConstant.ACTION + value;

        RestClient.postRequest(RestClient.SUBSCRIPTION_API + api_url, new RestClient.ResponseHandler(){
            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);


            }

            @Override
            void onSuccess(String response) {
                super.onSuccess(response);

            }
        });


    }


    public static void setInAppNotificationBehaviour(OSInAppDisplayOption displayOption) {
        inAppOption = displayOption.toString();
    }

    public static void setWebViewActivity(Class<?>  setActivity){
        getWebActivity = setActivity;
    }

    public static void setLandingURL(WebView mWebView) {
        if (mWebView != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            String mUrl = preferenceUtil.getStringData(AppConstant.WEB_LANDING_URL);
            if (mUrl != null) {
                mWebView.loadUrl(mUrl);
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
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String encodeData = "";
        //validation
        HashMap<String, Object> filterEventData = checkValidationEvent(data, 1);
        if (filterEventData.size() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(filterEventData);
                encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                String api_url = AppConstant.API_PID + preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.ACT + eventName +
                        AppConstant.ET_ + "evt" + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.VAL + encodeData;//URLEncoder.encode(database, "UTF-8");

                RestClient.postRequest(RestClient.EVENT_URL + api_url, new RestClient.ResponseHandler() {
                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }

                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                    }
                });
            }
        } else {
            Log.e(AppConstant.APP_NAME_TAG, "Event length more than 32...");
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
    public static void addUserProperty(HashMap<String, Object> object)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String encodeData = "";
        if (object != null&&object.size()>0) {
            HashMap<String, Object>  newListUserProfile = new HashMap<String, Object>();
            for (Map.Entry<String,Object> refineEntry : object.entrySet()) {
                if (refineEntry.getKey()!=null&&!refineEntry.getKey().isEmpty()){
                    String newKey = refineEntry.getKey().toLowerCase();
                    newListUserProfile.put(newKey,refineEntry.getValue());
                }
            }
            if (newListUserProfile.size()>0) {
                HashMap<String, Object> filterUserPropertyData = checkValidationUserProfile(newListUserProfile, 1);
                if (filterUserPropertyData.size() > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(filterUserPropertyData);
                        encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                        String api_url = AppConstant.API_PID + preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.ACT + "add" +
                                AppConstant.ET_ + "userp" + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.VAL + encodeData;//URLEncoder.encode(database, "UTF-8");

                        RestClient.postRequest(RestClient.PROPERTIES_URL + api_url, new RestClient.ResponseHandler() {
                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }

                            @Override
                            void onSuccess(String response) {
                                super.onSuccess(response);
                            }
                        });
                    }
                }
            }
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
    public static void setIcon(int icon1)
    {
        icon=icon1;
    }

    public static void setSubscription(Boolean enable) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        int value = 2;
        if (enable != null) {
            if (enable) {
                value = 0;
            }
            String api_url = AppConstant.API_PID + preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN)
                    + AppConstant.BTYPE_ + AppConstant.BTYPE +AppConstant.DTYPE_ + AppConstant.DTYPE  +AppConstant.APPVERSION + Util.getSDKVersion() + AppConstant.PTE_ + AppConstant.PTE +
                    AppConstant.OS + AppConstant.SDKOS + AppConstant.PT_+ AppConstant.PT + AppConstant.GE_ + AppConstant.GE + AppConstant.ACTION + value;

            RestClient.postRequest(RestClient.SUBSCRIPTION_API + api_url, new RestClient.ResponseHandler() {
                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);


                }

                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);


                }
            });

        }

    }

    public static void setFirebaseAnalytics(boolean isSet){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        preferenceUtil.setBooleanData(AppConstant.FIREBASE_ANALYTICS_TRACK,isSet);
    }

    public static void iZootoHandleNotification(Context context,final Map<String,String> data)
    {
        Log.d(TAG, AppConstant.NOTIFICATIONRECEIVED);

        try {

            if(data.get(AppConstant.CAMPNAME)!=null) {

                JSONObject payloadObj = new JSONObject(data.get(AppConstant.CAMPNAME));
                if (payloadObj.optLong(AppConstant.CREATEDON) > PreferenceUtil.getInstance(iZooto.appContext).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    payload.setFetchURL(payloadObj.optString(AppConstant.FETCHURL));
                    payload.setKey(payloadObj.optString(AppConstant.KEY));
                    payload.setId(payloadObj.optString(AppConstant.ID));
                    payload.setRid(payloadObj.optString(AppConstant.RID));
                    payload.setLink(payloadObj.optString(AppConstant.LINK));
                    payload.setTitle(payloadObj.optString(AppConstant.TITLE));
                    payload.setMessage(payloadObj.optString(AppConstant.NMESSAGE));
                    payload.setIcon(payloadObj.optString(AppConstant.ICON));
                    payload.setReqInt(payloadObj.optInt(AppConstant.REQINT));
                    payload.setTag(payloadObj.optString(AppConstant.TAG));
                    payload.setBanner(payloadObj.optString(AppConstant.BANNER));
                    payload.setAct_num(payloadObj.optInt(AppConstant.ACTNUM));
                    payload.setBadgeicon(payloadObj.optString(AppConstant.BADGE_ICON));
                    payload.setBadgecolor(payloadObj.optString(AppConstant.BADGE_COLOR));
                    payload.setSubTitle(payloadObj.optString(AppConstant.SUBTITLE));
                    payload.setGroup(payloadObj.optInt(AppConstant.GROUP));
                    payload.setBadgeCount(payloadObj.optInt(AppConstant.BADGE_COUNT));

                    // Button 1
                    payload.setAct1name(payloadObj.optString(AppConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(AppConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(AppConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(AppConstant.ACT1ID));
                    // Button 2
                    payload.setAct2name(payloadObj.optString(AppConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(AppConstant.ACT2LINK));
                    payload.setAct2icon(payloadObj.optString(AppConstant.ACT2ICON));
                    payload.setAct2ID(payloadObj.optString(AppConstant.ACT2ID));

                    payload.setInapp(payloadObj.optInt(AppConstant.INAPP));
                    payload.setTrayicon(payloadObj.optString(AppConstant.TARYICON));
                    payload.setSmallIconAccentColor(payloadObj.optString(AppConstant.ICONCOLOR));
                    payload.setSound(payloadObj.optString(AppConstant.SOUND));
                    payload.setLedColor(payloadObj.optString(AppConstant.LEDCOLOR));
                    payload.setLockScreenVisibility(payloadObj.optInt(AppConstant.VISIBILITY));
                    payload.setGroupKey(payloadObj.optString(AppConstant.GKEY));
                    payload.setGroupMessage(payloadObj.optString(AppConstant.GMESSAGE));
                    payload.setFromProjectNumber(payloadObj.optString(AppConstant.PROJECTNUMBER));
                    payload.setCollapseId(payloadObj.optString(AppConstant.COLLAPSEID));
                    payload.setPriority(payloadObj.optInt(AppConstant.PRIORITY));
                    payload.setRawPayload(payloadObj.optString(AppConstant.RAWDATA));
                    payload.setAp(payloadObj.optString(AppConstant.ADDITIONALPARAM));
                    payload.setCfg(payloadObj.optInt(AppConstant.CFG));

                }
                else
                    return;
            }
            else
            {
                JSONObject payloadObj = new JSONObject(data);
                if (payloadObj.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(iZooto.appContext).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP))
                {
                    payload = new Payload();
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

                }
                else
                    return;
            }




            // return;
        } catch (Exception e) {

            e.printStackTrace();
            Lg.d(TAG,e.toString());
        }



        if (iZooto.appContext == null)
            iZooto.appContext = context;
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                iZooto.processNotificationReceived(payload);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    public static void addTag(final List<String> topicName){
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
                        if (Util.isMatchedString(filterTopicName)){
                            try {
                                FirebaseMessaging.getInstance().subscribeToTopic(filterTopicName);
                                preferenceUtil.setStringData(AppConstant.GET_TOPIC_NAME, filterTopicName);
                                topicList.add(filterTopicName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Log.e(TAG, "subscribeTopic: add " + topicList);
                topicApi(AppConstant.ADD_TOPIC, topicList);
            }
        }
    }
    public static void removeTag(final List<String> topicName){
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
    }
    private static void topicApi(String action, List<String> topic){
        if (topic.size() > 0){
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                String encodeData = "";
                try {
                    HashMap<String, List<String>> data = new HashMap<>();
                    data.put(AppConstant.TOPIC, topic);
                    JSONObject jsonObject = new JSONObject(data);
                    encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                String api_url = AppConstant.API_PID + preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.ACT + action +
                        AppConstant.ET_ + "userp" + AppConstant.ANDROID_ID + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.VAL + encodeData;
                RestClient.postRequest(RestClient.PROPERTIES_URL + api_url, new RestClient.ResponseHandler() {
                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                    }
                });
            }
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

}
