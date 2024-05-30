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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class AdvertisingIdClient {
    private static final String TAG = AdvertisingIdClient.class.getSimpleName();
    public interface Listener {
        void onAdvertisingIdClientFinish(AdInfo adInfo);

        void onAdvertisingIdClientFail(Exception exception);
    }

    protected Listener mListener;
    protected Handler mHandler;

    public static synchronized void getAdvertisingId(Context context, Listener listener) {
        new AdvertisingIdClient().start(context, listener);
    }

    public static class AdInfo {
        private final String mAdvertisingId;
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

    protected void start(final Context context, final Listener listener) {
        if (listener == null) {
            Log.e(TAG, "getAdvertisingId - Error: null listener, dropping call");
            return;
        }

        mHandler = new Handler(Looper.getMainLooper());
        mListener = listener;

        if (context == null) {
            invokeFail(new Exception(TAG + " - Error: context null"));
            return;
        }

        new Thread(() -> getAdvertisingIdInfo(context)).start();
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
                    if (TextUtils.isEmpty(id)) {
                        Log.w(TAG, "getAdvertisingIdInfo - Error: ID Not available");
                        invokeFail(new Exception("Advertising ID extraction Error: ID Not available"));
                    } else {
                        invokeFinish(new AdInfo(id, adInterface.isLimitAdTrackingEnabled(true)));
                    }
                }
            } catch (Exception exception) {
                Log.w(TAG, "getAdvertisingIdInfo - Error: " + exception);
                invokeFail(exception);
            } finally {
                context.unbindService(connection);
            }
        } catch (Exception exception) {
            Log.w(TAG, "getAdvertisingIdInfo - Error: " + exception);
            invokeFail(exception);
        }
    }

    protected void invokeFinish(final AdInfo adInfo) {
        mHandler.post(() -> {
            if (mListener != null) {
                mListener.onAdvertisingIdClientFinish(adInfo);
            }
        });
    }

    protected void invokeFail(final Exception exception) {
        mHandler.post(() -> {
            if (mListener != null) {
                mListener.onAdvertisingIdClientFail(exception);
            }
        });
    }

    protected class AdvertisingConnection implements ServiceConnection {
        private IBinder binder;

        public void onServiceConnected(ComponentName name, IBinder service) {
            this.binder = service;
        }

        public void onServiceDisconnected(ComponentName name) {
            this.binder = null;
        }

        public IBinder getBinder() throws IllegalStateException {
            if (this.binder == null) {
                throw new IllegalStateException("Service is not connected");
            }
            return this.binder;
        }
    }

    protected class AdvertisingInterface implements IInterface {
        private final IBinder binder;

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
}