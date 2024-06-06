package com.izooto.fcmreceiver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.izooto.AppConstant;
import com.izooto.PreferenceUtil;
import com.izooto.Util;
import com.izooto.iZooto;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NotificationWorkManagerOSProcessor extends Worker {
    static boolean isNotificationRestored = false;
    private final Context context;
    private final static String tagName = "NotificationWorkManager";
    private static final ConcurrentHashMap<String, Boolean> notificationId = new ConcurrentHashMap<>();

    public NotificationWorkManagerOSProcessor(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    static boolean notificationWorkManagerEnqueueing(Context context, Bundle extras, boolean invokerNotificationRestoring) {
        return notificationsEnqueueProcessing(context, extras, invokerNotificationRestoring);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                if (iZooto.initialized()) {
                    return Result.success();
                }
            }
            Data result = getInputData();
            String id = result.getString(AppConstant.IZ_BEGIN_ENQUEUE_ID);
            isNotificationRestored = result.getBoolean("is_restored", false);
            if (id == null) {
                return Result.failure();
            }
            try {
                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(result.getString(AppConstant.IZ_JSON_PAYLOAD)));
                if (isNotificationRestored) {
                    NotificationsProcessor.processNotificationService(context, jsonObject);
                }
            } catch (Throwable e) {
                Util.handleExceptionOnce(context, e.toString(), tagName, "doWork");
                return Result.failure();
            } finally {
                removeNotificationIdProcessed(id);
            }
             return Result.success();
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), tagName, "doWork");
            return Result.failure();
        }
    }

    private static boolean addNotificationIdProcessed(String osNotificationId) {
        if (Util.isStringNotEmpty(osNotificationId)) {
            if (notificationId.containsKey(osNotificationId)) {
                Log.e("IdProcessed", "NotificationWorkManagerOSProcessor notification with notificationId: " + osNotificationId + " already queued");
                return false;
            } else {
                notificationId.put(osNotificationId, true);
            }
        }
        return true;
    }

    private static boolean notificationsEnqueueProcessing(Context context, Bundle extras, boolean invokerNotificationRestoring) {
        try {
            JSONObject remoteData = new JSONObject(Util.setExtrasAsJson(context, extras));
            String androidNotificationId = extras.getString(AppConstant.IZ_BEGIN_ENQUEUE_ID);

            if (androidNotificationId == null) {
                Log.e("wmEnqueueProcessing", "Notification beginEnqueueingWork with id null");
                return false;
            }
            if (!addNotificationIdProcessed(androidNotificationId)) {
                Log.e("wmEnqueueProcessing", "Notification beginEnqueueingWork with id duplicated");
                return true;
            }

            Data inputData = new Data.Builder()
                    .putString(AppConstant.IZ_JSON_PAYLOAD, remoteData.toString())
                    .putString(AppConstant.IZ_BEGIN_ENQUEUE_ID, androidNotificationId)
                    .putBoolean("is_restored", invokerNotificationRestoring)
                    .build();
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest
                    .Builder(NotificationWorkManagerOSProcessor.class)
                    .setInputData(inputData)
                    .setInitialDelay(0, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                            androidNotificationId,
                            ExistingWorkPolicy.KEEP,
                            workRequest);
            WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(workRequest.getId())
                    .observeForever(workInfo -> {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.d("Tracker", "COMPLETED");
                        }
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.RUNNING) {
                            Log.d("Tracker", "RUNNING");
                        }
                    });
            return true;

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), tagName, "notificationsEnqueueProcessing");
            return false;
        }
    }

    private static void removeNotificationIdProcessed(String osNotificationId) {
        if (Util.isStringNotEmpty(osNotificationId)) {
            notificationId.remove(osNotificationId);
        }
    }
}
