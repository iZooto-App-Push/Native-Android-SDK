package com.izooto;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationPreview {
    private static Bitmap notificationIcon, notificationBanner;
   // private static int icon;
    private static  int badgeColor;
    private static int priority,lockScreenVisibility;
    static String channelId;
    static int OBTAINED_VALUES;
    static int FCDTSM = 9;
    static long MILLIS = 1000;
    static long TOTAL_MILLIS;
    static long EQUALS_TIMES;
    static NotificationManager notificationManager;
    static int notificationId;
    static int mColor = 0x33000000;

    static void receiveCustomNotification(final Payload payload){
         if(iZooto.appContext !=null) {
             final Handler handler = new Handler(Looper.getMainLooper());
             final Runnable notificationRunnable = new Runnable() {
                 @SuppressLint("SuspiciousIndentation")
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
                             lastclickIndex = String.valueOf(data.charAt(data.length() - 3));
                             lastViewIndex = String.valueOf(data.charAt(data.length() - 3));
                             lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                             lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                         } else {
                             clickIndex = "0";
                             lastclickIndex = "0";
                             lastViewIndex = "0";
                             lastSeventhIndex = "0";
                             lastNinthIndex = "0";

                         }

                         NotificationEventManager.badgeCountUpdate(payload.getBadgeCount());
                         /* Create a notification default channel name*/
                         PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                         String soundName = preferenceUtil.getSoundName(AppConstant.NOTIFICATION_SOUND_NAME);
                         if(soundName!=null) {
                             if(soundName.isBlank()){
                                 channelId = iZooto.appContext.getString(R.string.channel_id_without_sound);
                             }else{
                                 channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
                             }
                         } else {
                             channelId = iZooto.appContext.getString(R.string.default_channel_id);
                         }
                         NotificationCompat.Builder notificationBuilder = null;
                         Notification summaryNotification = null;
                         Intent intent = null;
                         badgeColor = NotificationEventManager.getBadgeColor(payload.getBadgecolor());
                         lockScreenVisibility = NotificationEventManager.setLockScreenVisibility(payload.getLockScreenVisibility());
                         intent = NotificationEventManager.notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, clickIndex, lastclickIndex, 100, 0);
                         PendingIntent pendingIntent=null;
                         if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
                                 pendingIntent = PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                                         PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                         }
                         else {
                             pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                                     PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                         }

                         /*---------------------------collapsed view----------------------- */
                         RemoteViews collapsedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.layout_custom_notification);
                         collapsedView.setTextViewText(R.id.tv_message, "" + payload.getTitle());
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                             collapsedView.setViewVisibility(R.id.iv_large_icon, View.GONE);
                             collapsedView.setViewVisibility(R.id.rlc2, View.GONE);
                         } else {
                             collapsedView.setViewVisibility(R.id.rlc2, View.VISIBLE);
                             collapsedView.setTextViewText(R.id.tv_display_time, "" + Util.getTimeWithoutDate().toLowerCase());
                             collapsedView.setViewPadding(R.id.rlp, 20, 20, 20, 0);
                             collapsedView.setViewPadding(R.id.llp, 20, 0, 20, 0);
                             if (Util.notificationMode()){
                                 collapsedView.setViewPadding(R.id.tv_message_temp, 0, 0, 0, 0);
                                 collapsedView.setViewPadding(R.id.tv_display_time, 0, 0, 0, 0);
                             }else {
                                 collapsedView.setViewPadding(R.id.tv_message_temp, 8, 0, 0, 0);
                                 collapsedView.setViewPadding(R.id.ll_timer_notification_for_below, 8, 0, 0, 0);
                                 collapsedView.setViewPadding(R.id.tv_display_time, 8, 0, 0, 0);
                             }
                             if (notificationIcon != null)
                                 collapsedView.setImageViewBitmap(R.id.iv_large_icon, notificationIcon);
                             else {
                                 if (iZooto.appContext.getApplicationInfo().icon != 0)
                                     collapsedView.setImageViewResource(R.id.iv_large_icon, iZooto.appContext.getApplicationInfo().icon);
                             }
                         }


                         /*---------------------------expanded view----------------------- */
                         RemoteViews expandedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.layout_custom_notification_expand);
                         expandedView.setTextViewText(R.id.tv_notification_title, "" + payload.getTitle());

                         if (payload.getAct1name().isEmpty() && payload.getAct2name().isEmpty()) {
                             expandedView.setViewVisibility(R.id.ll_button, View.GONE);
                         }
                   // notification large icon
                         if (notificationIcon != null) {
                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                 expandedView.setViewVisibility(R.id.iv_large_icon, View.GONE);
                             } else {
                                 expandedView.setImageViewBitmap(R.id.iv_large_icon, notificationIcon);
                             }
                         }
                         else {
                             if (iZooto.appContext.getApplicationInfo().icon != 0)
                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                                     expandedView.setImageViewResource(R.id.iv_large_icon, 0);
                                 }else {
                                     expandedView.setImageViewResource(R.id.iv_large_icon, iZooto.appContext.getApplicationInfo().icon);
                                 }
                         }
                         // banner image
                             if (notificationBanner != null) {
                                 expandedView.setImageViewBitmap(R.id.iv_banner_ig, notificationBanner);
                             }
                             else {
                                 if (iZooto.bannerImage != 0) {
                                     expandedView.setImageViewResource(R.id.iv_banner_ig, iZooto.bannerImage);
                                 }
                                 else
                                 {
                                     expandedView.setImageViewResource(R.id.iv_banner_ig, iZooto.appContext.getApplicationInfo().icon);
                                 }
                             }

                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                             expandedView.setViewVisibility(R.id.ll_display_time, View.GONE);
                         } else {
                             expandedView.setTextViewText(R.id.tv_display_time, "" + Util.getTimeWithoutDate().toLowerCase());
                             expandedView.setViewVisibility(R.id.ll_display_time, View.VISIBLE);

                         }
                         Uri uri = Util.getSoundUri(iZooto.appContext, soundName);
                         notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId)
                                 .setSmallIcon(getDefaultSmallIconId())
                                 .setContentTitle(payload.getTitle())
                                 .setContentText(payload.getMessage())
                                 .setContentIntent(pendingIntent)
                                 .setVisibility(lockScreenVisibility)
                                 .setCustomContentView(collapsedView)
                                 .setCustomBigContentView(expandedView)
                                 .setAutoCancel(true);
                         /* show the timer notification functionality */
                         if (payload.getExpiryTimerValue() != null && !payload.getExpiryTimerValue().isEmpty()) {
                             try {
                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                     if(!Util.getTimerValue(payload.getCreated_Time(), payload.getExpiryTimerValue()).equals("")) {
                                         if (isPatternMatched(payload.getExpiryTimerValue())) {
                                             OBTAINED_VALUES = Integer.parseInt(Util.getTimerValue(payload.getCreated_Time(), payload.getExpiryTimerValue()));
                                             TOTAL_MILLIS = OBTAINED_VALUES * MILLIS;
                                             EQUALS_TIMES = SystemClock.elapsedRealtime() + TOTAL_MILLIS;
                                             notificationBuilder.setTimeoutAfter(TOTAL_MILLIS);
                                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                 collapsedView.setViewVisibility(R.id.ll_timer_notification_for_below, View.GONE);
                                                 collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.VISIBLE);
                                                 collapsedView.setViewVisibility(R.id.tv_ll, View.VISIBLE);
                                                 expandedView.setViewPadding(R.id.ll_timer_notification, 0,15,0,0);
                                                 if (Util.notificationMode()) {
                                                     collapsedView.setViewPadding(R.id.tv_message_temp, 10, 0, 0, 0);
                                                     collapsedView.setInt(R.id.tv_message_temp, "setGravity", Gravity.START);
                                                     expandedView.setInt(R.id.tv_notification_title, "setGravity", Gravity.START);
                                                 } else {
                                                     collapsedView.setViewPadding(R.id.tv_message_temp, 0, 0, 0, 0);
                                                     collapsedView.setInt(R.id.tv_message_temp, "setGravity", Gravity.START);
                                                 }
                                                 collapsedView.setChronometerCountDown(R.id.tv_notification_timer_, true);
                                                 collapsedView.setChronometer(R.id.tv_notification_timer_, EQUALS_TIMES, ("%tH:%tM:%tS"), true);

                                             } else {
                                                 collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.GONE);
                                                 collapsedView.setViewVisibility(R.id.ll_timer_notification_for_below, View.VISIBLE);
                                                 collapsedView.setChronometerCountDown(R.id.tv_notification_timer_for_below, true);
                                                 collapsedView.setChronometer(R.id.tv_notification_timer_for_below, EQUALS_TIMES, ("%tH:%tM:%tS"), true);
                                             }
                                             expandedView.setViewVisibility(R.id.ll_timer_notification, View.VISIBLE);
                                             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                                                 expandedView.setViewPadding(R.id.ll_timer_notification, 0,20,0,0);
                                             }

                                             expandedView.setChronometerCountDown(R.id.tv_notification_timer, true);
                                             expandedView.setChronometer(R.id.tv_notification_timer, EQUALS_TIMES, ("%tH:%tM:%tS"), true);
                                         }
                                     }
                                     else {
                                         notificationBuilder.setTimeoutAfter(0);
                                         collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.GONE);
                                         expandedView.setViewVisibility(R.id.ll_timer_notification, View.GONE);
                                     }
                                 }else {
                                     expandedView.setViewVisibility(R.id.ll_timer_notification, View.GONE);
                                     collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.GONE);
                                     collapsedView.setViewVisibility(R.id.ll_timer_notification_for_below, View.GONE);
                                 }
                             } catch (Exception e) {
                                 Log.e("Tpn", Objects.requireNonNull(e.getMessage()));
                             }
                         } else {
                             notificationBuilder.setTimeoutAfter(0);
                             collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.GONE);
                             expandedView.setViewVisibility(R.id.ll_timer_notification, View.GONE);
                             collapsedView.setViewVisibility(R.id.ll_timer_notification_for_below, View.GONE);
                         }
                         /* override the  notification functionality */
                         if (payload.getTag() != null && !payload.getTag().isEmpty()) {
                             notificationId = Util.convertStringToDecimal(payload.getTag());
                         } else {
                             notificationId = (int) System.currentTimeMillis();
                         }
                         /* make sticky notification */
                         if (payload.getMakeStickyNotification() != null && !payload.getMakeStickyNotification().isEmpty() && payload.getMakeStickyNotification().equals("1")){
                             try {
                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                     notificationBuilder.setOngoing(Util.enableStickyNotification(payload));
                                     if (payload.getExpiryTimerValue() != null && !payload.getExpiryTimerValue().isEmpty() && payload.getExpiryTimerValue().length() > FCDTSM && OBTAINED_VALUES > FCDTSM) {
                                         collapsedView.setViewVisibility(R.id.tv_dismissed_, View.GONE);
                                         collapsedView.setViewVisibility(R.id.tv_close_icon, View.GONE);
                                     } else {
                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                             if (Util.notificationMode()) {
                                                 collapsedView.setViewPadding(R.id.tv_message_temp, 50, 0, 0, 0);
                                                 collapsedView.setInt(R.id.tv_message_temp, "setGravity", Gravity.START);
                                                 expandedView.setInt(R.id.tv_notification_title, "setGravity", Gravity.START);
                                             } else {
                                                 collapsedView.setViewPadding(R.id.tv_message_temp, 0, 0, 50, 0);
                                                 collapsedView.setInt(R.id.tv_message_temp, "setGravity", Gravity.START);
                                             }
                                             collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.VISIBLE);
                                             collapsedView.setViewVisibility(R.id.tv_close_icon, View.VISIBLE);
                                             collapsedView.setViewVisibility(R.id.tv_ll, View.GONE);
                                         } else {
                                             collapsedView.setViewVisibility(R.id.ll_timer_notification_, View.GONE);
                                             collapsedView.setViewVisibility(R.id.ll_timer_notification_for_below, View.GONE);
                                             collapsedView.setViewVisibility(R.id.tv_dismissed_, View.VISIBLE);
                                         }
                                     }
                                     expandedView.setViewVisibility(R.id.tv_dismissed, View.VISIBLE);
                                     Intent cancelIntent = dismissedNotification(payload, notificationId,3);
                                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                         cancelIntent.setPackage(Util.getPackageName(iZooto.appContext));
                                         pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, notificationId, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                     } else {
                                         pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, notificationId, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                     }
                                     expandedView.setOnClickPendingIntent(R.id.tv_dismissed, pendingIntent);
                                     collapsedView.setOnClickPendingIntent(R.id.tv_dismissed_, pendingIntent);
                                     collapsedView.setOnClickPendingIntent(R.id.tv_close_icon, pendingIntent);
                                 }else {
                                     expandedView.setViewVisibility(R.id.tv_dismissed, View.GONE);
                                     collapsedView.setViewVisibility(R.id.tv_dismissed_, View.GONE);
                                     collapsedView.setViewVisibility(R.id.tv_close_icon, View.GONE);
                                     notificationBuilder.setOngoing(false);
                                 }
                             }catch (Exception e){
                                 Log.e("pv",e.getMessage());
                             }


                         }else {
                             expandedView.setViewVisibility(R.id.tv_dismissed, View.GONE);
                             collapsedView.setViewVisibility(R.id.tv_dismissed_, View.GONE);
                             collapsedView.setViewVisibility(R.id.tv_close_icon, View.GONE);
                             notificationBuilder.setOngoing(false);
                         }
                         try {
                             BigInteger accentColor = Util.getAccentColor();
                             if (accentColor != null)
                                 notificationBuilder.setColor(accentColor.intValue());
                         } catch (Throwable t) {}

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
//                         int notificaitionId;
//                         if (payload.getTag() != null && !payload.getTag().isEmpty())
//                             notificaitionId = Util.convertStringToDecimal(payload.getTag());
//                         else
//                             notificaitionId = (int) System.currentTimeMillis();

                         if (payload.getAct1name() != null && !payload.getAct1name().isEmpty()) {
                             expandedView.setViewVisibility(R.id.tv_btn1, View.VISIBLE);
                             String button1;
                             if (payload.getAct1name().length() > 17) {
                                 button1 = payload.getAct1name().substring(0, 14) + "...";
                             } else {
                                 button1 = payload.getAct1name();
                             }
                             expandedView.setTextViewText(R.id.tv_btn1, "" + button1.replace("~",""));
                             String phone = NotificationEventManager.getPhone(payload.getAct1link());
                             Intent btn1 = cnotificationClick(payload, payload.getAct1link(), payload.getLink(), payload.getAct2link(), phone, clickIndex, lastclickIndex, notificationId, 1);
                             if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
                                 btn1.setPackage(Util.getPackageName(iZooto.appContext));

                                 pendingIntent = PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                             }
                             else
                             {
                                 pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                             }

                             expandedView.setOnClickPendingIntent(R.id.tv_btn1, pendingIntent);

                         }


                         if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                             expandedView.setViewVisibility(R.id.tv_btn2, View.VISIBLE);
                             String button2;
                             if (payload.getAct2name().length() > 17) {
                                 button2 = payload.getAct2name().substring(0, 14) + "...";
                             } else {
                                 button2 = payload.getAct2name();
                             }
                             expandedView.setTextViewText(R.id.tv_btn2, "" + button2.replace("~",""));
                             String phone = NotificationEventManager.getPhone(payload.getAct2link());
                             Intent btn2 = NotificationEventManager.notificationClick(payload, payload.getAct2link(), payload.getLink(), payload.getAct1link(), phone, clickIndex, lastclickIndex, notificationId, 2);

                             if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
                                 btn2.setPackage(Util.getPackageName(iZooto.appContext));

                                 pendingIntent = PendingIntent.getActivity(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                             }
                             else
                             {
                                 pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                             }
                             expandedView.setOnClickPendingIntent(R.id.tv_btn2, pendingIntent);
                         }

