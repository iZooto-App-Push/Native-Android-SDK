package com.izooto;
import static com.izooto.NewsHubAlert.newsHubDBHelper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
            Runnable runnable = () -> {
                try {
                    if (iZooto.initialized()) {return;}
                    executeBackgroundTask(remoteMessage);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
                            String cid = jsonObject.optString(ShortPayloadConstant.ID);
                            String rid = jsonObject.optString(ShortPayloadConstant.RID);
                            int cfg = jsonObject.optInt(ShortPayloadConstant.CFG);
                            String cfgData = Util.getIntegerToBinary(cfg);
                            if (cfgData != null && !cfgData.isEmpty()) {
                                String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                                if (impIndex.equalsIgnoreCase("1")) {
                                    NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_HMS);
                                }
                            }

                            // NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL,cid,rid,-1,AppConstant.PUSH_HMS);
                            AdMediation.mediationGPL(context, jsonObject, urlData);
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
                        String cid = jsonObject.optString(ShortPayloadConstant.ID);
                        String rid = jsonObject.optString(ShortPayloadConstant.RID);
                        int cfg = jsonObject.optInt(ShortPayloadConstant.CFG);
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
                if (payloadObj.optLong(ShortPayloadConstant.CREATEDON) > PreferenceUtil.getInstance(context).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    payload.setCreated_Time(payloadObj.optString(ShortPayloadConstant.CREATEDON));
                    payload.setFetchURL(payloadObj.optString(ShortPayloadConstant.FETCHURL));
                    payload.setKey(payloadObj.optString(ShortPayloadConstant.KEY));
                    payload.setId(payloadObj.optString(ShortPayloadConstant.ID));
                    payload.setRid(payloadObj.optString(ShortPayloadConstant.RID));
                    payload.setLink(payloadObj.optString(ShortPayloadConstant.LINK));
                    payload.setTitle(payloadObj.optString(ShortPayloadConstant.TITLE));
                    payload.setMessage(payloadObj.optString(ShortPayloadConstant.NMESSAGE));
                    payload.setIcon(payloadObj.optString(ShortPayloadConstant.ICON));
                    payload.setReqInt(payloadObj.optInt(ShortPayloadConstant.REQINT));
                    payload.setTag(payloadObj.optString(ShortPayloadConstant.TAG));
                    payload.setBanner(payloadObj.optString(ShortPayloadConstant.BANNER));
                    payload.setAct_num(payloadObj.optInt(ShortPayloadConstant.ACTNUM));
                    payload.setBadgeicon(payloadObj.optString(ShortPayloadConstant.BADGE_ICON));
                    payload.setBadgecolor(payloadObj.optString(ShortPayloadConstant.BADGE_COLOR));
                    payload.setSubTitle(payloadObj.optString(ShortPayloadConstant.SUBTITLE));
                    payload.setGroup(payloadObj.optInt(ShortPayloadConstant.GROUP));
                    payload.setBadgeCount(payloadObj.optInt(ShortPayloadConstant.BADGE_COUNT));
                    // Button 2
                    payload.setAct1name(payloadObj.optString(ShortPayloadConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(ShortPayloadConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(ShortPayloadConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(ShortPayloadConstant.ACT1ID));
                    // Button 2
                    payload.setAct2name(payloadObj.optString(ShortPayloadConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(ShortPayloadConstant.ACT2LINK));
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
                    payload.setAp(payloadObj.optString(ShortPayloadConstant.ADDITIONALPARAM));
                    payload.setCfg(payloadObj.optInt(ShortPayloadConstant.CFG));
                    payload.setTime_to_live(payloadObj.optString(ShortPayloadConstant.TIME_TO_LIVE));
                    payload.setPush_type(String.valueOf(PushType.hms));
                    // Notification Channel
                    payload.setChannel(payloadObj.optString(ShortPayloadConstant.NOTIFICATION_CHANNEL));
                    payload.setVibration(payloadObj.optString(ShortPayloadConstant.VIBRATION));
                    payload.setBadge(payloadObj.optInt(ShortPayloadConstant.BADGE));
                    payload.setOtherChannel(payloadObj.optString(ShortPayloadConstant.OTHER_CHANNEL));
                    payload.setSound(payloadObj.optString(ShortPayloadConstant.SOUND));

                    payload.setDefaultNotificationPreview(payloadObj.optInt(ShortPayloadConstant.TEXTOVERLAY));
//                    payload.setSound(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortPayloadConstant.MAX_NOTIFICATION));
                    payload.setRc(payloadObj.optString(ShortPayloadConstant.RC));
                    payload.setRv(payloadObj.optString(ShortPayloadConstant.RV));
                    payload.setOfflineCampaign(payloadObj.optString(ShortPayloadConstant.OFFLINE_CAMPAIGN));
                    payload.setExpiryTimerValue(payloadObj.optString(ShortPayloadConstant.EXPIRY_TIMER_VALUE));
                    payload.setMakeStickyNotification(payloadObj.optString(ShortPayloadConstant.MAKE_STICKY_NOTIFICATION));

                    try {
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

                    }
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
