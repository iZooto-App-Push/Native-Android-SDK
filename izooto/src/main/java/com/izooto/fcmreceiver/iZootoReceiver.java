package com.izooto.fcmreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.izooto.AppConstant;
import com.izooto.PreferenceUtil;
import com.izooto.ShortPayloadConstant;
import com.izooto.Util;
import com.izooto.iZooto;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class iZootoReceiver extends BroadcastReceiver {
    private static final String FCM_RECEIVE_ACTION = "com.google.android.c2dm.intent.RECEIVE";
    private static final String FCM_TYPE = "gcm";
    private static final String MESSAGE_TYPE_EXTRA_KEY = "msg_type";
    String tagName = "FCMReceiver";
    public static final String SHARED_PREFS_NAME = "processed_notifications";
    private static final long PROCESSED_NOTIFICATION_OUT = 5L * 60L * 60L * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.e("Push Notification","First");
            processIntent(context, intent);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "iZootoReceiver", "onReceive");
        }

    }

    private void processIntent(Context context, Intent intent) {
        try {
            if (context == null || intent.getExtras() == null) {
                return;
            }
            if (!Util.hasWorkManagerDependency()) {
                return;
            }
            Log.e("Push Notification","Second");

            iZooto.appContext = context;
            Bundle bundle = intent.getExtras();
            String notificationId = null;
            if (bundle == null || "google.com/iid".equals(bundle.getString("from"))) {
                return;
            }
            try {
                if (bundle.containsKey(AppConstant.GLOBAL)) {
                    Log.e("Push Notification","Three");

                    JSONObject jsonObject = new JSONObject(Util.setExtrasAsJson(context, bundle));
                    String globalKey = jsonObject.optString(AppConstant.GLOBAL);
                    JSONObject payload = new JSONObject(globalKey);
                    if (payload.has(ShortPayloadConstant.CREATEDON)) {
                        notificationId = payload.optString(ShortPayloadConstant.CREATEDON);
                    }
                } else {
                    notificationId = bundle.getString(ShortPayloadConstant.CREATEDON);
                }
            } catch (Exception e) {
                Log.e("Push Notification","Four");

                Util.handleExceptionOnce(context, e.toString(), tagName, "onMessageReceived");
            }

            if (Util.getDataKey(bundle) || Util.getNotificationKey(bundle)) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                String finalNotificationId = notificationId;
                executorService.execute(() -> {
                    try {
                        Log.e("Push Notification","Five");

                        processNotification(context, bundle, finalNotificationId);
                    } catch (Exception e) {
                        Util.handleExceptionOnce(context, e.toString(), tagName, "processIntent");
                    }
                });
                executorService.shutdown();
                setAbort();
            } else {
                Log.d(AppConstant.TAG, ShortPayloadConstant.PAYLOAD_FORMAT);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), tagName, "onMessageReceived");
        } finally {
            setSuccessfulResultCode();
        }

        if (!isFCMMessage(intent)) {
            setSuccessfulResultCode();
        }
    }

    private void processNotification(Context context, Bundle bundle, String notificationId) {
        if (context == null || bundle == null) {
            return;
        }
        try {
            Log.e("Push Notification","Six");

            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (notificationId == null || checkProcessedNotification(context, notificationId)) {
                if (!preferenceUtil.getBoolean(AppConstant.IZ_TIME_OUT)) {
                    NotificationProcessingListener.processedNotificationId(context, PROCESSED_NOTIFICATION_OUT);
                    preferenceUtil.setBooleanData(AppConstant.IZ_TIME_OUT, true);
                }
                return;
            }
            if (Util.getOsNotificationId(context) != null) {
                bundle.putString(AppConstant.IZ_BEGIN_ENQUEUE_ID, Util.getOsNotificationId(context));
            }
            boolean wmWorkInfoProcessing = NotificationProcessingListener.beginEnqueueWorkProcessing(context, bundle, true);
            if (wmWorkInfoProcessing) {
                storeProcessedNotification(context, notificationId);
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), tagName, "processNotification");
        }
    }

    private void setSuccessfulResultCode() {
        if (isOrderedBroadcast()) {
            setResultCode(Activity.RESULT_OK);
        }
    }

    private void setAbort() {
        if (isOrderedBroadcast()) {
            abortBroadcast();
            setResultCode(Activity.RESULT_OK);
        }
    }

    private static boolean isFCMMessage(Intent intent) {
        try {
            if (FCM_RECEIVE_ACTION.equals(intent.getAction())) {
                String messageType = intent.getStringExtra(MESSAGE_TYPE_EXTRA_KEY);
                return messageType == null || FCM_TYPE.equals(messageType);
            }
            return false;

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "FCMReceiver", "isFCMMessage");
            return false;
        }
    }


    private boolean checkProcessedNotification(Context context, String notificationId) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.contains(notificationId);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), tagName, "checkProcessedNotification");
            return false;
        }
    }

    private void storeProcessedNotification(Context context, String notificationId) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(notificationId, true).apply();
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), tagName, "storeProcessedNotification");
        }
    }

}