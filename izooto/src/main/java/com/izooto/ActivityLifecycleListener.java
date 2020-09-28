package com.izooto;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

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

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        iZooto.onActivityResumed(activity);

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
