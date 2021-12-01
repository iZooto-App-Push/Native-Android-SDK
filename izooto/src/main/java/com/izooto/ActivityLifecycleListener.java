package com.izooto;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    @Nullable private static ActivityLifecycleListener mActivityLifecycleListener;

    static void registerActivity(@NonNull final Application application) {

        if (mActivityLifecycleListener == null) {
            mActivityLifecycleListener = new ActivityLifecycleListener();
            application.registerActivityLifecycleCallbacks(mActivityLifecycleListener);
        }
    }
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
       DebugFileManager.createExternalStoragePublic(activity,"onActivityCreated","[Log.e]->");

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");



    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        iZooto.onActivityResumed(activity);
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");



    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");


    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");


    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted"+Util.getAndroidId(activity)+"->FCMTOKEN "+PreferenceUtil.getInstance(activity).getStringData(AppConstant.FCM_DEVICE_TOKEN),"[Log.e]->");

    }
}
