package com.izooto;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class NotificationEventManager {
    private static Bitmap notificationIcon, notificationBanner;

    public static void manageNotification(Payload payload) {
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty())
            showNotification(payload);
        else
            processPayload(payload);

    }

    private static void processPayload(final Payload payload) {
        RestClient.get(payload.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        parseJson(payload, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);

            }
        });
    }

    private static void parseJson(Payload payload, JSONObject jsonObject) {
        try {
            payload.setLink(getParsedvalue(jsonObject, payload.getLink()));
            payload.setTitle(getParsedvalue(jsonObject, payload.getTitle()));
            payload.setMessage(getParsedvalue(jsonObject, payload.getMessage()));
            payload.setIcon(getParsedvalue(jsonObject, payload.getIcon()));
            payload.setBanner(getParsedvalue(jsonObject, payload.getBanner()));
            payload.setAct1name(getParsedvalue(jsonObject, payload.getAct1name()));
            payload.setAct1link(getParsedvalue(jsonObject, payload.getAct1link()));
            payload.setAct2name(getParsedvalue(jsonObject, payload.getAct2name()));
            payload.setAct2link(getParsedvalue(jsonObject, payload.getAct2link()));

            showNotification(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String getParsedvalue(JSONObject jsonObject, String sourceString) {
        try {
            if (sourceString.startsWith("~"))
                return sourceString.replace("~", "");
            else {
                if (sourceString.contains(".")) {
                    JSONObject jsonObject1 = null;
                    String[] linkArray = sourceString.split("\\.");
                    for (int i = 0; i < linkArray.length; i++) {
                        if (linkArray[i].contains("[")) {
                            String[] linkArray1 = linkArray[i].split("\\[");
                            if (jsonObject1 == null)
                                jsonObject1 = jsonObject.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                            else
                                jsonObject1 = jsonObject1.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                        } else {
                            return jsonObject1.optString(linkArray[i]);
                        }

                    }
                } else
                    return jsonObject.getString(sourceString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void showNotification(final Payload payload) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable notificationRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String link = payload.getLink();
                String link1 = payload.getAct1link();
                String link2 = payload.getAct2link();
                if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
                    if (link.contains("{BROWSERKEYID}"))
                        link = link.replace("{BROWSERKEYID}", PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    if (link1.contains("{BROWSERKEYID}"))
                        link1 = link1.replace("{BROWSERKEYID}", PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    if (link2.contains("{BROWSERKEYID}"))
                        link2 = link2.replace("{BROWSERKEYID}", PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                } else {
                    link = getFinalUrl(payload);
                }
                String channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
                Notification.Builder notificationBuilder = null;
              //  NotificationCompat.Builder notificationBuilder = null;
                Intent intent = null;

                if (payload.getInapp() == 1)
                    intent = WebViewActivity.createIntent(iZooto.appContext, link);
                else

              //  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(AppConstant.KEY_WEB_URL, link);
                intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, 100);
                intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
                intent.putExtra(AppConstant.KEY_IN_CID,payload.getId());
                intent.putExtra(AppConstant.KEY_IN_RID,payload.getRid());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                        //  PendingIntent pendingIntent2 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);


//                PendingIntent pendingIntent = PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
//                        PendingIntent.FLAG_ONE_SHOT);

                notificationBuilder = new Notification.Builder(iZooto.appContext, channelId)
                        .setContentTitle(payload.getTitle())
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentText(payload.getMessage())
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                      //  .setStyle(new NotificationCompat.BigTextStyle().bigText(payload.getMessage()))
                        .setStyle(new Notification.BigTextStyle().bigText(payload.getMessage()))
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                        .setSound(defaultSoundUri)
                        //.setOngoing(true)
                        .setAutoCancel(true);


                if(payload.getLedColor()!=null && !payload.getLedColor().isEmpty())
                    notificationBuilder.setColor(Color.parseColor(payload.getLedColor()));
                if (notificationIcon != null)
                    notificationBuilder.setLargeIcon(notificationIcon);
                else if (notificationBanner != null)
                    notificationBuilder.setLargeIcon(notificationBanner);
                if (notificationBanner != null)
                    notificationBuilder.setStyle(new Notification.BigPictureStyle()
                            .bigPicture(notificationBanner)
                            .bigLargeIcon(notificationIcon).setSummaryText(payload.getMessage()));
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                    notificationBuilder.setColor(ContextCompat.getColor(iZooto.appContext, R.color.colorPrimary));


                NotificationManager notificationManager =
                        (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                int notificaitionId = (int) System.currentTimeMillis();
                if (payload.getAct1name() != null && !payload
                        .getAct1name().isEmpty()) {
                    Intent btn1 = new Intent(iZooto.appContext, NotificationActionReceiver.class);
//                    btn1.setAction(AppConstant.ACTION_BTN_ONE);
                    btn1.putExtra(AppConstant.KEY_WEB_URL, link1);
                    btn1.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificaitionId);
                    btn1.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
                    btn1.putExtra(AppConstant.KEY_IN_CID,payload.getId());
                    btn1.putExtra(AppConstant.KEY_IN_RID,payload.getRid());

                    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Action action1 =
                            new Notification.Action.Builder(
                                    0, payload.getAct1name(), pendingIntent1
                            ).build();
                    notificationBuilder.addAction(action1);

                }


                if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                    Intent btn2 = new Intent(iZooto.appContext, NotificationActionReceiver.class);
//                    btn2.setAction(AppConstant.ACTION_BTN_TWO);
                    btn2.putExtra(AppConstant.KEY_WEB_URL, link2);
                    btn2.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificaitionId);
                    btn2.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
                    btn2.putExtra(AppConstant.KEY_IN_CID,payload.getId());
                    btn2.putExtra(AppConstant.KEY_IN_RID,payload.getRid());
                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Action action2 =
                            new Notification.Action.Builder(
                                    0, payload.getAct2name(), pendingIntent2
                            ).build();
                    notificationBuilder.addAction(action2);
                }

                assert notificationManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    NotificationChannel channel = new NotificationChannel(channelId,
                            "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                

                notificationManager.notify(notificaitionId, notificationBuilder.build());




                try {

                    String api_url = "?pid=" + iZooto.mIzooToAppId   +
                            "&cid="+payload.getId() + "&bKey=" + PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&rid="+payload.getRid() +"&op=view";

                    RestClient.postRequest(RestClient.IMPRESSION_URL+api_url, new RestClient.ResponseHandler() {


                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            if(payload!=null)
                            iZooto.notificationView(payload);

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                notificationBanner = null;
                notificationIcon = null;
                link = "";
                link1 = "";
                link2 = "";
               // iZooto.notificationClicked();


            }

        };


        new AppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                String smallIcon = payload.getIcon();
                String banner = payload.getBanner();
                try {
                    if (smallIcon != null && !smallIcon.isEmpty())
                        notificationIcon = Util.getBitmapFromURL(smallIcon);
                    if (banner != null && !banner.isEmpty()) {
                        notificationBanner = Util.getBitmapFromURL(banner);

                    }
                    handler.post(notificationRunnable);
                } catch (Exception e) {
                    Lg.e("Error", e.getMessage());
                    e.printStackTrace();
                    handler.post(notificationRunnable);
                }
            }
        });
    }


    private static String getFinalUrl(Payload payload) {
        byte[] data = new byte[0];
        try {
            data = payload.getLink().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedLink = Base64.encodeToString(data, Base64.DEFAULT);
        Uri builtUri = Uri.parse(payload.getLink())
                .buildUpon()
                .appendQueryParameter("id", payload.getId())
                .appendQueryParameter("client", payload.getKey())
                .appendQueryParameter("rid", payload.getRid())
                .appendQueryParameter("bkey", PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN))
                .appendQueryParameter("frwd", encodedLink)
                .build();
        return builtUri.toString();
    }

}
