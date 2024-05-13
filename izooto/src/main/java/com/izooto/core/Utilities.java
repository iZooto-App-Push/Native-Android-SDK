package com.izooto.core;

import android.content.Context;

import com.izooto.AppConstant;
import com.izooto.PreferenceUtil;
import com.izooto.Util;

public class Utilities {
    private static final String IZ_CLASSNAME = "Utilities";

    // Private constructor to prevent instantiation
    private Utilities() {
        throw new AssertionError("Utilities class cannot be instantiated");
    }

    // Method to check if a string is empty or null
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Method to check if a user registered
    public static boolean hasUserRegistered(Context context) {
        if (context == null) {
            return false;
        }
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String pid = preferenceUtil.getStringData(AppConstant.APPPID);
            String fcmToken = preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN);
            String hmsToken = preferenceUtil.getStringData(AppConstant.HMS_TOKEN);
            return !(isNullOrEmpty(pid) || (isNullOrEmpty(fcmToken) && isNullOrEmpty(hmsToken)));
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "hasUserRegistered");
            return false;
        }
    }

    // Method to check if a user has pid
    public static boolean hasUserPid(Context context) {
        if (context == null) {
            return false;
        }
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String pid = preferenceUtil.getStringData(AppConstant.APPPID);
            return !(isNullOrEmpty(pid));
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASSNAME, "hasUserPid");
            return false;
        }
    }

}
