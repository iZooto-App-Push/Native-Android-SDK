package com.app.izoototest;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.izooto.NotificationWebViewListener;
import com.izooto.iZooto;
import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;
import com.izooto.PushTemplate;

public class AppController extends Application

{

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        iZooto.initWithContext(this);
        iZooto.setAppId("09f06385e06cc10d0fc7a5e1c002cd9338a2c94f");
        iZooto.setTokenReceivedListener(new TokenReceivedListener() {
            @Override
            public void onTokenReceived(String token) {
                Log.e("Token",token);
            }
        });
        iZooto.setDefaultTemplate(PushTemplate.DEFAULT);
        iZooto.setNotificationReceiveListener(new NotificationHelperListener() {
            @Override
            public void onNotificationReceived(Payload payload) {
                Log.e("Received Payload",payload.getTitle());
            }

            @Override
            public void onNotificationOpened(String data) {
                Log.e("onNotificationOpened",data);


            }
        });

    }



}