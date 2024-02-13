package com.izooto;

import static com.izooto.NewsHubAlert.newsHubDBHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XiaomiPushReceiver extends PushMessageReceiver {
    static final  String XIAOMI_TAG="XiaomiPushReceiver";
    private Payload payload;
    static final String IZ_METHOD_NAME = "handleNow";
    static final String IZ_ERROR_NAME = "Payload Error";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // data payload notification received
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        executeBackgroundTask(context, miPushMessage);
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            executorService.execute(runnable);
        } catch (Exception ex){
            Util.handleExceptionOnce(context, ex.toString(), XIAOMI_TAG, "onReceivePassThroughMessage");
        }
    }

    // notification received
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageArrived(context, miPushMessage);
        notificationBarView(context, miPushMessage.getContent());
    }

    // notification click
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageClicked(context, miPushMessage);
        notificationBarClick(context, miPushMessage.getContent());
    }

    private void executeBackgroundTask(Context context, MiPushMessage miPushMessage) {
        try {
            String payload = miPushMessage.getContent();
            Log.v("Push Type", AppConstant.PUSH_XIAOMI);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (preferenceUtil.getEnableState(AppConstant.NOTIFICATION_ENABLE_DISABLE)) {
                if (payload != null && !payload.isEmpty())
                    handleNow(context, payload);
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString() , XIAOMI_TAG, "executeBackgroundTask");
        }
    }
    // notification received

    private void handleNow(final Context context, final String data) {
        Log.d(XIAOMI_TAG, AppConstant.NOTIFICATIONRECEIVED);
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
                                    NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_XIAOMI);
                                }
                            }
                            AdMediation.getMediationGPL(context, jsonObject, urlData);
                            preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);

                        } else {
                            NotificationEventManager.handleNotificationError(IZ_ERROR_NAME, data, XIAOMI_TAG, IZ_METHOD_NAME);
                        }
                    } catch (Exception ex) {
                        Util.setException(context, ex + IZ_ERROR_NAME + data, XIAOMI_TAG, IZ_METHOD_NAME);
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
                                NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1, AppConstant.PUSH_XIAOMI);
                            }
                        }
                        JSONObject jsonObject1 = new JSONObject(data);
                        AdMediation.getMediationData(context, jsonObject1, AppConstant.PUSH_XIAOMI, "");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);
                    } catch (Exception ex) {
                        Util.setException(context, ex + IZ_ERROR_NAME + data, XIAOMI_TAG, IZ_METHOD_NAME);
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
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.SOUND));
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
                    payload.setPush_type(AppConstant.PUSH_XIAOMI);
                    payload.setMaxNotification(payloadObj.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                    payload.setFallBackDomain(payloadObj.optString(ShortpayloadConstant.FALL_BACK_DOMAIN));
                    payload.setFallBackSubDomain(payloadObj.optString(ShortpayloadConstant.FALLBACK_SUB_DOMAIN));
                    payload.setFallBackPath(payloadObj.optString(ShortpayloadConstant.FAll_BACK_PATH));
                    payload.setDefaultNotificationPreview(payloadObj.optInt(ShortpayloadConstant.TEXTOVERLAY));
                    payload.setNotification_bg_color(payloadObj.optString(ShortpayloadConstant.BGCOLOR));

                    // Notification Channel feature .............

                    payload.setChannel(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_CHANNEL));
                    payload.setVibration(payloadObj.optString(ShortpayloadConstant.VIBRATION));
                    payload.setBadge(payloadObj.optInt(ShortpayloadConstant.BADGE));
                    payload.setOtherChannel(payloadObj.optString(ShortpayloadConstant.OTHER_CHANNEL));

