package com.example.abc.qbsample.Utils;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.example.abc.qbsample.CoreApp;

public class DeviceUtils {

    public static String getDeviceUid() {
        Context context = CoreApp.getInstance();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String uniqueDeviceId;
        if (telephonyManager.getDeviceId() != null) {
            uniqueDeviceId = telephonyManager.getDeviceId(); //*** use for mobiles
        } else {
            uniqueDeviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID); //*** use for tablets
        }
        return uniqueDeviceId;
    }

}
