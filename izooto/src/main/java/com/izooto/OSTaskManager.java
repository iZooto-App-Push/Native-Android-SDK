package com.izooto;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class OSTaskManager {

    static final String OS_PENDING_EXECUTOR = "OS_PENDING_EXECUTOR_";

    // The concurrent queue in which we pin pending tasks upon finishing initialization
    private final ConcurrentLinkedQueue<Runnable> taskQueueWaitingForInit = new ConcurrentLinkedQueue<>();
    private final AtomicLong lastTaskId = new AtomicLong();
    private ExecutorService pendingTaskExecutor;
    static final String ADD_USERPROPERTY = "addUserProperty()";
    static final String ADD_EVENT = "addEvent()";
    static final String SET_SUBSCRIPTION = "setSubscription()";
    static final String SET_FIREBASE_ANALYTICS = "setFirebaseAnalytics()";
    static final String ADD_TAG = "addTag()";
    static final String REMOVE_TAG = "removeTag()";
    static final String SET_CUSTOM_TEMPLATE = "setCustomTemplate()";
    static final String SET_SUBSCRIBER_ID="setSubscriberID()";
    static final HashSet<String> METHODS_ADD_IN_QUEUE_FOR = new HashSet<>(Arrays.asList(
            ADD_USERPROPERTY,
            ADD_EVENT,
            SET_SUBSCRIPTION,
            SET_FIREBASE_ANALYTICS,
            ADD_TAG,
            REMOVE_TAG,
            SET_CUSTOM_TEMPLATE,
            SET_SUBSCRIBER_ID
    ));

    boolean shouldQueueTaskForInit(String task) {
        return iZooto.mIzooToAppId == null && METHODS_ADD_IN_QUEUE_FOR.contains(task);
    }

    boolean shouldRunTaskThroughQueue() {
        // Don't schedule again a running pending task
        if (Thread.currentThread().getName().contains(OS_PENDING_EXECUTOR))
            return false;

        if (iZooto.isInitCompleted() && pendingTaskExecutor == null) {
            // There never were any waiting tasks
            return false;
        }

        // If init isn't finished and the pending executor hasn't been defined yet...
        if (!iZooto.isInitCompleted() && pendingTaskExecutor == null)
            return true;

        // or if the pending executor is alive and hasn't been shutdown yet...
        return !pendingTaskExecutor.isShutdown();
    }



    void addTaskToQueue(Runnable runnable) {
        addTaskToQueue(new PendingTaskRunnable(this, runnable));
    }

    private void addTaskToQueue(PendingTaskRunnable task) {
        task.taskId = lastTaskId.incrementAndGet();

        if (pendingTaskExecutor == null) {
            Log.d(AppConstant.APP_NAME_TAG, "Adding a task to the pending queue with ID:" + task.taskId);
            // The tasks haven't been executed yet...add them to the waiting queue
            taskQueueWaitingForInit.add(task);
        } else if (!pendingTaskExecutor.isShutdown()) {
            Log.d(AppConstant.APP_NAME_TAG, "Executor is still running, add to the executor with ID:" + task.taskId);
            try {
                // If the executor isn't done with tasks, submit the task to the executor
                pendingTaskExecutor.submit(task);
            } catch (RejectedExecutionException e) {
                Log.d(AppConstant.APP_NAME_TAG,"Executor is shutdown, running task manually with ID: " + task.taskId);
                task.run();
                e.printStackTrace();
            }
        }
    }

    /**
     * Called by iZooto.initHanlder() as last step on the initHandler
     * Run available pending tasks on an Executor
     */
    void startPendingTasks() {

        if (!taskQueueWaitingForInit.isEmpty()) {
            pendingTaskExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable runnable) {
                    Thread newThread = new Thread(runnable);
                    newThread.setName(OS_PENDING_EXECUTOR + newThread.getId());
                    return newThread;
                }
            });

            while (!taskQueueWaitingForInit.isEmpty()) {
                pendingTaskExecutor.submit(taskQueueWaitingForInit.poll());
            }
        }
    }

    private void onTaskRan(long taskId) {
        if (lastTaskId.get() == taskId) {
            Log.d(AppConstant.APP_NAME_TAG, "Last Pending Task has ran, shutting down");
            pendingTaskExecutor.shutdown();
        }
    }

    private static class PendingTaskRunnable implements Runnable {
        private OSTaskManager osTaskManager;
        private Runnable innerTask;

        private long taskId;

        PendingTaskRunnable(OSTaskManager controller, Runnable innerTask) {
            this.osTaskManager = controller;
            this.innerTask = innerTask;
        }

        @Override
        public void run() {
            innerTask.run();
            osTaskManager.onTaskRan(taskId);
        }

        @Override
        public String toString() {
            return "PendingTaskRunnable{" +
                    "innerTask=" + innerTask +
                    ", taskId=" + taskId +
                    '}';
        }
    }

}