package com.example.abc.qbsample.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.SplashActivity;
import com.example.abc.qbsample.Utils.NotificationUtils;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by abc on 5/7/2016.
 */
public class GcmPushListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(Helper.EXTRA_GCM_MESSAGE);
        System.out.println("___________________from = [" + from + "], data = [" + data + "]");
        /*if (ActivityLifecycle.getInstance().isBackground()) {

        }*/
        showNotification(message);

        sendPushMessageBroadcast(message);
    }
    private static final int NOTIFICATION_ID = 1;

    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                "QBSampleNotification", message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }

    protected void sendPushMessageBroadcast(String message) {
        Intent gcmBroadcastIntent = new Intent(Helper.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(Helper.EXTRA_GCM_MESSAGE, message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }
}
