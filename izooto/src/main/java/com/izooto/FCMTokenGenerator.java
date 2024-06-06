package com.izooto;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

/* Developed By Amit Gupta */
public class FCMTokenGenerator implements TokenGenerator {
    private FirebaseApp firebaseApp;

    @Override
    public void getToken(final Context context, final String senderId, final TokenGenerationHandler callback) {
        if (context == null) {
            return;
        }

        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_FCM_TOKEN)) {
            if (callback != null)
                callback.complete(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initFireBaseApp(senderId);
                    FirebaseMessaging messageApp = firebaseApp.get(FirebaseMessaging.class);
                    messageApp.getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    try {
                                        if (!task.isSuccessful()) {
                                            Util.setException(context, task.getException().toString() + "Token Generate Failure", "getToken", "FCMTokenGenerator");
                                            return;
                                        }
                                        String token = task.getResult();
                                        if (token != null && !token.isEmpty()) {
                                            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                                            if (!token.equals(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN)) || !AppConstant.SDKVERSION.equals(preferenceUtil.getStringData(AppConstant.CHECK_SDK_UPDATE))
                                                    || !preferenceUtil.getStringData(AppConstant.CHECK_APP_VERSION).equalsIgnoreCase(Util.getAppVersion(context))) {
                                                preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                                                preferenceUtil.setStringData(AppConstant.CHECK_SDK_UPDATE, AppConstant.SDKVERSION);
                                                preferenceUtil.setStringData(AppConstant.CHECK_APP_VERSION, Util.getAppVersion(context));
                                            }
                                            preferenceUtil.setStringData(AppConstant.FCM_DEVICE_TOKEN, token);
                                            if (callback != null)
                                                callback.complete(token);
                                        } else {
                                            callback.failure(AppConstant.FCMERROR);
                                        }

                                    } catch (Exception e) {
                                        Util.setException(context, e.toString(), "FCMTokenGenerator", "getToken");
                                        if (callback != null)
                                            callback.failure(e.getMessage());
                                    }
                                }
                            });

                } catch (Exception e) {
                    Util.handleExceptionOnce(context, e.toString(), "FCMTokenGenerator", "getToken");
                    if (callback != null)
                        callback.failure(e.getMessage());
                }
            }
        }).start();
    }

    void removeDeviceAddress(Context context, String senderId) {
        try {
            initFireBaseApp(senderId);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            FirebaseMessaging messageApp = firebaseApp.get(FirebaseMessaging.class);
            messageApp.deleteToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) != null && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {
                                preferenceUtil.setStringData(AppConstant.FCM_DEVICE_TOKEN, null);
                                firebaseApp.delete();
                            }
                        } else {
                            Log.e("token", "delete failed!");
                        }
                    });
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "FCMTokenGenerator", "removeDeviceAddress");
        }
    }

    public void initFireBaseApp(final String senderId) {
        if (firebaseApp != null)
            return;
        try {
            if (!get_Project_ID().isEmpty() && !getAPI_KEY().isEmpty() && !senderId.isEmpty()) {
                FirebaseOptions firebaseOptions =
                        new FirebaseOptions.Builder()
                                .setGcmSenderId(senderId) //senderID
                                .setApplicationId(get_App_ID()) //application ID
                                .setApiKey(getAPI_KEY()) //Application Key
                                .setProjectId(get_Project_ID()) //Project ID
                                .build();
                firebaseApp = FirebaseApp.initializeApp(iZooto.appContext, firebaseOptions, AppConstant.SDK_NAME);
                Lg.d(AppConstant.FCMNAME, firebaseApp.getName());
            } else {
                Log.v(AppConstant.APP_NAME_TAG, AppConstant.IZ_MISSING_GOOGLE_JSON_SERVICES_FILE);
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "FCMTokenGenerator", "initFireBaseApp");
        }
    }

    // To get default api_key
    private static String getAPI_KEY() {
        try {
            String apiKey = FirebaseOptions.fromResource(iZooto.appContext).getApiKey();
            if (apiKey != null)
                return apiKey;
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    // To get default app_id
    private static String get_App_ID() {
        try {
            String application_id = FirebaseOptions.fromResource(iZooto.appContext).getApplicationId();
            if (application_id != null)
                return application_id;
        } catch (Exception ex) {
            return "";
        }
        return "";
    }

    // To get default project_id
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
}
