package com.izooto;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class FCMTokenGenerator implements TokenGenerator {

    private FirebaseApp firebaseApp;
    private String token = "";

    @Override
    public void getToken(final Context context, final String senderId, final String apiKey, final String appId, final TokenGenerationHandler callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initFireBaseApp(senderId, apiKey, appId);
                    final FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance(firebaseApp);
                    token = instanceId.getToken(senderId, FirebaseMessaging.INSTANCE_ID_SCOPE);

                    if (token != null && !token.isEmpty()) {
                        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                        if (!token.equals(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN)))
                            preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                        preferenceUtil.setStringData(AppConstant.FCM_DEVICE_TOKEN, token);
                        if (callback != null)
                            callback.complete(token);
                    } else {
                        callback.failure("Unable to generate FCM token, there may be something wrong with sender id");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.failure(e.getMessage());
                }
            }
        }).start();

    }

    private void initFireBaseApp(final String senderId, final String apiKey, final String appId) {
        if (firebaseApp != null)
            return;

        FirebaseOptions firebaseOptions =
                new FirebaseOptions.Builder()
                        .setGcmSenderId(senderId)
                        .setApplicationId(appId)
                        .setApiKey(apiKey)
                        .build();
        firebaseApp = FirebaseApp.initializeApp(iZooto.appContext, firebaseOptions, "IZOOTO");
        Lg.d("firebase app name: ", firebaseApp.getName());
    }
}
