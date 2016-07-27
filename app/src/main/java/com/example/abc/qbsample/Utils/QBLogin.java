package com.example.abc.qbsample.Utils;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

/**
 * Created by abc on 5/24/2016.
 */
public abstract class QBLogin {
    public QBUser currentUser ;
    public QBSession currentSession;
    public QBResponseException error;
    public QBLogin(QBUser user){
        QBLogin.this.currentUser = user;
        QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                QBLogin.this.currentSession = qbSession;
                OnSessionCreated();
                QBChatService.getInstance().login(QBLogin.this.currentUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        OnLoginSuccessfull();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        QBLogin.this.error = e;
                        OnLoginError();
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                QBLogin.this.error = e;
                OnLoginError();
            }
        });
    }
    public QBLogin(String username,String password){
        System.out.println("___________THE USER NAME IS : "+username+" _______ & ________ The Password is : "+password);
        QBLogin.this.currentUser = new QBUser(username,password);
        QBAuth.createSession(username, password, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                QBLogin.this.currentSession = qbSession;
                OnSessionCreated();
                QBChatService.setDefaultConnectionTimeout(10000);
                QBChatService.setDefaultPacketReplyTimeout(10000);
                QBChatService.getInstance().login(QBLogin.this.currentUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        OnLoginSuccessfull();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        QBLogin.this.error = e;
                        OnLoginError();
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                QBLogin.this.error = e;
                OnLoginError();
            }
        });
    }
    public void OnSessionCreated(){
        System.out.println("Session is Created..................");
    };
    public abstract void OnLoginSuccessfull();
    public abstract void OnLoginError();
}
