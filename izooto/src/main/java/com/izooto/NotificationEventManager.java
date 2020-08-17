package com.izooto;



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Random;


public class NotificationEventManager {
    private static Bitmap notificationIcon, notificationBanner;//,act1Icon,act2Icon;
    private static int icon;
    private static  int badgeColor;
    private static int priority,lockScreenVisibility;

    public static void manageNotification(Payload payload) {
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty())
            showNotification(payload);
        else
            processPayload(payload);

    }

    private static void processPayload(final Payload payload) {
        RestClient.get(payload.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {

                        Object json = new JSONTokener(response).nextValue();
                        if(json instanceof JSONObject)
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            parseJson(payload,jsonObject);
                        }
                        else if(json instanceof  JSONArray)
                        {
                            JSONArray jsonArray=new JSONArray(response);
                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("",jsonArray);
                            parseJson(payload,jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);

            }
        });
    }


    private static void parseJson(Payload payload, JSONObject jsonObject) {
        try {
            payload.setLink(getParsedValue(jsonObject, payload.getLink()));
            if (!payload.getLink().startsWith("http://") && !payload.getLink().startsWith("https://")) {
                String url = payload.getLink();
                url = "http://" + url;
                payload.setLink(url);

            }
            payload.setBanner(getParsedValue(jsonObject, payload.getBanner()));
            payload.setTitle(getParsedValue(jsonObject, payload.getTitle()));
            payload.setMessage(getParsedValue(jsonObject, payload.getMessage()));
            payload.setIcon(getParsedValue(jsonObject, payload.getIcon()));
            payload.setAct1name(getParsedValue(jsonObject,payload.getAct1name()));
            payload.setAct1link(getParsedValue(jsonObject,payload.getAct1link()));
            if (!payload.getAct1link().startsWith("http://") && !payload.getAct1link().startsWith("https://")) {
                String url = payload.getAct1link();
                url = "http://" + url;
                payload.setAct1link(url);

            }
            payload.setAp("");
            payload.setInapp(0);
            if(payload.getTitle()!=null && !payload.getTitle().equalsIgnoreCase("")) {
                showNotification(payload);
                Log.e("Notification Send","Yes");
            }
            else {
                Log.e("Notification Send","No");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getParsedValue(JSONObject jsonObject, String sourceString) {
        try {
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
//                    else if(linkArray.length==3)
//                    {
//                            if (linkArray[1].contains("[")) {
//                                String[] link1 = linkArray[1].split("\\[");
//
//                                jsonObject1 = jsonObject1.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", "")));
//                                Log.e("ABC1", jsonObject1.toString());
//                                return jsonObject1.getString(linkArray[2]);
//
//
//                            } else {
//
//                                return jsonObject.getString(sourceString);
//                            }
//
//
//                    }
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


                } else
                    return jsonObject.getString(sourceString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

private static void showNotification(final Payload payload) {
    final Handler handler = new Handler(Looper.getMainLooper());
    final Runnable notificationRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            String link = payload.getLink();
            String link1 = payload.getAct1link();
            String link2 = payload.getAct2link();
            if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
                if (link.contains(AppConstant.BROWSERKEYID))
                    link = link.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                if (link1.contains(AppConstant.BROWSERKEYID))
                    link1 = link1.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                if (link2.contains(AppConstant.BROWSERKEYID))
                    link2 = link2.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
            } else {
                link = getFinalUrl(payload);
            }

            String clickIndex = "0";
            String impressionIndex ="0";

            String data=Util.getIntegerToBinary(payload.getCfg());
            if(data!=null && !data.isEmpty()) {
                clickIndex = String.valueOf(data.charAt(data.length() - 2));
                impressionIndex = String.valueOf(data.charAt(data.length() - 1));
            }
            else
            {
                clickIndex = "0";
                impressionIndex="0";
            }




            String channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
            NotificationCompat.Builder notificationBuilder = null;
            Notification summaryNotification = null;
            int SUMMARY_ID = 0;

            Intent intent = null;
            if (iZooto.icon!=0)
            {
                icon=iZooto.icon;
            }
            else
            {
                int checkExistence = iZooto.appContext.getResources().getIdentifier(payload.getBadgeicon(), "drawable", iZooto.appContext.getPackageName());
                if ( checkExistence != 0 ) {  // the resource exists...
                    icon = checkExistence;
                }
                else {  // checkExistence == 0  // the resource does NOT exist!!
                    int checkExistenceMipmap = iZooto.appContext.getResources().getIdentifier(payload.getBadgeicon(), "mipmap", iZooto.appContext.getPackageName());
                    if ( checkExistenceMipmap != 0 ) {  // the resource exists...
                        icon = checkExistenceMipmap;
                    }else {
                        icon=R.drawable.ic_notifications_black_24dp;
                    }

                }

            }


            if (payload.getBadgecolor().contains("#")){
                try{
                    badgeColor = Color.parseColor(payload.getBadgecolor());
                } catch(IllegalArgumentException ex){
                    // handle your exceptizion
                    badgeColor = Color.TRANSPARENT;
                    ex.printStackTrace();
                }
            }else if (payload.getBadgecolor()!=null&&!payload.getBadgecolor().isEmpty()){
                try{
                    badgeColor = Color.parseColor("#"+payload.getBadgecolor());
                } catch(IllegalArgumentException ex){ // handle your exception
                    badgeColor = Color.TRANSPARENT;
                    ex.printStackTrace();
                }
            }else {
                badgeColor = Color.TRANSPARENT;
            }

            lockScreenVisibility = setLockScreenVisibility(payload.getLockScreenVisibility());


            intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(AppConstant.KEY_WEB_URL, link);
            intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, 100);
            intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
            intent.putExtra(AppConstant.KEY_IN_CID,payload.getId());
            intent.putExtra(AppConstant.KEY_IN_RID,payload.getRid());
            intent.putExtra(AppConstant.KEY_IN_BUTOON,0);
            intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA,payload.getAp());
            intent.putExtra(AppConstant.KEY_IN_PHONE,AppConstant.NO);
            intent.putExtra(AppConstant.KEY_IN_ACT1ID,payload.getAct1ID());
            intent.putExtra(AppConstant.KEY_IN_ACT2ID,payload.getAct2ID());
            intent.putExtra(AppConstant.LANDINGURL,payload.getLink());
            intent.putExtra(AppConstant.ACT1TITLE,payload.getAct1name());
            intent.putExtra(AppConstant.ACT2TITLE,payload.getAct2name());
            intent.putExtra(AppConstant.ACT1URL,payload.getAct1link());
            intent.putExtra(AppConstant.ACT2URL,payload.getAct2link());
            intent.putExtra(AppConstant.CLICKINDEX,clickIndex);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            //  RemoteViews collapsedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.remote_view);
            //  collapsedView.setTextViewText(R.id.notificationTitle,""+payload.getTitle());
            //  collapsedView.setTextViewText(R.id.notificationMessage,""+payload.getMessage());
//
//                RemoteViews epandsView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.remote_view_expands);
//                collapsedView.setTextViewText(R.id.notificationTitle,payload.getTitle());
            notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId)
                    .setContentTitle(payload.getTitle())
                    .setSmallIcon(icon)
                    .setContentText(payload.getMessage())
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(payload.getMessage()))
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                    .setSound(defaultSoundUri)
                    .setVisibility(lockScreenVisibility)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    // .setCustomContentView(collapsedView)
                    // .setCustomBigContentView(epandsView)
                    .setAutoCancel(true);





            if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)){
                priority = priorityForLessOreo(payload.getPriority());
                notificationBuilder.setPriority(priority);

            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (payload.getGroup() == 1) {

                    notificationBuilder.setGroup(payload.getGroupKey());

                    summaryNotification =
                            new NotificationCompat.Builder(iZooto.appContext, channelId)
                                    .setContentTitle(payload.getTitle())
                                    .setContentText(payload.getMessage())
                                    .setSmallIcon(icon)
                                    .setColor(badgeColor)
                                    .setStyle(new NotificationCompat.InboxStyle()
                                            .addLine(payload.getMessage())
                                            .setBigContentTitle(payload.getGroupMessage()))
                                    .setGroup(payload.getGroupKey())
                                    .setGroupSummary(true)
                                    .build();


                }
            }

            if (!payload.getSubTitle().contains("null")&&payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                notificationBuilder.setSubText(payload.getSubTitle());

            }
            if (payload.getBadgecolor()!=null&&!payload.getBadgecolor().isEmpty()){
                notificationBuilder.setColor(badgeColor);
            }
            if(payload.getLedColor()!=null && !payload.getLedColor().isEmpty())
                notificationBuilder.setColor(Color.parseColor(payload.getLedColor()));
            if (notificationIcon != null)
                notificationBuilder.setLargeIcon(notificationIcon);
            else if (notificationBanner != null)
                notificationBuilder.setLargeIcon(notificationBanner);
            if (notificationBanner != null && !payload.getSubTitle().contains("null") && payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(notificationBanner)
                        .bigLargeIcon(notificationIcon)/*.setSummaryText(payload.getMessage())*/);
            }else if (notificationBanner != null)
            {
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(notificationBanner)
                        .bigLargeIcon(notificationIcon).setSummaryText(payload.getMessage()));

            }

            NotificationManager notificationManager =
                    (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificaitionId = (int) System.currentTimeMillis();
            if (payload.getAct1name() != null && !payload
                    .getAct1name().isEmpty()) {
                Intent btn1 = new Intent(iZooto.appContext, NotificationActionReceiver.class);
                String phone;

                String checknumber = decodeURL(payload.getAct1link());
                if (checknumber.contains(AppConstant.TELIPHONE))
                    phone = checknumber;
                else
                    phone = AppConstant.NO;

                btn1.putExtra(AppConstant.KEY_WEB_URL, link1);
                btn1.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificaitionId);
                btn1.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
                btn1.putExtra(AppConstant.KEY_IN_CID, payload.getId());
                btn1.putExtra(AppConstant.KEY_IN_RID, payload.getRid());
                btn1.putExtra(AppConstant.KEY_IN_BUTOON, 1);
                btn1.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, payload.getAp());
                btn1.putExtra(AppConstant.KEY_IN_PHONE, phone);
                btn1.putExtra(AppConstant.KEY_IN_ACT1ID,payload.getAct1ID());
                btn1.putExtra(AppConstant.KEY_IN_ACT2ID,payload.getAct2ID());
                btn1.putExtra(AppConstant.LANDINGURL,payload.getLink());
                btn1.putExtra(AppConstant.ACT1TITLE,payload.getAct1name());
                btn1.putExtra(AppConstant.ACT2TITLE,payload.getAct2name());
                btn1.putExtra(AppConstant.ACT1URL,payload.getAct1link());
                btn1.putExtra(AppConstant.ACT2URL,payload.getAct2link());
                btn1.putExtra(AppConstant.CLICKINDEX,clickIndex);




                PendingIntent pendingIntent1 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action action1 =
                        new NotificationCompat.Action.Builder(
                                0,  HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.gray) + "\">" + payload.getAct1name() + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
                                pendingIntent1).build();
                notificationBuilder.addAction(action1);


            }


            if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
                Intent btn2 = new Intent(iZooto.appContext, NotificationActionReceiver.class);
