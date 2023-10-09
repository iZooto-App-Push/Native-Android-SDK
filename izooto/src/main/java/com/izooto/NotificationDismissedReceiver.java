package com.izooto;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    private  static String TAG = "NotificationDismissedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null) {
            try {
                String GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE = "15";
                Intent it = new Intent(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
                context.sendBroadcast(it);
                Bundle extras = intent.getExtras();
                int notificationID = extras.getInt(AppConstant.KEY_NOTIFICITON_ID);
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationID);
                notificationDestroyApi(context, extras);
            }catch(Exception e){
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (!preferenceUtil.getBoolean(AppConstant.IZ_LISTENER_KEY)) {
                    preferenceUtil.setBooleanData(AppConstant.IZ_LISTENER_KEY, true);
                    Util.setException(context, e.toString(), TAG, AppConstant.IZ_LISTENER_ERROR);
                }
            }
        }
    }

    // track the close button clicks
    private static void notificationDestroyApi(Context context, Bundle extras) {
        if (context != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            try {
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty()) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
                        mapData.put(AppConstant.CID_, extras.getString(AppConstant.KEY_IN_CID));
                        mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
                        mapData.put(AppConstant.RID_, extras.getString(AppConstant.KEY_IN_RID));
                        mapData.put(AppConstant.PUSH, extras.getString(AppConstant.PUSH));
                        mapData.put(AppConstant.TP_TYPE, preferenceUtil.getStringData(AppConstant.TP_TYPE));
                        mapData.put(AppConstant.P_OP,AppConstant.DISMISSED);
                        RestClient.postRequest(RestClient.PERSISTENT_NOTIFICATION_DISMISS_URL, mapData, null, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                Log.e("Dismiss Data",mapData.toString());
                            }
                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }
                        });
                        DebugFileManager.createExternalStoragePublic(context,mapData.toString(),AppConstant.IZ_LISTENER_ERROR);
                    }
                }
            } catch (Exception e) {
                if (!preferenceUtil.getBoolean(AppConstant.DISMISSED)){
                    preferenceUtil.setBooleanData(AppConstant.DISMISSED,true);
                    Util.setException(context, e.toString(), TAG, AppConstant.IZ_LISTENER_ERROR);
                }
                DebugFileManager.createExternalStoragePublic(context, TAG + e, "[Log.e]->Exception->");
            }
        }
    }
}