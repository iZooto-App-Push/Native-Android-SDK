package com.izooto;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE = "15";
            Intent it = new Intent(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
            context.sendBroadcast(it);
            Bundle tempBundle = intent.getExtras();
            int notificationID = tempBundle.getInt(AppConstant.KEY_NOTIFICITON_ID);
            NotificationManager notificationManager =
                    (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationID);
        }catch (Exception e){
            Log.e("NotificationDismissed",e.getMessage());
        }
    }
}