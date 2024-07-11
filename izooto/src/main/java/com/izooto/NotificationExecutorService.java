package com.izooto;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationExecutorService {
    private final Context mContext;
    public NotificationExecutorService(final Context context) {
        this.mContext = context;
    }

    public void executeNotification(final Handler handler, final Runnable runnable, final Payload payload) {
        if (mContext != null) {
            try {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(() -> {
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
                executorService.shutdown();
            } catch (Exception e) {
                Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NotificationExecutorService", "executeNotification");
            }
        }
    }


}
