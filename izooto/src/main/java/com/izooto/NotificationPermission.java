package com.izooto;
import static com.izooto.iZooto.appContext;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.HashMap;
import java.util.Map;
public class NotificationPermission extends Activity {
    private static final int NOTIFICATION_PERMISSION_CODE = 123456;
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission(android.Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_CODE);
    }
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {

            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {

            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   permissionAllow(this);
                    finish();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    permissionDisallow(this);
                        finish();
                    } else {
                        finish();
                    }

                }
            overridePendingTransition(R.anim.izooto_notification_permission_fade_in, R.anim.izooto_notification_permission_fade_out);
        }
    }


   public static void permissionAllow(Context context) {

        if (context != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            try {

                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(context));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.QSDK_VERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.KEY_HMS, "" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                        mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.OPTIN_, "" + AppConstant.OPTIN);
                        mapData.put(AppConstant.NDC_, "" + AppConstant.NDC);
                        mapData.put(AppConstant.SDC_, "" + AppConstant.SDC);
                        mapData.put(AppConstant.ALLOWED_, "" + AppConstant.ALLOWED);
                        RestClient.postRequest(RestClient.NOTIFICATION_PERMISSION_ALLOW_URL, mapData, null, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_PROMPT_ALLOW, null);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_PROMPT_ALLOW,mapData.toString());

                            }
                        });

                    }
                }

            } catch (Exception ex) {
                if(appContext!=null) {
                    Util.setException(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "permissionAllow");
                    DebugFileManager.createExternalStoragePublic(appContext, "permissionAllow" + ex, "[Log.e]->Exception->");
                }
            }
        }

    }


    public static void permissionDisallow(Context context) {

        if (context != null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            try {
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && preferenceUtil.getIntData(AppConstant.CAN_STORED_QUEUE) > 0) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(context));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.QSDK_VERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.KEY_HMS, "" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                        mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.OPTIN_, "" + AppConstant.OPTIN);
                        mapData.put(AppConstant.NDC_, "" + AppConstant.NDC);
                        mapData.put(AppConstant.SDC_, "" + AppConstant.SDC);
                        mapData.put(AppConstant.DENIED_, "" + AppConstant.DENIED);
                        RestClient.postRequest(RestClient.NOTIFICATION_PERMISSION_DISALLOW_URL, mapData, null, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_PROMPT_DISALLOW,null);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                                preferenceUtil.setStringData(AppConstant.NOTIFICATION_PROMPT_DISALLOW, mapData.toString());

                            }
                        });
                    }
                }

            } catch (Exception ex) {
                if (appContext != null) {
                    Util.setException(appContext, ex.toString(), AppConstant.APP_NAME_TAG, "permissionDisallow");
                    DebugFileManager.createExternalStoragePublic(appContext, "permissionDisallow" + ex, "[Log.e]->Exception->");
                }
            }
        }
    }
}




