package com.izooto;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GooglePlayServiceUtil {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    public static boolean checkForPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(iZooto.appContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Toast.makeText(iZooto.appContext, "Play services not available or may be not updated.", Toast.LENGTH_SHORT);
            } else {
                Log.i(AppConstant.APP_NAME_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }


}
