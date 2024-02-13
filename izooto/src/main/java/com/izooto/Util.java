package com.izooto;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import static com.izooto.ShortpayloadConstant.TAG;


public class Util {

    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static int CIPHER_KEY_LEN = 16;
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

    public static String decrypt(String key, String data) {
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
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ISO-8859-1"), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] decodedEncryptedData = android.util.Base64.decode(parts[0], android.util.Base64.DEFAULT);

            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    private static Bitmap getBitMap(String src) {
        try{
            return BitmapFactory.decodeStream(new URL(src).openConnection().getInputStream());
        } catch (Exception e){
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, src + " "+e.toString(), "[Log-> e]-> getBitMap");
            return null;
        }
    }
    public static Bitmap getBitmapFromURL(String url) {
        if (url == null)
            return null;
        if(url!=null && !url.isEmpty()) {
            String trimmedName = url.trim();
            trimmedName = trimmedName.replace("///", "/");
            trimmedName = trimmedName.replace("//", "/");
            trimmedName = trimmedName.replace("http:/", "https://");
            trimmedName = trimmedName.replace("https:/", "https://");
            if(checkImageExtension(trimmedName)){
                if (trimmedName.startsWith("http://") || trimmedName.startsWith("https://")) {
                    Bitmap bmp = getBitMap(trimmedName);
                    if (bmp != null) {
                        return bmp;
                    }
                }
            } else {
                DebugFileManager.createExternalStoragePublic(iZooto.appContext, url, "[Log-> e]->getBitmapFromURL");
                return null;
            }
        }
        else
        {
            return  null;
        }
        return null;

    }
    static  boolean checkImageExtension(String trimmedName) {
        return trimmedName.contains(".jpeg") || trimmedName.contains(".jpg") || trimmedName.contains(".png") || trimmedName.contains(".webp") || trimmedName.contains(".WEBP") || trimmedName.contains(".PNG") || trimmedName.contains(".JPEG") || trimmedName.contains(".JPG") || trimmedName.contains(".GIF") || trimmedName.contains(".gif");
    }
    public static String getAndroidId(Context mContext){
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.v(TAG, "android id ---- "+android_id);
        return android_id;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static boolean isNotificationEnabled(Context context){
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

    public static String getSDKVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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

    public boolean isInitializationValid() {
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
    public static String trimString(String optString) {
        if(optString.length()>32)
        {
            int length=optString.length()-32;
            return optString.substring(0,length);
        }
        return null;
    }


    public static String getDeviceLanguage()
    {
        //return Locale.getDefault().getDisplayLanguage();
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = iZooto.appContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = iZooto.appContext.getResources().getConfiguration().locale;
        }
        // Log.e("lanuguage",locale.getCountry());
        return locale.getDisplayLanguage();

    }
    public static String getIntegerToBinary(int number)
    {
        return String.format("%16s", Integer.toBinaryString(number)).replace(' ', '0');

    }
    public static boolean checkNotificationEnable()
    {
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
    public static boolean isMatchedString(String s) {
        try {
            Pattern pattern= Pattern.compile("[a-zA-Z0-9-_.~%]{1,900}");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }
    public static int convertStringToDecimal(String number){
        char[] numChar = number.toCharArray();
        int intValue = 0;
        int decimal = 1;
        for(int index = numChar.length ; index > 0 ; index --){
            if(index == 1 ){
                if(numChar[index - 1] == '-'){
                    return intValue * -1;
                } else if(numChar[index - 1] == '+'){
                    return intValue;
                }
            }
            intValue = intValue + (((int)numChar[index-1] - 48) * (decimal));
            decimal = decimal * 10;
        }
        return intValue;
    }
    public static CharSequence  makeBoldString(CharSequence title) {
        if (Build.VERSION.SDK_INT >= 24) {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\"><b>"+title+"</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY);// for 24 api and more
        } else {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\"><b>"+title+"</b></font>"); // or for older api
        }
        return title;
    }

    public static CharSequence makeBlackString(CharSequence title) {
        if (Build.VERSION.SDK_INT >= 24) {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\">"+title+"</font>", HtmlCompat.FROM_HTML_MODE_LEGACY); // for 24 api and more
        } else {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(iZooto.appContext, R.color.iz_black) + "\">"+title+"</font>"); // or for older api
        }
        return title;
    }
    public static Bitmap makeCornerRounded(Bitmap image){
        try {
            Bitmap imageRounded = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
            Canvas canvas = new Canvas(imageRounded);
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.drawRoundRect((new RectF(0.0f, 0.0f, image.getWidth(), image.getHeight())), 10, 10, mPaint);
            return imageRounded;
        }
        catch (Exception ex)
        {
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getDeviceLanguageTag()
    {
        if(iZooto.appContext!=null) {
            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = iZooto.appContext.getResources().getConfiguration().getLocales().get(0);
                return locale.getDefault().getDisplayLanguage();
            } else {
                return "iz-ln";
            }
        }
        else {
            return "iz_ln";
        }

    }
    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
    public static boolean hasHMSLibraries() {
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


    public static void sleepTime(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
    public static String dayDifference(String currentDate, String previousDate){
        if (previousDate.isEmpty())
            return "";
        String dayDifference = "";
        try {
            Date date1;
            Date date2;
            SimpleDateFormat dates = new SimpleDateFormat("yyyy.MM.dd");
            date1 = dates.parse(currentDate);
            date2 = dates.parse(previousDate);
            long difference = date1.getTime() - date2.getTime();
            long differenceDates = difference / (24 * 60 * 60 * 1000);
            dayDifference = Long.toString(differenceDates);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return dayDifference;
    }
    public static int getBinaryToDecimal(int cfg){
        String fourthDg, fifthDg, sixthDg;

        String data = Util.getIntegerToBinary(cfg);
        if(data!=null && !data.isEmpty()) {
            fourthDg = String.valueOf(data.charAt(data.length() - 4));
            fifthDg = String.valueOf(data.charAt(data.length() - 5));
            sixthDg = String.valueOf(data.charAt(data.length() - 6));
        }else {
            fourthDg = "0";
            fifthDg = "0";
            sixthDg = "0";
        }
        String dataCFG = sixthDg + fifthDg + fourthDg;
        int decimalData = Integer.parseInt(dataCFG,2);
        return decimalData;
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
    public static void setException(Context context, String exception, String className,String methodName) {
        if (context == null)
            return;
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (!exception.isEmpty()){
                Map<String, String> mapData = new HashMap<>();
                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                mapData.put(AppConstant.TOKEN,"" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(context));
                mapData.put(AppConstant.EXCEPTION_, "" + exception);
                mapData.put(AppConstant.METHOD_NAME, "" + methodName);
                mapData.put(AppConstant.ClASS_NAME, "" + className);
                mapData.put(AppConstant.SDK,""+AppConstant.SDKVERSION);
                mapData.put(AppConstant.ANDROIDVERSION,"" + Build.VERSION.RELEASE);
                mapData.put(AppConstant.DEVICENAME,"" + Util.getDeviceName());
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
                    if (info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static HashMap<String, Object> toMap(JSONObject jsonobj)  throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }   return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }   return list;
    }
    static String getTimeWithoutDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        String currentDate = sdf.format(new Date());
        return currentDate;
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
            Util.handleExceptionOnce(context, e.toString(), "trackMediation_Impression_Click", "Util");

            Util.setException(context, e.toString(), "trackClickOffline()", "Util");
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
        } catch (PackageManager.NameNotFoundException e) {
            return "App Version  is not Found";
        }
    }
    @NonNull
    static Intent openURLInBrowserIntent(@NonNull Uri uri) {
        SchemaType type = uri.getScheme() != null ? SchemaType.fromString(uri.getScheme()) : null;
        if (type == null) {
            type = SchemaType.HTTP;
            if (!uri.toString().contains("://")) {
                uri = Uri.parse("http://" + uri.toString());
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
    @SuppressWarnings("ConstantConditions")
    public static void setPackageNameFromResolveInfoList(Context context, Intent launchIntent) {
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(launchIntent, 0);
        if (resolveInfoList != null) {
            String appPackageName = context.getPackageName();
            for (ResolveInfo resolveInfo : resolveInfoList) {
                if (appPackageName.equals(resolveInfo.activityInfo.packageName)) {
                    launchIntent.setPackage(appPackageName);
                    break;
                }
            }
        }
    }
    @Nullable
    static BigInteger getAccentColor() {
        try {
            if(iZooto.appContext == null)
                return null;
            String defaultColor = getResourceString(iZooto.appContext, AppConstant.NOTIFICATION_ACCENT_COLOR, null);
            if(defaultColor!=null) {
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
        } catch (Throwable t) {

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

    protected static boolean notificationMode(){
        Locale locale = Locale.getDefault();
        return TextUtils.getLayoutDirectionFromLocale(locale) != ViewCompat.LAYOUT_DIRECTION_LTR;
    }


    /* check the expiry time to current time difference in seconds form */
    /* check the expiry time to current time difference in seconds form */
    static String getTimerValue(String createdTime, String expTime){
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
                DebugFileManager.createExternalStoragePublic(iZooto.appContext,AppConstant.IZ_TIMER_VALUE_MESSAGE,AppConstant.IZ_TIMER_MESSAGE);
                return "";
            }
        }
        catch (Exception e) {
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,AppConstant.IZ_TIMER_VALUE_MESSAGE,AppConstant.IZ_TIMER_MESSAGE);
            return "";
        }
    }

    // convert minutes into millis
    private static long MinutesToMillisecondsConverter(int minutes) {
        return (minutes * 60 * 1000L);
    }

    // news hub
    protected static String getColorCode(String color){
        if (color.startsWith("#")){
            return color;
        }else {
            return "#"+color;
        }
    }

    // NewsHub
    protected static String toSHA1(String url) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(url.getBytes("UTF-8"));

        return new BigInteger(1, crypt.digest()).toString(16);
    }

    protected static void newsHubClickApi(Context context, Payload userModal) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            HashMap<String, String> hashMap = new HashMap<>();
            if (preferenceUtil != null) {
                hashMap.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                hashMap.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
                hashMap.put(AppConstant.VER_, "" + AppConstant.SDKVERSION);
                hashMap.put(AppConstant.OP, "click");
                hashMap.put("cs", "1");
                hashMap.put(AppConstant.ISID_, "1");
                if (userModal != null) {
                    hashMap.put(AppConstant.CID_, userModal.getId());
                    hashMap.put(AppConstant.RID, userModal.getRid());
                }
            }
            RestClient.postRequest(RestClient.NOTIFICATIONCLICK, hashMap, null, new RestClient.ResponseHandler() {
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
            e.printStackTrace();
        }
    }
    static boolean enableStickyNotification(Payload payload){
        return payload.getMakeStickyNotification() != null &&
                !payload.getMakeStickyNotification().isEmpty() && payload.getMakeStickyNotification().equals("1");
    }

    // parse vibration pattern
    static long[] parseVibrationPattern(Object patternObj) {
        try {
            JSONArray jsonVibArray;
            if (patternObj instanceof String) {
                jsonVibArray = new JSONArray((String) patternObj);
            }
            else {
                jsonVibArray = (JSONArray) patternObj;
            }
            long[] longArray = new long[jsonVibArray.length()];
            for (int i = 0; i < jsonVibArray.length(); i++) {
                longArray[i] = jsonVibArray.optLong(i);
            }
            return longArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    // To Handle Exception once
    static void handleExceptionOnce(Context context, String exception, String className, String methodName){
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if(!preferenceUtil.getBoolean(methodName)){
            setException(context, exception, className, methodName);
            preferenceUtil.setBooleanData(methodName, true);
        }
        DebugFileManager.createExternalStoragePublic(context, exception + " " +methodName, "[Log.e]-> "+className);
    }
    /* News Hub offline campaigns */
    protected static int getValidIdForCampaigns(Payload payload){
        int digit = 0;
        try{
            String digits = payload.getRid().trim();
            digit = digits.charAt(0) - '0';
        }catch (Exception e){
            Util.handleExceptionOnce(iZooto.appContext,e.toString(),"Util","getValidIdForCampaigns");
            DebugFileManager.createExternalStoragePublic(iZooto.appContext, e.toString(), "[Log.e]->RID");
        }
        return digit;
    }


}
