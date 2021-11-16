package com.izooto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class NotificationPreview {
    private static Bitmap notificationIcon, notificationBanner, shareIcon;
    private static int icon;
    private static  int badgeColor;
    private static int priority,lockScreenVisibility;
    public static String iZootoReceivedPayload;

     static void receiveCustomNotification(final Payload payload){
         if(iZooto.appContext !=null) {
             final Handler handler = new Handler(Looper.getMainLooper());
             final Runnable notificationRunnable = new Runnable() {
                 @RequiresApi(api = Build.VERSION_CODES.O)
                 @Override
                 public void run() {

                     if (payload.getTitle().isEmpty())
                         return;

                     String clickIndex = "0";
                     String impressionIndex = "0";
                     String lastclickIndex = "0";
                     String lastViewIndex = "0";
                     String lastSeventhIndex = "0";
                     String lastNinthIndex = "0";

                     try {

                         String data = Util.getIntegerToBinary(payload.getCfg());
                         if (data != null && !data.isEmpty()) {
                             clickIndex = String.valueOf(data.charAt(data.length() - 2));
                             impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                             lastclickIndex = String.valueOf(data.charAt(data.length() - 3));
                             lastViewIndex = String.valueOf(data.charAt(data.length() - 3));
                             lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                             lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                         } else {
                             clickIndex = "0";
                             impressionIndex = "0";
                             lastclickIndex = "0";
                             lastViewIndex = "0";
                             lastSeventhIndex = "0";
                             lastNinthIndex = "0";

                         }

                         NotificationEventManager.badgeCountUpdate(payload.getBadgeCount());


                         String channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
                         NotificationCompat.Builder notificationBuilder = null;
                         Notification summaryNotification = null;
                         int SUMMARY_ID = 0;
                         Intent intent = null;

                         icon = NotificationEventManager.getBadgeIcon(payload.getBadgeicon());
                         badgeColor = NotificationEventManager.getBadgeColor(payload.getBadgecolor());
                         lockScreenVisibility = NotificationEventManager.setLockScreenVisibility(payload.getLockScreenVisibility());

                         intent = NotificationEventManager.notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, clickIndex, lastclickIndex, 100, 0);
                         Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                       // PendingIntent pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                                // PendingIntent.FLAG_ONE_SHOT |PendingIntent.FLAG_IMMUTABLE |PendingIntent.FLAG_UPDATE_CURRENT);
                         PendingIntent pendingIntent=null;
                         if(Build.VERSION.SDK_INT>Build.VERSION_CODES.R) {
                             Log.e("Response","AppVersion");
                             if (Util.isAppInForeground(iZooto.appContext)) {
                                 pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                                         PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                             } else {
                                 Log.e("Response","AppVersion1");

                                 pendingIntent = PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                                         PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                             }
                         }
                         else {
                             Log.e("Response","AppVersion2");

                             pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                                     PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                         }

                         /*---------------------------collapsed view----------------------- */
                         RemoteViews collapsedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.layout_custom_notification);
                         collapsedView.setTextViewText(R.id.tv_message, "" + payload.getTitle());
                         collapsedView.setTextViewText(R.id.tv_display_time, "" + Util.getTimeWithoutDate());
                         boolean isDarkThemeOn = (iZooto.appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
//        if (isDarkThemeOn) {
//            collapsedView.setInt(R.id.linear_layout_collapse, "setBackgroundColor", Color.BLACK);
//            collapsedView.setTextColor(R.id.tv_message, Color.WHITE);
//            collapsedView.setTextColor(R.id.tv_display_time, Color.WHITE);
//        }
                         if (notificationIcon != null)
                             collapsedView.setImageViewBitmap(R.id.iv_large_icon, notificationIcon);
                         else {
                             if (iZooto.appContext.getApplicationInfo().icon != 0)
                                 collapsedView.setImageViewResource(R.id.iv_large_icon, iZooto.appContext.getApplicationInfo().icon);
                         }


                         /*---------------------------expanded view----------------------- */
                         RemoteViews expandedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.layout_custom_notification_expand);
                         expandedView.setTextViewText(R.id.tv_notification_title, "" + payload.getTitle());
                         if (notificationIcon != null)
                             expandedView.setImageViewBitmap(R.id.iv_large_icon, notificationIcon);
                         else {
                             if (iZooto.appContext.getApplicationInfo().icon != 0)
                                 expandedView.setImageViewResource(R.id.iv_large_icon, iZooto.appContext.getApplicationInfo().icon);
                         }
                         if (notificationBanner != null)
                             expandedView.setImageViewBitmap(R.id.iv_banner_ig, notificationBanner);
                         else {
                             if (iZooto.bannerImage != 0)
                                 expandedView.setImageViewResource(R.id.iv_banner_ig, iZooto.bannerImage);
                         }

                         expandedView.setTextViewText(R.id.tv_display_time, "" + Util.getTimeWithoutDate());


                         notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId)
                                 .setSmallIcon(icon)
                                 .setContentTitle(payload.getTitle())
                                 .setContentText(payload.getMessage())
                                 .setContentIntent(pendingIntent)
                                 .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                                 .setSound(defaultSoundUri)
                                 .setVisibility(lockScreenVisibility)
                                 .setCustomContentView(collapsedView)
                                 .setCustomBigContentView(expandedView)
                                 .setAutoCancel(true);


                         if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                             notificationBuilder.setCustomHeadsUpContentView(collapsedView);
                         }

                         if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                             if (payload.getPriority() == 0)
                                 notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                             else {
                                 priority = NotificationEventManager.priorityForLessOreo(payload.getPriority());
                                 notificationBuilder.setPriority(priority);
                             }


                         }


                         if (payload.getLedColor() != null && !payload.getLedColor().isEmpty())
                             notificationBuilder.setColor(Color.parseColor(payload.getLedColor()));

                         NotificationManager notificationManager =
                                 (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
//                int notificaitionId = (int) System.currentTimeMillis();
                         int notificaitionId;
                         if (payload.getTag() != null && !payload.getTag().isEmpty())
                             notificaitionId = Util.convertStringToDecimal(payload.getTag());
                         else
                             notificaitionId = (int) System.currentTimeMillis();

                         if (payload.getAct1name() != null && !payload.getAct1name().isEmpty()) {
                             expandedView.setViewVisibility(R.id.tv_btn1, View.VISIBLE);
                             String button1;
                             if (payload.getAct1name().length() > 17) {
                                 button1 = payload.getAct1name().substring(0, 14) + "...";
                             } else {
                                 button1 = payload.getAct1name();
                             }
                             expandedView.setTextViewText(R.id.tv_btn1, "" + button1);
                             String phone = NotificationEventManager.getPhone(payload.getAct1link());
                             Intent btn1 = NotificationEventManager.notificationClick(payload, payload.getAct1link(), payload.getLink(), payload.getAct2link(), phone, clickIndex, lastclickIndex, notificaitionId, 1);
                             PendingIntent pendingIntent1 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT);
                             expandedView.setOnClickPendingIntent(R.id.tv_btn1, pendingIntent1);
                         }


                         if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                             expandedView.setViewVisibility(R.id.tv_btn2, View.VISIBLE);
                             String button2;
                             if (payload.getAct2name().length() > 17) {
                                 button2 = payload.getAct2name().substring(0, 14) + "...";
                             } else {
                                 button2 = payload.getAct2name();
                             }
                             expandedView.setTextViewText(R.id.tv_btn2, "" + button2);
                             String phone = NotificationEventManager.getPhone(payload.getAct2link());
                             Intent btn2 = NotificationEventManager.notificationClick(payload, payload.getAct2link(), payload.getLink(), payload.getAct1link(), phone, clickIndex, lastclickIndex, notificaitionId, 2);
                             PendingIntent pendingIntent2 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);
                             expandedView.setOnClickPendingIntent(R.id.tv_btn2, pendingIntent2);
                         }

