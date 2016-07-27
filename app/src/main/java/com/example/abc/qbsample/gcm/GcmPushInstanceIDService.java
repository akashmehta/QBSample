package com.example.abc.qbsample.gcm;

import android.util.Log;

import com.example.abc.qbsample.Utils.Helper;
import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by abc on 5/7/2016.
 */
public class GcmPushInstanceIDService extends InstanceIDListenerService {
    public static final String TAG = GcmPushInstanceIDService.class.getSimpleName();
    @Override
    public void onTokenRefresh() {
        Log.e(TAG, "onTokenRefresh: In GCM PUSH INSTANCE ID SERVICE");
        GooglePlayServicesHelper playServicesHelper = new GooglePlayServicesHelper();
        if (playServicesHelper.checkPlayServicesAvailable()) {
            playServicesHelper.registerForGcm(getSenderId());
        }
    }

    protected String getSenderId(){
        return Helper.SENDER_ID;
    }
}
