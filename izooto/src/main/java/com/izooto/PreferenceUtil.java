package com.izooto;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


public class PreferenceUtil {

    public static final String SHARED_PREF_NAME = "izooto";
    private static PreferenceUtil mContext;
    private final SharedPreferences mSpref;
    private String TAG = PreferenceUtil.class.getSimpleName();

    private PreferenceUtil(Context context) {
        mSpref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (mContext == null)
            mContext = new PreferenceUtil(context);
        return mContext;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void logoutUser(Context context) {
        SharedPreferences appInstallInfoSharedPref = context.getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor appInstallInfoEditor = appInstallInfoSharedPref.edit();
        appInstallInfoEditor.clear();
        appInstallInfoEditor.commit();
    }


    public int getIntData(String key) {
        return mSpref.getInt(key, 0);
    }


    public void setFloatData(Context context, String key, float value) {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putFloat(key, value);
        appInstallInfoEditor.commit();
    }

    public void setIntData(String key, int value) {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putInt(key, value);
        appInstallInfoEditor.commit();
    }

    public void setStringData(String key, String value) {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putString(key, value);
        appInstallInfoEditor.apply();
    }

    public boolean getBoolean(String key) {
        return mSpref.getBoolean(key, false);
    }

    public String getStringData(String key) {
        return mSpref.getString(key, "");

    }

    public String getStringDataFilterCount(String key) {
        return mSpref.getString(key, "0");

    }

    public String getTokenStringData(String key) {
//        String refreshedToken = mSpref.getString(key, "");
//        if (refreshedToken == null || refreshedToken.length() == 0)
//            refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        return refreshedToken;
        final String[] refreshedToken = {mSpref.getString(key, "")};

        if (refreshedToken[0] == null || refreshedToken[0].length() == 0) {

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            refreshedToken[0] = token;

                        }
                    });
        }
        return refreshedToken[0];



    }

    public void setBooleanData(String key, boolean value) {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putBoolean(key, value);
        appInstallInfoEditor.apply();
    }

    public long getLongValue(String key) {
        if (mSpref.contains(key))
            return mSpref.getLong(key, 0L);
        else
            Lg.e(TAG, "KEY NOT FOUND");

        return 0l;
    }

    public void setLongData(String key, long value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putLong(key, value);
        editor.apply();
    }
    public void setiZootoID(String key,String id)
    {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putString(key, id);
        appInstallInfoEditor.apply();
    }
    public String getiZootoID(String key)
    {
        return mSpref.getString(key, "");
    }


}
