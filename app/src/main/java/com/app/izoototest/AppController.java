package com.app.izoototest;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

import com.izooto.ActivityLifecycleListener;
import com.izooto.NotificationWebViewListener;
import com.izooto.iZooto;
import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;
import com.izooto.PushTemplate;

public class AppController extends Application implements TokenReceivedListener,NotificationHelperListener, NotificationWebViewListener

{

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        iZooto.initialize(this)
                .setNotificationReceiveListener(this)
                .setLandingURLListener(this)
                .setTokenReceivedListener(this)
                .build();
        iZooto.promptForPushNotifications();

    }
    @Override
    public void onTokenReceived(String token) {
        Log.e("TokenData",token);

    }
    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("Payload",payload.getTitle());
    }

    @Override
    public void onNotificationOpened(String data) {
         Intent intent=new Intent(this,MainActivity.class);// launch activity name
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(intent);
    }


    @Override
    public void onWebView(String landingUrl) {
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}