package com.izooto;

import static com.izooto.AppConstant.APP_NAME_TAG;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;

import com.google.firebase.FirebaseOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Util {
    private static final String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static final int CIPHER_KEY_LEN = 16;
    private static final long TIME_OUT = 20 * 1000L;

    public enum SchemaType {
        DATA("data"),
        HTTPS("https"),
        HTTP("http"),
        ;
        private final String text;

        SchemaType(final String text) {
            this.text = text;
        }

        public static SchemaType fromString(String text) {
            for (SchemaType type : SchemaType.values()) {
                if (type.text.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static String decrypt(Context context,String key, String data) {
        try {
            if (key.length() < CIPHER_KEY_LEN) {
                int numPad = CIPHER_KEY_LEN - key.length();

                StringBuilder keyBuilder = new StringBuilder(key);
                for (int i = 0; i < numPad; i++) {
                    keyBuilder.append("0"); //0 pad to len 16 bytes
                }
                key = keyBuilder.toString();

            } else if (key.length() > CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
            }

            String[] parts = data.split(":");

            IvParameterSpec iv = new IvParameterSpec(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.ISO_8859_1), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] decodedEncryptedData = android.util.Base64.decode(parts[0], android.util.Base64.DEFAULT);

            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);

        } catch (Exception ex) {
            Util.handleExceptionOnce(context,ex.toString(),"Utils","decrypt");
        }
        return null;

    }

    private static Bitmap getBitMap(String src) {
        try {
            return BitmapFactory.decodeStream(new URL(src).openConnection().getInputStream());
        } catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, src + " " + e, "[Log-> e]-> getBitMap");
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(String url) {
        if (url == null)
            return null;

        if (!url.isEmpty()) {
            String trimmedName = url.trim();
            trimmedName = trimmedName.replace("///", "/");
            trimmedName = trimmedName.replace("//", "/");
            trimmedName = trimmedName.replace("http:/", "https://");
            trimmedName = trimmedName.replace("https:/", "https://");
            if (trimmedName.startsWith("http://") || trimmedName.startsWith("https://")) {
                return getBitMap(trimmedName);
            } else {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, url, "[Log-> e]->getBitmapFromURL");
                return null;
            }
        }
        return null;
    }



    static String getAndroidId(Context mContext) {
        try {
            @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            return android_id;
        } catch (Exception ex){
            return "";
        }
    }

    static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    static boolean isNotificationEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    static String getSDKVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception ex) {
            Util.handleExceptionOnce(context,ex.toString(),"Utils","getSDKVersion");
        }
        return "";

    }

    boolean hasFCMLibrary() {
        try {
            return com.google.firebase.messaging.FirebaseMessaging.class != null;
        } catch (Throwable e) {
            return false;
        }
    }

    boolean isInitializationValid() {
        checkForFcmDependency();
        return true;
    }

    boolean checkForFcmDependency() {
        if (!hasFCMLibrary()) {
            Lg.d(AppConstant.APP_NAME_TAG, AppConstant.CHECKFCMLIBRARY);
            return false;
        }
        return true;

    }

     static String getDeviceLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = iZooto.appContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = iZooto.appContext.getResources().getConfiguration().locale;
        }
        return locale.getDisplayLanguage();

    }

     public static String getIntegerToBinary(int number) {
        return String.format("%16s", Integer.toBinaryString(number)).replace(' ', '0');

    }

     static boolean checkNotificationEnable() {
        return NotificationManagerCompat.from(iZooto.appContext).areNotificationsEnabled();

    }

     public static String getPackageName(Context context) {
        ApplicationInfo ai;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
            //e.printStackTrace();
        }
        return context.getPackageName();
    }

     static boolean isMatchedString(String s) {
        try {
            Pattern pattern = Pattern.compile("[a-zA-Z0-9-_.~%]{1,900}");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

     static int convertStringToDecimal(String number) {
        char[] numChar = number.toCharArray();
        int intValue = 0;
        int decimal = 1;
        for (int index = numChar.length; index > 0; index--) {
            if (index == 1) {
                if (numChar[index - 1] == '-') {
                    return intValue * -1;
                } else if (numChar[index - 1] == '+') {
                    return intValue;
                }
            }
            intValue = intValue + (((int) numChar[index - 1] - 48) * (decimal));
            decimal = decimal * 10;
        }
        return intValue;
    }

     static CharSequence makeBoldString(CharSequence title) {
        if (Build.VERSION.SDK_INT >= 24) {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\"><b>" + title + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY);// for 24 api and more
        } else {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\"><b>" + title + "</b></font>"); // or for older api
        }
        return title;
    }

     static CharSequence makeBlackString(CharSequence title) {
        if (Build.VERSION.SDK_INT >= 24) {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\">" + title + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY); // for 24 api and more
        } else {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\">" + title + "</font>"); // or for older api
        }
        return title;
    }

     static Bitmap makeCornerRounded(Bitmap image) {
        try {
            Bitmap imageRounded = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
            Canvas canvas = new Canvas(imageRounded);
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.drawRoundRect((new RectF(0.0f, 0.0f, image.getWidth(), image.getHeight())), 10, 10, mPaint);
            return imageRounded;
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext,ex.toString(),"Utils","makeCornerRounded");
            return null;
        }
    }

    public static boolean isAppInForeground(Context context) {
        List<ActivityManager.RunningTaskInfo> task =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningTasks(1);
        if (task.isEmpty()) {
            return false;
        }
        return task
                .get(0)
                .topActivity
                .getPackageName()
                .equalsIgnoreCase(context.getPackageName());
    }

    static String getDeviceLanguageTag() {
        if (iZooto.appContext != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Locale.getDefault().getDisplayLanguage();
            } else {
                return "iz-ln";
            }
        } else {
            return "iz_ln";
        }

    }

   public static String getTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        return sdf.format(new Date());
    }

    static boolean hasHMSLibraries() {
        return hasHMSAGConnectLibrary() && hasHMSPushKitLibrary();
    }

    private static boolean hasHMSAGConnectLibrary() {
        try {
            return com.huawei.agconnect.config.AGConnectServicesConfig.class != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    private static boolean hasHMSPushKitLibrary() {
        try {
            return com.huawei.hms.aaid.HmsInstanceId.class != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }



     static void sleepTime(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            Log.d(APP_NAME_TAG,e.toString());
        }
    }

    public static String dayDifference(String currentDate, String previousDate) {
        if (previousDate.isEmpty())
            return "";
        String dayDifference = "";
        try {
            Date date1;
            Date date2;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dates = new SimpleDateFormat("yyyy.MM.dd");
            date1 = dates.parse(currentDate);
            date2 = dates.parse(previousDate);
            long difference = date1.getTime() - date2.getTime();
            long differenceDates = difference / (24 * 60 * 60 * 1000);
            dayDifference = Long.toString(differenceDates);
        } catch (Exception exception) {
            Util.handleExceptionOnce(iZooto.appContext,exception.toString(),"Utils","dayDifference");
        }
        return dayDifference;
    }

    public static int getBinaryToDecimal(int cfg) {
        String fourthDg, fifthDg, sixthDg;

        String data = Util.getIntegerToBinary(cfg);
        if (data != null && !data.isEmpty()) {
            fourthDg = String.valueOf(data.charAt(data.length() - 4));
            fifthDg = String.valueOf(data.charAt(data.length() - 5));
            sixthDg = String.valueOf(data.charAt(data.length() - 6));
        } else {
            fourthDg = "0";
            fifthDg = "0";
            sixthDg = "0";
        }
        String dataCFG = sixthDg + fifthDg + fourthDg;
        return Integer.parseInt(dataCFG, 2);
    }

    static boolean isValidResourceName(String name) {
        return (name != null && !name.matches("^[0-9]"));
    }

    static Uri getSoundUri(Context context, String sound) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        int soundId;
        if (isValidResourceName(sound)) {
            soundId = resources.getIdentifier(sound, "raw", packageName);
            if (soundId != 0)
                return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + soundId);
        }
        soundId = resources.getIdentifier("izooto_default_sound", "raw", packageName);
        if (soundId != 0)
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + soundId);
        return null;
    }

    public static void setException(Context context, String exception, String className, String methodName) {
        if (context == null)
            return;
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (!exception.isEmpty()) {
                Map<String, String> mapData = new HashMap<>();
                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                mapData.put(AppConstant.TOKEN, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
                mapData.put(AppConstant.EXCEPTION_, exception);
                mapData.put(AppConstant.METHOD_NAME, methodName);
                mapData.put(AppConstant.ClASS_NAME, className);
                mapData.put(AppConstant.SDK, AppConstant.SDKVERSION);
                mapData.put(AppConstant.ANDROIDVERSION, Build.VERSION.RELEASE);
                mapData.put(AppConstant.DEVICE_NAME, Util.getDeviceName());
                RestClient.postRequest(RestClient.APP_EXCEPTION_URL, mapData,null, new RestClient.ResponseHandler() {
                    @Override
                    void onSuccess(final String response) {
                        super.onSuccess(response);
                    }

                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Exception ex -- ", e.toString());
        }
    }

    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static HashMap<String, Object> toMap(JSONObject jsonobj) throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    static String getTimeWithoutDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        return sdf.format(new Date());
    }

    static void trackClickOffline(Context context, String apiUrl, String constantValue, String rid, String cid, int click) {
        if (context == null)
            return;

        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            JSONObject payloadJSON = new JSONObject();
            JSONArray jsonArray;
            if (!preferenceUtil.getStringData(constantValue).isEmpty()) {
                jsonArray = new JSONArray(preferenceUtil.getStringData(constantValue));
            } else
                jsonArray = new JSONArray();
            payloadJSON.put(AppConstant.APPPID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            payloadJSON.put(AppConstant.SDK, AppConstant.SDKVERSION);
            payloadJSON.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
            if (!apiUrl.isEmpty())
                payloadJSON.put(AppConstant.STORE_URL, apiUrl);
            if (!rid.isEmpty())
                payloadJSON.put(AppConstant.RID, rid);

            if (constantValue.equalsIgnoreCase(AppConstant.IZ_NOTIFICATION_CLICK_OFFLINE)) {
                payloadJSON.put("notification_op", "click");
                if (!cid.isEmpty())
                    payloadJSON.put(AppConstant.CID_, cid);
                if (click != 0)
                    payloadJSON.put("click", click);
            }

            jsonArray.put(payloadJSON);

            preferenceUtil.setStringData(constantValue, jsonArray.toString());
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "trackClickOffline()", "Util");
        }

    }

    static void trackMediation_Impression_Click(Context context, String hitName, String impressionORClickDATA) {
        if (context == null)
            return;

        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            JSONObject payloadJSON = new JSONObject();
            JSONArray jsonArray;
            if (!preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS).isEmpty()) {
                jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.STORE_MEDIATION_RECORDS));
            } else
                jsonArray = new JSONArray();
            payloadJSON.put(AppConstant.STORE_MED_API, hitName);
            payloadJSON.put(AppConstant.STORE_MED_DATA, impressionORClickDATA.replace("\n", ""));
            jsonArray.put(payloadJSON);
            preferenceUtil.setStringData(AppConstant.STORE_MEDIATION_RECORDS, jsonArray.toString());
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "trackMediation_Impression_Click", "Util");
        }
    }

    static boolean ridExists(JSONArray jsonArray, String rid) {
        return jsonArray.toString().contains("\"rid\":\"" + rid + "\"");
    }

    static String getAppVersion(Context context) {
        if (context == null)
            return "App Version  is not Found";
        PackageManager pm = context.getPackageManager();
        String pkgName = context.getPackageName();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
            return pkgInfo.versionName;
        } catch (Exception ex) {
            return "App Version  is not Found";
        }
    }

    @NonNull
    static Intent openURLInBrowserIntent(@NonNull Uri uri) {
        SchemaType type = uri.getScheme() != null ? SchemaType.fromString(uri.getScheme()) : null;
        if (type == null) {
            type = SchemaType.HTTP;
            if (!uri.toString().contains("://")) {
                uri = Uri.parse("http://" + uri);
            }
        }
        Intent intent;
        switch (type) {
            case DATA:
                intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
                intent.setData(uri);
                break;
            case HTTPS:
            case HTTP:
            default:
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
        }
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK
        );
        return intent;
    }

    @Nullable
    static BigInteger getAccentColor() {
        try {
            if (iZooto.appContext == null)
                return null;
            String defaultColor = getResourceString(iZooto.appContext, AppConstant.NOTIFICATION_ACCENT_COLOR, null);

            if (defaultColor != null) {
                if (defaultColor.charAt(0) == '#') {
                    String default_Color = "";
                    default_Color = defaultColor.replace("#", "");
                    if (default_Color != null) {
                        return new BigInteger(default_Color, 16);
                    }
                } else {
                    if (defaultColor != null) {
                        return new BigInteger(defaultColor, 16);
                    }
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "getAccentColor", "Util");
        }
        return null;
    }

    static String getResourceString(@NonNull Context context, String key, String defaultStr) {
        Resources resources = context.getResources();
        int bodyResId = resources.getIdentifier(key, AppConstant.STRING_RESOURCE_NAME, context.getPackageName());
        if (bodyResId != 0)
            return resources.getString(bodyResId);
        return defaultStr;
    }

    // news hub
    public static String getColorCode(String color) {
        if (color.startsWith("#")) {
            return color;
        } else {
            return "#" + color;
        }
    }

    // NewsHub
    protected static String toSHA1(String url) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(url.getBytes(StandardCharsets.UTF_8));

        return new BigInteger(1, crypt.digest()).toString(16);
    }

    protected static void newsHubClickApi(Context context, Payload userModal) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            HashMap<String, String> hashMap = new HashMap<>();
            if (preferenceUtil != null) {
                hashMap.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                hashMap.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
                hashMap.put(AppConstant.VER_, AppConstant.SDKVERSION);
                hashMap.put("link",userModal.getLink());

            }
            RestClient.postRequest(RestClient.iZ_PULSE_FEATURE_CLICK, hashMap, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "newsHubClickApi");
        }
    }


    /**
     * getApplication name
     */
    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    /**
     * getChannelName
     */
    static String getChannelName(Context context) {
        if (context != null) {
            String channelName = "";
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (preferenceUtil.getStringData(AppConstant.iZ_STORE_CHANNEL_NAME) != "" && !preferenceUtil.getStringData(AppConstant.iZ_STORE_CHANNEL_NAME).isEmpty()) {
                channelName = preferenceUtil.getStringData(AppConstant.iZ_STORE_CHANNEL_NAME);
            } else {
                channelName = AppConstant.CHANNEL_NAME;
            }
            return channelName;
        }
        return AppConstant.CHANNEL_NAME;
    }

    public static boolean notificationMode() {
        Locale locale = Locale.getDefault();
        return TextUtils.getLayoutDirectionFromLocale(locale) != ViewCompat.LAYOUT_DIRECTION_LTR;
    }

    /* check the expiry time to current time difference in seconds form */
    static String getTimerValue(String createdTime, String expTime) {
        try {
            long timerValue = 0;
            long timerValue_In_Seconds = 0;
            long deliveryTime = System.currentTimeMillis();
            long et_In_Millis = MinutesToMillisecondsConverter(Integer.parseInt(expTime));
            long expiryTime = Long.parseLong(createdTime) + et_In_Millis;

            if (deliveryTime >= Long.parseLong(createdTime) && deliveryTime <= expiryTime) {
                timerValue = expiryTime - deliveryTime;
                timerValue_In_Seconds = (timerValue / 1000);
            }

            if (timerValue_In_Seconds >= 1 && timerValue_In_Seconds < 3600) {
                return String.valueOf(timerValue_In_Seconds);
            } else {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, AppConstant.IZ_TIMER_VALUE_MESSAGE, AppConstant.IZ_TIMER_MESSAGE);
                return "";
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "getTimerValue");
            return "";
        }
    }

    // convert minutes into millis
    private static long MinutesToMillisecondsConverter(int minutes) {
        return (minutes * 60 * 1000L);
    }

    // parse vibration pattern
    static long[] parseVibrationPattern(Object patternObj) {
        try {
            JSONArray jsonVibArray;
            if (patternObj instanceof String) {
                jsonVibArray = new JSONArray((String) patternObj);
            } else {
                jsonVibArray = (JSONArray) patternObj;
            }
            long[] longArray = new long[jsonVibArray.length()];
            for (int i = 0; i < jsonVibArray.length(); i++) {
                longArray[i] = jsonVibArray.optLong(i);
            }
            return longArray;
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "parseVibrationPattern");
        }
        return null;
    }

    static boolean enableSticky(Payload payload) {
        return payload.getMakeStickyNotification() != null &&
                !payload.getMakeStickyNotification().isEmpty() && payload.getMakeStickyNotification().equals("1");
    }

     public static String getTimeAgo(String timestamp) {

         try {
             SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
             dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
             Date date = dateFormat.parse(timestamp);
             if (date != null) {
                 long timeInMillis = date.getTime();
                 long now = System.currentTimeMillis();
                 CharSequence relativeTimeSpan = DateUtils.getRelativeTimeSpanString(timeInMillis, now, DateUtils.MINUTE_IN_MILLIS);
                 return relativeTimeSpan.toString()
                         .replace(" minutes", "m")
                         .replace(" minute", "m")
                         .replace(" hours", "h")
                         .replace(" hour", "h")
                         .replace(" seconds", "s")
                         .replace(" second", "s");
             }
         } catch (Exception e) {
             Util.handleExceptionOnce(iZooto.appContext,e.toString(),"Util","getValidIdForCampaigns");
         }
         return "";
     }

    // To PulseManager Exception once
    public static void handleExceptionOnce(Context context, String exception, String className, String methodName) {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (!preferenceUtil.getBoolean(methodName)) {
            setException(context, exception, className, methodName);
            preferenceUtil.setBooleanData(methodName, true);
        }
        DebugFileManager.createExternalStoragePublic(context, exception + " " + methodName, "[Log.e]-> " + className);
    }

    /* News Hub offline campaigns */
    public static int getValidIdForCampaigns(Payload payload) {
        int digit = 0;
        try {
            String digits = payload.getRid().trim();
            digit = digits.charAt(0) - '0';
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "getValidIdForCampaigns");
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, e.toString(), "[Log.e]->RID");
        }
        return digit;
    }

    static boolean isDarkModeEnabled(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active
                return false;
            default:
                // Unknown mode
                return false;
        }
    }

    // To get default sender_id
    static String getSenderId() {
        try {
            FirebaseOptions firebaseOptions = FirebaseOptions.fromResource(iZooto.appContext);
            if (firebaseOptions != null) {
                String senderId = firebaseOptions.getGcmSenderId();
                if (senderId != null) {
                    return senderId;
                }
            } else {
                Util.handleExceptionOnce(iZooto.appContext, "Firebase options is null", "FCMTokenGenerator", "getSenderId()");

            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), "FCMTokenGenerator", "getSenderId()");

        }
        Log.v(AppConstant.APP_NAME_TAG, "Sender ID should not be null");
        return "";
    }

    public static String setExtrasAsJson(Context context, Bundle bundle) {
        try {
            JSONObject json = new JSONObject();
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                try {
                    json.put(key, JSONObject.wrap(bundle.get(key)));
                } catch (JSONException e) {
                    Util.handleExceptionOnce(context, e.toString(), "Util", "setExtrasAsJson");

                }
            }
            return json.toString();
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), "Util", "setExtrasAsJson");
            return null;
        }
    }


    public static Map<String, String> setJsonAsMap(Context context, JSONObject data){
        Map<String, String> map = new HashMap<>();
        try{
            Iterator<?> keys = data.keys();
            while (keys.hasNext()){
                String key = (String) keys.next();
                String value = data.getString(key);
                map.put(key, value);
            }
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), "Util", "setJsonAsMap");
        }
        return map;
    }


     public static String getOsNotificationId(Context context) {
        UUID uuid = null;
        try {
            uuid = UUID.randomUUID();
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), "Util", "getOsNotificationId");
        }
        if (uuid != null) {
            return uuid.toString();
        }
        return null;
    }


    public static boolean isStringNotEmpty(String body) {
        return !TextUtils.isEmpty(body);
    }

   public static boolean getNotificationKey(Bundle extras){
        return extras.containsKey(ShortPayloadConstant.GCM_TITLE) && extras.containsKey(ShortPayloadConstant.GCM_MESSAGE) && extras.containsKey(ShortPayloadConstant.GCM_ID);
    }

    public static boolean getDataKey(Bundle extras){
        return ((extras.containsKey(ShortPayloadConstant.TITLE) && extras.containsKey(AppConstant.P_CFG) && extras.containsKey(ShortPayloadConstant.RID)) || (extras.containsKey(AppConstant.AD_NETWORK) || extras.containsKey(AppConstant.GLOBAL_PUBLIC_KEY)));
    }

    /*  Required interaction */
    static long getRequiredInteraction(Payload payload) {
        long getRequiredInteraction = 0L;
        try {
            if (Util.verifyRequiredInteraction(payload)) {
                getRequiredInteraction = TIME_OUT;
            } else {
                getRequiredInteraction = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "getRequiredInteraction");
        }
        return getRequiredInteraction;
    }

    private static boolean verifyRequiredInteraction(Payload payload) {
        boolean verifyRequiredInteraction = false;
        try {
            verifyRequiredInteraction = payload.getMakeStickyNotification() != null && !payload.getMakeStickyNotification().isEmpty() && payload.getMakeStickyNotification().equals("2");
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "verifyRequiredInteraction");
        }
        return verifyRequiredInteraction;
    }


       static boolean areNotificationsEnabled(Context context, String channelId) {
        try {
            boolean notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
            if (!notificationsEnabled) {
                return false;
            }
            // Channels were introduced in O
            if (channelId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getNotificationManager(context) != null) {
                    NotificationChannel channel = getNotificationManager(context).getNotificationChannel(channelId);
                    return channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
                }
            }
        } catch (Throwable t) {
            Util.handleExceptionOnce(context, t.toString(), "Util", "areNotificationsEnabled");
        }
        return true;
    }


    private static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    static String channelId(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification().getChannelId();
        }
        return null;
    }

    @RequiresApi(api = 33)
    static boolean supportsNativePrompt(Context context) {
        return Build.VERSION.SDK_INT > 32 &&
                getTargetSdkVersion(context) > 32;
    }


    private static int getTargetSdkVersion(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return applicationInfo.targetSdkVersion;
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "Util", "getTargetSdkVersion");
        }
        return Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    static boolean isPulseDeepLink(int cfgValue) {
        try {
            String eleventhDigit = "0";
            String data = Util.getIntegerToBinary(cfgValue);
            if (!data.isEmpty()) {
                eleventhDigit = String.valueOf(data.charAt(data.length() - 11));
            }
            return eleventhDigit.equals("1");
        } catch (Exception ex) {
            return false;
        }
    }

}

