package com.app.izoototest;

import android.app.Application;
import android.util.Log;

import com.izooto.IZootoNotificationMessagereceiver;
import com.izooto.Lg;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

public class AppController extends Application implements TokenReceivedListener
{

    @Override
    public void onCreate() {
        super.onCreate();

        iZooto.initialize(this).setTokenReceivedListener(this).build();

    }

   @Override
    public void onTokenReceived(String token) {
        Lg.i("Device token", token + "");


    }



}