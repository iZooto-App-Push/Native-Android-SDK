package com.izooto.feature.pulseweb

import android.util.Log
import com.izooto.AppConstant
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object PulseURLManager {
    @JvmField
    var encodedURL: String? = null
    @JvmStatic
    fun updatePulseURL(notificationData: String?) {
        try {
            encodedURL = jsonUrlEncode(notificationData)
        } catch (ex: Exception) {
            Log.e(AppConstant.APP_NAME_TAG, ex.toString())
        }
    }

    @JvmStatic
    fun encodeFeedSource(feedUrl : String?) : String?{
        try {
            return jsonUrlEncode(feedUrl)
        } catch (ex : Exception){
            Log.e(AppConstant.APP_NAME_TAG, ex.toString())
        }
        return ""
    }

    private fun jsonUrlEncode(data: String?): String? {
        try {
            val encodedString = URLEncoder.encode(data, StandardCharsets.UTF_8.toString())
            return encodedString
        } catch (ex: Exception) {
            Log.e(AppConstant.APP_NAME_TAG, ex.toString())
        }
        return ""
    }

}
