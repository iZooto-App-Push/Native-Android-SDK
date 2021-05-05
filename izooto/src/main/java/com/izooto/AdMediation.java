package com.izooto;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class AdMediation {
    private static Payload payload;
    private static List<Payload> payloadList=new ArrayList<>();
    private static List<Payload> adPayload =new ArrayList<>();
    private static List<Payload> passiveList=new ArrayList<>();
    private static List<JSONObject> failsList=new ArrayList<>();
    public static List<String> clicksData=new ArrayList<>();
    private static List<JSONObject> successList=new ArrayList<>();

    public static void getAdJsonData(Map<String,String> data)
    {

       try
       {
            PreferenceUtil preferenceUtil=PreferenceUtil.getInstance(iZooto.appContext);
              payloadList.clear();
              passiveList.clear();
              adPayload.clear();
              clicksData.clear();
               successList.clear();
               failsList.clear();
                JSONArray jsonArray =new JSONArray(data.get(AppConstant.AD_NETWORK));
               JSONObject jsonObject=new JSONObject(data.get(AppConstant.GLOBAL));

              long start = System.currentTimeMillis(); //fetch start time
               if (jsonArray.length() > 0) {
                       for (int i = 0; i < jsonArray.length(); i++) {
                           JSONObject payloadObj = jsonArray.getJSONObject(i);
                           if (jsonObject.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(iZooto.appContext).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                               payload = new Payload();
                               payload.setAd_type(jsonObject.optString(ShortpayloadConstant.AD_TYPE));
                               payload.setAdID(payloadObj.optString(ShortpayloadConstant.AD_ID));
                               payload.setReceived_bid(payloadObj.optString(ShortpayloadConstant.RECEIVED_BID).replace("['","").replace("']",""));
                               payload.setFetchURL(payloadObj.optString(ShortpayloadConstant.FETCHURL));
                               payload.setKey(jsonObject.optString(ShortpayloadConstant.KEY));
                               payload.setId(payloadObj.optString(ShortpayloadConstant.ID).replace("['","").replace("']",""));
                               payload.setStartTime(start);
                               payload.setRid(jsonObject.optString(ShortpayloadConstant.RID));
                               payload.setLink(payloadObj.optString(ShortpayloadConstant.LINK).replace("['","").replace("']",""));
                               payload.setTitle(payloadObj.optString(ShortpayloadConstant.TITLE).replace("['","").replace("']",""));
                               payload.setMessage(payloadObj.optString(ShortpayloadConstant.NMESSAGE).replace("['","").replace("']",""));
                               payload.setIcon(payloadObj.optString(ShortpayloadConstant.ICON).replace("['","").replace("']",""));
                               payload.setReqInt(payloadObj.optInt(ShortpayloadConstant.REQINT));
                               payload.setTag(payloadObj.optString(ShortpayloadConstant.TAG));
                               payload.setBanner(payloadObj.optString(ShortpayloadConstant.BANNER).replace("['","").replace("']",""));
                               payload.setBadgeicon(payloadObj.optString(ShortpayloadConstant.BADGE_ICON).replace("['","").replace("']",""));
                               payload.setBadgecolor(payloadObj.optString(ShortpayloadConstant.BADGE_COLOR).replace("['","").replace("']",""));
                               payload.setSubTitle(payloadObj.optString(ShortpayloadConstant.SUBTITLE).replace("['","").replace("']",""));
                               payload.setGroup(payloadObj.optInt(ShortpayloadConstant.GROUP));
                               payload.setBadgeCount(payloadObj.optInt(ShortpayloadConstant.BADGE_COUNT));
                               if(jsonObject.has("b")) {
                                   payload.setAct_num(jsonObject.optInt(ShortpayloadConstant.ACTNUM));
                                   payload.setAct1name(jsonObject.optString(ShortpayloadConstant.ACT1NAME).replace("['","").replace("']",""));
                                   payload.setAct2name(jsonObject.optString(ShortpayloadConstant.ACT2NAME).replace("['","").replace("']",""));

                               }
                               else
                               {
                                   payload.setAct_num(jsonObject.optInt(ShortpayloadConstant.ACTNUM));
                                   payload.setAct1name(payloadObj.optString(ShortpayloadConstant.ACT1NAME).replace("['","").replace("']",""));
                                   payload.setAct2name(payloadObj.optString(ShortpayloadConstant.ACT2NAME).replace("['","").replace("']",""));

                               }


                               // Button 1
                               payload.setAct1link(payloadObj.optString(ShortpayloadConstant.ACT1LINK).replace("['","").replace("']",""));
                               payload.setAct1icon(payloadObj.optString(ShortpayloadConstant.ACT1ICON).replace("['","").replace("']",""));
                               payload.setAct1ID(payloadObj.optString(ShortpayloadConstant.ACT1ID));
                               // Button 2
                               payload.setAct2link(payloadObj.optString(ShortpayloadConstant.ACT2LINK).replace("['","").replace("']",""));
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
                               payload.setCfg(jsonObject.optInt(ShortpayloadConstant.CFG));
                               payload.setCpc(payloadObj.optString(ShortpayloadConstant.CPC).replace("['","").replace("']",""));
                               payload.setRc(payloadObj.optString(ShortpayloadConstant.RC).replace("['","").replace("']",""));
                               payload.setRv(payloadObj.optString(ShortpayloadConstant.RV).replace("['","").replace("']",""));
                               payload.setPassive_flag(payloadObj.optString(ShortpayloadConstant.Passive_Flag));
                               payload.setCpm(payloadObj.optString(ShortpayloadConstant.CPM).replace("['","").replace("']",""));
                               payload.setCtr(payloadObj.optString(ShortpayloadConstant.CTR).replace("['","").replace("']",""));
                               payload.setFallBackDomain(jsonObject.optString(ShortpayloadConstant.FALL_BACK_DOMAIN));
                               payload.setFallBackSubDomain(jsonObject.optString(ShortpayloadConstant.FALLBACK_SUB_DOMAIN));
                               payload.setFallBackPath(jsonObject.optString(ShortpayloadConstant.FAll_BACK_PATH));
                               payload.setTime_out(jsonObject.optInt(ShortpayloadConstant.TIME_OUT));
                               payload.setAdTimeOut(payloadObj.optInt(ShortpayloadConstant.AD_TIME_OUT));
                               if(payload.getPassive_flag().equalsIgnoreCase("1") && jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("6"))
                               {
                                   passiveList.add(payload);
                               }
                               else {
                                   payloadList.add(payload);
                               }
                           } else {
                               return;
                           }
                       }
                       if (payloadList.size() > 0) {
                           if(jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("4"))
                           {
                               processPayload(payloadList.get(0), 4, 0);

                           }
                           if(jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("5"))
                           {
                              preferenceUtil.setBooleanData("Send",true);

                               for (int i = 0; i < payloadList.size(); i++) {
                                   if(preferenceUtil.getBoolean("Send")) {
                                      processPayload(payloadList.get(i), 5, i);
                                      Thread.sleep(2000);
                               }
                              }
                           }
                           if(jsonObject.optString(AppConstant.AD_TYPE).equalsIgnoreCase("6")) {

                               int i=0;
                               do {
                                   processPayload(payloadList.get(i), 6, i);
                                   Thread.sleep(2000);
                                   i++;


                               }while (i<payloadList.size());


                           }
                       }
                   }


       }
       catch (Exception ex)
       {
           Log.v(AppConstant.APP_NAME_TAG,ex.toString());
       }
    }

    private static void callUpdateAPI(String updateURL) {
        RestClient.get(updateURL, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);

            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                Log.e("Failure",""+statusCode);

            }
        });
    }

    private static void processPayload(final Payload payload, final int adIndex,final int indexValue) {
        final long start = System.currentTimeMillis(); //fetch start time
        int calculateTime=0;
       int adTime = payload.getAdTimeOut();
       if(adTime!=0)
           calculateTime=payload.getAdTimeOut();
       else
        calculateTime=payload.getTime_out();


        RestClient.getRequest(payload.getFetchURL(),calculateTime*1000, new RestClient.ResponseHandler(){
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    long end = System.currentTimeMillis(); //fetch end time
                    try {
                        Object json = new JSONTokener(response).nextValue();
                        if (json != null) {
                            if (json instanceof JSONObject) {
                                JSONObject jsonObject = new JSONObject(response);
                                payload.setResponseTime((end - start));
                                payload.setIndex(indexValue);
                                parseJson(payload, jsonObject, adIndex, indexValue);
                            } else if (json instanceof JSONArray) {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("", jsonArray);
                                payload.setResponseTime((end - start));
                                payload.setIndex(indexValue);
                                parseJson(payload, jsonObject, adIndex, indexValue);
                            }
                        }

                    } catch (JSONException e) {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("b", "-1");
                            data.put("a", payload.getAdID());
                            data.put("t", end-start);
                            data.put("rb",-1);
                            failsList.add(data);

                            if(adIndex==4)
                            {
                                String fallBackURL=callFallbackAPI(payload);
                                ShowFallBackResponse(fallBackURL,payload);
                            }

                            if(failsList.size()-1==5)
                            {
                                   String fallBackURL = callFallbackAPI(payload);
                                   ShowFallBackResponse(fallBackURL, payload);

                            }


                        }
                        catch (Exception ignored)
                        {
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
                       data.put("rb",-1);
                      if(statusCode==-1 && payload.getTime_out()!=0 || payload.getAdTimeOut()!=0)
                       data.put("t", -2);
                      else
                          data.put("t", -1);
                    if(adIndex==6)
                    {
                        parseJson(payload, data, adIndex, indexValue);

                    }
                    else
                    {
                         failsList.add(data);

                    }

                       if(adIndex==4)
                       {
                           String fallBackURL=callFallbackAPI(payload);
                           ShowFallBackResponse(fallBackURL,payload);
                       }
                       if(failsList.size()==5)
                       {
                           String fallBackURL=callFallbackAPI(payload);
                           ShowFallBackResponse(fallBackURL,payload);
                       }
                   }
                   catch (Exception ignored)
                   {
                   }



            }
        });
    }


    private static void parseJson(Payload payload, JSONObject jsonObject,int adIndex,int adNetwork) {
        try {
            PreferenceUtil preferenceUtil=PreferenceUtil.getInstance(iZooto.appContext);

            payload.setTitle(getParsedValue(jsonObject, payload.getTitle()));
            if (payload.getReceived_bid().equalsIgnoreCase("-1"))
                payload.setReceived_bid(payload.getReceived_bid());
            else
                payload.setReceived_bid(getParsedValue(jsonObject, payload.getReceived_bid()));
            if(payload.getTitle()=="")
            {
                payload.setCpc("-1");
                payload.setReceived_bid("-1");
            }
            else {

                payload.setCpc(getParsedValue(jsonObject, payload.getCpc()));
                if(payload.getCtr()!="") {
                    payload.setCtr(getParsedValue(jsonObject, payload.getCtr()));
                    payload.setCpm(getParsedValue(jsonObject, payload.getCpm()));


                    if (payload.getCpm() != "") {
                        if (payload.getCtr() != "") {
                            Float d = Float.parseFloat(payload.getCpm());
                            Float r = Float.parseFloat(payload.getCtr());
                            Float dat = 10 * r;
                            Float dataC = d / dat;
                            payload.setCpc(String.valueOf(dataC));
                        }

                    }
                }
            }

            payload.setLink(getParsedValue(jsonObject, payload.getLink()));

            if (!payload.getLink().startsWith("http://") && !payload.getLink().startsWith("https://")) {
                String url = payload.getLink();
                url = "https://" + url;
                payload.setLink(url);

            }
            payload.setBanner(getParsedValue(jsonObject, payload.getBanner()));
            payload.setMessage(getParsedValue(jsonObject, payload.getMessage()));
            payload.setIcon(getParsedValue(jsonObject, payload.getIcon()));
            payload.setAct1link(getParsedValue(jsonObject,payload.getAct1link()));

            if(payload.getAct_num()==1) {

                if (payload.getAct1link() != null) {
                    payload.setAct1name(payload.getAct1name());
                }
                if (!payload.getAct1link().startsWith("http://") && !payload.getAct1link().startsWith("https://")) {
                    String url = payload.getAct1link();
                    url = "https://" + url;
                    payload.setAct1link(url);

                }
            }
            if(payload.getIcon()!=null && payload.getIcon()!="")
            {
                if (!payload.getIcon().startsWith("http://") && !payload.getIcon().startsWith("https://")) {
                    String url = payload.getIcon();
                    url = "https://" + url;
                    payload.setIcon(url);

                }

            }
            if(payload.getBanner()!=null && payload.getBanner()!="")
            {
                if (!payload.getBanner().startsWith("http://") && !payload.getBanner().startsWith("https://")) {
                    String url = payload.getBanner();
                    url = "https://" + url;
                    payload.setBanner(url);

                }

            }
            if(payload.getCpc()=="" && payload.getReceived_bid()=="" )
            {
                payload.setCpc("-1");
                payload.setReceived_bid("-1");
            }

            payload.setAp("");
            payload.setInapp(0);
            JSONObject data = new JSONObject();
            data.put("b", payload.getCpc());
            data.put("a", payload.getAdID());
            if(payload.getResponseTime()==0)
                data.put("t", -1);
                 else
                 data.put("t", payload.getResponseTime());
                if (payload.getReceived_bid() != null && !payload.getReceived_bid().isEmpty())
                    data.put("rb", payload.getReceived_bid());
                   successList.add(data);

            if(adIndex==4)
            {
                finalAdPayload(payload);
            }
            if(adIndex==5 && preferenceUtil.getBoolean("Send"))
            {
                if(payload.getTitle()!=null && !payload.getTitle().equalsIgnoreCase("")) {
                    preferenceUtil.setBooleanData("Send",false);
                    finalAdPayload(payload);
                }
                else
                {
                    if(failsList.size()>0) {
                        String fallBackURL = callFallbackAPI(payload);
                        ShowFallBackResponse(fallBackURL, payload);
                    }
                }


            }
            if(adIndex==6  ) {
                 adPayload.add(payload);
               if(adNetwork==(payloadList.size())-1) {
                   showNotification(adPayload);
               }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private static void showNotification(final List<Payload> adPayload) {
        if(adPayload.size()>0) {
            try {
                int winnerIndex = 0;
                int passiveIndex = 0;
                double passiveNetwork=0.0;
                double winnerNetwork =Float.parseFloat(adPayload.get(0).getCpc());
                for (int index = 0; index < adPayload.size(); index++) {
                    if (adPayload.get(index).getCpc() != null && !adPayload.get(index).getCpc().isEmpty()) {
                        if (Float.parseFloat(adPayload.get(index).getCpc()) > winnerNetwork) {
                            winnerNetwork = Float.parseFloat(adPayload.get(index).getCpc());
                            winnerIndex = index;

                        }

                    }
                }
                if (passiveList.size() > 0) {
                    for (int index = 0; index < passiveList.size(); index++) {
                        if (Float.parseFloat(passiveList.get(index).getCpc()) >= Float.parseFloat(passiveList.get(0).getCpc())) {
                            passiveIndex = index;
                            passiveNetwork = Float.parseFloat(passiveList.get(index).getCpc());

                        }
                    }
                    if (passiveNetwork > winnerNetwork) {
                            fetchPassiveAPI(passiveList.get(passiveIndex));
                        } else {
                            if (adPayload.get(winnerIndex).getTitle() != null && !adPayload.get(winnerIndex).getTitle().equalsIgnoreCase("")) {
                                finalAdPayload(adPayload.get(winnerIndex));
                                if (passiveList.size() > 0) {
                                    JSONObject jsonObject1 = new JSONObject();
                                    jsonObject1.put("b", -1);
                                    jsonObject1.put("rb",passiveList.get(passiveIndex).getReceived_bid());
                                    jsonObject1.put("a", passiveList.get(passiveIndex).getAdID());
                                        jsonObject1.put("t", -1);
                                    successList.add(jsonObject1);
                                    passiveList.clear();
                                }

                            }
                        }
                    }
                else
                {

                    finalAdPayload(adPayload.get(winnerIndex));


                }


            }
            catch (Exception ex)
            {
            Log.e("Exception ex",ex.toString());
            }
        }


    }

    private static void fetchPassiveAPI(final Payload payload) {
        final long start = System.currentTimeMillis(); //fetch start time

        RestClient.get(payload.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {
                        long end = System.currentTimeMillis(); //fetch end time

                        Object json = new JSONTokener(response).nextValue();


                            JSONObject jsonObject1 = new JSONObject();
                            jsonObject1.put("b", payload.getCpc());
                            jsonObject1.put("rb",payload.getReceived_bid());
                            jsonObject1.put("a", payload.getAdID());
                            jsonObject1.put("t", (end-start));
                            successList.add(jsonObject1);


                        if(json instanceof JSONObject)
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            payload.setResponseTime((end-start));
                            parseAgain1Json(payload,jsonObject);

                        }
                        else if(json instanceof  JSONArray)
                        {
                            JSONArray jsonArray=new JSONArray(response);
                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("",jsonArray);
                            payload.setResponseTime((end-start));
                            parseAgain1Json(payload,jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }

            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                Log.e("Failure",""+statusCode);



            }
        });
    }

    private static void finalAdPayload(final Payload payloadData)
    {

        RestClient.get(payloadData.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {
                        final long end = System.currentTimeMillis(); //fetch start time

                        Object json = new JSONTokener(response).nextValue();
                        if (json != null) {
                            if (json instanceof JSONObject) {
                                JSONObject jsonObject = new JSONObject(response);
                                parseAgainJson(payloadData, jsonObject);

                            } else if (json instanceof JSONArray) {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("", jsonArray);
                                parseAgainJson(payloadData, jsonObject);
                            }
                        }
                        } catch(JSONException e){
                          //  e.printStackTrace();

                        }

                }

            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                Log.e("Failure",""+statusCode);



                }



        });

    }

    private static String callFallbackAPI(Payload payload) {
        String Message = "FallBAckAPI";
        String domain = "flbk.izooto.com";
        try {
            if(payload.getFallBackSubDomain()!="") {
                domain = payload.getFallBackSubDomain() + ".izooto.com";
            }
            else if(payload.getFallBackDomain()!="")
            {
                domain=payload.getFallBackDomain();
            }
            String path ="default.json";
            if(payload.getFallBackPath()!="")
                path =payload.getFallBackPath();

            String finalURL="https://"+domain+"/"+path;


        return finalURL;

        } catch (Exception ex) {
            Log.e("Exception",Message+ ex.toString());

        }
        return "";
    }



    private static void parseAgain1Json(Payload payload1, JSONObject jsonObject) {
        String dataValue="";
        try {

            payload1.setTitle(getParsedValue(jsonObject, payload1.getTitle()));
            payload1.setMessage(getParsedValue(jsonObject, payload1.getMessage()));

            payload1.setLink(getParsedValue(jsonObject, payload1.getLink()));
            payload1.setCpc(getParsedValue(jsonObject,payload1.getCpc()));
            payload1.setReceived_bid(getParsedValue(jsonObject,payload1.getReceived_bid()));
            if (!payload1.getLink().startsWith("http://") && !payload1.getLink().startsWith("https://")) {
                String url = payload1.getLink();
                url = "https://" + url;
                payload1.setLink(url);

            }
            payload1.setBanner(getParsedValue(jsonObject, payload1.getBanner()));
            payload1.setIcon(getParsedValue(jsonObject, payload1.getIcon()));
            payload1.setAct1link(getParsedValue(jsonObject,payload1.getAct1link()));
            payload1.setCtr(getParsedValue(jsonObject,payload1.getCtr()));
            payload1.setCpm(getParsedValue(jsonObject,payload1.getCpm()));
            payload1.setReceived_bid(getParsedValue(jsonObject,payload1.getReceived_bid()));

            if(payload1.getAct_num()==1) {

                if (payload1.getAct1link() != null) {
                    payload1.setAct1name(payload1.getAct1name());
                }
                if (!payload1.getAct1link().startsWith("http://") && !payload1.getAct1link().startsWith("https://")) {
                    String url = payload1.getAct1link();
                    url = "https://" + url;
                    payload1.setAct1link(url);

                }
            }
            if(payload1.getIcon()!=null && payload1.getIcon()!="")
            {
                if (!payload1.getIcon().startsWith("http://") && !payload1.getIcon().startsWith("https://")) {
                    String url = payload1.getIcon();
                    url = "https://" + url;
                    payload1.setIcon(url);

                }

            }
            if(payload1.getBanner()!=null && payload1.getBanner()!="")
            {
                if (!payload1.getBanner().startsWith("http://") && !payload1.getBanner().startsWith("https://")) {
                    String url = payload1.getBanner();
                    url = "https://" + url;
                    payload1.setBanner(url);

                }

            }


            if(payload1.getRv()!=null && !payload1.getRv().isEmpty())
            {

                if(payload1.getRv().startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(payload1.getRv());
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String rv = jsonArray.getString(i);
                            payload1.setRv(getParsedValue(jsonObject,rv));
                            callRandomView(payload1.getRv());
                        }
                    }
                }
                else
                {

                    payload1.setRv(getParsedValue(jsonObject,payload1.getRv()));
                    callRandomView(payload1.getRv());
                }

            }
            if(payload1.getRc()!=null && !payload1.getRc().isEmpty())
            {
                if(payload1.getRc().startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(payload1.getRc());
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String rc = jsonArray.getString(i);
                            payload1.setRc(getParsedValue(jsonObject,rc));
                            clicksData.add(payload1.getRc());
                        }
                    }
                }
                else
                {

                    payload1.setRc(getParsedValue(jsonObject,payload1.getRc()));
                    clicksData.add(payload1.getRc());

                }

            }

            if(payload1.getTitle()!=null && !payload1.getTitle().equalsIgnoreCase("")) {
                long end = System.currentTimeMillis();
                JSONObject finalData=new JSONObject();
                finalData.put("pid",payload1.getKey());
                finalData.put("rid",payload1.getRid());
                finalData.put("type",payload1.getAd_type());
                finalData.put("ta",(end-payload1.getStartTime()));
                finalData.put("av",Util.getSDKVersion(iZooto.appContext));
                JSONObject servedObject=new JSONObject();
                servedObject.put("a",payload1.getAdID());
                servedObject.put("b",payload1.getCpc());
                servedObject.put("t",payload1.getResponseTime());
                if(payload1.getReceived_bid()!=null && !payload1.getReceived_bid().isEmpty() && payload1.getReceived_bid()!="")
                    servedObject.put("rb",payload1.getReceived_bid());
                finalData.put("served",servedObject);
                successList.addAll(failsList);
                JSONArray jsonArray =new JSONArray(successList);
                finalData.put("bids",jsonArray);
                 dataValue=finalData.toString().replaceAll("\\\\", " ");
                callMediationIMpression(dataValue);
                NotificationActionReceiver.medClick=dataValue;
                NotificationEventManager.receiveAds(payload1);
                Log.e(AppConstant.NOTIFICATION_MESSAGE,AppConstant.YES);

            }
            else {
                String fallBackURL=callFallbackAPI(payload);
                ShowFallBackResponse(fallBackURL,payload);
                Log.e(AppConstant.NOTIFICATION_MESSAGE,AppConstant.NO);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ShowFallBackResponse(String fallBackAPI,  final Payload payload) {
        RestClient.get(fallBackAPI, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                try
                {
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject!=null)
                    {
                        payload.setTitle(jsonObject.optString(ShortpayloadConstant.TITLE));
                        payload.setMessage(jsonObject.optString(ShortpayloadConstant.NMESSAGE));
                        payload.setLink(jsonObject.optString(ShortpayloadConstant.LINK));
                        payload.setLink(jsonObject.optString(ShortpayloadConstant.ICON));
                        payload.setBanner(jsonObject.optString(ShortpayloadConstant.BANNER));
                        payload.setAct1link(jsonObject.optString(ShortpayloadConstant.ACT1LINK));
                        payload.setRid(payload.getRid());
                        NotificationEventManager.receiveAds(payload);
                        ShowCLCIKAndImpressionData(payload);

                    }



                }
                catch (Exception ex)
                {
                    Log.e("Exception Fallback",ex.toString());
                }


            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                Log.e("Failure",""+statusCode);

            }
        });
    }

    private static void ShowCLCIKAndImpressionData(Payload payload) {
        try {
            long end = System.currentTimeMillis();
            JSONObject finalData = new JSONObject();
            finalData.put("pid", payload.getKey());
            finalData.put("rid", payload.getRid());
            finalData.put("type", payload.getAd_type());
            finalData.put("ta", (end - payload.getStartTime()));
            finalData.put("av", Util.getSDKVersion(iZooto.appContext));
            JSONObject servedObject = new JSONObject();
            servedObject.put("a",0);
            servedObject.put("b", 0);
           // servedObject.put("rb",-1);

            if(payload.getResponseTime()==0)
            servedObject.put("t",-1);
            else
                servedObject.put("t",payload.getResponseTime());

            finalData.put("served", servedObject);
            successList.addAll(failsList);
            JSONArray jsonArray = new JSONArray(successList);
            finalData.put("bids", jsonArray);
          String  dataValue = finalData.toString().replaceAll("\\\\", " ");
            callMediationIMpression(dataValue);
            NotificationActionReceiver.medClick = dataValue;
            Log.e(AppConstant.NOTIFICATION_MESSAGE, AppConstant.YES);
        }catch (Exception ex){

        }
    }


    private static void parseAgainJson(Payload payload1, JSONObject jsonObject) {
       String dataValue = "";
        try {

            if(payload1.getRv()!=null && !payload1.getRv().isEmpty())
            {

               if(payload1.getRv().startsWith("[")) {
                   JSONArray jsonArray = new JSONArray(payload1.getRv());
                   if (jsonArray.length() > 0) {
                       for (int i = 0; i < jsonArray.length(); i++) {
                           String rv = jsonArray.getString(i);
                           payload1.setRv(getParsedValue(jsonObject,rv));
                           callRandomView(payload1.getRv());
                       }
                   }
               }
                else
                {

                   payload1.setRv(getParsedValue(jsonObject,payload1.getRv()));
                   callRandomView(payload1.getRv());
                }

            }
            if(payload1.getRc()!=null && !payload1.getRc().isEmpty())
            {
                if(payload1.getRc().startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(payload1.getRc());
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String rc = jsonArray.getString(i);
                            payload1.setRc(getParsedValue(jsonObject,rc));
                            clicksData.add(payload1.getRc());
                        }
                    }
                }
                else
                {

                    payload1.setRc(getParsedValue(jsonObject,payload1.getRc()));
                    clicksData.add(payload1.getRc());

                }

            }

            if(payload1.getTitle()!=null && !payload1.getTitle().equalsIgnoreCase("")) {
                long end = System.currentTimeMillis();
                JSONObject finalData=new JSONObject();
                finalData.put("pid",payload1.getKey());
                finalData.put("rid",payload1.getRid());
                finalData.put("type",payload1.getAd_type());
                finalData.put("ta",(end-payload1.getStartTime()));
                finalData.put("av",Util.getSDKVersion(iZooto.appContext));
                JSONObject servedObject=new JSONObject();
                servedObject.put("a",payload1.getAdID());
                servedObject.put("b",payload1.getCpc());
                servedObject.put("t",payload1.getResponseTime());
                if(payload1.getReceived_bid()!=null && !payload1.getReceived_bid().isEmpty() && payload1.getReceived_bid()!="")
                    servedObject.put("rb",payload1.getReceived_bid());
                finalData.put("served",servedObject);
                failsList.addAll(successList);
                JSONArray jsonArray =new JSONArray(failsList);
                finalData.put("bids",jsonArray);
                 dataValue=finalData.toString().replaceAll("\\\\", " ");
                callMediationIMpression(dataValue);
                NotificationActionReceiver.medClick=dataValue;
                NotificationEventManager.receiveAds(payload1);
                Log.e(AppConstant.NOTIFICATION_MESSAGE,AppConstant.YES);
            }
            else {

                    String fallBackAPI = callFallbackAPI(payload1);
                    ShowFallBackResponse(fallBackAPI, payload1);
                    Log.e(AppConstant.NOTIFICATION_MESSAGE, AppConstant.NO);

            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void callMediationIMpression(String finalData) {
        try
        {
            JSONObject jsonObject=new JSONObject(finalData);
            RestClient.postRequest1(RestClient.MEDIATION_IMPRESSION,jsonObject, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);


            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
                Log.e("Failure",""+statusCode);

            }
        });
        }
        catch (Exception ex)
        {
Log.v("Exception ",ex.toString());
        }
    }


    private static void callRandomView(String rv) {
        RestClient.get(rv, new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);


            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);

            }
        });
    }

    private static String getParsedValue(JSONObject jsonObject, String sourceString) {
        try {
            if(sourceString.matches("[0-9]{1,13}(\\.[0-9]*)?"))
            {
                return sourceString;
            }
            if (sourceString.startsWith("~"))
                return sourceString.replace("~", "");
            else {
                if (sourceString.contains(".")) {
                    JSONObject jsonObject1 = null;
                    String[] linkArray = sourceString.split("\\.");
                    if(linkArray.length==2 || linkArray.length==3)
                    {
                        for (int i = 0; i < linkArray.length; i++) {
                            if (linkArray[i].contains("[")) {
                                String[] linkArray1 = linkArray[i].split("\\[");

                                if (jsonObject1 == null)
                                    jsonObject1 = jsonObject.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                                else {
                                    jsonObject1 = jsonObject1.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                                }

                            } else {
                                return jsonObject1.optString(linkArray[i]);
                            }

                        }
                    }
                    else if(linkArray.length==4)
                    {
                        if (linkArray[2].contains("[")) {
                            String[] linkArray1 = linkArray[2].split("\\[");
                            if(jsonObject1==null) {
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                            }
                            else
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                            return jsonObject1.getString(linkArray[3]);

                        }

                    }
                    else if(linkArray.length==5)
                    {
                        if (linkArray[2].contains("[")) {
                            String[] link1 = linkArray[2].split("\\[");
                            if (jsonObject1 == null)
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", ""))).getJSONObject(linkArray[3]);
                            else
                                jsonObject1 = jsonObject1.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", ""))).getJSONObject(linkArray[3]);


                            return jsonObject1.optString(linkArray[4]);
                        }
                    }
                    else
                    {
                        jsonObject.getString(sourceString);
                    }


                } else {
                    return jsonObject.getString(sourceString);
                }
            }
        } catch (Exception e) {
           // e.printStackTrace();
        }
        return "";
    }

}
