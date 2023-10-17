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

public class iZootoHmsMessagingService extends HmsMessageService {
    private Payload payload;


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        HMSTokenGenerator.getTokenFromOnNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("Push Type", AppConstant.HMS);
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(this);
        if (preferenceUtil.getEnableState(AppConstant.NOTIFICATION_ENABLE_DISABLE)) {
            handleNow(this, remoteMessage.getData());
        }
    }

    private void handleNow(Context context, String data) {
        try {

            PreferenceUtil preferenceUtil =PreferenceUtil.getInstance(context);
            JSONObject payloadObj = new JSONObject(data);
            if(payloadObj.has(AppConstant.AD_NETWORK) || payloadObj.has(AppConstant.GLOBAL) || payloadObj.has(AppConstant.GLOBAL_PUBLIC_KEY))
            {
                if(payloadObj.has(AppConstant.GLOBAL_PUBLIC_KEY))
                {
                    try
                    {
                        JSONObject jsonObject=new JSONObject(Objects.requireNonNull(payloadObj.optString(AppConstant.GLOBAL)));
                        String urlData=payloadObj.optString(AppConstant.GLOBAL_PUBLIC_KEY);
                        if(jsonObject.toString()!=null && urlData!=null && !urlData.isEmpty()) {
                            String cid = jsonObject.optString(ShortpayloadConstant.ID);
                            String rid = jsonObject.optString(ShortpayloadConstant.RID);
                            int cfg=jsonObject.optInt(ShortpayloadConstant.CFG);
                            String cfgData=Util.getIntegerToBinary(cfg);
                            if(cfgData!=null && !cfgData.isEmpty()) {
                                String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                                if(impIndex.equalsIgnoreCase("1"))
                                {
                                    NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1,AppConstant.PUSH_HMS);

                                }

                            }

                           // NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL,cid,rid,-1,AppConstant.PUSH_HMS);
                            AdMediation.getMediationGPL(context, jsonObject, urlData);
                            preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);

                        }
                        else
                        {
                            NotificationEventManager.handleNotificationError("Payload Error",data.toString(),"HMSMessagingSevices","HandleNow");
                        }
                    }
                    catch (Exception ex)
                    {

                        Util.setException(context,ex.toString()+"PayloadError"+data.toString(),"HMSMessagingService","handleNow");
                    }

                }
                else {
                    try {
                        JSONObject jsonObject = new JSONObject(payloadObj.optString(AppConstant.GLOBAL));
                        String cid = jsonObject.optString(ShortpayloadConstant.ID);
                        String rid = jsonObject.optString(ShortpayloadConstant.RID);
                        int cfg=jsonObject.optInt(ShortpayloadConstant.CFG);
                        String cfgData=Util.getIntegerToBinary(cfg);
                        if(cfgData!=null && !cfgData.isEmpty()) {
                            String impIndex = String.valueOf(cfgData.charAt(cfgData.length() - 1));
                            if(impIndex.equalsIgnoreCase("1"))
                            {
                                NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1,AppConstant.PUSH_FCM);

                            }

                        }

                       // NotificationEventManager.impressionNotification(RestClient.IMPRESSION_URL, cid, rid, -1,AppConstant.PUSH_XIAOMI);
                        JSONObject jsonObject1=new JSONObject(data.toString());
                        AdMediation.getMediationData(context, jsonObject1,AppConstant.PUSH_HMS,"");
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION, true);
                    }
                    catch (Exception ex)
                    {
                        Util.setException(context,ex.toString()+"PayloadError"+data.toString(),"DATBMessagingService","handleNow");

                    }
                }
            }
            else {
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
                    payload.setPush_type(AppConstant.PUSH_HMS);
                    payload.setDefaultNotificationPreview(payloadObj.optInt(ShortpayloadConstant.TEXTOVERLAY));
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                    payload.setRc(payloadObj.optString(ShortpayloadConstant.RC));
                    payload.setRv(payloadObj.optString(ShortpayloadConstant.RV));
                    payload.setExpiryTimerValue(payloadObj.optString(ShortpayloadConstant.EXPIRY_TIMER_VALUE));
                    payload.setMakeStickyNotification(payloadObj.optString(ShortpayloadConstant.MAKE_STICKY_NOTIFICATION));
                    payload.setOfflineCampaign(payloadObj.optString(ShortpayloadConstant.OFFLINE_CAMPAIGN));

                    if (payload.getOfflineCampaign() != null && !payload.getOfflineCampaign().isEmpty()) {
                        preferenceUtil.setStringData(ShortpayloadConstant.OFFLINE_CAMPAIGN, payload.getOfflineCampaign());
                    } else {
                        newsHubDBHelper.addNewsHubPayload(payload);
                    }

                    if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                        String campaigns = preferenceUtil.getStringData(ShortpayloadConstant.OFFLINE_CAMPAIGN);
                        if (campaigns != null && campaigns.equals(AppConstant.NEWS_HUB_CAMPAIGN)) {
                            newsHubDBHelper.addNewsHubPayload(payload);
                        } else {
                            Log.e("offlineCampaign", "...");
                        }

                    }

                } else {
                    return;
                }

                if (iZooto.appContext == null)
                    iZooto.appContext = context;
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        NotificationEventManager.handleImpressionAPI(payload,AppConstant.PUSH_HMS);
                        iZooto.processNotificationReceived(context,payload);

                    } // This is your code
                };
                mainHandler.post(myRunnable);
            }
        } catch (Exception e) {

            DebugFileManager.createExternalStoragePublic(context,e.toString()+data,"[Log.e]-> HMS ->");
            Util.setException(context, e.toString(), "HMSMessagingServices", "handleNow");

        }




    }
}
