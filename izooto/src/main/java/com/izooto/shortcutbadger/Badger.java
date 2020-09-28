package com.izooto.shortcutbadger;

import android.content.ComponentName;
import android.content.Context;

import java.util.List;

public interface Badger {
    void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgerException;

    /**
     * Called to let {@link ShortcutBadger} knows which launchers are supported by this badger. It should return a
     * @return List containing supported launchers package names
     */
    List<String> getSupportLaunchers();
}
