package com.izooto.fcmreceiver;

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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.izooto.AdMediation;
import com.izooto.AppConstant;
import com.izooto.DebugFileManager;
import com.izooto.NewsHubDBHelper;
import com.izooto.NotificationEventManager;
import com.izooto.NotificationExecutorService;
import com.izooto.Payload;
import com.izooto.PreferenceUtil;
import com.izooto.PushType;
import com.izooto.R;
import com.izooto.RestClient;
import com.izooto.ShortPayloadConstant;
import com.izooto.Util;
import com.izooto.iZooto;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


class NotificationsProcessor {
    private static final String TagName = "NotificationsProcessor";
    private static final String IZ_METHOD_NAME = "handleNow";
    private static final String IZ_ERROR_NAME = "Payload Error";

    static void processNotificationService(Context context, JSONObject data) {
        if (context == null ) {return;}
        try {
            Map<String, String> mapData = new HashMap<>(Util.setJsonAsMap(context, data));
            mapData.put(AppConstant.PUSH_TYPE, AppConstant.FCM_TYPE);
            if (data.has(ShortPayloadConstant.GCM_TITLE)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sendNotification(context, data);
                }
            } else {
                processingNotificationView(context, mapData);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), TagName, "processNotificationService");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void sendNotification(Context context, JSONObject data) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            String channelId = context.getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(android.R.drawable.ic_popup_reminder)
                            .setContentTitle(Objects.requireNonNull(data.optString(ShortPayloadConstant.GCM_TITLE)))
                            .setContentText(Objects.requireNonNull(data.optString(ShortPayloadConstant.GCM_MESSAGE)))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        AppConstant.CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), TagName, "sendNotification");
        }
    }


    /* Handle the iZooto Push Notification Payload*/
    static void processingNotificationView(Context context, final Map<String, String> data) {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        try {
            if (data.get(AppConstant.AD_NETWORK) != null || data.get(AppConstant.GLOBAL) != null || data.get(AppConstant.GLOBAL_PUBLIC_KEY) != null) {
                if (data.get(AppConstant.GLOBAL_PUBLIC_KEY) != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(data.get(AppConstant.GLOBAL)));
                        String urlData = data.get(AppConstant.GLOBAL_PUBLIC_KEY);
                        if (urlData != null && !urlData.isEmpty()) {
                            String cid = jsonObject.optString(ShortPayloadConstant.ID);
                            String rid = jsonObject.optString(ShortPayloadConstant.RID);
                            int cfg = jsonObject.optInt(ShortPayloadConstant.CFG);
                            String cfgData = Util.getIntegerToBinary(cfg);
                            if (!cfgData.isEmpty()) {
                                String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                                if (impIndex.equalsIgnoreCase("1")) {
                                    NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_FCM);
                                }
                            }
                            AdMediation.mediationGPL(context, jsonObject, urlData);
                            preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                        } else {
                            NotificationEventManager.handleNotificationError(IZ_ERROR_NAME, data.toString(), TagName, IZ_METHOD_NAME);
                        }
                    } catch (Exception ex) {
                        Util.handleExceptionOnce(context, ex + IZ_ERROR_NAME + data, TagName, IZ_METHOD_NAME);
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(data.get(AppConstant.GLOBAL)));
                        String cid = jsonObject.optString(ShortPayloadConstant.ID);
                        String rid = jsonObject.optString(ShortPayloadConstant.RID);
                        int cfg = jsonObject.optInt(ShortPayloadConstant.CFG);
                        String cfgData = Util.getIntegerToBinary(cfg);
                        if (!cfgData.isEmpty()) {
                            String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                            if (impIndex.equalsIgnoreCase("1")) {
                                NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_FCM);
                            }
                        }
                        JSONObject jsonObject1;
                        if (Objects.equals(data.get(AppConstant.PUSH_TYPE), AppConstant.FCM_TYPE)) {
                            jsonObject1 = new JSONObject(data);
                        } else {
                            jsonObject1 = new JSONObject(data.toString());
                        }
                        AdMediation.getMediationData(context, jsonObject1, AppConstant.PUSH_FCM, "");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);
                    } catch (Exception ex) {
                        Util.handleExceptionOnce(context, ex + IZ_ERROR_NAME + data, TagName, IZ_METHOD_NAME + "-> mediation");
                    }
                }
            } else {
                try {
                    preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                    JSONObject payloadObj = new JSONObject(data);
                    if (payloadObj.optLong(ShortPayloadConstant.CREATEDON) > PreferenceUtil.getInstance(context).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                        Payload payload = new Payload();
                        payload.setCreated_Time(payloadObj.optString(ShortPayloadConstant.CREATEDON));
                        payload.setFetchURL(payloadObj.optString(ShortPayloadConstant.FETCHURL));
                        payload.setKey(payloadObj.optString(ShortPayloadConstant.KEY));
                        payload.setId(payloadObj.optString(ShortPayloadConstant.ID).replace("['", "").replace("']", "").replace("~", ""));
                        payload.setRid(payloadObj.optString(ShortPayloadConstant.RID));
                        payload.setLink(payloadObj.optString(ShortPayloadConstant.LINK).replace("['", "").replace("']", ""));
                        payload.setTitle(payloadObj.optString(ShortPayloadConstant.TITLE).replace("['", "").replace("']", ""));
                        payload.setMessage(payloadObj.optString(ShortPayloadConstant.NMESSAGE).replace("['", "").replace("']", ""));
                        payload.setIcon(payloadObj.optString(ShortPayloadConstant.ICON).replace("['", "").replace("']", ""));
                        payload.setReqInt(payloadObj.optInt(ShortPayloadConstant.REQINT));
                        payload.setTag(payloadObj.optString(ShortPayloadConstant.TAG));
                        payload.setBanner(payloadObj.optString(ShortPayloadConstant.BANNER).replace("['", "").replace("']", ""));
                        payload.setAct_num(payloadObj.optInt(ShortPayloadConstant.ACTNUM));
                        payload.setBadgeicon(payloadObj.optString(ShortPayloadConstant.BADGE_ICON).replace("['", "").replace("']", ""));
                        payload.setBadgecolor(payloadObj.optString(ShortPayloadConstant.BADGE_COLOR));
                        payload.setSubTitle(payloadObj.optString(ShortPayloadConstant.SUBTITLE));
                        payload.setGroup(payloadObj.optInt(ShortPayloadConstant.GROUP));
                        payload.setBadgeCount(payloadObj.optInt(ShortPayloadConstant.BADGE_COUNT));
                        // Button 2
                        payload.setAct1name(payloadObj.optString(ShortPayloadConstant.ACT1NAME));
                        payload.setAct1link(payloadObj.optString(ShortPayloadConstant.ACT1LINK).replace("['", "").replace("']", ""));
                        payload.setAct1icon(payloadObj.optString(ShortPayloadConstant.ACT1ICON));
                        payload.setAct1ID(payloadObj.optString(ShortPayloadConstant.ACT1ID));
                        // Button 2
                        payload.setAct2name(payloadObj.optString(ShortPayloadConstant.ACT2NAME));
                        payload.setAct2link(payloadObj.optString(ShortPayloadConstant.ACT2LINK).replace("['", "").replace("']", ""));
                        payload.setAct2icon(payloadObj.optString(ShortPayloadConstant.ACT2ICON));
                        payload.setAct2ID(payloadObj.optString(ShortPayloadConstant.ACT2ID));

                        payload.setInapp(payloadObj.optInt(ShortPayloadConstant.INAPP));
                        payload.setTrayicon(payloadObj.optString(ShortPayloadConstant.TARYICON));
                        payload.setSmallIconAccentColor(payloadObj.optString(ShortPayloadConstant.ICONCOLOR));
                        payload.setLedColor(payloadObj.optString(ShortPayloadConstant.LEDCOLOR));
                        payload.setLockScreenVisibility(payloadObj.optInt(ShortPayloadConstant.VISIBILITY));
                        payload.setGroupKey(payloadObj.optString(ShortPayloadConstant.GKEY));
                        payload.setGroupMessage(payloadObj.optString(ShortPayloadConstant.GMESSAGE));
                        payload.setFromProjectNumber(payloadObj.optString(ShortPayloadConstant.PROJECTNUMBER));
                        payload.setCollapseId(payloadObj.optString(ShortPayloadConstant.COLLAPSEID));
                        payload.setPriority(payloadObj.optInt(ShortPayloadConstant.PRIORITY));
                        payload.setRawPayload(payloadObj.optString(ShortPayloadConstant.RAWDATA));
                        payload.setRc(payloadObj.optString(ShortPayloadConstant.RC));
                        payload.setRv(payloadObj.optString(ShortPayloadConstant.RV));
                        payload.setAp(payloadObj.optString(ShortPayloadConstant.ADDITIONALPARAM));
                        payload.setCfg(payloadObj.optInt(ShortPayloadConstant.CFG));
                        payload.setPush_type(String.valueOf(PushType.fcm));
                        // Notification Channel .............
                        payload.setChannel(payloadObj.optString(ShortPayloadConstant.NOTIFICATION_CHANNEL));
                        payload.setVibration(payloadObj.optString(ShortPayloadConstant.VIBRATION));
                        payload.setBadge(payloadObj.optInt(ShortPayloadConstant.BADGE));
                        payload.setOtherChannel(payloadObj.optString(ShortPayloadConstant.OTHER_CHANNEL));
                        payload.setSound(payloadObj.optString(ShortPayloadConstant.SOUND));
                        payload.setMaxNotification(payloadObj.optInt(ShortPayloadConstant.MAX_NOTIFICATION));
                        payload.setFallBackDomain(payloadObj.optString(ShortPayloadConstant.FALL_BACK_DOMAIN));
                        payload.setFallBackSubDomain(payloadObj.optString(ShortPayloadConstant.FALLBACK_SUB_DOMAIN));
                        payload.setFallBackPath(payloadObj.optString(ShortPayloadConstant.FAll_BACK_PATH));
                        payload.setDefaultNotificationPreview(payloadObj.optInt(ShortPayloadConstant.TEXTOVERLAY));
                        payload.setNotification_bg_color(payloadObj.optString(ShortPayloadConstant.BGCOLOR));
                        payload.setOfflineCampaign(payloadObj.optString(ShortPayloadConstant.OFFLINE_CAMPAIGN));
                        payload.setExpiryTimerValue(payloadObj.optString(ShortPayloadConstant.EXPIRY_TIMER_VALUE));
                        payload.setMakeStickyNotification(payloadObj.optString(ShortPayloadConstant.MAKE_STICKY_NOTIFICATION));

                        try (NewsHubDBHelper newsHubDBHelper = new NewsHubDBHelper(context)) {
                            if (payload.getRid() != null && !payload.getRid().isEmpty()) {
                                preferenceUtil.setIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN, Util.getValidIdForCampaigns(payload));
                            } else {
                                Log.e("campaign", "rid null or empty!");
                            }
                            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                                int campaigns = preferenceUtil.getIntData(ShortPayloadConstant.OFFLINE_CAMPAIGN);
                                if (campaigns == AppConstant.CAMPAIGN_SI || campaigns == AppConstant.CAMPAIGN_SE) {
                                    Log.e("campaign", "...");
                                } else {
                                    newsHubDBHelper.addNewsHubPayload(payload);
                                }
                            }
                        } catch (Exception e) {
                            Util.handleExceptionOnce(context, e.toString(), TagName, IZ_METHOD_NAME);
                        }

                        final Handler mainHandler = new Handler(Looper.getMainLooper());
                        final Runnable myRunnable = () -> {
                            NotificationEventManager.handleImpressionAPI(payload, AppConstant.PUSH_FCM);
                            iZooto.processNotificationReceived(context, payload);
                        };

                        try {
                            NotificationExecutorService notificationExecutorService = new NotificationExecutorService(context);
                            notificationExecutorService.executeNotification(mainHandler, myRunnable, payload);

                        } catch (Exception e) {
                            Util.handleExceptionOnce(context, e.toString(), TagName, IZ_METHOD_NAME);
                        }
                        DebugFileManager.createExternalStoragePublic(context, data.toString(), " Log-> ");
                    } else {
                        String updateDaily = NotificationEventManager.getDailyTime(context);
                        if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                            preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                            NotificationEventManager.handleNotificationError(IZ_ERROR_NAME + payloadObj.optString("t"), payloadObj.toString(), TagName, IZ_METHOD_NAME);
                        }
                    }
                } catch (Exception e) {
                    Util.handleExceptionOnce(context, data + e.toString(), TagName, IZ_METHOD_NAME);
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, data + e.toString(), TagName, IZ_METHOD_NAME);
        }
    }

}

