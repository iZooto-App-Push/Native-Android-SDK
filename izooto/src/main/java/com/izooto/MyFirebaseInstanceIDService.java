/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izooto;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;


public class MyFirebaseInstanceIDService extends FirebaseMessagingService {//FirebaseInstanceIdService

    private static final String TAG = "MyFirebaseIIDService";
    private int mIzooToAppId;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
//    @Override
//    public void onTokenRefresh() {
//        // Get updated InstanceID token.
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "Refreshed token: " + refreshedToken);
//
//        // If you want to send messages to this application instance or
//        // manage this apps subscriptions on the server side, send the
//        // Instance ID token to your app server.
//        if (!PreferenceUtil.getInstance(this).getBoolean(AppConstant.IS_TOKEN_UPDATED))
//            sendRegistrationToServer(refreshedToken);
//        else if (!PreferenceUtil.getInstance(this).getStringData(AppConstant.FCM_DEVICE_TOKEN).equals(refreshedToken)) {
//            sendRegistrationToServer(refreshedToken);
//        }
//    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
//
//        // If you want to send messages to this application instance or
//        // manage this apps subscriptions on the server side, send the
//        // Instance ID token to your app server.
       if (!PreferenceUtil.getInstance(this).getBoolean(AppConstant.IS_TOKEN_UPDATED))
            sendRegistrationToServer(token);
        else if (!PreferenceUtil.getInstance(this).getStringData(AppConstant.FCM_DEVICE_TOKEN).equals(token)) {
           sendRegistrationToServer(token);
       }
    }

    private void sendRegistrationToServer(String token) {
        try {
            PreferenceUtil.getInstance(this).setStringData(AppConstant.FCM_DEVICE_TOKEN, token);
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            String encryptionKey = "";
            if (bundle != null) {
                if (bundle.containsKey(AppConstant.IZOOTO_ENCRYPTION_KEY)) {
                    encryptionKey = bundle.getString(AppConstant.IZOOTO_ENCRYPTION_KEY);
                }
                if (bundle.containsKey(AppConstant.IZOOTO_APP_ID)) {
                    mIzooToAppId = bundle.getInt(AppConstant.IZOOTO_APP_ID);
                }
                if (mIzooToAppId == 0) {
                    Lg.e(AppConstant.APP_NAME_TAG, "IZooTo App Id is missing.");
                } else if (encryptionKey == null || encryptionKey.isEmpty()) {
                    Lg.e(AppConstant.APP_NAME_TAG, "IZooTo Encryption key is missing.");
                } else {
                    Lg.i("IZooTo Encryption key: ", encryptionKey);
                    Lg.i("IZooTo App Id: ", mIzooToAppId + "");
                    final String finalEncryptionKey = encryptionKey;
                    RestClient.get(AppConstant.GOOGLE_JSON_URL + mIzooToAppId + ".js", new RestClient.ResponseHandler() {
                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            try {
                                JSONObject jsonObject = new JSONObject(Util.decrypt(finalEncryptionKey, response));
                                String senderId = jsonObject.getString("senderId");
//                                String appId = jsonObject.getString("appId");
//                                String apiKey = jsonObject.getString("apiKey");
                                if (senderId != null && !senderId.isEmpty()) {
                                    iZooto.setIzooToAppId(mIzooToAppId);
                                    iZooto.setSenderId(senderId);
                                    iZooto.registerToken();
                                } else
                                    Lg.e(AppConstant.APP_NAME_TAG, getString(R.string.something_wrong_fcm_sender_id));
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
}
