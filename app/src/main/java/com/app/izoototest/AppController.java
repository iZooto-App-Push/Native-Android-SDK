package com.app.izoototest;

import android.app.Application;
import android.util.Log;

import com.izooto.iZooto;
import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;

public class AppController extends Application implements TokenReceivedListener,NotificationHelperListener

{

    @Override
    public void onCreate() {
        super.onCreate();

        iZooto.initialize(this)
                .setNotificationReceiveListener(this)
                .setTokenReceivedListener(this)

                .build();


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
     Log.e("Data",data);
    }





}