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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
            FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);

        }
    }

    public static void registerToken() {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
            String appVersion = Util.getSDKVersion();
            String api_url = AppConstant.ADDURL + AppConstant.STYPE + AppConstant.PID + mIzooToAppId + AppConstant.BTYPE_ + AppConstant.BTYPE + AppConstant.DTYPE_ + AppConstant.DTYPE + AppConstant.TIMEZONE + System.currentTimeMillis() + AppConstant.APPVERSION + appVersion +
                    AppConstant.OS + AppConstant.SDKOS + AppConstant.ALLOWED_ + AppConstant.ALLOWED + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.CHECKSDKVERSION+Util.getSDKVersion()+AppConstant.LANGUAGE+Util.getDeviceLanguage();

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


        } else {
            Log.e("Event Name Length", "Event length more than 32...");
        }
    }
    private static HashMap<String, Object> checkValidationEvent(HashMap<String, Object> data,int index){
        HashMap<String, Object>  newList= new HashMap<String, Object>();
        for (HashMap.Entry<String,Object> array:data.entrySet()) {
            if (index<=16){
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String){
                    if (array.getValue().toString().length()>0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 32));
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
    }


    public static void removeUserProperty(HashMap<String,Object> removeData) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String encodeData = "";
        String appVersion = Util.getSDKVersion();
        if (removeData != null && removeData.size() > 0) {
            HashMap<String, Object> newListRemoveUserProperty = new HashMap<String, Object>();
            for (Map.Entry<String, Object> refineEntry : removeData.entrySet()) {
                if (refineEntry.getKey() != null && !refineEntry.getKey().isEmpty()) {
                    String newKey = refineEntry.getKey().toLowerCase();
                    newListRemoveUserProperty.put(newKey, refineEntry.getValue());
                }
            }
            if (newListRemoveUserProperty.size() > 0) {
                HashMap<String, Object> filterData = checkValidationUserProfile(newListRemoveUserProperty, 1);
                if (filterData.size() > 0) {
                    try {

                        JSONObject jsonObject = new JSONObject(filterData);
                        encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    String api_url = "?pid=" + mIzooToAppId + "&bKey=" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) +
                            "&btype=" + AppConstant.BTYPE + "&dtype=" + AppConstant.DTYPE + "&bver="+ appVersion +
                            "&pte=" + AppConstant.PTE + "&os=" + AppConstant.SDKOS + "&pt=0" + "&et=userp" + "&ge=1" + "&val="+encodeData;
                    RestClient.postRequest(RestClient.PROPERTIES_URL + api_url, new RestClient.ResponseHandler(){
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


    private static HashMap<String, Object> checkValidationUserProfile(HashMap<String, Object> data,int index){
        HashMap<String, Object>  newList= new HashMap<String, Object>();
        int indexForValue = 1;
        for (HashMap.Entry<String,Object> array:data.entrySet()) {
            if (index<=64){
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String){
                    if (array.getValue().toString().length()>0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 32));
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
                                    String newListValue = ListData.substring(0, Math.min(ListData.length(), 32));
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



    // add user properties


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
