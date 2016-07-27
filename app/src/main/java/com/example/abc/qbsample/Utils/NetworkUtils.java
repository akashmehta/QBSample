package com.example.abc.qbsample.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by abc on 5/24/2016.
 */
public class NetworkUtils {
    public static final int NOT_CONNECTED = 0;
    public static final int WIFI = 1;
    public static final int MOBILE = 2;

    /**
     * The Reference is http://stackoverflow.com/questions/35355715/android-run-in-background-whenever-internet-connected
     * @param mContext
     * @return
     */
    public static int getConnectionStatus (Context mContext){
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null){
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                return WIFI;
            }   else{
                return MOBILE;
            }
        }
        return NOT_CONNECTED;
    }
}


