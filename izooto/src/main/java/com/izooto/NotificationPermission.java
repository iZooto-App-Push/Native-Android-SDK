package com.izooto;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.izooto.AppConstant;
import com.izooto.Lg;
import com.izooto.PreferenceUtil;
import com.izooto.R;
import com.izooto.iZooto;

public class NotificationPermission extends Activity {
    private static final int NOTIFICATION_PERMISSION_CODE = 90905676;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission(android.Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_CODE);
    }

    public void checkPermission(String permission, int requestCode) {
        try {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            } else {
                finish();
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "checkPermission");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            try {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                        if (iZooto.hms_appId != null && !iZooto.hms_appId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Huawei") && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_HUAWEI_TOKEN)) {
                            iZooto.initHmsService(iZooto.appContext);
                        }
                        if (iZooto.senderId != null && !iZooto.senderId.isEmpty()) {
                            iZooto.init(iZooto.appContext, iZooto.senderId);
                        } else {
                            Lg.e(AppConstant.APP_NAME_TAG, iZooto.appContext.getString(R.string.something_wrong_fcm_sender_id));
                        }
                        finish();
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                        finish();
                    } else {
                        finish();
                    }
                }
                overridePendingTransition(R.anim.izooto_notification_permission_fade_in, R.anim.izooto_notification_permission_fade_out);
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), AppConstant.APP_NAME_TAG, "onRequestPermissions");
            }
        }
    }

}




