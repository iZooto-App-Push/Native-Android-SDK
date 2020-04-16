package com.izooto;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.izooto.AppConstant.TAG;

public class iZooto {

    static Context appContext;
    private static String senderId;
    public static int mIzooToAppId;
    public static Builder mBuilder;
    public static int icon;
    private static Payload payload;
    public static boolean checkMethodOverrideOrNot;

    public static void setSenderId(String senderId) {
        iZooto.senderId = senderId;
    }
    public static void setIzooToAppId(int izooToAppId) {
        mIzooToAppId = izooToAppId;
    }
    public static iZooto.Builder initialize(Context context) {
        return new iZooto.Builder(context);
    }
    private static void init(Builder builder) {
        final Context context = builder.mContext;
        appContext = context.getApplicationContext();
        mBuilder = builder;
        builder.mContext = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            if (bundle != null) {
                if (bundle.containsKey(AppConstant.IZOOTO_APP_ID)) {
                    mIzooToAppId = bundle.getInt(AppConstant.IZOOTO_APP_ID);
                }
                if (mIzooToAppId == 0) {
                    Lg.e(AppConstant.APP_NAME_TAG, AppConstant.MISSINGID);
                }
                else {
                    Lg.i(AppConstant.APP_NAME_TAG+AppConstant.APPID, mIzooToAppId + "");

                    RestClient.get(AppConstant.GOOGLE_JSON_URL + mIzooToAppId + ".js", new RestClient.ResponseHandler() {

                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            try {
                                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(Util.decrypt(AppConstant.SECRETKEY, response)));
                                senderId = jsonObject.getString(AppConstant.SENDERID);
                                String appId = jsonObject.getString(AppConstant.APPID);
                                String apiKey = jsonObject.getString(AppConstant.APIKEY);
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

        initFireBaseApp(senderId, apiKey, appId);
        FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
        fcmTokenGenerator.getToken(context, senderId, apiKey, appId, new TokenGenerator.TokenGenerationHandler() {

            @Override
            public void complete(String id) {
                Util util = new Util();
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (util.isInitializationValid()) {
                    Lg.i(AppConstant.APP_NAME_TAG, AppConstant.DEVICETOKEN  + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    registerToken();
                }
            }

            @Override
            public void failure(String errormsg) {
                Lg.e(AppConstant.APP_NAME_TAG, errormsg);
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
            //FirebaseApp.initializeApp(appContext, firebaseOptions, "[DEFAULT]");

        }
    }

    public static void registerToken() {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
            String appVersion = Util.getSDKVersion();
            String api_url = "app.php?s=" + AppConstant.STYPE + "&pid=" + mIzooToAppId + "&btype=" + AppConstant.BTYPE + "&dtype=" + AppConstant.DTYPE + "&tz=" + System.currentTimeMillis() + "&bver=" + appVersion +
                    "&os=" + AppConstant.SDKOS + "&allowed=" + AppConstant.ALLOWED + "&bKey=" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&check="+Util.getSDKVersion();

            try {
                String deviceName = URLEncoder.encode(Util.getDeviceName(), "utf-8");
                String osVersion = URLEncoder.encode(Build.VERSION.RELEASE, "utf-8");
                api_url += "&osVersion=" + osVersion + "&deviceName=" + deviceName;
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

    public static void processNotificationReceived(Payload payload) {
        if(payload!=null) {
            NotificationEventManager.manageNotification(payload);
        }

    }

    public static void notificationView(Payload payload)
    {
        if(payload!=null)
        {
            if(mBuilder!=null && mBuilder.mNotificationHelper!=null)
            {
                mBuilder.mNotificationHelper.onNotificationReceived(payload);
            }
        }
    }
    public static void notificationActionHandler(String data)
    {
        if(mBuilder!=null && mBuilder.mNotificationHelper!=null)
        {
            mBuilder.mNotificationHelper.onNotificationOpened(data);
        }

    }


    public static class Builder {
        Context mContext;
        private TokenReceivedListener mTokenReceivedListener;
        private NotificationHelperListener mNotificationHelper;
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



        public void build() {
            iZooto.init(this);
        }

    }
    // send events  with event name and event data
    public static void addEvent(String eventName, HashMap<String,String> data) {
        String database = data.toString();
        if (database != null && eventName != null) {
            if (!(eventName.length() > 32)) {
                HashMap<String, String>  newListEvent= new HashMap<String, String>();
                for (Map.Entry<String,String> refineEntry : data.entrySet()) {
                    if (!refineEntry.getKey().isEmpty() && refineEntry.getKey()!=null){
                        String newKey = refineEntry.getKey().toLowerCase();
                        newListEvent.put(newKey,refineEntry.getValue());
                    }
                }
                addEventAPI(eventName,newListEvent);
            }else {
                String newEventName = eventName.substring(0,32);
                HashMap<String, String>  newListEvent= new HashMap<String, String>();
                for (Map.Entry<String,String> refineEntry : data.entrySet()) {
                    if (!refineEntry.getKey().isEmpty() && refineEntry.getKey()!=null){
                        String newKey = refineEntry.getKey().toLowerCase();
                        newListEvent.put(newKey,refineEntry.getValue());
                    }
                }
                addEventAPI(newEventName,newListEvent);
            }
        }
    }
    public static void addEventAPI(String eventName,HashMap<String,String> data){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String encodeData = "";
        eventName = eventName.replaceAll(" ", "_");
        HashMap<String, String> validationHashMap = checkValidationEvent(data, 1);
        if (validationHashMap.size() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(validationHashMap.toString());
                encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            String api_url = "?pid=" + mIzooToAppId + "&act=" + eventName +
                    "&et=evt" + "&bKey=" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&val=" + encodeData;//URLEncoder.encode(database, "UTF-8");
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
    }
    public static HashMap<String, String> checkValidationEvent(HashMap<String, String> data,int index){
        HashMap<String, String>  newList= new HashMap<String, String>();
        for (HashMap.Entry<String,String> array:data.entrySet()) {
            if (index<=16&&array.getKey().length()<=32&&array.getValue().length()<=32){
                newList.put(array.getKey(),array.getValue());
                index ++;
            }else if (index<=16&&array.getKey().length()>32&&array.getValue().length()>32){
                String newKey = array.getKey().substring(0,32);
                String newValue = array.getValue().substring(0,32);
                newList.put(newKey,newValue);
                index ++;
            }else if (index<=16&&array.getKey().length()<=32&&array.getValue().length()>32){
                String newValue = array.getValue().substring(0,32);
                newList.put(array.getKey(),newValue);
                index ++;
            }else if (index<=16&&array.getKey().length()>32&&array.getValue().length()<=32){
                String newKey = array.getKey().substring(0,32);
                newList.put(newKey,array.getValue());
                index ++;
            }/*else {
             *//* if (index>16){
                int newindex = index-16;
                array.getValue().substring(0,32);
            }*//*
        }*/
        }
        return newList;
    }






    // send user properties
    public static void addUserProperty(HashMap<String,String> object)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String database = object.toString();
        String encodeData = "";
        if (database != null) {
            HashMap<String, String>  newListUserProfile = new HashMap<String, String>();
            for (Map.Entry<String,String> refineEntry : object.entrySet()) {
                if (!refineEntry.getKey().isEmpty() && refineEntry.getKey()!=null){
                    String newKey = refineEntry.getKey().toLowerCase();
                    newListUserProfile.put(newKey,refineEntry.getValue());
                }
            }
            HashMap<String, String> filterHash = checkValidationUserProfile(newListUserProfile, 1);
            if (filterHash.size() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(filterHash.toString());
                    encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String api_url = "?pid=" + mIzooToAppId + "&act=add" +
                        "&et=userp" + "&bKey=" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&val=" + encodeData;//URLEncoder.encode(database, "UTF-8");
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
    public static HashMap<String, String> checkValidationUserProfile(HashMap<String, String> data,int index){
        HashMap<String, String>  newList= new HashMap<String, String>();
        for (HashMap.Entry<String,String> array:data.entrySet()) {
            if (index<=64&&array.getKey().length()<=32&&array.getValue().length()<=32){
                newList.put(array.getKey(),array.getValue());
                index ++;
            }else if (index<=64&&array.getKey().length()>32&&array.getValue().length()>32){
                String newKey = array.getKey().substring(0,32);
                String newValue = array.getValue().substring(0,32);
                newList.put(newKey,newValue);
                index ++;
            }else if (index<=64&&array.getKey().length()<=32&&array.getValue().length()>32){
                String newValue = array.getValue().substring(0,32);
                newList.put(array.getKey(),newValue);
                index ++;
            }else if (index<=64&&array.getKey().length()>32&&array.getValue().length()<=32){
                String newKey = array.getKey().substring(0,32);
                newList.put(newKey,array.getValue());
                index ++;
            }
        }
        return newList;
    }



    public static void setIcon(int icon1)
    {
        icon=icon1;
    }
    public static void iZootoHandleNotification(final Map<String,String> data)
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
                    payload.setAct1name(payloadObj.optString(AppConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(AppConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(AppConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(AppConstant.ACT1ID));
                    payload.setAct2name(payloadObj.optString(AppConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(AppConstant.ACT2LINK));
                    payload.setAct2icon(payloadObj.optString(AppConstant.ACT2ICON));
                    payload.setAct1ID(payloadObj.optString(AppConstant.ACT2ID));
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

                    payload.setAct1name(payloadObj.optString(ShortpayloadConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(ShortpayloadConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(ShortpayloadConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(ShortpayloadConstant.ACT1ID));

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



        //  if (iZooto.appContext == null)
        //   iZooto.appContext = this;
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                iZooto.processNotificationReceived(payload);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }
}
