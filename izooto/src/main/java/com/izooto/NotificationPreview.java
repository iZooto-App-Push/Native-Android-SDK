package com.izooto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class NotificationPreview {
    private static Bitmap notificationIcon, notificationBanner;
   // private static int icon;
    private static  int badgeColor;
    private static int priority,lockScreenVisibility;

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


                         String channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
                         NotificationCompat.Builder notificationBuilder = null;
                         Notification summaryNotification = null;
                         Intent intent = null;

                      //   icon = NotificationEventManager.getBadgeIcon(payload.getBadgeicon());
                         badgeColor = NotificationEventManager.getBadgeColor(payload.getBadgecolor());
                         lockScreenVisibility = NotificationEventManager.setLockScreenVisibility(payload.getLockScreenVisibility());

                         intent = NotificationEventManager.notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, clickIndex, lastclickIndex, 100, 0);
                        // Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

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
                         if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)
                         {
                             collapsedView.setViewVisibility(R.id.iv_large_icon,View.GONE);

                         }
                         else {
                             collapsedView.setTextViewText(R.id.tv_display_time, "" + Util.getTimeWithoutDate());

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
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                             if ((payload.getAct1name() == null || payload.getAct1name().isEmpty()) && (payload.getAct2name() == null || payload.getAct2name().isEmpty())) {
                                 expandedView.setViewVisibility(R.id.iz_tv_layout, View.GONE);
                             }
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
                             // Diplay time
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                             expandedView.setViewVisibility(R.id.tv_display_time, View.GONE);
                         }else {
                             expandedView.setTextViewText(R.id.tv_display_time, "" + Util.getTimeWithoutDate());
                         }

                         Uri uri = Util.getSoundUri(iZooto.appContext, iZooto.soundID);

                         notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId)
                                 .setSmallIcon(getDefaultSmallIconId())
                                 .setContentTitle(payload.getTitle())
                                 .setContentText(payload.getMessage())
                                 .setContentIntent(pendingIntent)
                               //  .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                                // .setSound(defaultSoundUri)
                                 .setVisibility(lockScreenVisibility)
                                 .setCustomContentView(collapsedView)
                                 .setCustomBigContentView(expandedView)
                                 .setAutoCancel(true);
                         if (uri != null) {
                             notificationBuilder.setSound(uri);
                         } else {
                             notificationBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000});
                         }
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
                             expandedView.setTextViewText(R.id.tv_btn1, "" + button1.replace("~",""));
                             String phone = NotificationEventManager.getPhone(payload.getAct1link());
                             Intent btn1 = cnotificationClick(payload, payload.getAct1link(), payload.getLink(), payload.getAct2link(), phone, clickIndex, lastclickIndex, notificaitionId, 1);
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
                             Intent btn2 = NotificationEventManager.notificationClick(payload, payload.getAct2link(), payload.getLink(), payload.getAct1link(), phone, clickIndex, lastclickIndex, notificaitionId, 2);

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
                                 channel = new NotificationChannel(channelId,
                                         AppConstant.CHANNEL_NAME, priority);
                             } else {

                                 priority = NotificationEventManager.priorityForImportance(payload.getPriority());
                                 channel = new NotificationChannel(channelId,
                                         AppConstant.CHANNEL_NAME, priority);
                             }


                             if (iZooto.soundID != null) {
                                 priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                                 channel = new NotificationChannel(channelId,
                                         AppConstant.CHANNEL_NAME, priority);
                                 AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                         .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                         .setUsage(AudioAttributes.USAGE_ALARM)
                                         .build();
                               //  Uri uri = Util.getSoundUri(iZooto.appContext, iZooto.soundID);
                                 if (uri != null)
                                     channel.setSound(uri, audioAttributes);
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
}
