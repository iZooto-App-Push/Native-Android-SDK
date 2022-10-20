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

public class AppController extends Application implements TokenReceivedListener,NotificationHelperListener, NotificationWebViewListener

{

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        iZooto.initialize(this)
                .setNotificationReceiveListener(this)
                .setTokenReceivedListener(this)
                .build();
        iZooto.isHybrid = true;
      //iZooto.setNotificationSound("pikachu");// no use extesnion  name
        iZooto.setDefaultTemplate(PushTemplate.DEFAULT);
       // iZooto.setDefaultNotificationBanner(R.drawable.splash_image);
    }

    @Override
    public void onTokenReceived(String token) {
        Log.e("TokenData",token);

    }

    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("Payload",payload.getTitle());
        Log.e("Payload",payload.getBanner());
        Log.e("Payload",payload.getLink());
        Log.e("Payload",payload.getMessage());
        Log.e("Payload",payload.getAdID());





    }

    @Override
    public void onNotificationOpened(String data) {
         Intent intent=new Intent(this,MainActivity.class);// launch activity name
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(intent);
    }


    @Override
    public void onWebView(String landingUrl) {
        Log.e("LandingURL",landingUrl);
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}