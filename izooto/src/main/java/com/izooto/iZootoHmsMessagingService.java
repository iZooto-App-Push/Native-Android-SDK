package com.izooto;

import static com.izooto.NewsHubAlert.newsHubDBHelper;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class iZootoHmsMessagingService extends HmsMessageService {
    private Payload payload;
    static final String IZ_TAG_NAME = "iZootoHmsMessagingService";
    static final String IZ_METHOD_NAME = "handleNow";
    private final String IZ_ERROR_NAME = "Payload Error";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        HMSTokenGenerator.getTokenFromOnNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        executeBackgroundTask(remoteMessage);
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            executorService.execute(runnable);
        } catch (Exception ex){
            Util.handleExceptionOnce(this, remoteMessage + ex.toString(), IZ_TAG_NAME, "onMessageReceived");
        }
    }

    private void executeBackgroundTask(RemoteMessage remoteMessage) {
        try {
            Log.i("Push Type", AppConstant.HMS);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(this);
            if (preferenceUtil.getEnableState(AppConstant.NOTIFICATION_ENABLE_DISABLE)) {
                handleNow(this, remoteMessage.getData());
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(this, ex.toString(), IZ_TAG_NAME, "executeBackgroundTask");
        }
    }
    private void handleNow(final Context context, final String data) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            JSONObject payloadObj = new JSONObject(data);
            if (payloadObj.has(AppConstant.AD_NETWORK) || payloadObj.has(AppConstant.GLOBAL) || payloadObj.has(AppConstant.GLOBAL_PUBLIC_KEY)) {
                if (payloadObj.has(AppConstant.GLOBAL_PUBLIC_KEY)) {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(payloadObj.optString(AppConstant.GLOBAL)));
                        String urlData = payloadObj.optString(AppConstant.GLOBAL_PUBLIC_KEY);
                        if (jsonObject.toString() != null && urlData != null && !urlData.isEmpty()) {
                            String cid = jsonObject.optString(ShortpayloadConstant.ID);
                            String rid = jsonObject.optString(ShortpayloadConstant.RID);
                            int cfg = jsonObject.optInt(ShortpayloadConstant.CFG);
                            String cfgData = Util.getIntegerToBinary(cfg);
                            if (cfgData != null && !cfgData.isEmpty()) {
                                String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                                if (impIndex.equalsIgnoreCase("1")) {
                                    NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_HMS);
                                }
                            }

                            // NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL,cid,rid,-1,AppConstant.PUSH_HMS);
                            AdMediation.getMediationGPL(context, jsonObject, urlData);
                            preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                        } else {
                            NotificationEventManager.handleNotificationError(IZ_ERROR_NAME, data, IZ_TAG_NAME, IZ_ERROR_NAME);
                        }
                    } catch (Exception ex) {
                        Util.setException(context, ex + IZ_ERROR_NAME + data, IZ_TAG_NAME, IZ_METHOD_NAME);
                    }

                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(payloadObj.optString(AppConstant.GLOBAL));
                        String cid = jsonObject.optString(ShortpayloadConstant.ID);
                        String rid = jsonObject.optString(ShortpayloadConstant.RID);
                        int cfg = jsonObject.optInt(ShortpayloadConstant.CFG);
                        String cfgData = Util.getIntegerToBinary(cfg);
                        if (cfgData != null && !cfgData.isEmpty()) {
                            String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                            if (impIndex.equalsIgnoreCase("1")) {
                                NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_HMS);
                            }
                        }

                        JSONObject jsonObject1 = new JSONObject(data);
                        AdMediation.getMediationData(context, jsonObject1, AppConstant.PUSH_HMS, "");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);

                    } catch (Exception ex) {
                        Util.setException(context, ex + IZ_ERROR_NAME + data, IZ_TAG_NAME, IZ_METHOD_NAME);
                    }
                }
            } else {
                preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                if (payloadObj.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(context).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    payload.setCreated_Time(payloadObj.optString(ShortpayloadConstant.CREATEDON));
                    payload.setFetchURL(payloadObj.optString(ShortpayloadConstant.FETCHURL));
                    payload.setKey(payloadObj.optString(ShortpayloadConstant.KEY));
                    payload.setId(payloadObj.optString(ShortpayloadConstant.ID));
                    payload.setRid(payloadObj.optString(ShortpayloadConstant.RID));
                    payload.setLink(payloadObj.optString(ShortpayloadConstant.LINK));
                    payload.setTitle(payloadObj.optString(ShortpayloadConstant.TITLE));
                    payload.setMessage(payloadObj.optString(ShortpayloadConstant.NMESSAGE));
                    payload.setIcon(payloadObj.optString(ShortpayloadConstant.ICON));
                    payload.setReqInt(payloadObj.optInt(ShortpayloadConstant.REQINT));
                    payload.setTag(payloadObj.optString(ShortpayloadConstant.TAG));
                    payload.setBanner(payloadObj.optString(ShortpayloadConstant.BANNER));
                    payload.setAct_num(payloadObj.optInt(ShortpayloadConstant.ACTNUM));
                    payload.setBadgeicon(payloadObj.optString(ShortpayloadConstant.BADGE_ICON));
                    payload.setBadgecolor(payloadObj.optString(ShortpayloadConstant.BADGE_COLOR));
                    payload.setSubTitle(payloadObj.optString(ShortpayloadConstant.SUBTITLE));
                    payload.setGroup(payloadObj.optInt(ShortpayloadConstant.GROUP));
                    payload.setBadgeCount(payloadObj.optInt(ShortpayloadConstant.BADGE_COUNT));
                    // Button 2
                    payload.setAct1name(payloadObj.optString(ShortpayloadConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(ShortpayloadConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(ShortpayloadConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(ShortpayloadConstant.ACT1ID));
                    // Button 2
                    payload.setAct2name(payloadObj.optString(ShortpayloadConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(ShortpayloadConstant.ACT2LINK));
                    payload.setAct2icon(payloadObj.optString(ShortpayloadConstant.ACT2ICON));
                    payload.setAct2ID(payloadObj.optString(ShortpayloadConstant.ACT2ID));

                    payload.setInapp(payloadObj.optInt(ShortpayloadConstant.INAPP));
                    payload.setTrayicon(payloadObj.optString(ShortpayloadConstant.TARYICON));
                    payload.setSmallIconAccentColor(payloadObj.optString(ShortpayloadConstant.ICONCOLOR));
                    payload.setLedColor(payloadObj.optString(ShortpayloadConstant.LEDCOLOR));
                    payload.setLockScreenVisibility(payloadObj.optInt(ShortpayloadConstant.VISIBILITY));
                    payload.setGroupKey(payloadObj.optString(ShortpayloadConstant.GKEY));
                    payload.setGroupMessage(payloadObj.optString(ShortpayloadConstant.GMESSAGE));
                    payload.setFromProjectNumber(payloadObj.optString(ShortpayloadConstant.PROJECTNUMBER));
                    payload.setCollapseId(payloadObj.optString(ShortpayloadConstant.COLLAPSEID));
                    payload.setPriority(payloadObj.optInt(ShortpayloadConstant.PRIORITY));
                    payload.setRawPayload(payloadObj.optString(ShortpayloadConstant.RAWDATA));
                    payload.setAp(payloadObj.optString(ShortpayloadConstant.ADDITIONALPARAM));
                    payload.setCfg(payloadObj.optInt(ShortpayloadConstant.CFG));
                    payload.setTime_to_live(payloadObj.optString(ShortpayloadConstant.TIME_TO_LIVE));
                    payload.setPush_type(AppConstant.PUSH_HMS);
                    // Notification Channel
                    payload.setChannel(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_CHANNEL));
                    payload.setVibration(payloadObj.optString(ShortpayloadConstant.VIBRATION));
                    payload.setBadge(payloadObj.optInt(ShortpayloadConstant.BADGE));
                    payload.setOtherChannel(payloadObj.optString(ShortpayloadConstant.OTHER_CHANNEL));
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.SOUND));

                    payload.setDefaultNotificationPreview(payloadObj.optInt(ShortpayloadConstant.TEXTOVERLAY));
//                    payload.setSound(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                    payload.setRc(payloadObj.optString(ShortpayloadConstant.RC));
                    payload.setRv(payloadObj.optString(ShortpayloadConstant.RV));
                    payload.setOfflineCampaign(payloadObj.optString(ShortpayloadConstant.OFFLINE_CAMPAIGN));
                    payload.setExpiryTimerValue(payloadObj.optString(ShortpayloadConstant.EXPIRY_TIMER_VALUE));
                    payload.setMakeStickyNotification(payloadObj.optString(ShortpayloadConstant.MAKE_STICKY_NOTIFICATION));

                    try {
                        if (payload.getRid() != null && !payload.getRid().isEmpty()) {
                            preferenceUtil.setIntData(ShortpayloadConstant.OFFLINE_CAMPAIGN, Util.getValidIdForCampaigns(payload));
                        } else {
                            Log.v("campaign", "rid null or empty!");
                        }
                        if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                            int campaigns = preferenceUtil.getIntData(ShortpayloadConstant.OFFLINE_CAMPAIGN);
                            if (campaigns == AppConstant.CAMPAIGN_SI || campaigns == AppConstant.CAMPAIGN_SE) {
                                Log.v("campaign", "...");
                            } else {
                                newsHubDBHelper.addNewsHubPayload(payload);
                            }

                        }
                    }catch (Exception e){
                        Util.handleExceptionOnce(context,e.toString(),"iZootoHMSMessagingService","handleNow");
                    }
                } else {
                    return;
                }

                if (iZooto.appContext == null) {
                    iZooto.appContext = context;
                }

                final Handler mainHandler = new Handler(Looper.getMainLooper());
                final Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        NotificationEventManager.handleImpressionAPI(payload, AppConstant.PUSH_HMS);
                        iZooto.processNotificationReceived(context, payload);

                    } // This is your code
                };

                try {
                    NotificationExecutorService notificationExecutorService = new NotificationExecutorService(this);
                    notificationExecutorService.executeNotification(mainHandler, myRunnable, payload);

                } catch (Exception e){
                    Util.handleExceptionOnce(iZooto.appContext, e.toString(), IZ_TAG_NAME , "notificationExecutorService");
                }
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, data, " Log-> ");
            }
        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(context, e + data, "[Log.e]-> HMS ->");
            Util.handleExceptionOnce(context, e.toString(), IZ_TAG_NAME, IZ_METHOD_NAME);

        }
    }

}
