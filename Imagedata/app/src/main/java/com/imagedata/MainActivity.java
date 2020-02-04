package com.imagedata;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.webkit.WebView;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity {
    public SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView webView = findViewById(R.id.webview);
        webView.loadUrl("https://www.google.com");
        sendNotifcation();

    }

    @SuppressLint("WrongConstant")
    private void sendNotifcation() {
//        String idChannel = "my_channel_01";
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        NotificationChannel mChannel = null;
//        // The id of the channel.
//
//        int importance = notificationManager.IMPORTANCE_HIGH;
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setContentTitle("Dummy content title")
//                .setContentText("Dummy content text")
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine("Line 1")
//                        .addLine("Line 2")
//                        .setSummaryText("Inbox summary text")
//                        .setBigContentTitle("Big content title"))
//                .setNumber(2)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setCategory(Notification.CATEGORY_EVENT)
//                .setGroupSummary(true)
//                .setGroup("group");
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mChannel = new NotificationChannel(idChannel, "hrllo", importance);
//            // Configure the notification channel.
//
//
//            mChannel.setLightColor(Color.RED);
//            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//            notificationManager.createNotificationChannel(mChannel);
//
//            Notification notification = builder.build();
//            notificationManager.notify(123456, notification);


//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
//                    "YOUR_CHANNEL_NAME",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
//            mNotificationManager.createNotificationChannel(channel);
//        }
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
//                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
//                .setContentTitle("hloo") // title for notification
//                .setContentText("hllll")
//                .setContent()
//
//
//
//                // message for notification
//                .setAutoCancel(true); // clear notification after click
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(pi);
//        mNotificationManager.notify(0, mBuilder.build());

     

    }
    }


