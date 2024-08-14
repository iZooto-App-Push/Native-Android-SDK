package com.app.izoototest;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.izooto.AppConstant;
import com.izooto.NotificationHelperListener;
import com.izooto.NotificationReceiveHybridListener;
import com.izooto.NotificationWebViewListener;
import com.izooto.Payload;
import com.izooto.PreferenceUtil;
import com.izooto.TokenReceivedListener;
import com.izooto.core.SubscriptionInterval;
import com.izooto.iZooto;

public class AppController extends Application implements NotificationHelperListener, NotificationWebViewListener, NotificationReceiveHybridListener {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
// iZooto initialisation code
        iZooto.initialize(this)
//                .setAppId("f8908fd9817ad7f76dea934067955773a8f1c3d5")
                .setNotificationReceiveListener(this)
                .setLandingURLListener(this)
                .setTokenReceivedListener(token -> {})
                .build();

       iZooto.promptForPushNotifications();

    }

    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("ABC", payload.getAct1link());
        Log.e("ABC", payload.getAct2link());
        Log.e("ABC", payload.getLink());

    }

    @Override
    public void onNotificationOpened(String data) {
        Log.e("ABC",data);
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