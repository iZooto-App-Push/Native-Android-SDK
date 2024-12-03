package com.izooto;

import org.json.JSONException;
import org.json.JSONObject;

public class AppDetails {

    private String appName;
    private String appVersion;
    private String packageName;
    private String os_version;
    private String deviceId;
    private String deviceName;
    private String deviceModel;

    // Constructor to initialize app details
    public AppDetails(String appName, String appVersion, String packageName,String os_version,String deviceId,String deviceName,String deviceModel) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.packageName = packageName;
        this.os_version = os_version;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
    }

    // Getters
    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    // Convert AppDetails object to JSONObject
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("app_name", appName);
            jsonObject.put("app_version", appVersion);
            jsonObject.put("package_name", packageName);
            jsonObject.put("os_version",    os_version);
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("deviceName", deviceName);
            jsonObject.put("deviceModel", deviceName);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
