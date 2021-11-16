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
        Log.e("Response","onActivityCreated");
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
        ComponentName componentName = intent.getComponent();
        Log.e("Response",componentName.getClassName());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStarted","[Log.e]->");
        Log.e("Response","onActivityStarted");



    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        iZooto.onActivityResumed(activity);
        DebugFileManager.createExternalStoragePublic(activity,"onActivityResumed","[Log.e]->");
        Log.e("Response","onActivityResumed");



    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityPaused","[Log.e]->");
        Log.e("Response","onActivityPaused");


    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityStopped","[Log.e]->");
        Log.e("Response","onActivityStopped");


    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivitySaveInstanceState","[Log.e]->");
        Log.e("Response","onActivitySaveInstanceState");

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        DebugFileManager.createExternalStoragePublic(activity,"onActivityDestroyed","[Log.e]->");
        Log.e("Response","onActivityDestroyed");

    }
}
