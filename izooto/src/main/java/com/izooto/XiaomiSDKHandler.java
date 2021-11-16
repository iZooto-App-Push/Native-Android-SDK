package com.izooto;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

public class XiaomiSDKHandler implements XiaomiSDKHandlerListener {
    private boolean isRegistered;
    private String XIAOMI_LOG_TAG="iZooto_xiaomi";
    private String mi_app_id,mi_app_key;
    private Context mContext;


    public XiaomiSDKHandler(Context context, String appID, String appKey)
    {
        mi_app_id = appID;
        mi_app_key = appKey;
        mContext =context;
        register(mi_app_id,mi_app_key);
    }
    @Override
    public String appId() {
        return null;
    }

    @Override
    public String apiKey() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String onMIToken() {
        String token = null;
        if (!isRegistered) {
            init();
        }
        try {
                token = MiPushClient.getRegId(mContext);
                Log.e("Token",token);

        } catch (Throwable t) {
        }

        return token;
    }
    @RestrictTo(value = RestrictTo.Scope.LIBRARY)
    public void register(String appId, String appKey)  {
        try {
            MiPushClient.registerPush(mContext, appId, appKey);
            isRegistered = true;

        } catch (Throwable throwable) {
            isRegistered = false;
        }
    }
    boolean shouldInit(String mainProcessName) {

        ActivityManager am = ((ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE));

        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();

        int myPid = android.os.Process.myPid();


        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private void init() {
        String packageName = mContext.getPackageName();
        if (shouldInit(packageName)) {
            String appId = mi_app_id;//appId();
            String appKey = mi_app_key;///apiKey();
            try {
                register(appId, appKey);
            } catch (Throwable t) {
                Log.e("MIException",toString());
            }
        }
    }
}
