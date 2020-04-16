package com.izooto;

public interface NotificationHelperListener {
    void onNotificationReceived(Payload payload);
    void onNotificationOpened(String data);

}
