package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TargetActivity extends Activity {
    private String mUrl;
    private int inApp;
    private String rid;
    private  String cid;
    private int btnCount;
    private String api_url;
    private String additionalData;
    private String phoneNumber;
    private String act1ID;
    private String act2ID;
    private String langingURL;
    private String act2URL;
    private String act1URL;
    private String btn1Title;
    private String btn2Title;
    private String clickIndex = "0";
    private String lastClickIndex = "0";
    public static String notificationClick = "";
    public static String WebViewClick = "";
    public  static  String medClick="";
    private String pushType;
    private int cfg;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=TargetActivity.this;
        if(context!=null) {
            Intent intent =getIntent();
            context=TargetActivity.this;
            getBundleData(TargetActivity.this,intent);
            String GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE = "15";
            Intent it = new Intent(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
            context.sendBroadcast(it);
            getBundleData(context, intent);
            mUrl.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(context).getStringData(AppConstant.FCM_DEVICE_TOKEN));
            getBundleData(context, intent);
            try {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (clickIndex.equalsIgnoreCase("1")) {
                    String clkURL;
                    int dataCfg = Util.getBinaryToDecimal(cfg);

                    if (dataCfg > 0) {
                        clkURL = "https://clk" + dataCfg + ".izooto.com/clk" + dataCfg;
                    } else {
                        clkURL = RestClient.NOTIFICATIONCLICK;
                    }

                    notificationClickAPI(context, clkURL, cid, rid, btnCount, -1,pushType);
                    String lastEighthIndex = "0";
                    String lastTenthIndex = "0";
                    String dataInBinary = Util.getIntegerToBinary(cfg);
                    if (dataInBinary != null && !dataInBinary.isEmpty()) {
                        lastEighthIndex = String.valueOf(dataInBinary.charAt(dataInBinary.length() - 8));
                        lastTenthIndex = String.valueOf(dataInBinary.charAt(dataInBinary.length() - 10));
                    } else {
                        lastEighthIndex = "0";
                        lastTenthIndex = "0";
                    }
                    if (lastClickIndex.equalsIgnoreCase("1") || lastEighthIndex.equalsIgnoreCase("1")) {

                        String dayDiff1 = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK_WEEKLY));
                        String updateWeekly = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK_WEEKLY);
                        String updateDaily = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK_DAILY);
                        String time = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK);
                        String lciURL;

                        if (dataCfg > 0){
                            lciURL = "https://lci" +dataCfg + ".izooto.com/lci" + dataCfg;
                        }else
                            lciURL = RestClient.LASTNOTIFICATIONCLICKURL;
                        if (lastEighthIndex.equalsIgnoreCase("1")) {

                            if (lastTenthIndex.equalsIgnoreCase("1")) {
                                if (!updateDaily.equalsIgnoreCase(Util.getTime())) {
                                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK_DAILY, Util.getTime());
                                    lastClickAPI(context, lciURL, rid, -1);                                }
                            } else {
                                if (updateWeekly.isEmpty() || Integer.parseInt(dayDiff1) >= 7) {
                                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK_WEEKLY, Util.getTime());
                                    lastClickAPI(context, lciURL, rid, -1);                                }
                            }
                        } else if (lastClickIndex.equalsIgnoreCase("1") && lastEighthIndex.equalsIgnoreCase("0")) {
                            String dayDiff = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_CLICK));
                            if (time.isEmpty() || Integer.parseInt(dayDiff) >= 7) {
                                preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK, Util.getTime());
                                lastClickAPI(context, lciURL, rid, -1);                            }
                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                if (AdMediation.clicksData.size() > 0) {
                    for (int i = 0; i < AdMediation.clicksData.size(); i++) {
                        if (i == AdMediation.clicksData.size()) {
                        }
                        callRandomClick(AdMediation.clicksData.get(i));
                    }

                }
            }
            if (medClick != "") {
                callMediationClicks(medClick,0);
            }

            if (additionalData.equalsIgnoreCase("")) {
                additionalData = "1";
            }


            if (!additionalData.equalsIgnoreCase("1") && inApp >= 0) {

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(AppConstant.BUTTON_ID_1, act1ID);
                hashMap.put(AppConstant.BUTTON_TITLE_1, btn1Title);
                hashMap.put(AppConstant.BUTTON_URL_1, act1URL);
                hashMap.put(AppConstant.ADDITIONAL_DATA, additionalData);
                hashMap.put(AppConstant.LANDING_URL, langingURL);
                hashMap.put(AppConstant.BUTTON_ID_2, act2ID);
                hashMap.put(AppConstant.BUTTON_TITLE_2, btn2Title);
                hashMap.put(AppConstant.BUTTON_URL_2, act2URL);
                hashMap.put(AppConstant.ACTION_TYPE, String.valueOf(btnCount));
                JSONObject jsonObject = new JSONObject(hashMap);
                if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                    iZooto.notificationActionHandler(jsonObject.toString());
                    finish();
                }
                else {
                    if (Util.isAppInForeground(context))
                        iZooto.notificationActionHandler(jsonObject.toString());
                    else
                        notificationClick = jsonObject.toString();
                }
                if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                    launchApp(context);
                }
            } else {

                if (inApp == 1 && phoneNumber.equalsIgnoreCase(AppConstant.NO)) {
                    {
                        if (iZooto.mWebViewListener != null && !preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                            iZooto.notificationInAppAction(mUrl);
                        } else if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                            if (Util.isAppInForeground(context))
                                iZooto.notificationInAppAction(mUrl);
                            else {
                                WebViewClick = mUrl;
                                NotificationActionReceiver.launchApp(context);
                            }
                        } else
                            iZootoWebViewActivity.startActivity(context, mUrl);
                        finish();
                    }
                } else if (inApp == 2 && phoneNumber.equalsIgnoreCase(AppConstant.NO)) {
                    NotificationActionReceiver.launchApp(context);
                } else {
                    try {
                        if (phoneNumber.equalsIgnoreCase(AppConstant.NO)) {
                            if(mUrl!=null && !mUrl.isEmpty()) {

                                if (!mUrl.startsWith("http://") && !mUrl.startsWith("https://")) {
                                    String url = "https://" + mUrl;
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    context.startActivity(browserIntent);
                                    finish();


                                } else {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                                    browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    context.startActivity(browserIntent);
                                    finish();
                                }

                            }

                            else
                            {


                                Util.setException(context, "URL is not defined"+mUrl+"Browser is not present", AppConstant.APPName_3, "onReceived");
                                DebugFileManager.createExternalStoragePublic(iZooto.appContext,"URL is not correct or Browser is not present","[Log.e]->URL ERROR");

                            }
                        } else {
                            Intent browserIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
                            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(browserIntent);
                            finish();
                        }

                    } catch (Exception ex) {
                        Util.setException(context,ex.toString()+ "URL is not defined"+mUrl+"Browser is not present", AppConstant.APPName_3, "onReceived");
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"URL is not correct or Browser is not present","[Log.e]->URL ERROR");

                    }
                }
            }
        }


    }
    private void getBundleData(Context context, Intent intent) {
        Bundle tempBundle = intent.getExtras();
        if (tempBundle != null) {
            if (tempBundle.containsKey(AppConstant.KEY_WEB_URL))
                mUrl = tempBundle.getString(AppConstant.KEY_WEB_URL);
            if (tempBundle.containsKey(AppConstant.KEY_IN_APP))
                inApp = tempBundle.getInt(AppConstant.KEY_IN_APP);
            if (tempBundle.containsKey(AppConstant.KEY_IN_RID))
                rid = tempBundle.getString(AppConstant.KEY_IN_RID);
            if (tempBundle.containsKey(AppConstant.KEY_IN_CID))
                cid = tempBundle.getString(AppConstant.KEY_IN_CID);
            if(tempBundle.containsKey(AppConstant.KEY_IN_BUTOON))
                btnCount = tempBundle.getInt(AppConstant.KEY_IN_BUTOON);
            if(tempBundle.containsKey(AppConstant.KEY_IN_ADDITIONALDATA))
                additionalData = tempBundle.getString(AppConstant.KEY_IN_ADDITIONALDATA);
            if(tempBundle.containsKey(AppConstant.KEY_IN_PHONE))
                phoneNumber=tempBundle.getString(AppConstant.KEY_IN_PHONE);
            if(tempBundle.containsKey(AppConstant.KEY_IN_ACT1ID))
                act1ID=tempBundle.getString(AppConstant.KEY_IN_ACT1ID);
            if(tempBundle.containsKey(AppConstant.KEY_IN_ACT2ID))
                act2ID=tempBundle.getString(AppConstant.KEY_IN_ACT2ID);
            if(tempBundle.containsKey(AppConstant.LANDINGURL))
                langingURL=tempBundle.getString(AppConstant.LANDINGURL);
            if(tempBundle.containsKey(AppConstant.ACT1URL))
                act1URL=tempBundle.getString(AppConstant.ACT1URL);
            if(tempBundle.containsKey(AppConstant.ACT2URL))
                act2URL=tempBundle.getString(AppConstant.ACT2URL);
            if(tempBundle.containsKey(AppConstant.ACT1TITLE))
                btn1Title=tempBundle.getString(AppConstant.ACT1TITLE);
            if(tempBundle.containsKey(AppConstant.ACT2TITLE))
                btn2Title=tempBundle.getString(AppConstant.ACT2TITLE);
            if(tempBundle.containsKey(AppConstant.CLICKINDEX))
                clickIndex=tempBundle.getString(AppConstant.CLICKINDEX);
            if(tempBundle.containsKey(AppConstant.LASTCLICKINDEX))
                lastClickIndex=tempBundle.getString(AppConstant.LASTCLICKINDEX);
            if(tempBundle.containsKey(AppConstant.PUSH))
                pushType=tempBundle.getString(AppConstant.PUSH);
            if(tempBundle.containsKey(AppConstant.CFGFORDOMAIN))
                cfg=tempBundle.getInt(AppConstant.CFGFORDOMAIN);



            if (tempBundle.containsKey(AppConstant.KEY_NOTIFICITON_ID)) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(tempBundle.getInt(AppConstant.KEY_NOTIFICITON_ID));
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static void notificationClickAPI(Context context, String clkURL, String cid, String rid, int btnCount, int i,String pushType) {
        if (context == null)
            return;

        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            Map<String,String> mapData= new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
            mapData.put(AppConstant.CID_, cid);
            mapData.put(AppConstant.ANDROID_ID,"" + Util.getAndroidId(context));
            mapData.put(AppConstant.RID_,"" + rid);
            mapData.put(AppConstant.PUSH, pushType);
            mapData.put("op","click");
            if (btnCount != 0)
                mapData.put("btn","" + btnCount);
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,mapData.toString(),"clickData");

            RestClient.postRequest(clkURL, mapData,null, new RestClient.ResponseHandler() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);
                    try {
                        JSONArray jsonArrayOffline;

                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty() && i>=0) {
                            jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                            jsonArrayOffline.remove(i);
                            preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE, null);
                        }
                    } catch (Exception e) {
                        Util.setException(iZooto.appContext,e.toString(),AppConstant.APPName_3,"notificationClickAPI");
                    }
                }
                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    try {
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE).isEmpty()) {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE));
                            if (!Util.ridExists(jsonArrayOffline, rid)) {
                                Util.trackClickOffline(context, clkURL, AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE, rid, cid, btnCount);
                            }
                        } else {
                            Util.trackClickOffline(context, clkURL, AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE, rid, cid, btnCount);
                        }
                    } catch (Exception e) {
                        Util.setException(iZooto.appContext,e.toString(),AppConstant.APPName_3,"notificationClickAPI->onFailure");
                    }
                }
            });


        } catch (Exception e) {
            Util.setException(context, e.toString(), "notificationClickAPI", "NotificationActionReceiver");
        }
    }
    static void lastClickAPI(Context context, String lciURL, String rid, int i){
        if (context == null)
            return;

        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            preferenceUtil.setStringData(AppConstant.CURRENT_DATE_CLICK, Util.getTime());
            HashMap<String, Object> data = new HashMap<>();
            data.put(AppConstant.LAST_NOTIFICAION_CLICKED, true);
            JSONObject jsonObject = new JSONObject(data);
            Map<String,String> mapData= new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
            mapData.put(AppConstant.ANDROID_ID,"" + Util.getAndroidId(context));
            mapData.put(AppConstant.VAL,"" + jsonObject.toString());
            mapData.put(AppConstant.ACT,"add");
            mapData.put(AppConstant.ISID_,"1");
            mapData.put(AppConstant.ET_,"" + AppConstant.USERP_);

            RestClient.postRequest(lciURL, mapData,null, new RestClient.ResponseHandler() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);
                    try {
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE).isEmpty() && i >= 0) {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE));
                            jsonArrayOffline.remove(i);
                            preferenceUtil.setStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE, null);
                        }
                    } catch (Exception e) {
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"LastClick"+e.toString(),"[Log.V]->");
                    }
                }
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    try {
                        if (!preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE).isEmpty()) {
                            JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE));
                            if (!Util.ridExists(jsonArrayOffline, rid)) {
                                Util.trackClickOffline(context, lciURL, AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE, rid, "0", 0);
                            }
                        } else
                            Util.trackClickOffline(context, lciURL, AppConstant.IZ_NOTIFICATION_LAST_CLICK_OFFLINE, rid, "0", 0);
                    } catch (Exception e) {
                        DebugFileManager.createExternalStoragePublic(iZooto.appContext,"LastClick"+e.toString(),"[Log.V]->");

                    }

                }
            });
        } catch (Exception e) {
            Util.setException(context, e.toString(), "lastClickAPI", "NotificationActionReceiver");
        }

    }
    static void callRandomClick(String rv) {
        if(!rv.isEmpty()) {
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
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static void callMediationClicks(final String medClick, int cNUmber) {
        try {
            if(!medClick.isEmpty()) {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext,medClick,"mediationClick");
                JSONObject jsonObject = new JSONObject(medClick);
                RestClient.postRequest(RestClient.MEDIATION_CLICKS, null,jsonObject, new RestClient.ResponseHandler() {
                    @SuppressLint("NewApi")
                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        PreferenceUtil preferenceUtil=PreferenceUtil.getInstance(iZooto.appContext);
                        if (!preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS).isEmpty() && cNUmber >= 0) {
                            try {
                                JSONArray jsonArrayOffline = new JSONArray(preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS));
                                jsonArrayOffline.remove(cNUmber);
                                preferenceUtil.setStringData(AppConstant.STORE_MEDIATION_RECORDS, null);
                            }
                            catch (Exception ex)
                            {
                                DebugFileManager.createExternalStoragePublic(iZooto.appContext,"MediationCLick"+ex.toString(),"[Log.V]->");


                            }
                        }
                        else {
                            NotificationActionReceiver.medClick = "";
                        }
                    }
                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                        Util.trackMediation_Impression_Click(iZooto.appContext,AppConstant.MED_CLICK,medClick);


                    }
                });
            }
        }
        catch (Exception ex)
        {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,"MediationCLick"+ex.toString(),"[Log.V]->");

        }
    }


    static void launchApp(Context context){

        PackageManager pm = context.getPackageManager();
        Intent launchIntent = null;
        String name = "";
        try {
            if (pm != null && !Util.isAppInForeground(context)) {
                ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                name = (String) pm.getApplicationLabel(app);
                launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
                Intent intentAppLaunch = launchIntent; // new Intent();
                intentAppLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intentAppLaunch);
            }
            iZooto.Log(iZooto.LOG_LEVEL.DEBUG, "Found it:" + name);
        } catch (PackageManager.NameNotFoundException e) {
            Util.setException(context,e.toString(),AppConstant.APPName_3,"launch App");

        }
    }

}
