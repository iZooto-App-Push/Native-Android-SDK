package com.izooto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommonDataManager {

    private static CommonDataManager instance;

    private Map<String, Object> additionalParams;

    private CommonDataManager() {
        additionalParams = new HashMap<>();
    }

    // Singleton instance
    public static synchronized CommonDataManager getInstance() {
        if (instance == null) {
            instance = new CommonDataManager();
        }
        return instance;
    }

    // Method to add a custom parameter
    public void addCustomParam(String key, Object value) {
        additionalParams.put(key, value);
    }

    // Get the common parameters as a JSONObject
    public JSONObject getCommonParamsAsJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            // Add additional custom parameters
            for (Map.Entry<String, Object> entry : additionalParams.entrySet()) {
                if (entry.getValue() instanceof AppDetails) {
                    // If the parameter is an AppDetails object, convert it to JSON
                    jsonObject.put(entry.getKey(), ((AppDetails) entry.getValue()).toJson());
                } else {
                    // Otherwise, just put the value
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}