//
                         expandedView.setOnClickPendingIntent(R.id.ll_share_notification, shareNotification(payload.getLink()));
//
                         assert notificationManager != null;
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                             NotificationChannel channel;
                             if (payload.getPriority() == 0) {
                                 priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                             } else {
                                 priority = NotificationEventManager.priorityForImportance(payload.getPriority());
                              }
                             if(soundName!= null){
                                 if(soundName.isBlank()){
                                     notificationBuilder.setSilent(true);
                                     channel = new NotificationChannel(channelId, AppConstant.NOTIFICATION_SILENT_CHANNEL, priority);
                                     channel.setSound(null, null);

                                 } else{
                                     notificationBuilder.setSilent(false);
                                     notificationBuilder.setSound(uri);
                                     channel = new NotificationChannel(channelId, Util.getChannelName(iZooto.appContext), priority);
                                     AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                             .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                             .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                             .build();
                                     if (uri != null) {
                                         channel.setSound(uri, audioAttributes);
                                     } else {
                                         channel.setSound(null, null);
                                     }
                                 }
                             }else {
                                 notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                                 channel = new NotificationChannel(channelId, Util.getChannelName(iZooto.appContext), priority);
                             }
                             notificationManager.createNotificationChannel(channel);
                         }
                         if (payload.getTag()!=null && !payload.getTag().isEmpty())
                         {
                             int notifyId = Util.convertStringToDecimal(payload.getTag());
                             notificationManager.notify(notifyId, notificationBuilder.build());
                         }
                         else
                         notificationManager.notify(notificationId, notificationBuilder.build());

                         if (lastViewIndex.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")) {
                             NotificationEventManager.lastViewNotificationApi(payload, lastViewIndex, lastSeventhIndex, lastNinthIndex);
                         }
                         if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK))
                             iZooto.notificationView(payload);
                         else {
                             NotificationEventManager.onReceiveNotificationHybrid(iZooto.appContext, payload);
                             NotificationEventManager.iZootoReceivedPayload = preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY);
                             iZooto.notificationViewHybrid(NotificationEventManager.iZootoReceivedPayload, payload);
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
    static Intent cnotificationClick(Payload payload, String getLink ,String getLink1, String getLink2, String phone, String finalClickIndex, String lastClick, int notificationId, int button){
        String link = getLink;
        String link1 = getLink1;
        String link2 = getLink2;
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
            if (link.contains(AppConstant.ANDROID_TOKEN) || link.contains(AppConstant.DEVICE_ID) || link.contains(AppConstant.R_XIAOMI_TOKEN)|| link.contains(AppConstant.R_HMS_TOKEN) || link.contains(AppConstant.R_FCM_TOKEN)) {
                if(Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                    link = link.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
                if(PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)!=null || PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)!=null) {
                    link = link.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
            }

            if (link1.contains(AppConstant.ANDROID_TOKEN) || link1.contains(AppConstant.DEVICE_ID) || link1.contains(AppConstant.R_XIAOMI_TOKEN)|| link1.contains(AppConstant.R_HMS_TOKEN) || link1.contains(AppConstant.R_FCM_TOKEN)) {
                if(Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                    link1 = link1.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
                if(PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)!=null || PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)!=null) {
                    link1 = link1.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
            }
            if (link2.contains(AppConstant.ANDROID_TOKEN) || link2.contains(AppConstant.DEVICE_ID) || link2.contains(AppConstant.R_XIAOMI_TOKEN)|| link2.contains(AppConstant.R_HMS_TOKEN) || link2.contains(AppConstant.R_FCM_TOKEN)) {
                if(Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                    link2 = link2.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
                if(PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)!=null || PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)!=null) {
                    link2 = link2.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
            }
        } else {
            String notificationLink = payload.getLink();
            notificationLink = getFinalUrl(payload);
        }

        Intent intent=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
                intent = new Intent(iZooto.appContext, TargetActivity.class);
        }
        else {
            intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);
        }
        if(link == null)
            link="";
        intent.putExtra(AppConstant.KEY_WEB_URL, link);
        intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificationId);
        intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
        intent.putExtra(AppConstant.KEY_IN_CID, payload.getId());
        intent.putExtra(AppConstant.KEY_IN_RID, payload.getRid());
        intent.putExtra(AppConstant.KEY_IN_BUTOON, button);
        intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, payload.getAp());
        intent.putExtra(AppConstant.KEY_IN_PHONE, phone);
        intent.putExtra(AppConstant.KEY_IN_ACT1ID, payload.getAct1ID());
        intent.putExtra(AppConstant.KEY_IN_ACT2ID, payload.getAct2ID());
        intent.putExtra(AppConstant.LANDINGURL, link);
        intent.putExtra(AppConstant.ACT1TITLE, payload.getAct1name());
        intent.putExtra(AppConstant.ACT2TITLE, payload.getAct2name());
        intent.putExtra(AppConstant.ACT1URL, link1);
        intent.putExtra(AppConstant.ACT2URL, link2);
        intent.putExtra(AppConstant.CLICKINDEX, finalClickIndex);
        intent.putExtra(AppConstant.LASTCLICKINDEX, lastClick);
        intent.putExtra(AppConstant.PUSH,payload.getPush_type());
        intent.putExtra(AppConstant.CFGFORDOMAIN, payload.getCfg());
        intent.putExtra(AppConstant.IZ_NOTIFICATION_TITLE_KEY_NAME,payload.getTitle());
        return intent;
    }
    private static String getFinalUrl(Payload payload) {
        byte[] data = new byte[0];
        try {
            data = payload.getLink().getBytes(AppConstant.UTF);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedLink = Base64.encodeToString(data, Base64.DEFAULT);
        Uri builtUri = Uri.parse(payload.getLink())
                .buildUpon()
                .appendQueryParameter(AppConstant.URL_ID, payload.getId())
                .appendQueryParameter(AppConstant.URL_CLIENT, payload.getKey())
                .appendQueryParameter(AppConstant.URL_RID, payload.getRid())
                .appendQueryParameter(AppConstant.URL_BKEY_, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN))
                .appendQueryParameter(AppConstant.URL_FRWD___, encodedLink)
                .build();
        return builtUri.toString();
    }
    // notification default icon
    private static int getDefaultSmallIconId() {
        int notificationIcon = getDrawableId("ic_stat_izooto_default");
        if (notificationIcon != 0) {
            return notificationIcon;
        }
        return android.R.drawable.ic_popup_reminder;
    }

    private static int getDrawableId(String name) {
        return iZooto.appContext.getResources().getIdentifier(name, "drawable", iZooto.appContext.getPackageName());
    }
    // handle the notification dismiss for persistent

    static Intent dismissedNotification(Payload payload ,int notificationId, int button){
        Intent intent = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(button == 3){
                intent = new Intent(iZooto.appContext, NotificationDismissedReceiver.class);
            }
            else {
                intent = new Intent(iZooto.appContext, TargetActivity.class);
            }
        }
        else {
            if(button == 3){
                intent = new Intent(iZooto.appContext, NotificationDismissedReceiver.class);
            }else {
                intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);
            }
        }
        intent.putExtra(AppConstant.KEY_WEB_URL, payload.getLink());
        intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificationId);
        intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
        intent.putExtra(AppConstant.KEY_IN_CID, payload.getId());
        intent.putExtra(AppConstant.KEY_IN_RID, payload.getRid());
        intent.putExtra(AppConstant.KEY_IN_BUTOON, button);
        intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, payload.getAp());
        intent.putExtra(AppConstant.KEY_IN_PHONE, "phone");
        intent.putExtra(AppConstant.KEY_IN_ACT1ID, payload.getAct1ID());
        intent.putExtra(AppConstant.KEY_IN_ACT2ID, payload.getAct2ID());
        intent.putExtra(AppConstant.LANDINGURL, payload.getLink());
        intent.putExtra(AppConstant.ACT1TITLE, payload.getAct1name());
        intent.putExtra(AppConstant.ACT2TITLE, payload.getAct2name());
        intent.putExtra(AppConstant.ACT1URL, payload.getAct1link());
        intent.putExtra(AppConstant.ACT2URL, payload.getAct2link());
        intent.putExtra(AppConstant.CLICKINDEX, "finalClickIndex");
        intent.putExtra(AppConstant.LASTCLICKINDEX, "lastClick");
        intent.putExtra(AppConstant.PUSH,payload.getPush_type());
        intent.putExtra(AppConstant.CFGFORDOMAIN, payload.getCfg());

        return intent;
    }
    private static boolean isPatternMatched(String patterns) {
        Pattern length = Pattern.compile(AppConstant.FORMAT);
        Matcher totalNumber = length.matcher(patterns);
        return totalNumber.find();
    }
}
