package com.app.izoototest;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.izooto.Lg;
import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

public class AppController extends Application

{

    @Override
    public void onCreate() {
        super.onCreate();
        iZooto.initialize(this).build();
       // iZooto.setIcon(R.drawable.splash_image);
       // iZooto.initialize(this).setNotificationReceiveListener(new ExampleNotificationHandleListener());
       // iZooto.initialize(this).setTokenReceivedListener(new ExampleTokenListener());
       // iZooto.setSubscription(true); // handle the notification showing from panel or not
       // iZooto.setIcon(R.drawable.ic_launcher_background);// handle the notification icon from app
      // Get Advertisement iD and Registration ID
//        iZooto.idsAvailable(this, new iZooto.Listener() {
//            @Override
//            public void idsAvailable(String adID, String registrationID) {
//                Log.e("AdvertiseMentID",adID);
//                Log.e("RegistrationID",registrationID);
//            }
//
//            @Override
//            public void onAdvertisingIdClientFail(Exception exception) {
//                Log.e("Failure",exception.toString());
//
//            }
//        });
      //  iZooto.setFirebaseAnalytics(true);// Firebase Analytics Enable /Disable
       // iZooto.iZootoHandleNotification(this,data);// Handle the notification data from Messaging service class
      //  iZooto.setInAppNotificationBehaviour(iZooto.OSInAppDisplayOption.InAppAlert);
        // Handle the notification behaviour
        /*
        1- iZooto.OSInAppDisplayOption.Notification - > Default Notification
        2- iZooto.OSInAppDisplayOption.NoNE-> Show Notification only when app is background
        3- iZooto.OSInAppDisplayOption.InAppAlert-> Show the alert dialog When app is foreground


         */
       // iZooto.initialize(this).unsubscribeWhenNotificationsAreDisabled(true)
        /*
        Check the notification is enable for app setting or not
         */
        /*
        iZooto.addUserProperty(data);//
        data=HashMap<String,Object>
        use for segmentation
        */
      /*
        iZooto.addEvent(eventName,data);//
        data = HashMap<String,Object>
        use for segmentation
                */





    }


    class ExampleTokenListener implements TokenReceivedListener
    {
        @Override
        public void onTokenReceived(String token) {
            Lg.i("Device token", token + "");


        }
    }
    class ExampleNotificationHandleListener implements  NotificationHelperListener
    {
        @Override
        public void onNotificationReceived(Payload payload) {
            Log.e("Received",payload.getTitle());
        }

        @Override
        public void onNotificationOpened(String data) {
            Log.e("Data",data);


        }


   }
}