//                if (payload.getSi() == 1){
//                    expandedView.setViewVisibility(R.id.ll_share_notification,0);
//                    if (!payload.getsIcon().isEmpty() && shareIcon != null)
//                        expandedView.setImageViewBitmap(R.id.iv_share_icon, shareIcon);
//                    if (!payload.getsText().isEmpty())
//                        expandedView.setTextViewText(R.id.tv_share_icon, ""+payload.getsText());
//                    Intent intentShareBtn = notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, clickIndex, lastclickIndex, notificaitionId,3);
//                    PendingIntent sendPendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), intentShareBtn, PendingIntent.FLAG_UPDATE_CURRENT);
                         expandedView.setOnClickPendingIntent(R.id.ll_share_notification, shareNotification(payload.getLink()));
//                }
                         assert notificationManager != null;
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                             NotificationChannel channel;
                             if (payload.getPriority() == 0) {
                                 priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                                 channel = new NotificationChannel(channelId,
                                         AppConstant.CHANNEL_NAME, priority);
                             } else {

                                 priority = NotificationEventManager.priorityForImportance(payload.getPriority());
                                 channel = new NotificationChannel(channelId,
                                         AppConstant.CHANNEL_NAME, priority);
                             }

                             //To set custom notification sound

                             if (iZooto.soundID != null) {
                                 priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                                 channel = new NotificationChannel(channelId,
                                         AppConstant.CHANNEL_NAME, priority);
                                 Uri uri = Util.getSoundUri(iZooto.appContext, iZooto.soundID);
                                 if (uri != null)
                                     channel.setSound(uri, null);
                                 else
                                     channel.setSound(null, null);
                             } else {
                                 channel.setSound(null, null);
                             }

                             notificationManager.createNotificationChannel(channel);
                         }

                if (payload.getTag()!=null && !payload.getTag().isEmpty()){
                    int notifyId = Util.convertStringToDecimal(payload.getTag());
                    notificationManager.notify(notifyId, notificationBuilder.build());
                }else
                         notificationManager.notify(notificaitionId, notificationBuilder.build());

                         if (lastViewIndex.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")) {
                             NotificationEventManager.lastViewNotificationApi(payload, lastViewIndex, lastSeventhIndex, lastNinthIndex);
                         }
                         final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                         if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK))
                             iZooto.notificationView(payload);
                         else {
                             NotificationEventManager.onReceiveNotificationHybrid(iZooto.appContext, payload);
                             NotificationEventManager.iZootoReceivedPayload = preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY);
                             iZooto.notificationViewHybrid(iZootoReceivedPayload, payload);
                         }

                         //Set Max notification in tray
                         if (payload.getMaxNotification() != 0)
                             NotificationEventManager.getMaximumNotificationInTray(iZooto.appContext, payload.getMaxNotification());

                     } catch (Exception e) {
                         Util.setException(iZooto.appContext, e.toString(), "receiveCustomNotification", "NotificationCustomView");
                         e.printStackTrace();
                     }

                     notificationBanner = null;
                     notificationIcon = null;
                     shareIcon = null;
                /*link = "";
                link1 = "";
                link2 = "";*/

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
    }

    public static PendingIntent shareNotification(String url) {
        if(url!=null && !url.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, url);
            sendIntent.setType("text/plain");
            return PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100), sendIntent, PendingIntent.FLAG_IMMUTABLE |PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else
        {
            Intent sendIntent = new Intent();
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "no url found here");
            sendIntent.setType("text/plain");
            return PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100), sendIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}