//                    payload.setSound(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                    payload.setFallBackDomain(payloadObj.optString(ShortpayloadConstant.FALL_BACK_DOMAIN));
                    payload.setFallBackSubDomain(payloadObj.optString(ShortpayloadConstant.FALLBACK_SUB_DOMAIN));
                    payload.setFallBackPath(payloadObj.optString(ShortpayloadConstant.FAll_BACK_PATH));
                    payload.setDefaultNotificationPreview(payloadObj.optInt(ShortpayloadConstant.TEXTOVERLAY));
                    payload.setNotification_bg_color(payloadObj.optString(ShortpayloadConstant.BGCOLOR));
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
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), XIAOMI_TAG , "handleNow");
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
                        NotificationEventManager.handleImpressionAPI(payload, AppConstant.PUSH_XIAOMI);
                        iZooto.processNotificationReceived(context, payload);
                    }
                };

                try {
                    NotificationExecutorService notificationExecutorService = new NotificationExecutorService(context);
                    notificationExecutorService.executeNotification(mainHandler, myRunnable, payload);
                } catch (Exception e){
                    Util.handleExceptionOnce(iZooto.appContext, e.toString(), XIAOMI_TAG , "notificationExecutorService");
                }
            }
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, XIAOMI_TAG, data);

        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, e.toString(), "[Log.e]->MIPush");
            Util.setException(context, e.toString(), XIAOMI_TAG, IZ_METHOD_NAME);
        }
    }


    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onReceiveRegisterResult(context, miPushCommandMessage);
    }

    // register device
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onCommandResult(context, miPushCommandMessage);
        if(context!=null) {
            try {
                PreferenceUtil preferenceUtils = PreferenceUtil.getInstance(context);
                preferenceUtils.setStringData(AppConstant.XiaomiToken, miPushCommandMessage.getCommandArguments().toString().replace("[", "").replace("]", ""));
                String mi_token =miPushCommandMessage.getCommandArguments().toString().replace("[", "").replace("]", "");
                Log.i(AppConstant.XiaomiToken, mi_token);
                if(mi_token!=null && !mi_token.isEmpty())
                {
                    DebugFileManager.createExternalStoragePublic(iZooto.appContext,mi_token,"[Log.e]-> MI Token->");

                    registerToken(context,mi_token);
                }
            }
            catch (Exception ex)
            {
                //Log.e("XMPush",ex.toString());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void registerToken(final Context context,String miToken) {
        if (context == null)
            return;
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (!preferenceUtil.getBoolean(AppConstant.IS_UPDATED_XIAOMI_TOKEN)) {
            try {
                if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty())
                    preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                Map<String,String> mapData= new HashMap<>();
                mapData.put(AppConstant.ADDURL, "" + AppConstant.STYPE);
                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                mapData.put(AppConstant.BTYPE_,"" + AppConstant.BTYPE);
                mapData.put(AppConstant.DTYPE_,"" + AppConstant.DTYPE);
                mapData.put(AppConstant.TIMEZONE,"" + System.currentTimeMillis());
                mapData.put(AppConstant.APPVERSION,"" + Util.getAppVersion(context));
                mapData.put(AppConstant.OS,"" + AppConstant.SDKOS);
                mapData.put(AppConstant.ALLOWED_,"" + AppConstant.ALLOWED);
                mapData.put(AppConstant.ANDROID_ID,"" + Util.getAndroidId(context));
                mapData.put(AppConstant.CHECKSDKVERSION,"" + AppConstant.SDKVERSION);
                mapData.put(AppConstant.LANGUAGE,"" + Util.getDeviceLanguage());
                mapData.put(AppConstant.QSDK_VERSION ,"" + AppConstant.SDKVERSION);
                mapData.put(AppConstant.TOKEN,"" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                mapData.put(AppConstant.ADVERTISEMENTID,"" + preferenceUtil.getStringData(AppConstant.ADVERTISING_ID));
                mapData.put(AppConstant.XIAOMITOKEN,miToken);
                mapData.put(AppConstant.PACKAGE_NAME,"" + context.getPackageName());
                mapData.put(AppConstant.SDKTYPE,"" + iZooto.SDKDEF);
                mapData.put(AppConstant.KEY_HMS,"" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                mapData.put(AppConstant.ANDROIDVERSION,"" + Build.VERSION.RELEASE);
                mapData.put(AppConstant.DEVICENAME,"" + Util.getDeviceName());
                mapData.put(AppConstant.H_PLUGIN_VERSION,preferenceUtil.getStringData(AppConstant.HYBRID_PLUGIN_VERSION));

                if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                    preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 3);
                } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                    preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
                } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                    preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
                }
                else {
                    preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 1);
                }

                RestClient.postRequest(RestClient.BASE_URL, mapData, null,new RestClient.ResponseHandler() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    void onSuccess(final String response) {
                        super.onSuccess(response);
                        preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_XIAOMI_TOKEN, true);
                        iZooto.lastVisitApi(context);
                        try {
                            if (!preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA).isEmpty()) {
                                Util.sleepTime(5000);
                                JSONObject json  = new JSONObject(preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA));
                                iZooto.addUserProperty(Util.toMap(json));
                            }
                            if (!preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN).isEmpty() && !preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV).isEmpty()) {
                                JSONObject json  = new JSONObject(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV));
                                iZooto.addEvent(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN), Util.toMap(json));
                            }
                            if (preferenceUtil.getBoolean(AppConstant.IS_SET_SUBSCRIPTION_METHOD))
                                iZooto.setSubscription(preferenceUtil.getBoolean(AppConstant.SET_SUBSCRITION_LOCAL_DATA));

                        } catch (Exception e) {
                            Util.setException(context, e.toString(), "xiaomi_registration ", AppConstant.APP_NAME_TAG);
                        }
                        if (preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty())
                            preferenceUtil.setLongData(AppConstant.DEVICE_REGISTRATION_TIMESTAMP, System.currentTimeMillis());

                    }
                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }
                });
            }catch (Exception e){
                Util.handleExceptionOnce(context, e.toString(), "MIRegisterToken", AppConstant.APP_NAME_TAG);

            }

        } else {
            iZooto.lastVisitApi(context);
        }

    }
