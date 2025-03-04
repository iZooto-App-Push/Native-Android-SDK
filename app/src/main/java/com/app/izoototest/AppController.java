package com.app.izoototest;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.ads.MobileAds;
import com.izooto.NotificationHelperListener;
import com.izooto.NotificationReceiveHybridListener;
import com.izooto.NotificationWebViewListener;
import com.izooto.Payload;
import com.izooto.iZooto;

public class AppController extends Application implements NotificationHelperListener, NotificationWebViewListener, NotificationReceiveHybridListener {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();

        iZooto.initialize(this)
                .setNotificationReceiveListener(this)
                .setLandingURLListener(this)
                .setTokenReceivedListener(token -> {})
                .build();

       iZooto.promptForPushNotifications();

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();
    }

    @Override
    public void onNotificationReceived(Payload payload) {
//        Log.e("ABC", payload.getAct1link());
//        Log.e("ABC", payload.getAct2link());
//        Log.e("ABC", payload.getLink());

    }

    @Override
    public void onNotificationOpened(String data) {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);

    }

    @Override
    public void onWebView(String landingUrl) {
        Log.e("ABC",landingUrl);
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }


    @Override
    public void onNotificationReceivedHybrid(String receiveData) {
        Log.e("ABC","hybrid >> "+receiveData);
    }
}