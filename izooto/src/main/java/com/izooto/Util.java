package com.izooto;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Util {

    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static int CIPHER_KEY_LEN = 16;

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

    public static boolean getNetworkState(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            }
            return null;
        } catch (IOException e) {
            // Log exception
            return null;
        }
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

    public static String getSDKVersion() {
        try {
            PackageInfo pInfo = iZooto.appContext.getPackageManager().getPackageInfo(iZooto.appContext.getPackageName(), 0);
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
    public static String trimeString(String optString) {
        if(optString.length()>32)
        {
            int length=optString.length()-32;
            return optString.substring(0,length);
        }
        return null;
    }

    public static boolean CheckValidationString(String optString) {
        if(optString.length()>32)
        {
            return true;
        }
        else
        {
            return false;
        }
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
//    public static String getAdvertisingID(final Context context) {
//        final String advertisementID;
//        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    AdvertisingIdClient.Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
//                    if (!advertisingIdInfo.isLimitAdTrackingEnabled()) {
//                        String id = advertisingIdInfo.getId();
//                        preferenceUtil.setStringData(AppConstant.ADVERTISEMENT_ID, id);
//                    } else {
//                        Log.e(AppConstant.APP_NAME_TAG, "If you stored the id you should remove it" );
//                    }
//                } catch (IOException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        advertisementID = preferenceUtil.getStringData(AppConstant.ADVERTISEMENT_ID);
//        return advertisementID;
//    }

}
