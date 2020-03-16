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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONObject;
import java.util.Map;
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "iZooto";
    private  Payload payload = null;
    private String deppLink= null;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getData().size() > 0) {
                Map<String, String> data = remoteMessage.getData();
                Log.d(AppConstant.APP_NAME_TAG, AppConstant.PAYLOAD + remoteMessage.getData());
                handleNow(data);

            }
            if (remoteMessage.getNotification() != null) {
                sendNotification(remoteMessage);
                Log.d(AppConstant.APP_NAME_TAG, AppConstant.PAYLOAD + remoteMessage.getNotification().getBody());
            }
        }
        catch (Exception ex)
        {
            Log.e(AppConstant.FIREBASEEXCEPTION,ex.toString());
        }


    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setAutoCancel(true)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    public   void handleNow(final Map<String, String> data) {

        Log.d(TAG, AppConstant.NOTIFICATIONRECEIVED);
        try {

            JSONObject payloadObj = new JSONObject(data.get(AppConstant.CAMPNAME));
            if (payloadObj.optLong(AppConstant.CREATEDON) > PreferenceUtil.getInstance(this).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
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
                payload.setAct2name(payloadObj.optString(AppConstant.ACT2NAME));
                payload.setAct2link(payloadObj.optString(AppConstant.ACT2LINK));
                payload.setAct2icon(payloadObj.optString(AppConstant.ACT2ICON));
                payload.setInapp(1);
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
                payload.setDeeplink(payloadObj.optString(AppConstant.DEEPLINK));
                payload.setType(payloadObj.optString("type"));
//                if(payloadObj.optString("type").equalsIgnoreCase("type"))
//                {
//                    payload.setEditbox_title(payloadObj.optString("editbox"));
//                    payload.setValidation(payloadObj.optString("validation"));
//                    payload.setDropdown_text(payloadObj.optString("dropdowntext"));
//                    payload.setType_input_to_payload(payloadObj.optString("payloadData"));
//                }

            } else return;
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