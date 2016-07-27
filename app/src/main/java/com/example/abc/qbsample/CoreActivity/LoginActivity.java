package com.example.abc.qbsample.CoreActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.AnimationUtils;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.MyKerningTextView;
import com.example.abc.qbsample.Utils.QBLogin;
import com.example.abc.qbsample.Utils.SharedPrefsHelper;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements QBVideoChatSignalingManagerListener, View.OnFocusChangeListener, View.OnTouchListener {
    QBChatService chatService;
    Button login, loginFB, loginInsta;
    MyKerningTextView sign_up;
    QBUser currentUser;
    QBLogin qbLogin;
    EditText login_username, login_password;
    View username_ul, password_ul;
    TextView username_err, password_err;
    CheckBox chk_remember;
    private static final String PREF_GCM_REG_ID = "registration_id";

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
        //
        subscription.setRegistrationID(registrationID);
        //
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login1);
        login = (Button) findViewById(R.id.login);
        loginFB = (Button) findViewById(R.id.loginFB);
        loginFB.setOnTouchListener(this);
        loginInsta = (Button) findViewById(R.id.loginInsta);
        loginInsta.setOnTouchListener(this);
        sign_up = (MyKerningTextView) findViewById(R.id.sign_up_txt);
        sign_up.setText(Html.fromHtml("<span style=letter-spacing:10px>DON'T HAVE AN ACCOUNT? <font color='#0071bb'>SIGN UP</font></span>"));
        String sign_up_txt = "DON'T HAVE AN ACCOUNT? SIGN UP";

        sign_up.setText(sign_up_txt);

        sign_up.setDifference(8);
        sign_up.setEndIndex(sign_up_txt.length());
        sign_up.setKerningFactor(5.5f);

        login_username = (EditText) findViewById(R.id.login_username);
        login_username.setOnFocusChangeListener(this);
        login_password = (EditText) findViewById(R.id.login_password);
        login_password.setOnFocusChangeListener(this);

        chk_remember = (CheckBox) findViewById(R.id.chk_remember);

        username_ul = findViewById(R.id.username_ul);
        password_ul = findViewById(R.id.password_ul);

        username_err = (TextView) findViewById(R.id.username_err);
        password_err = (TextView) findViewById(R.id.password_err);


        chatService = QBChatService.getInstance();
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(it);
            }
        });

        login.setOnTouchListener(this);
    }


    @Override
    public void signalingCreated(QBSignaling qbSignaling, boolean b) {

    }

    private void offFocus() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.login_username:
                if (hasFocus) {
                    username_err.setVisibility(View.GONE);
                    username_ul.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    username_ul.setBackgroundColor(getResources().getColor(R.color.text_color));
                    if (login_username.getText().toString().equals("")) {
                        username_err.setVisibility(View.VISIBLE);
                        username_ul.setBackgroundColor(Color.RED);
                        username_err.setText(getResources().getString(R.string.empty_username));
                    }
                }
                break;
            case R.id.login_password:
                if (hasFocus) {
                    password_err.setVisibility(View.GONE);
                    password_ul.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    password_ul.setBackgroundColor(getResources().getColor(R.color.text_color));
                    if (login_password.getText().toString().equals("")) {
                        password_err.setVisibility(View.VISIBLE);
                        password_ul.setBackgroundColor(Color.RED);
                        password_err.setText(getResources().getString(R.string.empty_password));
                    } else if (login_password.getText().toString().length() < 8) {
                        password_err.setVisibility(View.VISIBLE);
                        password_ul.setBackgroundColor(Color.RED);
                        password_err.setText(getResources().getString(R.string.invalid_password));
                    }
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {

            case R.id.loginFB:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        loginFB.startAnimation(AnimationUtils.getDecreaseAnimation(loginFB));
                        break;
                    case MotionEvent.ACTION_UP:
                        loginFB.startAnimation(AnimationUtils.getIncreaseAnimation(loginFB));
                        break;
                }
                break;

            case R.id.loginInsta:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        loginInsta.startAnimation(AnimationUtils.getDecreaseAnimation(loginInsta));
                        break;
                    case MotionEvent.ACTION_UP:
                        loginInsta.startAnimation(AnimationUtils.getIncreaseAnimation(loginInsta));
                        break;
                }
                break;

            case R.id.login:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        login.startAnimation(AnimationUtils.getDecreaseAnimation(login));
                        login.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC);
                        break;
                    case MotionEvent.ACTION_UP:
                        login.startAnimation(AnimationUtils.getIncreaseAnimation(login));
                        login.getBackground().setColorFilter(Color.parseColor("#0071bb"), PorterDuff.Mode.SRC);
                        final Dialog dialog = Helper.getHelperInstance().loaderDialog(LoginActivity.this);

                        if (!login_username.getText().toString().equals("") && !login_password.getText().toString().equals("") && login_password.getText().toString().length() >= 8) {
                            dialog.show();
                            qbLogin = new QBLogin(login_username.getText().toString(), login_password.getText().toString()) {
                                @Override
                                public void OnSessionCreated() {
                                    qbLogin.currentUser.setId(qbLogin.currentSession.getUserId());
                                    subscribeToPushNotifications(String.valueOf(SharedPrefsHelper.getInstance().get
                                            (PREF_GCM_REG_ID, "")));
                                }

                                @Override
                                public void OnLoginSuccessfull() {
                                    QBUser user = null;
                                    dialog.dismiss();
                                    try {
                                        user = QBUsers.getUserByLogin(login_username.getText().toString());
                                        if (chk_remember.isChecked()) {
                                            Helper.getHelperInstance().setCurrentUser(LoginActivity.this, currentUser);
                                        }

                                        System.out.println("________________Login user is : " + user + " \nname : " + user.getLogin() + " \npass : " + user.getPassword());
                                        Intent it = new Intent(LoginActivity.this, ContactListActivity.class);
                                        it.putExtra("CurrentUser", user);
                                        it.putExtra("ExtraMSG", "success");
                                        finish();
                                        startActivity(it);
                                    } catch (QBResponseException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void OnLoginError() {
                                    System.out.println("________chat service error is : " + qbLogin.error.getMessage());
                                    dialog.dismiss();
                                    password_err.setVisibility(View.VISIBLE);
                                    password_err.setText("username or password is incorrect");
                                }
                            };

                        } else {
                            if (login_username.getText().toString().equals("")) {
                                username_err.setVisibility(View.VISIBLE);
                                username_ul.setBackgroundColor(Color.RED);
                                username_err.setText(getResources().getString(R.string.empty_username));
                            } else if (login_password.getText().toString().equals("")) {
                                password_err.setVisibility(View.VISIBLE);
                                password_ul.setBackgroundColor(Color.RED);
                                password_err.setText(getResources().getString(R.string.empty_password));
                            } else if (login_password.getText().toString().length() < 8) {
                                password_err.setVisibility(View.VISIBLE);
                                password_ul.setBackgroundColor(Color.RED);
                                password_err.setText(getResources().getString(R.string.invalid_password));
                            }
                        }
                        break;
                }
                break;

        }
        return true;
    }
}
