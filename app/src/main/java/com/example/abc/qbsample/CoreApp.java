package com.example.abc.qbsample;

import android.app.Application;

import com.example.abc.qbsample.Utils.ActivityLifecycle;
import com.example.abc.qbsample.Utils.Helper;
import com.quickblox.core.QBSettings;


/**
 * Created by abc on 5/7/2016.
 */
public class CoreApp extends Application {
    private static CoreApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        ActivityLifecycle.init(this);
        initCredentials(Helper.APP_ID, Helper.AUTH_KEY, Helper.AUTH_SECRET, Helper.ACCOUNT_KEY);
    }

    public static synchronized CoreApp getInstance() {
        return instance;
    }

    public void initCredentials(String APP_ID, String AUTH_KEY, String AUTH_SECRET, String ACCOUNT_KEY) {
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
