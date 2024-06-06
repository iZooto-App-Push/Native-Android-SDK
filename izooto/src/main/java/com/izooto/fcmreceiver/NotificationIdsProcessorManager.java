package com.izooto.fcmreceiver;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.izooto.PreferenceUtil;
import com.izooto.Util;

import java.util.concurrent.TimeUnit;

public class NotificationIdsProcessorManager extends Worker {
    private final Context context;
    private final static String tagName = "NotificationIdsProcessorManager";

    public NotificationIdsProcessorManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    static void withdrawDuplicateNotificationId(Context context, long interval) {
        NotificationIdsProcessorManager.processingForWithdrawDuplicateNotificationId(context, interval);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            SharedPreferences sharedPreferences = context.getSharedPreferences(iZootoReceiver.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            preferenceUtil.setBooleanData("timeOut", false);
            return Result.success();
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), tagName, "doWork");
            return Result.failure();
        }
    }

    private static void processingForWithdrawDuplicateNotificationId(Context context, long interval) {
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(NotificationIdsProcessorManager.class)
                    .setInitialDelay(interval, TimeUnit.MILLISECONDS).build();
            workManager.enqueue(oneTimeWorkRequest);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), tagName, "DuplicateNotificationId");
        }
    }
}
