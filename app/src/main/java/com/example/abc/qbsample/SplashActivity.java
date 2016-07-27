package com.example.abc.qbsample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;

import com.example.abc.qbsample.CoreActivity.ContactListActivity;
import com.example.abc.qbsample.CoreActivity.LoginActivity;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.QBLogin;
import com.example.abc.qbsample.Utils.SharedPrefsHelper;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    Intent it;
    QBLogin qbLogin;
    private static final String PREF_GCM_REG_ID = "registration_id";

    public static final String TAG = SplashActivity.class.getSimpleName();
    /*GooglePlayServicesHelper googlePlayServicesHelper;*/
    public void subscribeToPushNotifications(String registrationID) {
        QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
        subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
        //
        String deviceId;
        final TelephonyManager mTelephony = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId(); //*** use for mobiles
        } else {
            deviceId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID); //*** use for tablets
        }
        subscription.setDeviceUdid(deviceId);
        subscription.setRegistrationID(registrationID);

        QBPushNotifications.createSubscription(subscription, new QBEntityCallback<ArrayList<QBSubscription>>() {

            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                System.out.println("_______________________Subscriptionn is Done successfully");

            }

            @Override
            public void onError(QBResponseException error) {
                Log.e("GCMSubscriptionError", "error subscription error occured");
                error.printStackTrace();
            }
        });
    }

/*    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Helper.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Receiving event " + Helper.ACTION_NEW_GCM_EVENT + " with data: " + message);
        }
    };
    private void registerReceiver() {
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(Helper.ACTION_NEW_GCM_EVENT));
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);
        QBSettings.getInstance().init(getApplicationContext(), Helper.APP_ID, Helper.AUTH_KEY, Helper.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Helper.ACCOUNT_KEY);
        QBChatService.setDebugEnabled(true);

        /*googlePlayServicesHelper = new GooglePlayServicesHelper();
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            googlePlayServicesHelper.registerForGcm(Helper.SENDER_ID);
        }

        registerReceiver();*/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                final QBUser user = Helper.getHelperInstance().getCurrentUser(SplashActivity.this);
                if (user != null) {
                    qbLogin = new QBLogin(user) {
                        @Override
                        public void OnSessionCreated() {
                            subscribeToPushNotifications(String.valueOf(SharedPrefsHelper.getInstance().get
                                    (PREF_GCM_REG_ID, "")));
                        }

                        @Override
                        public void OnLoginSuccessfull() {
                            System.out.println("____________ Login success full ......... ");
                            it = new Intent(SplashActivity.this, ContactListActivity.class);
                            it.putExtra("CurrentUser", Helper.getHelperInstance().getCurrentUser(SplashActivity.this));
                            it.putExtra("ExtraMSG","success");
                            finish();
                            startActivity(it);
                        }

                        @Override
                        public void OnLoginError() {
                            qbLogin.error.printStackTrace();
                            it = new Intent(SplashActivity.this, ContactListActivity.class);
                            it.putExtra("CurrentUser", Helper.getHelperInstance().getCurrentUser(SplashActivity.this));
                            it.putExtra("ExtraMSG","error");
                            finish();
                            startActivity(it);
                        }
                    };
                } else {
                    it = new Intent(SplashActivity.this, LoginActivity.class);
                    finish();
                    startActivity(it);
                }

            }
        }, 3000);
    }
}
