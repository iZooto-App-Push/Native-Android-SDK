package com.izooto;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class NotificationExecutorService {
    private final Context mContext;
    public NotificationExecutorService(final Context context) {
        this.mContext = context;
    }

    protected void executeNotification(final Handler handler, final Runnable runnable, final Payload payload){
        if(mContext != null){
            try {
                new AppExecutors().diskIO().execute(() -> {
                    Bitmap notificationBanner;
                    Bitmap notificationIcon;
                    String smallIcon = payload.getIcon();
                    String banner = payload.getBanner();
                    try {
                        if (smallIcon != null && !smallIcon.isEmpty()) {
                            notificationIcon = Util.getBitmapFromURL(smallIcon);
                            payload.setIconBitmap(notificationIcon);
                        }
                        if (banner != null && !banner.isEmpty()) {
                            notificationBanner = Util.getBitmapFromURL(banner);
                            payload.setBannerBitmap(notificationBanner);
                        }
                        handler.post(runnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e){
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationExecutorService", "executeNotification");
            }
        }
    }
}