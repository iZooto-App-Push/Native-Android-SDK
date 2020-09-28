package com.izooto.shortcutbadger;

public class ShortcutBadgerException extends Exception {
    public ShortcutBadgerException(String message) {
        super(message);
    }

    public ShortcutBadgerException(String message, Exception e) {
        super(message, e);
    }
}
