package com.izooto;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class iZooto {

    static Context appContext;
    private static String senderId, mEncryptionKey;
    public static int mIzooToAppId;
    public static Builder mBuilder;


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
                if (bundle.containsKey(AppConstant.IZOOTO_ENCRYPTION_KEY)) {
                    mEncryptionKey = bundle.getString(AppConstant.IZOOTO_ENCRYPTION_KEY);
                }
                if (bundle.containsKey(AppConstant.IZOOTO_APP_ID)) {
                    mIzooToAppId = 40493;//bundle.getInt(AppConstant.IZOOTO_APP_ID);//40493
                }
                if (mIzooToAppId == 0) {
                    Lg.e(AppConstant.APP_NAME_TAG, "IZooTo App Id is missing.");
                } else if (mEncryptionKey == null || mEncryptionKey.isEmpty()) {
                    Lg.e(AppConstant.APP_NAME_TAG, "IZooTo Encryption key is missing.");
                } else {
                    Lg.i("IZooTo Encryption key: ", mEncryptionKey);
                    Lg.i("IZooTo App Id: ", mIzooToAppId + "");
                    RestClient.get(AppConstant.GOOGLE_JSON_URL + mIzooToAppId + ".js", new RestClient.ResponseHandler() {

                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            try {
                               // Log.e("iZootoResponse", response);
                                JSONObject jsonObject = new JSONObject(Util.decrypt(mEncryptionKey, response));
                                Lg.i("jsonObject: ", jsonObject.toString());
                                senderId = jsonObject.getString("senderId");
                                String appId = jsonObject.getString("appId");
                                String apiKey = jsonObject.getString("apiKey");
                                if (senderId != null && !senderId.isEmpty()) {
                                    init(context, apiKey, appId);
                                } else {
                                    Lg.e(AppConstant.APP_NAME_TAG, appContext.getString(R.string.something_wrong_fcm_sender_id));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } else {
                Lg.e(AppConstant.APP_NAME_TAG, "It seems you forgot to configure izooto_app id or izooto_sender_id property in your app level build.gradle");
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
                    Lg.i(AppConstant.APP_NAME_TAG, "Device Token " + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
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
            FirebaseApp firebaseApp = FirebaseApp.getInstance("[DEFAULT]");
            if (firebaseApp == null) {
                FirebaseApp.initializeApp(appContext, firebaseOptions, "[DEFAULT]");
            }
        } catch (IllegalStateException ex) {
            FirebaseApp.initializeApp(appContext, firebaseOptions, "[DEFAULT]");

        }
    }

    public static void registerToken() {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
            String appVersion = Util.getAppVersion();
            String api_url = "app.php?s=" + 2 + "&pid=" + mIzooToAppId + "&btype=" + 9 + "&dtype=" + 3 + "&tz=" + System.currentTimeMillis() + "&bver=" + appVersion +
                    "&os=" + 4 + "&allowed=" + 1 + "&bKey=" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&check=1.0";
            //mIzooToAppId
            try {
                String deviceName = URLEncoder.encode(Util.getDeviceName(), "utf-8");
                String osVersion = URLEncoder.encode(Build.VERSION.RELEASE, "utf-8");
                api_url += "&osVersion=" + osVersion + "&deviceName=" + deviceName;
            } catch (UnsupportedEncodingException e) {
                Lg.e("error: ", "unsupported encoding exception");
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
                   // Log.e("ResponseGet", "" + statusCode);

                }
            });

        }
    }

    public static void processNotificationReceived(Payload payload) {
        NotificationEventManager.manageNotification(payload);
       // mBuilder.iZootoNotificationMessagereceiver.notificationReceived(payload);


    }
    public static void checkActionType(boolean action)
    {
        Log.e("Hello","Hello");
        sendImpression();
    }

    private static void sendImpression() {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);

        String api_url = "?pid=" + mIzooToAppId   +
                "&cid=9730232" + "&bKey=" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&rid=1531474585288" +"&op=view";
      Log.e("IMPression",RestClient.IMPRESSION_URL+api_url);
        RestClient.postRequest(RestClient.IMPRESSION_URL + api_url, new RestClient.ResponseHandler() {

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


    public static class Builder {
        Context mContext;
        private TokenReceivedListener mTokenReceivedListener;
        private IZootoNotificationMessagereceiver iZootoNotificationMessagereceiver;
        public IZootoViewListener viewListener;

        private Builder(Context context) {
            mContext = context;
        }

        public Builder setTokenReceivedListener(TokenReceivedListener listener) {
            mTokenReceivedListener = listener;
            return this;
        }

        public Builder setMessageListenr(IZootoNotificationMessagereceiver iZootoNotificationMessagereceiver1) {
            iZootoNotificationMessagereceiver = iZootoNotificationMessagereceiver1;
            return this;
        }
        public Builder setViewListener(IZootoViewListener iZootoViewListener)
        {
            viewListener=iZootoViewListener;
            return this;
        }


        public void build() {
            iZooto.init(this);
        }

    }
// send event
    public static void SendEvent(String eventName, Object data) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String database = data.toString();
        String encodeData = "";
        if (database != null && eventName != null) {

            try {

                JSONObject jsonObject = new JSONObject(database);
                encodeData = URLEncoder.encode(jsonObject.toString(), "UTF-8");


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
    // send puser properties
    public static void SetUserProfile(Object object)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        String database = object.toString();
        String encodeData = "";
        if (database != null) {

            try {

                JSONObject jsonObject = new JSONObject(database);
                encodeData = URLEncoder.encode(jsonObject.toString(), "UTF-8");


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
