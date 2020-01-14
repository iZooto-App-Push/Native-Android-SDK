package com.izooto;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {

    private String mUrl;
    private int inApp;
    private String rid;
    private  String cid;
    private int btncount;
    private String api_url;


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
        getBundleData(context, intent);
        String appVersion = Util.getAppVersion();
        mUrl.replace("{BROWSERKEYID}", PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
        getBundleData(context, intent);
        try {

            if (btncount!=0) {
                api_url = "?pid=" + iZooto.mIzooToAppId + "&ver=" + appVersion +
                        "&cid=" + cid + "&bKey=" + PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&rid=" + rid + "&op=click&btn=" + btncount;
                Log.e("CliclURL", api_url);
            }
            else
            {
                api_url = "?pid=" + iZooto.mIzooToAppId + "&ver=" + appVersion +
                        "&cid=" + cid + "&bKey=" + PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&rid=" + rid + "&op=click";
                Log.e("CliclURL", api_url);

            }
            RestClient.postRequest(RestClient.NOTIFICATIONCLICK+api_url, new RestClient.ResponseHandler() {


                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }

                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                    iZooto.notificationClicked();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }





        if (inApp == 1)
            WebViewActivity.startActivity(context, mUrl);
        else {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(browserIntent);
            }
            catch (Exception ex)
            {
                Log.e("ex",ex.toString());
            }
        }

    }

    private void getBundleData(Context context, Intent intent) {
        Bundle tempBundle = intent.getExtras();
        if (tempBundle != null) {
            if (tempBundle.containsKey(AppConstant.KEY_WEB_URL))
                mUrl = tempBundle.getString(AppConstant.KEY_WEB_URL);
            if (tempBundle.containsKey(AppConstant.KEY_IN_APP))
                inApp = tempBundle.getInt(AppConstant.KEY_IN_APP);
            if (tempBundle.containsKey(AppConstant.KEY_IN_RID))
                rid = tempBundle.getString(AppConstant.KEY_IN_RID);
            if (tempBundle.containsKey(AppConstant.KEY_IN_CID))
                cid = tempBundle.getString(AppConstant.KEY_IN_CID);
             if(tempBundle.containsKey(AppConstant.KEY_IN_BUTOON))
                 btncount = tempBundle.getInt(AppConstant.KEY_IN_BUTOON);

            if (tempBundle.containsKey(AppConstant.KEY_NOTIFICITON_ID)) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(tempBundle.getInt(AppConstant.KEY_NOTIFICITON_ID));
            }
        }
    }
}