//                    btn2.setAction(AppConstant.ACTION_BTN_TWO);
                String phone;

                String checknumber =decodeURL(payload.getAct2link());
                if (checknumber.contains(AppConstant.TELIPHONE))
                    phone = checknumber;
                else
                    phone = AppConstant.NO;


                btn2.putExtra(AppConstant.KEY_WEB_URL, link2);
                btn2.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificaitionId);
                btn2.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
                btn2.putExtra(AppConstant.KEY_IN_CID,payload.getId());
                btn2.putExtra(AppConstant.KEY_IN_RID,payload.getRid());
                btn2.putExtra(AppConstant.KEY_IN_BUTOON,2);
                btn2.putExtra(AppConstant.KEY_IN_ADDITIONALDATA,payload.getAp());
                btn2.putExtra(AppConstant.KEY_IN_PHONE,phone);
                btn2.putExtra(AppConstant.KEY_IN_ACT1ID,payload.getAct1ID());
                btn2.putExtra(AppConstant.KEY_IN_ACT2ID,payload.getAct2ID());
                btn2.putExtra(AppConstant.LANDINGURL,payload.getLink());
                btn2.putExtra(AppConstant.ACT1TITLE,payload.getAct1name());
                btn2.putExtra(AppConstant.ACT2TITLE,payload.getAct2name());
                btn2.putExtra(AppConstant.ACT1URL,payload.getAct1link());
                btn2.putExtra(AppConstant.ACT2URL,payload.getAct2link());
                btn2.putExtra(AppConstant.CLICKINDEX,clickIndex);





                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action action2 =
                        new NotificationCompat.Action.Builder(
                                0, HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.gray) + "\">" + payload.getAct2name() + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
                                pendingIntent2).build();
                notificationBuilder.addAction(action2);
            }
            assert notificationManager != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                priority = priorityForImportance(payload.getPriority());
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title", priority);

                notificationManager.createNotificationChannel(channel);

            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (payload.getGroup() == 1) {
                    notificationManager.notify(SUMMARY_ID, summaryNotification);
                }
            }
            notificationManager.notify(notificaitionId, notificationBuilder.build());
            try {

                if(impressionIndex.equalsIgnoreCase("1")) {

                    final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);

                    String api_url = "?pid=" +  preferenceUtil.getiZootoID(AppConstant.APPPID) +
                            "&cid=" + payload.getId() + "&bKey=" + PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN) + "&rid=" + payload.getRid() + "&op=view";

                    RestClient.postRequest(RestClient.IMPRESSION_URL + api_url, new RestClient.ResponseHandler() {


                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            if (payload != null)
                                Log.e("imp","call");

                        }
                    });
                }
                iZooto.notificationView(payload);

            } catch (Exception e) {
                e.printStackTrace();
            }

            notificationBanner = null;
            notificationIcon = null;
            link = "";
            link1 = "";
            link2 = "";

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
                .appendQueryParameter("id", payload.getId())
                .appendQueryParameter("client", payload.getKey())
                .appendQueryParameter("rid", payload.getRid())
                .appendQueryParameter("bkey", PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN))
                .appendQueryParameter("frwd", encodedLink)
                .build();
        return builtUri.toString();
    }
    public static String decodeURL(String url)
    {


        if(url.contains("&frwd")) {
            String[] arrOfStr = url.split("&frwd=");
            String[] second = arrOfStr[1].split("&bkey=");
            String decodeData = new String(Base64.decode(second[0], Base64.DEFAULT));
            return decodeData;
        }
        else
        {
            return url;
        }



    }

    private static int priorityForImportance(int priority) {
        if (priority > 9)
            return NotificationManagerCompat.IMPORTANCE_MAX;
        if (priority > 7)
            return NotificationManagerCompat.IMPORTANCE_HIGH;
        return NotificationManagerCompat.IMPORTANCE_DEFAULT;
    }
    private static int priorityForLessOreo(int priority) {
        if (priority > 0)
            return Notification.PRIORITY_HIGH;
        return Notification.PRIORITY_DEFAULT;
    }
    private static int setLockScreenVisibility(int visibility) {
        if (visibility < 0)
            return NotificationCompat.VISIBILITY_SECRET;
        if (visibility == 0)
            return NotificationCompat.VISIBILITY_PRIVATE;
        return NotificationCompat.VISIBILITY_PUBLIC;

    }


}
