package com.izooto;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    @Nullable private static ActivityLifecycleListener mActivityLifecycleListener;
    static  boolean isCheckForeground = false;

   public static void registerActivity(@NonNull final Application application) {

        if (mActivityLifecycleListener == null) {
            mActivityLifecycleListener = new ActivityLifecycleListener();
            application.registerActivityLifecycleCallbacks(mActivityLifecycleListener);
        }
    }
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        iZooto.curActivity =activity;
        DebugFileManager.createExternalStoragePublic(activity,"onActivityCreated","[Log.e]->");
        storeForegroundData(activity,true);
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
        preferenceUtil.setBooleanData(AppConstant.DEVICE_ONCREATE_STATE, true);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        iZooto.curActivity =activity;
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");
        storeForegroundData(activity,true);
        isStoreDataKilledState(activity,true);
        isCheckForeground = true;

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        iZooto.curActivity =activity;
        iZooto.onActivityResumed(activity);
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");
        storeForegroundData(activity,true);
        isStoreDataKilledState(activity,true);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");
        storeForegroundData(activity,false);

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
        preferenceUtil.setBooleanData(AppConstant.DEVICE_BACKGROUND_STATE,true);
        isStoreDataKilledState(activity,false);
        preferenceUtil.setBooleanData(AppConstant.DEVICE_ONPAUSE_STATE,true);

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");
        storeForegroundData(activity,false);

    }


    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");
        storeForegroundData(activity,false);
        isStoreDataKilledState(activity,false);

    }

    public  void storeForegroundData(Activity activity, boolean isForeground){
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
        preferenceUtil.setBooleanData(AppConstant.DEVICE_STATE_CHECK,isForeground);
    }

    public void isStoreDataKilledState(Activity activity,boolean isData)
    {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(activity);
        preferenceUtil.setBooleanData(AppConstant.DEEPLINK_STATE,isData);
    }
}