// notification view api
    static void notificationBarView(Context context, String content) {
        if (context == null)
            return;

        if (content == null)
            return;

        try {
            JSONObject jsonObject = new JSONObject(content);
            Payload payload = new Payload();
            payload.setRid(jsonObject.optString(ShortpayloadConstant.RID));
            payload.setId(jsonObject.optString(ShortpayloadConstant.ID));
            payload.setCfg(jsonObject.optInt(ShortpayloadConstant.CFG));

            String lastView_Click = "0";
            String lastSeventhIndex = "0";
            String lastNinthIndex = "0";
            String data = Util.getIntegerToBinary(payload.getCfg());

            if (data != null && !data.isEmpty()) {
                lastView_Click = String.valueOf(data.charAt(data.length() - 3));
                lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
            } else {
                lastView_Click = "0";
                lastSeventhIndex = "0";
                lastNinthIndex = "0";
            }

            NotificationEventManager.handleImpressionAPI(payload, AppConstant.PUSH_XIAOMI);

            if (lastView_Click.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")) {
                NotificationEventManager.lastViewNotificationApi(payload, lastView_Click, lastSeventhIndex, lastNinthIndex);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "XiaomiPushReceiver", "onNotificationMessageArrived");
            DebugFileManager.createExternalStoragePublic(context, "onNotificationMessageArrived -> " + e.toString(),"[Log.e]->");
        }
    }

    // notification click track
    static void notificationBarClick(Context context, String payload) {
        if (context == null)
            return;

        if (payload == null)
            return;

        try {
            JSONObject jsonObject = new JSONObject(payload);
            String rid = jsonObject.optString(ShortpayloadConstant.RID);
            String cid = jsonObject.optString(ShortpayloadConstant.ID);
            int ia = jsonObject.optInt(ShortpayloadConstant.INAPP);
            String ln = jsonObject.optString(ShortpayloadConstant.LINK);
            String key = jsonObject.optString(ShortpayloadConstant.KEY);
            int cfg = jsonObject.optInt(ShortpayloadConstant.CFG);
            String ap = jsonObject.optString(ShortpayloadConstant.ADDITIONALPARAM);

            String clickIndex = "0";
            String lastView_Click = "0";
            String data = Util.getIntegerToBinary(cfg);

            if (data != null && !data.isEmpty()) {
                clickIndex = String.valueOf(data.charAt(data.length() - 2));
                lastView_Click = String.valueOf(data.charAt(data.length() - 3));
            } else {
                clickIndex = "0";
                lastView_Click = "0";
            }
            Intent intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);
                intent.putExtra(AppConstant.KEY_WEB_URL, ln);
                intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, 100);
                intent.putExtra(AppConstant.KEY_IN_APP, ia);
                intent.putExtra(AppConstant.KEY_IN_CID, cid);
                intent.putExtra(AppConstant.KEY_IN_RID, rid);
                intent.putExtra(AppConstant.KEY_IN_BUTOON, 0);
                intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, ap);
                intent.putExtra(AppConstant.KEY_IN_PHONE, AppConstant.NO);
                intent.putExtra(AppConstant.KEY_IN_ACT1ID, "");
                intent.putExtra(AppConstant.KEY_IN_ACT2ID, "");
                intent.putExtra(AppConstant.LANDINGURL, ln);
                intent.putExtra(AppConstant.ACT1TITLE, "");
                intent.putExtra(AppConstant.ACT2TITLE, "");
                intent.putExtra(AppConstant.ACT1URL, "");
                intent.putExtra(AppConstant.ACT2URL, "");
                intent.putExtra(AppConstant.CLICKINDEX, clickIndex);
                intent.putExtra(AppConstant.LASTCLICKINDEX, lastView_Click);
                intent.putExtra(AppConstant.PUSH, AppConstant.PUSH_XIAOMI);
                intent.putExtra(AppConstant.CFGFORDOMAIN, cfg);
                context.sendBroadcast(intent);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), XIAOMI_TAG, "notificationBarClick");

            DebugFileManager.createExternalStoragePublic(context, "notificationBarClick -> " + e.toString(),"[Log.e]->");
        }

    }
}

