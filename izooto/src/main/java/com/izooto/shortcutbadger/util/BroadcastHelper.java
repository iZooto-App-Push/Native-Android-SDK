package com.izooto.shortcutbadger.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.izooto.shortcutbadger.ShortcutBadgerException;
import com.izooto.shortcutbadger.impl.IntentConstants;

import java.util.Collections;
import java.util.List;

public class BroadcastHelper {

    public static boolean canResolveBroadcast(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
        return receivers != null && receivers.size() > 0;
    }
    public static List<ResolveInfo> resolveBroadcast(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);

        return receivers != null ? receivers : Collections.<ResolveInfo>emptyList();
    }

    public static void sendIntentExplicitly(Context context, Intent intent) throws ShortcutBadgerException {
        List<ResolveInfo> resolveInfos = resolveBroadcast(context, intent);

        if (resolveInfos.size() == 0) {
            throw new ShortcutBadgerException("unable to resolve intent: " + intent.toString());
        }

        for (ResolveInfo info : resolveInfos) {
            Intent actualIntent = new Intent(intent);

            if (info != null) {
                actualIntent.setPackage(info.resolvePackageName);
                context.sendBroadcast(actualIntent);
            }
        }
    }

    public static void sendDefaultIntentExplicitly(Context context, Intent intent) throws ShortcutBadgerException {
        boolean oreoIntentSuccess = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent oreoIntent = new Intent(intent);

            oreoIntent.setAction(IntentConstants.DEFAULT_OREO_INTENT_ACTION);

            try {
                sendIntentExplicitly(context, oreoIntent);
                oreoIntentSuccess = true;
            } catch (ShortcutBadgerException e) {
                oreoIntentSuccess = false;
            }
        }

        if (oreoIntentSuccess) {
            return;
        }

        // try pre-Oreo default intent
        sendIntentExplicitly(context, intent);
    }

}
