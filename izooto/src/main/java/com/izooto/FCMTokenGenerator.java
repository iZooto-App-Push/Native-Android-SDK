package com.izooto;

import android.content.Context;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Objects;


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

        new Thread(() -> {
            try {
                initFireBaseApp(senderId);
                FirebaseMessaging messageApp = firebaseApp.get(FirebaseMessaging.class);
                messageApp.getToken()
                        .addOnCompleteListener(task -> {
                            try {
                                if (!task.isSuccessful()) {
                                    return;
                                }
                                String token = task.getResult();
                                if (token != null && !token.isEmpty()) {
                                    PreferenceUtil preferenceUtil1 = PreferenceUtil.getInstance(context);
                                    if (!token.equals(preferenceUtil1.getStringData(AppConstant.FCM_DEVICE_TOKEN)) || !AppConstant.SDKVERSION.equals(preferenceUtil1.getStringData(AppConstant.CHECK_SDK_UPDATE))
                                            || !preferenceUtil1.getStringData(AppConstant.CHECK_APP_VERSION).equalsIgnoreCase(Util.getAppVersion(context))) {
                                        preferenceUtil1.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                                        preferenceUtil1.setStringData(AppConstant.CHECK_SDK_UPDATE, AppConstant.SDKVERSION);
                                        preferenceUtil1.setStringData(AppConstant.CHECK_APP_VERSION, Util.getAppVersion(context));
                                    }
                                    preferenceUtil1.setStringData(AppConstant.FCM_DEVICE_TOKEN, token);
                                    if (callback != null)
                                        callback.complete(token);
                                } else {
                                    callback.failure(AppConstant.FCMERROR);
                                }

                            } catch (Exception e) {
                                Util.handleExceptionOnce(context, e.toString(), "FCMTokenGenerator", "getToken");
                                if (callback != null)
                                    callback.failure(e.getMessage());
                            }
                        });

            } catch (Exception e) {
                Util.handleExceptionOnce(context, e.toString(), "FCMTokenGenerator", "getToken");
                if (callback != null)
                    callback.failure(e.getMessage());
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

    private void initFireBaseApp(final String senderId) {
            if (firebaseApp != null) {
                return;
            }
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

    }

    // To get default api_key
    private static String getAPI_KEY() {
        try {
            return Objects.requireNonNull(FirebaseOptions.fromResource(iZooto.appContext)).getApiKey();
        } catch (Exception e) {
            return "";
        }
    }

    // To get default app_id
    private static String get_App_ID() {
        try {
            return Objects.requireNonNull(FirebaseOptions.fromResource(iZooto.appContext)).getApplicationId();
        } catch (Exception ex) {
            return "";
        }
    }

    // To get default project_id
     static String get_Project_ID() {
        try {
            String project_id = Objects.requireNonNull(FirebaseOptions.fromResource(iZooto.appContext)).getProjectId();
            if (project_id != null)
                return project_id;
        } catch (Exception exception) {
            return "";
        }
        return "";
    }
}
