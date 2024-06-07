package com.izooto;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    @Nullable
    private static ActivityLifecycleListener mActivityLifecycleListener;
    static boolean isCheckForeground = false;
    String IZ_FCM_TOKEN_KEY = "->FCM_TOKEN ";

    public static void registerActivity(@NonNull final Application application) {
        try {
            if (mActivityLifecycleListener == null) {
                mActivityLifecycleListener = new ActivityLifecycleListener();
                application.registerActivityLifecycleCallbacks(mActivityLifecycleListener);
            }
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        try {
            iZooto.curActivity = activity;
            storeForegroundData(activity, true);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
            preferenceUtil.setBooleanData(AppConstant.DEVICE_ONCREATE_STATE, true);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        try {
            iZooto.curActivity = activity;
            storeForegroundData(activity, true);
            isStoreDataKilledState(activity, true);
            isCheckForeground = true;
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        try {
            iZooto.curActivity = activity;
            iZooto.onActivityResumed(activity);
            storeForegroundData(activity, true);
            isStoreDataKilledState(activity, true);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
            preferenceUtil.setBooleanData("Android8", false);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        try {
            storeForegroundData(activity, false);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
            preferenceUtil.setBooleanData(AppConstant.DEVICE_BACKGROUND_STATE, true);
            isStoreDataKilledState(activity, false);
            preferenceUtil.setBooleanData(AppConstant.DEVICE_ONPAUSE_STATE, true);
            preferenceUtil.setBooleanData("Android8", true);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        try {
            storeForegroundData(activity, false);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }


    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        try {
            storeForegroundData(activity, false);
            isStoreDataKilledState(activity, false);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    public void storeForegroundData(Activity activity, boolean isForeground) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
            preferenceUtil.setBooleanData(AppConstant.DEVICE_STATE_CHECK, isForeground);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }

    public void isStoreDataKilledState(Activity activity, boolean isData) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
            preferenceUtil.setBooleanData(AppConstant.DEEPLINK_STATE, isData);
        } catch (Exception ex){
            Log.v(AppConstant.APP_NAME_TAG,ex.toString());
        }
    }
}