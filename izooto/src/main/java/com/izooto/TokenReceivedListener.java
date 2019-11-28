package com.izooto;


public interface TokenReceivedListener {

    /**
     * This method will get invoked when device token will be received
     *
     * @param token device token
     */
    void onTokenReceived(String token);
    void onUpdatedToken(String token);

}
