package com.izooto;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;


public class PreferenceUtil {

    public static final String SHARED_PREF_NAME = "DATAB";
    private static PreferenceUtil mContext;
    private final SharedPreferences mSpref;
    private final String TAG = PreferenceUtil.class.getSimpleName();

    private PreferenceUtil(Context context) {
        mSpref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (mContext == null)
            mContext = new PreferenceUtil(context);
        return mContext;
    }

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

    public String getSoundName(String key) {
        return mSpref.getString(key, null);

    }

    public String getStringDataFilterCount(String key) {
        return mSpref.getString(key, "0");

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
            Lg.e(TAG, AppConstant.KEY_NOT_FOUND);

        return 0L;
    }

    public void setLongData(String key, long value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putLong(key, value);
        editor.apply();
    }
    public void setIZootoID(String key, String id)
    {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putString(key, id);
        appInstallInfoEditor.apply();
    }
    public String getiZootoID(String key)
    {
        return mSpref.getString(key, "");
    }
    public boolean getEnableState(String key) {
        return mSpref.getBoolean(key, true);
    }

    // InAppMessaging
    public void setExitIntentData(JSONObject existIntent) {

        SharedPreferences.Editor editor = mSpref.edit();
        try {
            editor.putString("exitIntent", existIntent.toString()).apply();
        } catch (Exception json) {
            json.printStackTrace();
        }
    }
    // InAppMessaging
    public JSONObject getExitIntentData() {
        JSONObject jsonObject = null;
        try {
            String value = mSpref.getString("exitIntent", "");
            assert value != null;
            if (!value.isEmpty()){
                jsonObject = new JSONObject(value);
            }
        } catch (Exception e) {
            Log.e("getExitIntentData",e.toString());
        }

        return jsonObject;
    }

    public void setPlacement(String key, JSONArray value) {
        SharedPreferences.Editor appInstallInfoEditor = mSpref.edit();
        appInstallInfoEditor.putString(key, value.toString());
        appInstallInfoEditor.apply();
    }

    public String getPlacement(String key) {
        return mSpref.getString(key, "");

    }

}
