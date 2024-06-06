package com.izooto;

import com.izooto.Payload;

public interface NotificationHelperListener {
    void onNotificationReceived(Payload payload);
    void onNotificationOpened(String data);

}
