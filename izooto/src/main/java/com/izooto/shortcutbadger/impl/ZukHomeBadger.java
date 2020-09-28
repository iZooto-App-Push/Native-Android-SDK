package com.izooto.shortcutbadger.impl;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.izooto.shortcutbadger.Badger;
import com.izooto.shortcutbadger.ShortcutBadgerException;

import java.util.Collections;
import java.util.List;

public class ZukHomeBadger implements Badger {

    private final Uri CONTENT_URI = Uri.parse("content://com.android.badge/badge");

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgerException {
        Bundle extra = new Bundle();
        extra.putInt("app_badge_count", badgeCount);
        context.getContentResolver().call(CONTENT_URI, "setAppBadgeCount", null, extra);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList("com.zui.launcher");
    }
}
