package com.app.izoototest;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.izooto.Lg;
import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

public class AppController extends Application implements TokenReceivedListener, NotificationHelperListener

{

    @Override
    public void onCreate() {
        super.onCreate();
        iZooto.initialize(this).setTokenReceivedListener(this)
                .setNotificationReceiveListener(this).build();

    }

   @Override
    public void onTokenReceived(String token) {
        Lg.i("Device token", token + "");


    }


    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("Received",payload.getTitle());
    }

    @Override
    public void onNotificationOpened(String data) {
        Log.e("Data",data);

    }
}