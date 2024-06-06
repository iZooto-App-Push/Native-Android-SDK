package com.izooto.fcmreceiver;

import android.content.Context;
import android.os.Bundle;

class NotificationProcessingListener {

    static boolean beginEnqueueWorkProcessing(Context context, Bundle bundle, boolean notificationInvoker) {
        return NotificationWorkManagerOSProcessor.notificationWorkManagerEnqueueing(context, bundle, notificationInvoker);
    }

    static void processedNotificationId(Context context, long interval) {
        NotificationIdsProcessorManager.withdrawDuplicateNotificationId(context, interval);
    }

}
