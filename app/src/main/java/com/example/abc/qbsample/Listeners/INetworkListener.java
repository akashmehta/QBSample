package com.example.abc.qbsample.Listeners;

import android.content.BroadcastReceiver;

/**
 * Created by abc on 5/24/2016.
 */
public interface INetworkListener {
    /**
     * Provides Broadcast receiver to check internet connectivity on run time
     * @return
     */
    public BroadcastReceiver getReceiver();

    /**
     * registers Broadcast receiver
     */
    public void registerReceiver();

    /**
     * unRegisters Broadcast receiver
     */
    public void unregisterReceiver();

    /**
     * Performs operation on Internet Connected
     */
    public void onInternetConnected();

    /**
     * Performs operation on Internet Disconnected
     */
    public void onInternetDisconnected();
}
