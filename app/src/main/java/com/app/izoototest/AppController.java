package com.app.izoototest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

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

    @Override
    public void onUpdatedToken(String token) {
        Log.e("UpdatedToken",token);
    }


}