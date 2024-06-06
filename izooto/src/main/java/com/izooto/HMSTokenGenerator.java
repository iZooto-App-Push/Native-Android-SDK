package com.izooto;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.push.HmsMessaging;
import com.izooto.core.Utilities;

public class HMSTokenGenerator implements HMSTokenListener {

    private static Context mContext;
    private static HMSTokenGeneratorHandler mHCMTokenGeneratorHandler;
    private static boolean callbackSuccessful;
    private static final int NEW_TOKEN_TIMEOUT = 30_000;

    @Override
    public void getHMSToken(final Context context, final HMSTokenGeneratorHandler hmsTokenGeneratorHandler) {
        mContext = context;
        mHCMTokenGeneratorHandler = hmsTokenGeneratorHandler;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    generateToken(context, hmsTokenGeneratorHandler);
                } catch (com.huawei.hms.common.ApiException e) {
                    Log.v(AppConstant.APP_NAME_TAG, "ApiException - " + e);
                    hmsTokenGeneratorHandler.complete(null);
                    hmsTokenGeneratorHandler.failure(e.getMessage());
                }

            }
        }).start();

    }

    private void generateToken(Context context, HMSTokenGeneratorHandler handler) throws com.huawei.hms.common.ApiException {
        try {
            if (!Util.hasHMSLibraries()) {
                handler.complete(null);
                return;
            }
            String HMS_APP_ID = "client/app_id";
            String appID = AGConnectServicesConfig.fromContext(context).getString(HMS_APP_ID);
            String token = HmsInstanceId.getInstance(context).getToken(appID, HmsMessaging.DEFAULT_TOKEN_SCOPE);
            if (!Utilities.isNullOrEmpty(token)) {
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                preferenceUtil.setStringData(AppConstant.HMS_TOKEN, token);
                Log.i(AppConstant.APP_NAME_TAG, "HMS generateToken:" + token);
                handler.complete(token);
            } else {
                handler.complete(null);
                waitForOnNewPushTokenEvent(handler);
            }
        } catch (Exception ex) {
            Log.e(AppConstant.APP_NAME_TAG, ex.toString());
        }
    }

    void waitForOnNewPushTokenEvent(@NonNull HMSTokenGeneratorHandler callback) {
        doTimeOutWait();
        if (!callbackSuccessful) {
            Log.v(AppConstant.APP_NAME_TAG, "HmsMessageService.onNewToken timed out.");
            callback.complete(null);
        }
    }

    private static void doTimeOutWait() {
        try {
            Thread.sleep(NEW_TOKEN_TIMEOUT);
        } catch (InterruptedException e) {
        }
    }

    public static void getTokenFromOnNewToken(String token) {

        if (mHCMTokenGeneratorHandler == null && mContext == null)
            return;
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(mContext);
        preferenceUtil.setStringData(AppConstant.HMS_TOKEN, token);
        callbackSuccessful = true;
        mHCMTokenGeneratorHandler.complete(token);

    }
}
