package com.izooto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class AdvertisingIdClient {

    private static final String TAG = AdvertisingIdClient.class.getSimpleName();

    //==============================================================================================
    // LISTENER
    //==============================================================================================
    public interface Listener {

        /**
         * Called when process completed using UI Thread
         *
         * @param adInfo <code>AdInfo</code> object
         */
        void onAdvertisingIdClientFinish(AdInfo adInfo);

        /**
         * Called when getting advertising id fails using UI Thread
         *
         * @param exception exception with extended message of the error.
         */
        void onAdvertisingIdClientFail(Exception exception);
    }

    protected Listener mListener;
    protected Handler mHandler;
    //==============================================================================================
    // Public methods
    //==============================================================================================

    /**
     * Method to invoke the process of getting advertisingid, using UIThread
     *
     * @param context  a valid context
     * @param listener valid Listener for callbacks
     */
    public static synchronized void getAdvertisingId(Context context, Listener listener) {

        new AdvertisingIdClient().start(context, listener);
    }

    /**
     * Ad Info data class with the results
     */
    public static class AdInfo {

        private final String  mAdvertisingId;
        private final boolean mLimitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {

            mAdvertisingId = advertisingId;
            mLimitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {

            return mAdvertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {

            return mLimitAdTrackingEnabled;
        }
    }
    //==============================================================================================
    // Inner classes
    //==============================================================================================

    /**
     * Advertising Service Connection
     */
    protected class AdvertisingConnection implements ServiceConnection {

        boolean retrieved = false;
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);

        public void onServiceConnected(ComponentName name, IBinder service) {

            try {
                this.queue.put(service);
            } catch (InterruptedException localInterruptedException) {}
        }

        public void onServiceDisconnected(ComponentName name) {}

        public IBinder getBinder() throws InterruptedException {

            if (this.retrieved) { throw new IllegalStateException(); }
            this.retrieved = true;
            return (IBinder) this.queue.take();
        }
    }

    /**
     * Advertising IInterface to get the ID
     */
    protected class AdvertisingInterface implements IInterface {

        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {

            binder = pBinder;
        }

        public IBinder asBinder() {

            return binder;
        }

        public String getId() throws RemoteException {

            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean) throws RemoteException {

            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }
    }

    //==============================================================================================
    // Inner methods
    //==============================================================================================
    protected void start(final Context context, final Listener listener) {

        if (listener == null) {
            Log.e(AppConstant.APP_NAME_TAG, "getAdvertisingId - Error: null listener, dropping call");
        } else {
            mHandler = new Handler(Looper.getMainLooper());
            mListener = listener;
            if (context == null) {
                invokeFail(new Exception(TAG + " - Error: context null"));
            } else {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        getAdvertisingIdInfo(context);
                    }
                }).start();
            }
        }
    }

    private void getAdvertisingIdInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.android.vending", 0);
            Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
            intent.setPackage("com.google.android.gms");
            AdvertisingConnection connection = new AdvertisingConnection();
            try {
                if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                    AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());
                    String id = adInterface.getId();
                    if(TextUtils.isEmpty(id)) {
                        Log.w(AppConstant.APP_NAME_TAG, "getAdvertisingIdInfo - Error: ID Not available");
                        invokeFail(new Exception("Advertising ID extraction Error: ID Not available"));
                    } else {
                        invokeFinish(new AdInfo(id, adInterface.isLimitAdTrackingEnabled(true)));
                    }
                }
            } catch (Exception exception) {
                Log.w(AppConstant.APP_NAME_TAG, "getAdvertisingIdInfo - Error: " + exception);
                invokeFail(exception);
            } finally {
                context.unbindService(connection);
            }
        } catch (Exception exception) {
            Log.w(AppConstant.APP_NAME_TAG, "getAdvertisingIdInfo - Error: " + exception);
            invokeFail(exception);
        }
    }

    //==============================================================================================
    // Listener helpers
    //==============================================================================================
    protected void invokeFinish(final AdInfo adInfo) {

        Log.v(AppConstant.APP_NAME_TAG, "invokeFinish");
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onAdvertisingIdClientFinish(adInfo);
                }
            }
        });
    }

    protected void invokeFail(final Exception exception) {

        Log.v(AppConstant.APP_NAME_TAG, "invokeFail: " + exception);
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onAdvertisingIdClientFail(exception);
                }
            }
        });
    }

}
