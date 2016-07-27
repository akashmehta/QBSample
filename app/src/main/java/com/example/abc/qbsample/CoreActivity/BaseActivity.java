package com.example.abc.qbsample.CoreActivity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.abc.qbsample.Utils.Helper;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import org.webrtc.VideoCapturerAndroid;

import java.util.Map;

/**
 * Created by abc on 6/7/2016.
 */
public abstract class BaseActivity extends AppCompatActivity
                            implements QBRTCClientSessionCallbacks, QBVideoChatSignalingManagerListener {
    Activity currentActivity = getActivity();
    public static final String TAG = BaseActivity.class.getSimpleName();

    void initConfig(){

        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(this);
        QBChatService.getInstance().setReconnectionAllowed(true);
        QBRTCClient.getInstance(this).setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                BaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Error on Camera.... and The error is : " + s);
                    }
                });
            }
        });
        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setDebugEnabled(true);
    }
    private void removeConfig(){
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().removeSignalingManagerListener(this);
    }
    void initSessionCallback(){
        Log.d(TAG, "initSessionCallback() called with: " + "_______");
        QBRTCClient.getInstance(currentActivity).addSessionCallbacksListener(this);
        QBRTCClient.getInstance(currentActivity).prepareToProcessCalls();
    }
    private void removeSessionCallback(){
        Log.d(TAG, "removeSessionCallback() called with: " + "");
        QBRTCClient.getInstance(currentActivity).removeSessionsCallbacksListener(this);
    }
    abstract Activity getActivity();
    abstract QBUser currentUser();

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        Log.e(TAG, "onReceiveNewSession:  _______________________________ received new  session _______________" );
        Map<String,String> userInfo = qbrtcSession.getUserInfo();
        Intent it = new Intent(currentActivity,CallActivity.class);
        it.putExtra(Helper.chatType,userInfo.get(Helper.chatType));
        it.putExtra(Helper.opponentID,qbrtcSession.getCallerID());
        it.putExtra(Helper.currentUserStr,currentUser());
        Helper.receivedSession = qbrtcSession;
        removeConfig();
        removeSessionCallback();
        startActivity(it);
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onUserNotAnswer() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onCallRejectByUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "], map = [" + map + "]");
        qbrtcSession.rejectCall(map);

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onCallAcceptByUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "], map = [" + map + "]");
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onReceiveHangUpFromUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "], map = [" + map + "]");
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onUserNoActions() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionClosed() called with: " + "qbrtcSession = [" + qbrtcSession + "]");
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionStartClose() called with: " + "qbrtcSession = [" + qbrtcSession + "]");
    }

    @Override
    public void signalingCreated(QBSignaling qbSignaling, boolean b) {
        QBRTCClient.getInstance(this).addSignaling((QBWebRTCSignaling) qbSignaling);
    }
}
