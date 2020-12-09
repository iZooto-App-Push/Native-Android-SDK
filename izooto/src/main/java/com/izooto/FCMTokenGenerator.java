package com.izooto;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

/* Developed By Amit Gupta */
public class FCMTokenGenerator implements TokenGenerator {

    private FirebaseApp firebaseApp;
    private String token = "";


    @Override
    public void getToken(final Context context, final String senderId, final String apiKey, final String appId, final TokenGenerationHandler callback) {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    initFireBaseApp(senderId);

                    FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance(firebaseApp);

                    token = instanceId.getToken(senderId, FirebaseMessaging.INSTANCE_ID_SCOPE);

                    if (token != null && !token.isEmpty()) {
                        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                        if (!token.equals(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN))) {
                            preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                            instanceId.deleteToken(senderId,FirebaseMessaging.INSTANCE_ID_SCOPE);
                            token = instanceId.getToken(senderId, FirebaseMessaging.INSTANCE_ID_SCOPE);

                        }
                        preferenceUtil.setStringData(AppConstant.FCM_DEVICE_TOKEN, token);
                        if (callback != null)
                            callback.complete(token);
                    } else {
                        callback.failure(AppConstant.FCMERROR);
                    }

                } catch (Exception e) {
                    if (callback != null)
                        callback.failure(e.getMessage());
                }
            }
        }).start();

    }
    public   void initFireBaseApp(final String senderId) {
        if (firebaseApp != null)
            return;



        if(get_Project_ID()!="" && get_Project_ID()!="" && getAPI_KEY()!="" && senderId!="") {
            FirebaseOptions firebaseOptions =
                    new FirebaseOptions.Builder()
                            .setGcmSenderId(senderId) //senderID
                            .setApplicationId(get_App_ID()) //application ID
                            .setApiKey(getAPI_KEY()) //Application Key
                            .setProjectId(get_Project_ID()) //Project ID
                            .build();
            firebaseApp = FirebaseApp.initializeApp(iZooto.appContext, firebaseOptions, AppConstant.SDKNAME);
            Lg.d(AppConstant.FCMNAME, firebaseApp.getName());
        }
        else
        {
            Log.e(AppConstant.APP_NAME_TAG,"Missing google-service.json file");
        }
    }
    private static String  getAPI_KEY()
    {
        try {
            String apiKey = FirebaseOptions.fromResource(iZooto.appContext).getApiKey();
            if (apiKey != null)
                return apiKey;
            //return new String(Base64.decode(FCM_DEFAULT_API_KEY_BASE64, Base64.DEFAULT));
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
