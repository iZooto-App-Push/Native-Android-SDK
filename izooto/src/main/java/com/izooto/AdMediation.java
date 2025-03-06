package com.izooto;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.izooto.core.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AdMediation {
    private static Payload payload;
    private static final List<Payload> payloadList = new ArrayList<>();
    private static final List<Payload> adPayload = new ArrayList<>();
    private static final List<Payload> passiveList = new ArrayList<>();
    static final List<JSONObject> failsList = new ArrayList<>();
    public static List<String> clicksData = new ArrayList<>();
    static final List<JSONObject> successList = new ArrayList<>();
    static List<String> storeList = new ArrayList<>();
    static int counterIndex = 0;
    static boolean isExecutionCompleted = false;
    private static final long TIMEUNITS = 2;


    // handle the mediation payload data
    public static void getMediationData(Context context, JSONObject data, String pushType, String globalPayloadObject) {
        if (context == null) {
            return;
        }
        try {
            iZooto.appContext = context;
            counterIndex = 0;
            payloadList.clear();
            passiveList.clear();
            adPayload.clear();
            clicksData.clear();
            successList.clear();
            failsList.clear();
            storeList.clear();
            JSONObject jsonObject;
            JSONArray jsonArray;
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (globalPayloadObject != null && !globalPayloadObject.isEmpty()) {
                jsonObject = new JSONObject(globalPayloadObject);
            } else {
                if (data.optString(AppConstant.PUSH_TYPE).equals(AppConstant.FCM_TYPE)) {
                    String globalKey = data.optString(AppConstant.GLOBAL);
                    jsonObject = new JSONObject(globalKey);
                } else {
                    jsonObject = data.getJSONObject(AppConstant.GLOBAL);
                }
            }
            if (data.optString(AppConstant.PUSH_TYPE).equals(AppConstant.FCM_TYPE)) {
                String adNetwork = data.optString(AppConstant.AD_NETWORK);
                jsonArray = new JSONArray(adNetwork);
            } else {
                jsonArray = data.getJSONArray(AppConstant.AD_NETWORK);
            }
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject payloadObj = jsonArray.getJSONObject(i);
                    if (jsonObject.optLong(ShortPayloadConstant.CREATEDON) > PreferenceUtil.getInstance(context).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                        payload = new Payload();
                        payload.setAd_type(jsonObject.optString(ShortPayloadConstant.AD_TYPE));
                        payload.setAdID(payloadObj.optString(ShortPayloadConstant.AD_ID));
                        payload.setReceived_bid(payloadObj.optString(ShortPayloadConstant.RECEIVED_BID).replace("['", "").replace("']", "").replace("~", ""));
                        payload.setFetchURL(payloadObj.optString(ShortPayloadConstant.FETCHURL));
                        payload.setKey(jsonObject.optString(ShortPayloadConstant.KEY));
                        if (jsonObject.has(ShortPayloadConstant.ID))
                            payload.setId(jsonObject.optString(ShortPayloadConstant.ID).replace("['", "").replace("']", "").replace("~", ""));
                        else
                            payload.setId(payloadObj.optString(ShortPayloadConstant.ID).replace("['", "").replace("']", "").replace("~", ""));

                        payload.setRid(jsonObject.optString(ShortPayloadConstant.RID));
                        payload.setLink(payloadObj.optString(ShortPayloadConstant.LINK).replace("['", "").replace("']", ""));
                        payload.setTitle(payloadObj.optString(ShortPayloadConstant.TITLE).replace("['", "").replace("']", ""));
                        payload.setMessage(payloadObj.optString(ShortPayloadConstant.NMESSAGE).replace("['", "").replace("']", ""));
                        payload.setIcon(payloadObj.optString(ShortPayloadConstant.ICON).replace("['", "").replace("']", ""));
                        payload.setReqInt(payloadObj.optInt(ShortPayloadConstant.REQINT));
                        payload.setTag(payloadObj.optString(ShortPayloadConstant.TAG));
                        payload.setTl(payloadObj.optString(ShortPayloadConstant.TIME_TO_LIVE));
                        payload.setFloorPrice(payloadObj.optString(ShortPayloadConstant.FLOOR_PRICE).replace("~", ""));
                        payload.setBanner(payloadObj.optString(ShortPayloadConstant.BANNER).replace("['", "").replace("']", ""));
                        payload.setBadgeicon(payloadObj.optString(ShortPayloadConstant.BADGE_ICON).replace("['", "").replace("']", ""));
                        payload.setBadgecolor(payloadObj.optString(ShortPayloadConstant.BADGE_COLOR).replace("['", "").replace("']", ""));
                        payload.setSubTitle(payloadObj.optString(ShortPayloadConstant.SUBTITLE).replace("['", "").replace("']", ""));
                        payload.setGroup(payloadObj.optInt(ShortPayloadConstant.GROUP));
                        payload.setBadgeCount(payloadObj.optInt(ShortPayloadConstant.BADGE_COUNT));
                        if (jsonObject.has("b")) {
                            payload.setAct_num(jsonObject.optInt(ShortPayloadConstant.ACTNUM));
                            payload.setAct1name(jsonObject.optString(ShortPayloadConstant.ACT1NAME).replace("['", "").replace("']", ""));
                            payload.setAct2name(jsonObject.optString(ShortPayloadConstant.ACT2NAME).replace("['", "").replace("']", ""));
                        } else {
                            payload.setAct_num(jsonObject.optInt(ShortPayloadConstant.ACTNUM));
                            payload.setAct1name(payloadObj.optString(ShortPayloadConstant.ACT1NAME).replace("['", "").replace("']", ""));
                            payload.setAct2name(payloadObj.optString(ShortPayloadConstant.ACT2NAME).replace("['", "").replace("']", ""));
                        }

                        // Button 1
                        payload.setAct1link(payloadObj.optString(ShortPayloadConstant.ACT1LINK).replace("['", "").replace("']", ""));
                        payload.setAct1icon(payloadObj.optString(ShortPayloadConstant.ACT1ICON).replace("['", "").replace("']", ""));
                        payload.setAct1ID(payloadObj.optString(ShortPayloadConstant.ACT1ID));
                        // Button 2

                        payload.setAct2link(payloadObj.optString(ShortPayloadConstant.ACT2LINK).replace("['", "").replace("']", ""));
                        payload.setAct2icon(payloadObj.optString(ShortPayloadConstant.ACT2ICON));
                        payload.setAct2ID(payloadObj.optString(ShortPayloadConstant.ACT2ID));
                        payload.setInapp(payloadObj.optInt(ShortPayloadConstant.INAPP));
                        payload.setTrayicon(payloadObj.optString(ShortPayloadConstant.TARYICON));
                        payload.setSmallIconAccentColor(payloadObj.optString(ShortPayloadConstant.ICONCOLOR));
                        payload.setSound(payloadObj.optString(ShortPayloadConstant.SOUND));
                        payload.setLedColor(payloadObj.optString(ShortPayloadConstant.LEDCOLOR));
                        payload.setLockScreenVisibility(payloadObj.optInt(ShortPayloadConstant.VISIBILITY));
                        payload.setGroupKey(payloadObj.optString(ShortPayloadConstant.GKEY));
                        payload.setGroupMessage(payloadObj.optString(ShortPayloadConstant.GMESSAGE));
                        payload.setFromProjectNumber(payloadObj.optString(ShortPayloadConstant.PROJECTNUMBER));
                        payload.setCollapseId(payloadObj.optString(ShortPayloadConstant.COLLAPSEID));
                        payload.setPriority(payloadObj.optInt(ShortPayloadConstant.PRIORITY));
                        payload.setRawPayload(payloadObj.optString(ShortPayloadConstant.RAWDATA));
                        payload.setAp(payloadObj.optString(ShortPayloadConstant.ADDITIONALPARAM));
                        payload.setCfg(jsonObject.optInt(ShortPayloadConstant.CFG));
                        payload.setCpc(payloadObj.optString(ShortPayloadConstant.CPC).replace("['", "").replace("']", "").replace("~", ""));
                        payload.setRc(payloadObj.optString(ShortPayloadConstant.RC));
                        payload.setRv(payloadObj.optString(ShortPayloadConstant.RV));
                        payload.setPassive_flag(payloadObj.optString(ShortPayloadConstant.Passive_Flag));
                        payload.setCpm(payloadObj.optString(ShortPayloadConstant.CPM).replace("['", "").replace("']", ""));
                        payload.setCtr(payloadObj.optString(ShortPayloadConstant.CTR).replace("['", "").replace("']", "").replace("~", ""));
                        payload.setFallBackDomain(jsonObject.optString(ShortPayloadConstant.FALL_BACK_DOMAIN));
                        payload.setFallBackSubDomain(jsonObject.optString(ShortPayloadConstant.FALLBACK_SUB_DOMAIN));
                        payload.setFallBackPath(jsonObject.optString(ShortPayloadConstant.FAll_BACK_PATH));
                        payload.setTime_out(jsonObject.optInt(ShortPayloadConstant.TIME_OUT));
                        payload.setAdTimeOut(payloadObj.optInt(ShortPayloadConstant.AD_TIME_OUT));
                        payload.setCreated_Time(jsonObject.optString(ShortPayloadConstant.CREATEDON));
                        payload.setPush_type(pushType);
                        payload.setDefaultNotificationPreview(jsonObject.optInt(ShortPayloadConstant.TEXTOVERLAY));
                        payload.setMakeStickyNotification(jsonObject.optString(ShortPayloadConstant.MAKE_STICKY_NOTIFICATION));

                        if (payload.getPassive_flag().equalsIgnoreCase("1") && jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("6")) {
                            passiveList.add(payload);
                        } else {
                            payloadList.add(payload);
                        }
                    } else {
                        String updateDaily = NotificationEventManager.getDailyTime(context);
                        if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                            preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                            NotificationEventManager.handleNotificationError(AppConstant.IZ_PAYLOAD_ERROR + payloadObj.optString("t"), null, "AdMediation", "getAdJsonData()");
                        }
                        return;
                    }
                }
                if (!payloadList.isEmpty()) {
                    if (jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("4")) {
                        processThoroughlyPayloads(payloadList.get(0), 4, 0);
                    }

                    if (jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("5")) {
                        try {
                            preferenceUtil.setBooleanData("Send", true);
                            ExecutorService taskExecutor = Executors.newFixedThreadPool(payloadList.size());
                            for (int i = 0; i < payloadList.size(); i++) {
                                int index = i;
                                taskExecutor.submit(() -> {
                                    if (preferenceUtil.getBoolean("Send")) {
                                        processThoroughlyPayloads(payloadList.get(index), 5, index);
                                    }
                                });
                            }
                            taskExecutor.shutdown();
                        } catch (Exception e) {
                            Util.handleExceptionOnce(context, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getMediationData");

                        }
                    }

                    if (jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("6")) {
                        try {
                            ExecutorService taskExecutor = Executors.newFixedThreadPool(payloadList.size());
                            for (int i = 0; i < payloadList.size(); i++) {
                                int index = i;
                                taskExecutor.submit(() -> processThoroughlyPayloads(payloadList.get(index), 6, index));
                            }
                            taskExecutor.shutdown();
                            try {
                                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                                scheduler.schedule(() -> {
                                    try {
                                        boolean isTaskCompleted = taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                                        if (isTaskCompleted) {
                                            executingMediation();
                                        }
                                    } catch (Exception e) {
                                        Util.handleExceptionOnce(context, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getMediationData");
                                    }

                                }, TIMEUNITS, TimeUnit.SECONDS);
                                scheduler.shutdown();

                            } catch (Exception e) {
                                Util.handleExceptionOnce(context, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getMediationData");
                            }

                        } catch (Exception e) {
                            Util.handleExceptionOnce(context, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getMediationData");
                        }
                    }

                }
            }

        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getJSONData"); // handle one time
        }

    }

    // handle the ad network payload response data
    private static void processThoroughlyPayloads(final Payload payload, final int adIndex, final int indexValue) {
        long start = System.currentTimeMillis();
        int calculateTime;
        int adTime = payload.getAdTimeOut();
        if (adTime != 0)
            calculateTime = payload.getAdTimeOut();
        else
            calculateTime = payload.getTime_out();
        counterIndex++;
        String fetchURL = Util.updateUrlParameter(payload.getFetchURL());
        RestClient.getRequest(fetchURL, calculateTime * 1000, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {
                        storeList.add(response);
                        Object json = new JSONTokener(response).nextValue();
                        if (json != null) {
                            if (json instanceof JSONObject) {
                                JSONObject jsonObject = new JSONObject(response);
                                payload.setResponseTime((System.currentTimeMillis() - start));
                                payload.setIndex(indexValue);
                                parseThoroughlyJson(payload, jsonObject, adIndex);
                            } else if (json instanceof JSONArray) {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("", jsonArray);
                                payload.setResponseTime((System.currentTimeMillis() - start));
                                payload.setIndex(indexValue);
                                parseThoroughlyJson(payload, jsonObject, adIndex);
                            } else {
                                JSONObject data = new JSONObject();
                                data.put("b", "-1");
                                data.put("a", payload.getAdID());
                                data.put("t", System.currentTimeMillis() - start);
                                data.put("rb", -1);
                                data.put("ln", "");
                                failsList.add(data);
                                if (adIndex == 4) {
                                    String fallBackURL = callFallbackAPI(payload);
                                    showFallBackResponse(fallBackURL, payload);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("b", "-1");
                            data.put("a", payload.getAdID());
                            data.put("t", System.currentTimeMillis() - start);
                            data.put("rb", -1);
                            data.put("ln", "");
                            failsList.add(data);

                            if (adIndex == 4) {
                                String fallBackURL = callFallbackAPI(payload);
                                showFallBackResponse(fallBackURL, payload);
                            }
                            if (failsList.size() - 1 == payloadList.size() - 1) {
                                if (!successList.isEmpty()) {
                                    Log.v("iZooto", "data");
                                } else {
                                    String fallBackURL = callFallbackAPI(payload);
                                    showFallBackResponse(fallBackURL, payload);
                                }
                            }
                        } catch (Exception ex) {
                            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "processPayload");// handle exception one time
                        }
                    }
                }
            }


            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                try {
                    JSONObject data = new JSONObject();
                    data.put("b", "-1");
                    data.put("a", payload.getAdID());
                    data.put("rb", -1);
                    if (statusCode == -1 && payload.getTime_out() != 0 || payload.getAdTimeOut() != 0) {
                        data.put("t", -2);
                    } else {
                        data.put("t", -1);
                    }
                    data.put("ln", "");
                    failsList.add(data);
                    if (failsList.size() == payloadList.size() && successList.isEmpty() && passiveList.isEmpty()) {
                        String fallBackURL = callFallbackAPI(payload);
                        showFallBackResponse(fallBackURL, payload);
                    }

                    if (adIndex == 6) {
                        if (successList.size() == payloadList.size() - 1 && failsList.size() == 1) {
                            parseThoroughlyJson(payload, null, adIndex);
                        } else if (failsList.size() == payloadList.size() - 1 && successList.size() == 1) {
                            parseThoroughlyJson(payload, null, adIndex);
                        }
                    }
                    if (adIndex == 4) {
                        String fallBackURL = callFallbackAPI(payload);
                        showFallBackResponse(fallBackURL, payload);
                    }
                } catch (Exception e) {
                    Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "processPayload -failure");// handle exception one time
                }
            }
        });

    }


    // Fetching the response from payload data
    @SuppressLint("SuspiciousIndentation")
    private static void parseThoroughlyJson(Payload payload, JSONObject jsonObject, int adIndex) {
        try {
            if (iZooto.appContext == null || jsonObject == null) {
                return;
            }
            try {
                if (payload.getRv() != null && !payload.getRv().isEmpty()) {
                    try {
                        String rv = Util.setParsedRcAndRvValues(payload.getRv(), jsonObject);
                        if (!Utilities.isNullOrEmpty(rv)) {
                            payload.setRv(rv);
                        }

                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseThoroughlyJson");
                    }
                }

                if (payload.getRc() != null && !payload.getRc().isEmpty()) {
                    try {
                        String rc = Util.setParsedRcAndRvValues(payload.getRc(), jsonObject);
                        if (!Utilities.isNullOrEmpty(rc)) {
                            clicksData.add(rc);
                        }

                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseThoroughlyJson");
                    }
                }

            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseAgainJson");
            }

            payload.setStartTime(System.currentTimeMillis());
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (payload.getTitle() != null && !payload.getTitle().isEmpty()) {
                payload.setTitle(getParsedValue(jsonObject, payload.getTitle()));
            }

            if (payload.getReceived_bid().equalsIgnoreCase("-1")) {
                payload.setReceived_bid(payload.getReceived_bid());
            } else {
                payload.setReceived_bid(getParsedValue(jsonObject, payload.getReceived_bid()));
            }
            if (Objects.equals(payload.getTitle(), "")) {
                payload.setCpc("-1");
                payload.setReceived_bid("-1");
            } else {
                try {
                    payload.setCpc(getParsedValue(jsonObject, payload.getCpc()));
                    if (!Objects.equals(payload.getCtr(), "")) {
                        payload.setCtr(getParsedValue(jsonObject, payload.getCtr()));
                        payload.setCpm(getParsedValue(jsonObject, payload.getCpm()));
                        if (!Objects.equals(payload.getCpm(), "")) {
                            if (!Objects.equals(payload.getCtr(), "")) {
                                float cpm = Float.parseFloat(payload.getCpm());
                                float ctr = Float.parseFloat(payload.getCtr());
                                float dat = 10 * ctr;
                                float cpc = cpm / dat;
                                payload.setCpc(String.valueOf(cpc));
                            }
                        }
                    }

                } catch (Exception e) {
                    Util.handleExceptionOnce(iZooto.appContext, e + "-> " + payload.getCpc(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseThoroughlyJson");
                }
            }
            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                payload.setLink(getParsedValue(jsonObject, payload.getLink()));
            }

            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                if (!payload.getLink().startsWith("http://") && !payload.getLink().startsWith("https://")) {
                    String url = payload.getLink();
                    url = "https://" + url;
                    payload.setLink(url);
                }
            }

            if (payload.getBanner() != null && !payload.getBanner().isEmpty()) {
                payload.setBanner(getParsedValue(jsonObject, payload.getBanner()));
            }
            if (payload.getMessage() != null && !payload.getMessage().isEmpty()) {
                payload.setMessage(getParsedValue(jsonObject, payload.getMessage()));
            }
            if (payload.getIcon() != null && !payload.getIcon().isEmpty()) {
                payload.setIcon(getParsedValue(jsonObject, payload.getIcon()));
            }

            if (payload.getAct1link() != null && !payload.getAct1link().isEmpty()) {
                payload.setAct1link(getParsedValue(jsonObject, payload.getAct1link()));
            }
            if (payload.getAct_num() == 1) {
                if (payload.getAct1link() != null) {
                    payload.setAct1name(payload.getAct1name().replace("~", ""));
                }
                if (payload.getAct1link() != null && !payload.getAct1link().isEmpty()) {
                    if (!payload.getAct1link().startsWith("http://") && !payload.getAct1link().startsWith("https://")) {
                        String url = payload.getAct1link();
                        url = "https://" + url;
                        payload.setAct1link(url);
                    }
                }
                if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                    payload.setAct2name(payload.getAct2name().replace("~", ""));
                }
                if (payload.getAct2link() != null && !payload.getAct2link().isEmpty()) {
                    payload.setAct2link(getParsedValue(jsonObject, payload.getAct2link()));
                    if (!payload.getAct2link().startsWith("http://") && !payload.getAct2link().startsWith("https://")) {
                        String url = payload.getAct2link();
                        url = "https://" + url;
                        payload.setAct2link(url);
                    }
                }

            }
            if (payload.getIcon() != null && !Objects.equals(payload.getIcon(), "")) {
                if (!payload.getIcon().startsWith("http://") && !payload.getIcon().startsWith("https://")) {
                    String url = payload.getIcon();
                    url = "https://" + url;
                    payload.setIcon(url);
                }
            }
            if (payload.getBanner() != null && !Objects.equals(payload.getBanner(), "")) {
                if (!payload.getBanner().startsWith("http://") && !payload.getBanner().startsWith("https://")) {
                    String url = payload.getBanner();
                    url = "https://" + url;
                    payload.setBanner(url);
                }
            }
            if (Objects.equals(payload.getCpc(), "") && Objects.equals(payload.getReceived_bid(), "")) {
                payload.setCpc("-1");
                payload.setReceived_bid("-1");
            }

            payload.setAp("");
            payload.setInapp(0);
            JSONObject data = new JSONObject();
            data.put("b", Double.parseDouble(payload.getCpc()));
            data.put("a", payload.getAdID());
            if (payload.getResponseTime() == 0)
                data.put("t", -1);
            else
                data.put("t", payload.getResponseTime());
            if (payload.getReceived_bid() != null && !payload.getReceived_bid().isEmpty())
                data.put("rb", Double.parseDouble(payload.getReceived_bid()));
            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                data.put("ln", payload.getLink());
            }
            successList.add(data);

            if (adIndex == 4) {
                defeaterPayload(payload);
            }
            if (adIndex == 5 && preferenceUtil.getBoolean("Send")) {
                if (payload.getTitle() != null && !payload.getTitle().equalsIgnoreCase("")) {
                    preferenceUtil.setBooleanData("Send", false);
                    defeaterPayload(payload);
                } else {
                    if (!failsList.isEmpty()) {
                        String fallBackURL = callFallbackAPI(payload);
                        showFallBackResponse(fallBackURL, payload);
                    }
                }

            }
            if (adIndex == 6) {
                adPayload.add(payload);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getAdNotificationData");
        }

    }

    private static void executingMediation() {
        if (AdMediation.payloadList.isEmpty() && AdMediation.passiveList.isEmpty()) {
            return;
        }

        try {
            long start = System.currentTimeMillis();
            int winnerIndex = -1;
            int passiveIndex = -1;
            double passiveNetwork = 0.0;
            double winnerNetwork = 0.0;
            // Determine the highest winning bid
            for (int index = 0; index < AdMediation.adPayload.size(); index++) {
                String cpcStr = AdMediation.adPayload.get(index).getCpc();
                String floorPrice = AdMediation.adPayload.get(index).getFloorPrice();

                if (processValidBid(cpcStr, floorPrice)) {
                    double cpc = Util.parseAndCalculate(cpcStr);
                    if (cpc > winnerNetwork) {
                        winnerNetwork = cpc;
                        winnerIndex = index;
                        isExecutionCompleted = true;
                    }
                }
            }

            // Compare with passive list
            if (!passiveList.isEmpty()) {
                for (int index = 0; index < passiveList.size(); index++) {
                    double cpc = Util.parseAndCalculate(passiveList.get(index).getCpc());
                    if (cpc >= Util.parseAndCalculate(passiveList.get(0).getCpc())) {
                        passiveIndex = index;
                        passiveNetwork = cpc;
                    }
                }

                if (passiveNetwork > winnerNetwork) {
                    passivePayload(passiveList.get(passiveIndex));
                    return;
                }
            }

            // Handle winner ad payload
            if (winnerIndex != -1 && isValidTitle(AdMediation.adPayload.get(winnerIndex).getTitle())) {
                processPassiveList(passiveIndex, start);
                defeaterPayload(AdMediation.adPayload.get(winnerIndex), 6);
            }

            isExecutionCompleted = false;

        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "bidingProcessing");
        }
    }

    /**
     * 🔹 Helper method to check if CPC is valid
     */
    private static boolean processValidBid(String cpc, String floorPrice) {
        if (cpc == null || cpc.isEmpty()) return false; // CPC must be present
        double cpcValue = Double.parseDouble(cpc);
        if (floorPrice != null && !floorPrice.isEmpty()) {
            return cpcValue > Double.parseDouble(floorPrice);
        }
        return true;
    }

    /**
     * 🔹 Helper method to check if title is valid
     */
    private static boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * 🔹 Handle Passive List Processing
     */
    private static void processPassiveList(int passiveIndex, long start) {
        if (!passiveList.isEmpty() && passiveIndex != -1) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("b", Double.parseDouble(passiveList.get(passiveIndex).getCpc()));
                jsonObject.put("a", passiveList.get(passiveIndex).getAdID());
                jsonObject.put("t", (System.currentTimeMillis() - start));
                jsonObject.put("rb", Double.parseDouble(passiveList.get(passiveIndex).getReceived_bid()));
                successList.add(jsonObject);
                passiveList.clear();

            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "executingMediation");
            }
        }
    }

    //handle gpl payload
    public static void mediationGPL(Context context, JSONObject payloadObj, String url) {
        if (context == null) {
            return;
        }
        try {
            iZooto.appContext = context;
            if (payloadObj != null && url != null && !url.isEmpty()) {
                if (payloadObj.optLong(ShortPayloadConstant.CREATEDON) > PreferenceUtil.getInstance(context).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    globalPayload(url, payload, payloadObj);
                } else {
                    String updateDaily = NotificationEventManager.getDailyTime(context);
                    if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                        preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                        NotificationEventManager.handleNotificationError(AppConstant.IZ_PAYLOAD_ERROR + payloadObj.optString("t"), null, AppConstant.IZ_AD_MEDIATION_CLASS, "GPL()");
                    }
                }
            } else {
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                String data = preferenceUtil.getStringData("iz_GPL_FIRST_TIME");
                if (!data.equalsIgnoreCase(Util.getTime())) {
                    preferenceUtil.setStringData("iz_GPL_FIRST_TIME", Util.getTime());
                    if (payloadObj != null) {
                        NotificationEventManager.handleNotificationError(AppConstant.IZ_PAYLOAD_ERROR + payloadObj.optString("t"), null, AppConstant.IZ_AD_MEDIATION_CLASS, "GPL()");
                    }
                }
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getMediationGPL");
        }

    }


    //handle the global payload
    static void globalPayload(String url, Payload payload, JSONObject globalPayloadObject) {
        if (url != null && iZooto.appContext != null) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (preferenceUtil.getStringData(AppConstant.STORAGE_PAYLOAD_DATA) != null && !url.equalsIgnoreCase(checkURL(preferenceUtil.getStringData(AppConstant.STORAGE_PAYLOAD_DATA)))) {
                RestClient.get(url, new RestClient.ResponseHandler() {
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        try {
                            JSONObject jsonObject = new JSONObject(response.replace("\n", ""));
                            if (jsonObject.has("an")) {
                                getMediationData(iZooto.appContext, jsonObject, payload.getPush_type(), globalPayloadObject.toString());
                                return;
                            }

                            if (globalPayloadObject.has(ShortPayloadConstant.CREATEDON)) {
                                payload.setCreated_Time(globalPayloadObject.optString(ShortPayloadConstant.CREATEDON));
                            } else {
                                payload.setCreated_Time(jsonObject.optString(ShortPayloadConstant.CREATEDON));
                            }
                            if (globalPayloadObject.has(ShortPayloadConstant.KEY)) {
                                payload.setKey(globalPayloadObject.optString(ShortPayloadConstant.KEY));
                            } else {
                                payload.setKey(jsonObject.optString(ShortPayloadConstant.KEY));
                            }
                            if (globalPayloadObject.has(ShortPayloadConstant.ID)) {
                                payload.setId(globalPayloadObject.optString(ShortPayloadConstant.ID));
                            } else {
                                payload.setId(jsonObject.optString(ShortPayloadConstant.ID));
                            }
                            if (globalPayloadObject.has(ShortPayloadConstant.RID)) {
                                payload.setRid(globalPayloadObject.optString(ShortPayloadConstant.RID));
                            } else {
                                payload.setRid(jsonObject.optString(ShortPayloadConstant.RID));
                            }

                            payload.setFetchURL(jsonObject.optString(ShortPayloadConstant.FETCHURL).replace("~", ""));
                            payload.setLink(jsonObject.optString(ShortPayloadConstant.LINK).replace("~", ""));
                            payload.setTitle(jsonObject.optString(ShortPayloadConstant.TITLE).replace("~", ""));
                            payload.setMessage(jsonObject.optString(ShortPayloadConstant.NMESSAGE).replace("~", ""));
                            payload.setIcon(jsonObject.optString(ShortPayloadConstant.ICON).replace("~", ""));
                            if (globalPayloadObject.has(ShortPayloadConstant.REQINT)) {
                                payload.setReqInt(globalPayloadObject.optInt(ShortPayloadConstant.REQINT));
                            } else {
                                payload.setReqInt(jsonObject.optInt(ShortPayloadConstant.REQINT));
                            }
                            if (globalPayloadObject.has(ShortPayloadConstant.TAG)) {
                                payload.setTag(globalPayloadObject.optString(ShortPayloadConstant.TAG).replace("~", ""));
                            } else {
                                payload.setTag(jsonObject.optString(ShortPayloadConstant.TAG).replace("~", ""));
                            }
                            if (globalPayloadObject.has(ShortPayloadConstant.ACT1NAME)) {
                                payload.setAct1name(globalPayloadObject.optString(ShortPayloadConstant.ACT1NAME));
                            } else {
                                payload.setAct1name(jsonObject.optString(ShortPayloadConstant.ACT1NAME));
                            }
                            if (globalPayloadObject.has(ShortPayloadConstant.ACT1LINK)) {
                                payload.setAct1link(globalPayloadObject.optString(ShortPayloadConstant.ACT1LINK).replace("~", ""));
                            } else {
                                payload.setAct1link(jsonObject.optString(ShortPayloadConstant.ACT1LINK).replace("~", ""));
                            }
                            payload.setBanner(jsonObject.optString(ShortPayloadConstant.BANNER).replace("~", ""));
                            payload.setAct_num(jsonObject.optInt(ShortPayloadConstant.ACTNUM));
                            payload.setBadgeicon(jsonObject.optString(ShortPayloadConstant.BADGE_ICON));
                            payload.setBadgecolor(jsonObject.optString(ShortPayloadConstant.BADGE_COLOR));
                            payload.setSubTitle(jsonObject.optString(ShortPayloadConstant.SUBTITLE).replace("~", ""));
                            payload.setGroup(jsonObject.optInt(ShortPayloadConstant.GROUP));
                            payload.setBadgeCount(jsonObject.optInt(ShortPayloadConstant.BADGE_COUNT));
                            // Button 1
                            payload.setAct1icon(jsonObject.optString(ShortPayloadConstant.ACT1ICON).replace("~", ""));
                            payload.setAct1ID(jsonObject.optString(ShortPayloadConstant.ACT1ID).replace("~", ""));
                            // Button 2
                            payload.setAct2name(jsonObject.optString(ShortPayloadConstant.ACT2NAME));
                            payload.setAct2link(jsonObject.optString(ShortPayloadConstant.ACT2LINK));
                            payload.setAct2icon(jsonObject.optString(ShortPayloadConstant.ACT2ICON));
                            payload.setAct2ID(jsonObject.optString(ShortPayloadConstant.ACT2ID));
                            payload.setInapp(jsonObject.optInt(ShortPayloadConstant.INAPP));
                            payload.setTrayicon(jsonObject.optString(ShortPayloadConstant.TARYICON).replace("~", ""));
                            payload.setSmallIconAccentColor(jsonObject.optString(ShortPayloadConstant.ICONCOLOR).replace("~", ""));
                            payload.setSound(jsonObject.optString(ShortPayloadConstant.SOUND).replace("~", ""));
                            payload.setLedColor(jsonObject.optString(ShortPayloadConstant.LEDCOLOR).replace("~", ""));
                            payload.setLockScreenVisibility(jsonObject.optInt(ShortPayloadConstant.VISIBILITY));
                            payload.setGroupKey(jsonObject.optString(ShortPayloadConstant.GKEY).replace("~", ""));
                            payload.setGroupMessage(jsonObject.optString(ShortPayloadConstant.GMESSAGE).replace("~", ""));
                            payload.setFromProjectNumber(jsonObject.optString(ShortPayloadConstant.PROJECTNUMBER).replace("~", ""));
                            payload.setCollapseId(jsonObject.optString(ShortPayloadConstant.COLLAPSEID).replace("~", ""));
                            payload.setPriority(jsonObject.optInt(ShortPayloadConstant.PRIORITY));
                            payload.setRawPayload(jsonObject.optString(ShortPayloadConstant.RAWDATA).replace("~", ""));
                            payload.setAp(jsonObject.optString(ShortPayloadConstant.ADDITIONALPARAM).replace("~", ""));
                            if (globalPayloadObject.has(ShortPayloadConstant.CFG)) {
                                payload.setCfg(globalPayloadObject.optInt(ShortPayloadConstant.CFG));
                            } else {
                                payload.setCfg(jsonObject.optInt(ShortPayloadConstant.CFG));
                            }
                            payload.setPush_type(AppConstant.PUSH_FCM);
                            payload.setPublic_global_key(url);
                            payload.setSound(jsonObject.optString(ShortPayloadConstant.NOTIFICATION_SOUND).replace("~", ""));
                            payload.setMaxNotification(jsonObject.optInt(ShortPayloadConstant.MAX_NOTIFICATION));
                            payload.setFallBackDomain(jsonObject.optString(ShortPayloadConstant.FALL_BACK_DOMAIN).replace("~", ""));
                            payload.setFallBackSubDomain(jsonObject.optString(ShortPayloadConstant.FALLBACK_SUB_DOMAIN).replace("~", ""));
                            payload.setFallBackPath(jsonObject.optString(ShortPayloadConstant.FAll_BACK_PATH).replace("~", ""));
                            DebugFileManager.createExternalStoragePublic(iZooto.appContext, response, "gpl_payload");


                            if (payload.getTitle() != null && !payload.getTitle().isEmpty()) {
                                iZooto.processNotificationReceived(iZooto.appContext, payload);
                                JSONObject storeObject = new JSONObject();
                                storeObject.put(AppConstant.IZ_GPL_URL, url);
                                storeObject.put("PayloadData", jsonObject.toString());
                                preferenceUtil.setStringData(AppConstant.STORAGE_PAYLOAD_DATA, storeObject.toString());
                            } else {
                                String fallBackURL = callFallbackAPI(payload);
                                showFallBackResponse(fallBackURL, payload);
                                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                                String cTime = preferenceUtil.getStringData("iz_gplPayload");
                                if (!cTime.equalsIgnoreCase(Util.getTime())) {
                                    preferenceUtil.setStringData("iz_gplPayload", Util.getTime());
                                    NotificationEventManager.handleNotificationError("Payload title is empty", payload.toString(), "NotificationEventManager", "globalPayload");
                                }
                            }
                        } catch (Exception ex) {
                            DebugFileManager.createExternalStoragePublic(iZooto.appContext, ex.toString(), "[Log.e]->globalPayload");
                            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), "globalPayload", AppConstant.IZ_AD_MEDIATION_CLASS);// handle exception one time
                        }
                    }


                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                        String fallBackURL = callFallbackAPI(payload);
                        showFallBackResponse(fallBackURL, payload);
                    }
                });
            } else {
                String payloadString = preferenceUtil.getStringData(AppConstant.STORAGE_PAYLOAD_DATA);
                try {
                    if (payloadString != null && !payloadString.isEmpty()) {
                        JSONObject getObjectData = new JSONObject(payloadString);
                        String jsonData = getObjectData.optString("PayloadData");
                        JSONObject jsonObject = new JSONObject(jsonData.replace("\n", ""));
                        payload.setCreated_Time(globalPayloadObject.optString(ShortPayloadConstant.CREATEDON).replace("~", ""));
                        if (globalPayloadObject.has(ShortPayloadConstant.CREATEDON)) {
                            payload.setCreated_Time(globalPayloadObject.optString(ShortPayloadConstant.CREATEDON));
                        } else {
                            payload.setCreated_Time(jsonObject.optString(ShortPayloadConstant.CREATEDON));
                        }
                        if (globalPayloadObject.has(ShortPayloadConstant.KEY)) {
                            payload.setKey(globalPayloadObject.optString(ShortPayloadConstant.KEY));
                        } else {
                            payload.setKey(jsonObject.optString(ShortPayloadConstant.KEY));
                        }
                        if (globalPayloadObject.has(ShortPayloadConstant.ID)) {
                            payload.setId(globalPayloadObject.optString(ShortPayloadConstant.ID));
                        } else {
                            payload.setId(jsonObject.optString(ShortPayloadConstant.ID));
                        }
                        if (globalPayloadObject.has(ShortPayloadConstant.RID)) {
                            payload.setRid(globalPayloadObject.optString(ShortPayloadConstant.RID));
                        } else {
                            payload.setRid(jsonObject.optString(ShortPayloadConstant.RID));
                        }

                        payload.setFetchURL(jsonObject.optString(ShortPayloadConstant.FETCHURL).replace("~", ""));
                        payload.setLink(jsonObject.optString(ShortPayloadConstant.LINK).replace("~", ""));
                        payload.setTitle(jsonObject.optString(ShortPayloadConstant.TITLE).replace("~", ""));
                        payload.setMessage(jsonObject.optString(ShortPayloadConstant.NMESSAGE).replace("~", ""));
                        payload.setIcon(jsonObject.optString(ShortPayloadConstant.ICON).replace("~", ""));
                        if (globalPayloadObject.has(ShortPayloadConstant.REQINT)) {
                            payload.setReqInt(globalPayloadObject.optInt(ShortPayloadConstant.REQINT));
                        } else {
                            payload.setReqInt(jsonObject.optInt(ShortPayloadConstant.REQINT));
                        }
                        if (globalPayloadObject.has(ShortPayloadConstant.TAG)) {
                            payload.setTag(globalPayloadObject.optString(ShortPayloadConstant.TAG).replace("~", ""));
                        } else {
                            payload.setTag(jsonObject.optString(ShortPayloadConstant.TAG).replace("~", ""));
                        }
                        if (globalPayloadObject.has(ShortPayloadConstant.ACT1NAME)) {
                            payload.setAct1name(globalPayloadObject.optString(ShortPayloadConstant.ACT1NAME));
                        } else {
                            payload.setAct1name(jsonObject.optString(ShortPayloadConstant.ACT1NAME));
                        }
                        if (globalPayloadObject.has(ShortPayloadConstant.ACT1LINK)) {
                            payload.setAct1link(globalPayloadObject.optString(ShortPayloadConstant.ACT1LINK).replace("~", ""));
                        } else {
                            payload.setAct1link(jsonObject.optString(ShortPayloadConstant.ACT1LINK).replace("~", ""));
                        }
                        payload.setBanner(jsonObject.optString(ShortPayloadConstant.BANNER).replace("~", ""));
                        payload.setAct_num(jsonObject.optInt(ShortPayloadConstant.ACTNUM));
                        payload.setBadgeicon(jsonObject.optString(ShortPayloadConstant.BADGE_ICON));
                        payload.setBadgecolor(jsonObject.optString(ShortPayloadConstant.BADGE_COLOR));
                        payload.setSubTitle(jsonObject.optString(ShortPayloadConstant.SUBTITLE).replace("~", ""));
                        payload.setGroup(jsonObject.optInt(ShortPayloadConstant.GROUP));
                        payload.setBadgeCount(jsonObject.optInt(ShortPayloadConstant.BADGE_COUNT));
                        // Button 1
                        payload.setAct1icon(jsonObject.optString(ShortPayloadConstant.ACT1ICON).replace("~", ""));
                        payload.setAct1ID(jsonObject.optString(ShortPayloadConstant.ACT1ID).replace("~", ""));
                        // Button 2
                        payload.setAct2name(jsonObject.optString(ShortPayloadConstant.ACT2NAME).replace("~", ""));
                        payload.setAct2link(jsonObject.optString(ShortPayloadConstant.ACT2LINK).replace("~", ""));
                        payload.setAct2icon(jsonObject.optString(ShortPayloadConstant.ACT2ICON).replace("~", ""));
                        payload.setAct2ID(jsonObject.optString(ShortPayloadConstant.ACT2ID).replace("~", ""));

                        payload.setInapp(jsonObject.optInt(ShortPayloadConstant.INAPP));
                        payload.setTrayicon(jsonObject.optString(ShortPayloadConstant.TARYICON).replace("~", ""));
                        payload.setSmallIconAccentColor(jsonObject.optString(ShortPayloadConstant.ICONCOLOR).replace("~", ""));
                        payload.setSound(jsonObject.optString(ShortPayloadConstant.SOUND).replace("~", ""));
                        payload.setLedColor(jsonObject.optString(ShortPayloadConstant.LEDCOLOR).replace("~", ""));
                        payload.setLockScreenVisibility(jsonObject.optInt(ShortPayloadConstant.VISIBILITY));
                        payload.setGroupKey(jsonObject.optString(ShortPayloadConstant.GKEY).replace("~", ""));
                        payload.setGroupMessage(jsonObject.optString(ShortPayloadConstant.GMESSAGE).replace("~", ""));
                        payload.setFromProjectNumber(jsonObject.optString(ShortPayloadConstant.PROJECTNUMBER).replace("~", ""));
                        payload.setCollapseId(jsonObject.optString(ShortPayloadConstant.COLLAPSEID).replace("~", ""));
                        payload.setPriority(jsonObject.optInt(ShortPayloadConstant.PRIORITY));
                        payload.setRawPayload(jsonObject.optString(ShortPayloadConstant.RAWDATA).replace("~", ""));
                        payload.setAp(jsonObject.optString(ShortPayloadConstant.ADDITIONALPARAM).replace("~", ""));
                        if (globalPayloadObject.has(ShortPayloadConstant.CFG)) {
                            payload.setCfg(globalPayloadObject.optInt(ShortPayloadConstant.CFG));
                        } else {
                            payload.setCfg(jsonObject.optInt(ShortPayloadConstant.CFG));
                        }
                        payload.setPush_type(AppConstant.PUSH_FCM);
                        payload.setPublic_global_key(url);
                        payload.setSound(jsonObject.optString(ShortPayloadConstant.NOTIFICATION_SOUND).replace("~", ""));
                        payload.setMaxNotification(jsonObject.optInt(ShortPayloadConstant.MAX_NOTIFICATION));
                        payload.setFallBackDomain(jsonObject.optString(ShortPayloadConstant.FALL_BACK_DOMAIN).replace("~", ""));
                        payload.setFallBackSubDomain(jsonObject.optString(ShortPayloadConstant.FALLBACK_SUB_DOMAIN).replace("~", ""));
                        payload.setFallBackPath(jsonObject.optString(ShortPayloadConstant.FAll_BACK_PATH).replace("~", ""));
                        Log.v(AppConstant.NOTIFICATION_MESSAGE, "YES");
                        if (payload.getTitle() != null && !payload.getTitle().isEmpty()) {
                            iZooto.processNotificationReceived(iZooto.appContext, payload);
                        } else {
                            String fallBackURL = callFallbackAPI(payload);
                            showFallBackResponse(fallBackURL, payload);
                        }
                    }

                } catch (Exception ex) {
                    String fallBackURL = callFallbackAPI(payload);
                    showFallBackResponse(fallBackURL, payload);
                }
            }
        }
    }

    static String checkURL(String jsonString) {
        String returnString = "";
        try {
            JSONObject objectData = new JSONObject(jsonString);
            returnString = objectData.optString(AppConstant.IZ_GPL_URL);
            return returnString;
        } catch (Exception ex) {
            return returnString;
        }
    }


    private static void passivePayload(final Payload payload) {
        final long start = System.currentTimeMillis(); //fetch start time
        RestClient.get(payload.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {
                        Object json = new JSONTokener(response).nextValue();
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("b", Double.parseDouble(payload.getCpc()));
                        jsonObject1.put("rb", Double.parseDouble(payload.getReceived_bid()));
                        jsonObject1.put("a", payload.getAdID());
                        jsonObject1.put("t", (System.currentTimeMillis() - start));
                        successList.add(jsonObject1);
                        if (json instanceof JSONObject) {
                            JSONObject jsonObject = new JSONObject(response);
                            payload.setResponseTime((System.currentTimeMillis() - start));
                            parsePassiveJson(payload, jsonObject);
                        } else if (json instanceof JSONArray) {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("", jsonArray);
                            payload.setResponseTime((System.currentTimeMillis() - start));
                            parsePassiveJson(payload, jsonObject);
                        }
                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "fetchPassiveAPI");
                    }
                }

            }


            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                String fallBackAPI = callFallbackAPI(payload);
                showFallBackResponse(fallBackAPI, payload);

            }
        });
    }


    private static void defeaterPayload(final Payload payload, int... adIndex) {
        try {
            if (iZooto.appContext == null) {
                return;
            }
            setDefeaterPayloadAppear(payload, adIndex);

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "defeaterPayload");
        }
    }


    static String callFallbackAPI(Payload payload) {
        String domain = "flbk.izooto.com";
        try {
            if (!Objects.equals(payload.getFallBackSubDomain(), "")) {
                domain = payload.getFallBackSubDomain() + ".izooto.com";
            } else if (!Objects.equals(payload.getFallBackDomain(), "")) {
                domain = payload.getFallBackDomain();
            }
            String path = "default.json";
            if (!Objects.equals(payload.getFallBackPath(), ""))
                path = payload.getFallBackPath();
            return "https://" + domain + "/" + path;

        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex + domain, "Fallback", "callFallbackAPI");
        }
        return "";
    }


    static void parsePassiveJson(Payload payload1, JSONObject jsonObject) {
        if (iZooto.appContext == null) {
            return;
        }

        String dataValue;
        try {
            if (payload1.getTitle() != null && !payload1.getTitle().isEmpty())
                payload1.setTitle(getParsedValue(jsonObject, payload1.getTitle()));
            if (payload1.getMessage() != null && !payload1.getMessage().isEmpty())
                payload1.setMessage(getParsedValue(jsonObject, payload1.getMessage()));

            if (payload1.getLink() != null && !payload1.getLink().isEmpty()) {
                payload1.setLink(getParsedValue(jsonObject, payload1.getLink()));
            }

            payload1.setCpc(getParsedValue(jsonObject, payload1.getCpc()));
            payload1.setReceived_bid(getParsedValue(jsonObject, payload1.getReceived_bid()));
            if (payload1.getLink() != null && !payload1.getLink().isEmpty()) {
                if (!payload1.getLink().startsWith("http://") && !payload1.getLink().startsWith("https://")) {
                    String url = payload1.getLink();
                    url = "https://" + url;
                    payload1.setLink(url);

                }
            }

            if (payload1.getBanner() != null && !payload1.getBanner().isEmpty())
                payload1.setBanner(getParsedValue(jsonObject, payload1.getBanner()));
            if (payload1.getIcon() != null && !payload1.getIcon().isEmpty())
                payload1.setIcon(getParsedValue(jsonObject, payload1.getIcon()));

            payload1.setAct1link(getParsedValue(jsonObject, payload1.getAct1link()));
            payload1.setCtr(getParsedValue(jsonObject, payload1.getCtr()));
            payload1.setCpm(getParsedValue(jsonObject, payload1.getCpm()));
            payload1.setReceived_bid(getParsedValue(jsonObject, payload1.getReceived_bid()));


            if (payload1.getAct_num() == 1) {

                if (payload1.getAct1link() != null) {
                    payload1.setAct1name(payload1.getAct1name().replace("~", ""));
                }
                if (!payload1.getAct1link().startsWith("http://") && !payload1.getAct1link().startsWith("https://")) {
                    String url = payload1.getAct1link();
                    url = "https://" + url;
                    payload1.setAct1link(url);


                }
                if (payload1.getAct2name() != null && !payload1.getAct2name().isEmpty())
                    payload1.setAct2name(payload1.getAct2name().replace("~", ""));
                payload1.setAct2link(getParsedValue(jsonObject, payload1.getAct2link()));
                if (!payload1.getAct2link().startsWith("http://") && !payload1.getAct2link().startsWith("https://")) {
                    String url = payload1.getAct2link();
                    url = "https://" + url;
                    payload1.setAct2link(url);
                }
            }
            if (payload1.getIcon() != null && !Objects.equals(payload1.getIcon(), "")) {
                if (!payload1.getIcon().startsWith("http://") && !payload1.getIcon().startsWith("https://")) {
                    String url = payload1.getIcon();
                    url = "https://" + url;
                    payload1.setIcon(url);
                }
            }
            if (payload1.getBanner() != null && !Objects.equals(payload1.getBanner(), "")) {
                if (!payload1.getBanner().startsWith("http://") && !payload1.getBanner().startsWith("https://")) {
                    String url = payload1.getBanner();
                    url = "https://" + url;
                    payload1.setBanner(url);
                }
            }

            try {
                if (payload1.getRv() != null && !payload1.getRv().isEmpty()) {
                    try {
                        String rv = Util.setParsedRcAndRvValues(payload1.getRv(), jsonObject);
                        if (!Utilities.isNullOrEmpty(rv)) {
                            payload1.setRv(rv);
                        }
                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseThoroughlyJson");
                    }

                }
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "parseRvValues");
            }
            try {
                if (payload1.getRc() != null && !payload1.getRc().isEmpty()) {
                    try {
                        String rc = Util.setParsedRcAndRvValues(payload1.getRc(), jsonObject);
                        if (!Utilities.isNullOrEmpty(rc)) {
                            clicksData.add(rc);
                        }

                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseThoroughlyJson");
                    }
                }
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationEventManager", "parseRvValues");
            }

            if (!successList.isEmpty()) {
                JSONObject finalData = new JSONObject();
                finalData.put("pid", PreferenceUtil.getInstance(iZooto.appContext).getiZootoID(AppConstant.APPPID));
                finalData.put("rid", payload1.getRid());
                finalData.put("type", payload1.getAd_type());
                finalData.put("ta", (System.currentTimeMillis() - payload1.getStartTime()));
                finalData.put("av", AppConstant.SDKVERSION);
                finalData.put("bKey", Util.getAndroidId(iZooto.appContext));
                finalData.put("result", payload1.getAdID());
                JSONObject servedObject = new JSONObject();
                servedObject.put("a", payload1.getAdID());
                servedObject.put("b", Double.parseDouble(payload1.getCpc()));
                servedObject.put("t", payload1.getResponseTime());
                if (payload1.getReceived_bid() != null && !payload1.getReceived_bid().isEmpty() && !Objects.equals(payload1.getReceived_bid(), "")) {
                    servedObject.put("rb", Double.parseDouble(payload1.getReceived_bid()));
                }
                servedObject.put("ti", payload1.getTitle());
                if (payload1.getLink() != null && !payload1.getLink().isEmpty()) {
                    servedObject.put("ln", payload1.getLink());
                } else {
                    servedObject.put("ln", "");
                }

                finalData.put("served", servedObject);
                successList.addAll(failsList);
                JSONArray jsonArray = new JSONArray(successList);
                finalData.put("bids", jsonArray);
                dataValue = finalData.toString().replaceAll("\\\\", " ");
                mediationImpression(dataValue, 0);
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    TargetActivity.medClick = dataValue;
                    preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
                } else {
                    preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
                    NotificationActionReceiver.medClick = dataValue;
                }
                if (payload1.getTitle() != null && !payload1.getTitle().isEmpty()) {
                    NotificationEventManager.notificationPreview(iZooto.appContext, payload1);
                    Log.v(AppConstant.NOTIFICATION_MESSAGE, AppConstant.YES);
                } else {
                    String fallBackURL = callFallbackAPI(payload);
                    showFallBackResponse(fallBackURL, payload);
                    Log.v(AppConstant.NOTIFICATION_MESSAGE, AppConstant.NO);
                }
            } else {
                String fallBackURL = callFallbackAPI(payload);
                showFallBackResponse(fallBackURL, payload);
                Log.v(AppConstant.NOTIFICATION_MESSAGE, AppConstant.NO);
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, AppConstant.IZ_PAYLOAD_ERROR + e, AppConstant.IZ_AD_MEDIATION_CLASS, "parseAgainJson");
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, e.toString(), "[Log.e]->AdMediation 868");
        }

    }

    static void showFallBackResponse(String fallBackAPI, final Payload payload) {
        RestClient.get(fallBackAPI, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    payload.setTitle(jsonObject.optString(ShortPayloadConstant.TITLE));
                    payload.setMessage(jsonObject.optString(ShortPayloadConstant.NMESSAGE));
                    payload.setLink(jsonObject.optString(ShortPayloadConstant.LINK));
                    payload.setIcon(jsonObject.optString(ShortPayloadConstant.ICON));
                    payload.setBanner(jsonObject.optString(ShortPayloadConstant.BANNER));
                    String act1Link = jsonObject.optString(ShortPayloadConstant.ACT1LINK);
                    String act1Name = jsonObject.optString(ShortPayloadConstant.ACT1NAME);
                    if (!Utilities.isNullOrEmpty(act1Link)) {
                        payload.setAct1link(act1Link);
                    } else {
                        payload.setAct1link(jsonObject.optString(ShortPayloadConstant.LINK));

                    }
                    if (!Utilities.isNullOrEmpty(act1Name)) {
                        payload.setAct1name(act1Name);
                    }
                    String act2Link = jsonObject.optString(ShortPayloadConstant.ACT2LINK);
                    String act2Name = jsonObject.optString(ShortPayloadConstant.ACT2NAME);
                    if (!Utilities.isNullOrEmpty(act2Link)) {
                        payload.setAct1link(act2Link);
                    } else {
                        payload.setAct2link(jsonObject.optString(ShortPayloadConstant.LINK));
                    }
                    if (!Utilities.isNullOrEmpty(act2Name)) {
                        payload.setAct1name(act2Name);
                    }
                    payload.setRid(payload.getRid());
                    NotificationEventManager.notificationPreview(iZooto.appContext, payload);
                    showClickAndImpressionData(payload);
                } catch (Exception ex) {
                    Util.handleExceptionOnce(iZooto.appContext, ex.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "ShowFallBackResponse");// need to one time sends exception
                }
            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
            }
        });
    }


    private static void showClickAndImpressionData(Payload payload) {
        if (iZooto.appContext == null) {
            return;
        }
        try {
            JSONObject finalData = new JSONObject();
            finalData.put("pid", PreferenceUtil.getInstance(iZooto.appContext).getiZootoID(AppConstant.APPPID));
            finalData.put("rid", payload.getRid());
            finalData.put("type", payload.getAd_type());
            finalData.put("ta", (System.currentTimeMillis() - payload.getStartTime()));
            finalData.put("av", AppConstant.SDKVERSION);
            finalData.put("bKey", Util.getAndroidId(iZooto.appContext));
            finalData.put("result", 0);
            JSONObject servedObject = new JSONObject();
            servedObject.put("a", 0);
            servedObject.put("b", 0);
            if (payload.getResponseTime() == 0) {
                servedObject.put("t", -1);
            } else {
                servedObject.put("t", payload.getResponseTime());
            }
            if (payload.getReceived_bid() != null && !payload.getReceived_bid().isEmpty()) {
                try {
                    servedObject.put("rb", Double.parseDouble(payload.getReceived_bid()));
                } catch (Exception e) {
                    Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "showClickAndImpressionData");
                }
            }
            servedObject.put("ti", payload.getTitle());
            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                servedObject.put("ln", payload.getLink());
            } else {
                servedObject.put("ln", "");
            }
            finalData.put("served", servedObject);

            successList.addAll(failsList);
            JSONArray jsonArray = new JSONArray(successList);
            finalData.put("bids", jsonArray);
            String dataValue = finalData.toString().replaceAll("\\\\", " ");
            mediationImpression(dataValue, 0);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                TargetActivity.medClick = dataValue;
                preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
            } else {
                preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
                NotificationActionReceiver.medClick = dataValue;
            }
            Log.v(AppConstant.NOTIFICATION_MESSAGE, AppConstant.YES);
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "ShowCLCIKAndImpressionData");// need to one time sends exception
        }

    }


    private static void setDefeaterPayloadAppear(Payload payload, int[] adIndex) {
        String dataValue;
        try {
            if (payload == null) {
                return;
            }
            if (payload.getTitle() != null && !payload.getTitle().equalsIgnoreCase("")) {
                JSONObject finalData = new JSONObject();
                finalData.put("pid", PreferenceUtil.getInstance(iZooto.appContext).getiZootoID(AppConstant.APPPID));
                finalData.put("rid", payload.getRid());
                finalData.put("type", payload.getAd_type());
                finalData.put("ta", (System.currentTimeMillis() - payload.getStartTime()));
                finalData.put("av", AppConstant.SDKVERSION);
                finalData.put("bKey", Util.getAndroidId(iZooto.appContext));
                finalData.put("result", payload.getAdID());
                JSONObject servedObject = new JSONObject();
                servedObject.put("a", payload.getAdID());
                servedObject.put("b", Double.parseDouble(payload.getCpc()));
                servedObject.put("t", payload.getResponseTime());
                if (payload.getReceived_bid() != null && !payload.getReceived_bid().isEmpty() && !Objects.equals(payload.getReceived_bid(), "")) {
                    servedObject.put("rb", Double.parseDouble(payload.getReceived_bid()));
                }
                servedObject.put("ti", payload.getTitle());
                if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                    servedObject.put("ln", payload.getLink());
                } else {
                    servedObject.put("ln", "");
                }

                finalData.put("served", servedObject);
                successList.addAll(failsList);
                JSONArray jsonArray = new JSONArray(successList);
                if (adIndex.length + 5 == 6) {
                    finalData.put("bids", jsonArray);
                } else {
                    finalData.put("bids", "");
                }
                if (payload.getRv() != null && !payload.getRv().isEmpty()) {
                    NotificationEventManager.callRandomView(payload.getRv());
                }

                dataValue = finalData.toString().replaceAll("\\\\", " ");
                mediationImpression(dataValue, 0);
                NotificationEventManager.notificationPreview(iZooto.appContext, payload);
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    TargetActivity.medClick = dataValue;
                    preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
                } else {
                    preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
                    NotificationActionReceiver.medClick = dataValue;
                }
                Log.v(AppConstant.NOTIFICATION_MESSAGE, AppConstant.YES);
            } else {
                String fallBackAPI = callFallbackAPI(payload);
                showFallBackResponse(fallBackAPI, payload);
                Log.v(AppConstant.NOTIFICATION_MESSAGE, AppConstant.NO);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "parseAgainJson");// need to one time sends exception
        }

    }


    static void showClicksAndImpressionData(Payload payload) {
        if (iZooto.appContext == null) {
            return;
        }
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            JSONObject finalData = new JSONObject();
            finalData.put("pid", preferenceUtil.getiZootoID(AppConstant.APPPID));
            finalData.put("rid", payload.getRid());
            finalData.put("type", payload.getAd_type());
            finalData.put("ta", (System.currentTimeMillis() - payload.getStartTime()));
            finalData.put("av", AppConstant.SDKVERSION);
            finalData.put("bKey", Util.getAndroidId(iZooto.appContext));
            finalData.put("result", payload.getAdID());
            JSONObject servedObject = new JSONObject();

            servedObject.put("a", 0);
            servedObject.put("b", 0);

            if (payload.getResponseTime() == 0) {
                servedObject.put("t", -1);
            } else {
                servedObject.put("t", payload.getResponseTime());
            }
            if (payload.getReceived_bid() != null && !payload.getReceived_bid().isEmpty() && !Objects.equals(payload.getReceived_bid(), "")) {
                servedObject.put("rb", Double.parseDouble(payload.getReceived_bid()));
            }
            servedObject.put("ti", payload.getTitle());
            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                servedObject.put("ln", payload.getLink());
            } else {
                servedObject.put("ln", "");
            }
            finalData.put("served", servedObject);
            successList.addAll(failsList);
            JSONArray jsonArray = new JSONArray(successList);
            finalData.put("bids", jsonArray);
            String dataValue = finalData.toString().replaceAll("\\\\", " ");
            mediationImpression(dataValue, 0);
            NotificationActionReceiver.medClick = dataValue;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                TargetActivity.medClick = dataValue;
                preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
            } else {
                preferenceUtil.setStringData(AppConstant.IZ_MEDIATION_CLICK_DATA, dataValue);
                NotificationActionReceiver.medClick = dataValue;
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, AppConstant.IZ_PAYLOAD_ERROR + ex + payload.getRid(), AppConstant.IZ_AD_MEDIATION_CLASS, "ShowClickAndImpressionData");
        }
    }

    static void mediationImpression(String finalData, int impNUmber) {
        if (iZooto.appContext == null) {
            return;
        }
        try {
            if (!successList.isEmpty()) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, storeList.toString(), "successResponseMediation");
            }
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, finalData, "mediation_impression");
            JSONObject jsonObject = new JSONObject(finalData);
            RestClient.postRequest(RestClient.MEDIATION_IMPRESSION, null, jsonObject, new RestClient.ResponseHandler() {
                @SuppressLint("NewApi")
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                    if (!preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS).isEmpty() && impNUmber >= 0) {
                        try {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS));
                            jsonArrayOffline.remove(impNUmber);
                            preferenceUtil.setStringData(AppConstant.STORE_MEDIATION_RECORDS, null);
                        } catch (Exception ex) {
                            DebugFileManager.createExternalStoragePublic(iZooto.appContext, ex.toString(), "[Log.e]-> ");
                        }

                    }
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    Util.trackMediation_Impression_Click(iZooto.appContext, AppConstant.MED_IMPRESION, jsonObject.toString());
                }
            });
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex + finalData, AppConstant.IZ_AD_MEDIATION_CLASS, "mediationImpression");
        }
    }

    static String getParsedValue(JSONObject jsonObject, String sourceString) {
        try {
            if (sourceString.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                return sourceString; // Directly return numeric values
            }
            if (sourceString.startsWith("~")) {
                return sourceString.substring(1); // Remove "~" prefix
            }

            String[] keys = sourceString.split("\\.");
            return extractValue(jsonObject, keys, 0);
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.IZ_AD_MEDIATION_CLASS, "getParsedValue");
            return "";
        }
    }

    /**
     * 🔹 Recursive method to extract nested values
     */
    private static String extractValue(JSONObject jsonObject, String[] keys, int index) {
        if (jsonObject == null || index >= keys.length) {
            return "";
        }

        String key = keys[index];

        if (key.contains("[")) {  // Handling JSON arrays (e.g., `list[0]`)
            String[] parts = key.split("\\[");
            String arrayKey = parts[0];
            int arrayIndex = Integer.parseInt(parts[1].replace("]", ""));

            JSONArray jsonArray = jsonObject.optJSONArray(arrayKey);
            if (jsonArray != null && arrayIndex < jsonArray.length()) {
                JSONObject nextJson = jsonArray.optJSONObject(arrayIndex);
                return (index == keys.length - 1) ? jsonArray.optString(arrayIndex) : extractValue(nextJson, keys, index + 1);
            }
        } else {
            JSONObject nextJson = jsonObject.optJSONObject(key);
            if (nextJson != null) {
                return extractValue(nextJson, keys, index + 1);
            }
            return jsonObject.optString(key, ""); // Return value if last key
        }
        return "";
    }

}
