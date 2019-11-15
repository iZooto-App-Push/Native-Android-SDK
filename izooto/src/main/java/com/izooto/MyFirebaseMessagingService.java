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

import android.os.Handler;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    Payload payload = null;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
      //  Log.d(TAG, "From: " + remoteMessage.getFrom()+remoteMessage.getData());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + data);
            handleNow(remoteMessage.getData());

        }
        else
        {
            Log.d(TAG, "Message data payload: " + "no notification");
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    // [END receive_message]


    private void handleNow(final Map<String, String> data) {
        Log.d(TAG, "Short lived task is done.");
        try {
            //JSONObject payloadObj = new JSONObject(data.get("campaignDetails"));
            JSONObject payloadObj = new JSONObject(data);

           // if (payloadObj.optLong("created_on") > PreferenceUtil.getInstance(this).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                payload = new Payload();
                payload.setFetchURL(payloadObj.optString("fetchURL"));
                payload.setKey(payloadObj.optString("key"));
                payload.setId(payloadObj.optString("id"));
                payload.setRid(payloadObj.optString("rid"));
                payload.setLink(payloadObj.optString("link"));
                payload.setTitle(payloadObj.optString("title"));
                payload.setMessage(payloadObj.optString("message"));
                payload.setIcon(payloadObj.optString("icon"));
                payload.setReqInt(payloadObj.optInt("reqInt"));
                payload.setTag(payloadObj.optString("tag"));
                payload.setBanner(payloadObj.optString("banner"));
                payload.setAct_num(payloadObj.optInt("act_num"));
                payload.setAct1name(payloadObj.optString("act1name"));
                payload.setAct1link(payloadObj.optString("act1link"));
                payload.setAct2name(payloadObj.optString("act2name"));
                payload.setAct2link(payloadObj.optString("act2link"));
                payload.setInapp(payloadObj.optInt("inapp"));
                payload.setTrayicon(payloadObj.optString("trayicon"));
                payload.setSmallIconAccentColor(payloadObj.optString("iconcolor"));
                payload.setSound(payloadObj.optString("sound"));
                payload.setLedColor(payloadObj.optString("ledColor"));
                payload.setLockScreenVisibility(payloadObj.optInt("visibility"));
                payload.setGroupKey(payloadObj.getString("gKey"));
                payload.setGroupMessage(payloadObj.optString("gMessage"));
                payload.setFromProjectNumber(payloadObj.optString("projectNumber"));
                payload.setCollapseId(payloadObj.optString("collapseID"));
                payload.setPriority(payloadObj.optInt("priority"));
                payload.setRawPayload(payloadObj.optString("rawData"));



           // } else return;
        } catch (Exception e) {
            e.printStackTrace();
            Lg.d(TAG,e.toString());
        }


        if (iZooto.appContext == null)
            iZooto.appContext = this;
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