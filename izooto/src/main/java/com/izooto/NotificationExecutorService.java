package com.izooto;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.izooto.AppExecutors;
import com.izooto.Payload;
import com.izooto.Util;
import com.izooto.iZooto;

public class NotificationExecutorService {
    private final Context mContext;
    public NotificationExecutorService(final Context context) {
        this.mContext = context;
    }

    public void executeNotification(final Handler handler, final Runnable runnable, final Payload payload) {
        if (mContext != null) {
            try {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    try {
                        Bitmap notificationIcon = null;
                        Bitmap notificationBanner = null;
                        String smallIcon = payload.getIcon();
                        String banner = payload.getBanner();
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
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationExecutorService", "executeNotification");
                    }
                });
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationExecutorService", "executeNotification");
            }
        }
    }


}
