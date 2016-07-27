package com.example.abc.qbsample.CoreActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.abc.qbsample.CoreFragments.AudioCallFragment;
import com.example.abc.qbsample.CoreFragments.IncomingCallFragment;
import com.example.abc.qbsample.CoreFragments.VideoViewFragment;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.ShareFile.SendAudioVideomsg;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.RingtonePlayer;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.squareup.picasso.Picasso;

import org.webrtc.PeerConnection;
import org.webrtc.VideoCapturerAndroid;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//import com.example.abc.qbsample.CoreFragments.EndCallFragment;

public class CallActivity extends AppCompatActivity implements   QBRTCClientSessionCallbacks,QBRTCSignalingCallback
                    , View.OnClickListener,QBVideoChatSignalingManagerListener
                    , QBRTCSessionConnectionCallbacks
{
    //QBRTCClient rtcClient;
    public QBRTCSession currentSession;
    private ImageView profile_img,remote_user_image;
    public Button startCall,hangUp,recieveCall,rec_audio,rec_video;
    public FragmentManager fm;
    FragmentTransaction ft;
    ProgressBar imageLoader,remoteLoader;
    public Map<String,String> userInfo = new HashMap();
    public String chatType ;
    public Boolean isCallReceived = false;
    public int opponentId ;

    public static final String TAG = CallActivity.class.getSimpleName();
    public VideoViewFragment getVideoViewFragment() {
        return videoViewFragment;
    }

    public void setVideoViewFragment(VideoViewFragment videoViewFragment) {
        this.videoViewFragment = videoViewFragment;
    }

    public AudioCallFragment getAudioCallFragment() {
        return audioCallFragment;
    }

    public void setAudioCallFragment(AudioCallFragment audioCallFragment) {
        this.audioCallFragment = audioCallFragment;
    }

    AudioCallFragment audioCallFragment;
    VideoViewFragment videoViewFragment = new VideoViewFragment();
    public RingtonePlayer ringtonePlayer;
    //private TextView fromUser;
    public QBRTCSession getCurrentSession(){
        Log.w(TAG, "getCurrentSession: Current Session is : __________________ "+currentSession );
        return currentSession;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(videoTracksCallbacks);
        }
    }
    public void removeVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks){
        currentSession.removeVideoTrackCallbacksListener(videoTracksCallbacks);
    }
    private void stopCallerRing(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });

    }

    @Override
    public void onClick(View v) {
        Intent it;
        switch (v.getId()){
            case R.id.rec_audio:
                it = new Intent(CallActivity.this, SendAudioVideomsg.class);
                startActivity(it);
                break;
            case R.id.rec_video:

                break;
            case R.id.hangUp:
                ringtonePlayer.stop();
                final Dialog hang_up_dialog = Helper.getHelperInstance().createDialog(CallActivity.this,true,Helper.hang_up_contentMsg);
                Button positiveBtn = (Button) hang_up_dialog.findViewById(R.id.positive_option);
                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hang_up_dialog.dismiss();
                        hangUp.setVisibility(View.GONE);
                        recieveCall.setVisibility(View.GONE);

                        currentSession.hangUp(userInfo);
                        // TODO current session is hanging up<><><><>
                        destroyCurrentSession();
                        Helper.isCallStarted = false;
                        finish();

                        /*EndCallFragment endCallFragment = new EndCallFragment();
                        ft = fm.beginTransaction();
                        ft.replace(R.id.videoViewFrame, endCallFragment);
                        ft.commit();*/
                    }
                });
                hang_up_dialog.show();
                break;
        }
    }

    @Override
    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
        if (!createdLocally) {
            // TODO adding QBVideoChatSignalingManagerListener
            QBRTCClient.getInstance(this).addSignaling((QBWebRTCSignaling) qbSignaling);
        }

    }

    private class LoadImage extends AsyncTask<InputStream,InputStream,Bitmap>{

        @Override
        protected Bitmap doInBackground(InputStream... params) {
            Bitmap image = BitmapFactory.decodeStream(params[0]);
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap s) {
            super.onPostExecute(s);
            profile_img.setVisibility(View.VISIBLE);
            imageLoader.setVisibility(View.GONE);
            profile_img.setPadding(Helper.dp_padding, Helper.dp_padding, Helper.dp_padding, Helper.dp_padding);
            profile_img.setImageBitmap(Helper.getRoundedCornerBitmap(s));
        }
    }
    List<Integer> opponents = new ArrayList<Integer>();
    private void createSessionInstance(){
        QBRTCSession newSessionWithOpponents;
        Log.e(TAG, "createSessionInstance: opponents list size : " + opponents.size() + " ______ Opponents" + opponents);
        if(chatType.equals(Helper.typeAudio)){
            newSessionWithOpponents= QBRTCClient.getInstance(this).createNewSessionWithOpponents(
                    opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);

        }else{
            newSessionWithOpponents = QBRTCClient.getInstance(this).createNewSessionWithOpponents(
                    opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);

        }

        initCurrentSession(newSessionWithOpponents);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy() called with: " + "");
        // TODO removing QBRTCClientSessionCallbacks
        QBRTCClient.getInstance(this).removeSessionsCallbacksListener(this);
        //rtcClient.destroy();
        opponents.clear();
        opponents = null;

        destroyCurrentSession();
        currentSession = null;

        // TODO removing QBVideoChatSignalingManagerListener
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().removeSignalingManagerListener(this);
    }
    public Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCallReceived = false;
        setContentView(R.layout.activity_video_chat);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        //ab.show();
        opponentId = getIntent().getExtras().getInt(Helper.opponentID);
        chatType = getIntent().getExtras().getString(Helper.chatType);

        startCall = (Button) findViewById(R.id.startCall);
        recieveCall = (Button) findViewById(R.id.recieveCall);
        hangUp = (Button) findViewById(R.id.hangUp);
        hangUp.setOnClickListener(CallActivity.this);
        profile_img = (ImageView) findViewById(R.id.profile_Img);
        remote_user_image = (ImageView) findViewById(R.id.remote_profile_Img);

        imageLoader = (ProgressBar) findViewById(R.id.ImageLoader);
        remoteLoader = (ProgressBar) findViewById(R.id.remoteImageLoader);

        /*QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                for (QBUser user :
                        users) {
                    Log.e(TAG,"user ids : " + user.getId() + " file id : " + user.getFileId());
                }
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
            }
        });*/

        QBUser current_user =(QBUser) getIntent().getSerializableExtra(Helper.currentUserStr);
        Log.e(TAG, "Current user is : " + current_user + " __________current user file id is : " + current_user.getFileId());
        if(current_user.getFileId()!=null){
            imageLoader.setVisibility(View.VISIBLE);
            profile_img.setVisibility(View.GONE);
            QBContent.downloadFileById(current_user.getFileId(), new QBEntityCallback<InputStream>() {
                @Override
                public void onSuccess(InputStream inputStream, Bundle bundle) {
                    new LoadImage().execute(inputStream);
                }

                @Override
                public void onError(QBResponseException e) {
                    profile_img.setVisibility(View.VISIBLE);
                    imageLoader.setVisibility(View.GONE);
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int i) {

                }
            });
        }

        QBUsers.getUser(opponentId, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                UserCustomData userCustomData = Helper.getCustomObject(user.getCustomData());
                if (userCustomData != null) {
                    Log.e(TAG, "________opponent user picture is : " + userCustomData.getProfile_picture_url());
                    Picasso.with(CallActivity.this)
                            .load(userCustomData.getProfile_picture_url())
                            .into(remote_user_image);
                }
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
        rec_audio = (Button) findViewById(R.id.rec_audio);
        rec_audio.setOnClickListener(this);
        rec_video = (Button) findViewById(R.id.rec_video);
        rec_video.setOnClickListener(this);
        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        //fromUser = (TextView) findViewById(R.id.chat_user_name);

        //rtcClient = QBRTCClient.getInstance(CallActivity.this);
        QBRTCClient.getInstance(this).addSessionCallbacksListener(this);
        QBRTCClient.getInstance(this).prepareToProcessCalls();
        QBChatService.getInstance().setReconnectionAllowed(true);
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(this);
        QBRTCClient.getInstance(this).setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                CallActivity.this.runOnUiThread(new Runnable() {
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
        List<PeerConnection.IceServer> iceServerList = new LinkedList<>();
        iceServerList.add(new PeerConnection.IceServer("turn:numb.default.com", "default@default.com", "default@default.com"));
        iceServerList.add(new PeerConnection.IceServer("turn:numb.default.com : 1234?transport=udp", "default@default.com", "petrbubnov@default.com"));
        iceServerList.add(new PeerConnection.IceServer("turn:numb.default.com : 1234?transport=tcp", "default@default.com", "default@default.com"));
        QBRTCConfig.setIceServerList(iceServerList);

        // TODO adding QBRTCClientSessionCallbacks


        fm = getSupportFragmentManager();

        Log.e(TAG, "onCreate: The Size of Opponent list is : _______ : " + opponents.size());
        opponents.clear();
        opponents.add(opponentId);
        System.out.println("__________________THE LENGTH OF Opponent list is : " + opponents.size());
        //addVideoTrackCallbacksListener(videoViewFragment);

        createSessionInstance();
        if(Helper.receivedSession!=null){
            // TODO removing QBRTCSessionConnectionCallbacks
            Helper.receivedSession.removeSessionCallbacksListener(CallActivity.this);
            // TODO removing QBRTCSignalingCallback
            Helper.receivedSession.removeSignalingCallback(CallActivity.this);
            initCurrentSession(Helper.receivedSession);
            Helper.receivedSession = null;
            ringtonePlayer.play(true);
            /*recieveCall.setVisibility(View.VISIBLE);
            hangUp.setVisibility(View.VISIBLE);
            recieveCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ringtonePlayer.stop();
                    switch (chatType) {
                        case Helper.typeAudio:
                            //receiveCall();
                            switchToAcceptAudioCall();
                            break;
                        case Helper.typeVideo:
                            //receiveCall();
                            switchToAcceptVideoCall();
                            break;
                    }

                }
            });*/
            IncomingCallFragment incomingCallFragment = new IncomingCallFragment();
            fm.beginTransaction().add(android.R.id.content,incomingCallFragment,Helper.IncomingCallFragment).commit();

        }
        startCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (chatType){
                    case Helper.typeAudio:
                        //startCall();
                        hangUp.setVisibility(View.VISIBLE);
                        switchToStartAudioCall();
                        break;
                    case Helper.typeVideo:
                        //startCall();
                        hangUp.setVisibility(View.VISIBLE);
                        switchToStartVideoCall();
                        break;
                }
            }
        });

    }
    public void initCurrentSession(QBRTCSession sesion) {
        this.currentSession = sesion;
        // TODO adding QBRTCSessionConnectionCallbacks
        this.currentSession.addSessionCallbacksListener(CallActivity.this);
        // TODO adding QBRTCSignalingCallback
        this.currentSession.addSignalingCallback(CallActivity.this);
    }

    public void destroyCurrentSession(){
        // TODO removing QBRTCSessionConnectionCallbacks
        this.currentSession.removeSessionCallbacksListener(CallActivity.this);
        // TODO removing QBRTCSignalingCallback
        this.currentSession.removeSignalingCallback(CallActivity.this);
    }

    /**
     * This method is use to start Audio call
     */
    public void switchToStartAudioCall(){
        audioCallFragment = new AudioCallFragment();
        Bundle b = new Bundle();
        b.putString(Helper.CallStatusKey, Helper.StartCall);
        audioCallFragment.setArguments(b);
        setAudioCallFragment(audioCallFragment);
        ft = fm.beginTransaction();
        ft.replace(android.R.id.content, audioCallFragment);
        ft.commit();
    }

    /**
     * This method is use to start video call
     */
    public void switchToStartVideoCall(){

        Bundle b = new Bundle();
        b.putString(Helper.CallStatusKey, Helper.StartCall);
        videoViewFragment.setArguments(b);
        setVideoViewFragment(videoViewFragment);
        ft = fm.beginTransaction();
        ft.replace(R.id.videoViewFrame, videoViewFragment);
        ft.commit();
    }

    /**
     * This method is use to accept Audio call
     */
    public void switchToAcceptAudioCall(){
        audioCallFragment = new AudioCallFragment();
        recieveCall.setVisibility(View.GONE);
        Bundle b = new Bundle();
        b.putString(Helper.CallStatusKey, Helper.AcceptCall);
        audioCallFragment.setArguments(b);
        ft = fm.beginTransaction();
        ft.replace(android.R.id.content, audioCallFragment);
        ft.commit();
    }

    /**
     * This method is use to accept Video call
     */
    public void switchToAcceptVideoCall(){

        recieveCall.setVisibility(View.GONE);
        Bundle b = new Bundle();
        b.putString(Helper.CallStatusKey, Helper.AcceptCall);
        videoViewFragment.setArguments(b);
        ft = fm.beginTransaction();
        ft.replace(R.id.videoViewFrame, videoViewFragment);
        ft.commit();
    }

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        Log.w(TAG, "onReceiveNewSession() called with: " + "qbrtcSession = [" + qbrtcSession + "]");
        isCallReceived = true;
        this.currentSession = qbrtcSession;
        this.userInfo = qbrtcSession.getUserInfo();
        //initCurrentSession(currentSession);
        /**/

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recieveCall.setVisibility(View.VISIBLE);
                ringtonePlayer.play(true);
                hangUp.setVisibility(View.VISIBLE);
                recieveCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ringtonePlayer.stop();
                        switch (chatType) {
                            case Helper.typeAudio:
                                //receiveCall();
                                switchToAcceptAudioCall();
                                break;
                            case Helper.typeVideo:
                                //receiveCall();
                                switchToAcceptVideoCall();
                                break;
                        }

                    }
                });
            }
        });

    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        stopCallerRing();
    }

    @Override
    public void onCallRejectByUser(final QBRTCSession qbrtcSession, Integer integer, final Map<String, String> map) {
        Log.e(TAG, "_________________Call is rejected by user");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
                /*currentSession.onChannelConnectionClosed();*/
                currentSession.hangUp(new HashMap<String, String>());
                // TODO currents session is hanging up<><><><>
                hangUp.setVisibility(View.GONE);
                recieveCall.setVisibility(View.GONE);
                destroyCurrentSession();
                Helper.isCallStarted = false;
                finish();
            }
        });
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if(currentSession.equals(qbrtcSession)){
            Log.e(TAG,"_________________ onCallAcceptByUser");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hangUp.setVisibility(View.VISIBLE);
                    ringtonePlayer.stop();
                }
            });

        }
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession qbrtcSession, Integer integer, final Map<String, String> map) {
        if(currentSession.equals(qbrtcSession)){
            Log.e(TAG,"_________________on Receive Hang Up From User");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO current session is hanging up<><><><>
                    currentSession.hangUp(new HashMap<String, String>());

                    hangUp.setVisibility(View.GONE);
                    recieveCall.setVisibility(View.GONE);
                    destroyCurrentSession();
                    Helper.isCallStarted = false;
                    finish();
/*                    EndCallFragment endCallFragment = new EndCallFragment();
                    ft = fm.beginTransaction();
                    ft.replace(R.id.videoViewFrame, endCallFragment);
                    // TODO ref http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
                    ft.commit();*/
                }
            });
        }
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.e(TAG, "_________________on User No Actions");
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.e(TAG, "_________________ on Session Closed");
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.e(TAG, "_________________ on Session Start Close");
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
        Log.e(TAG, "_________________ on Success Sending Packet");
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {
        Log.e(TAG,"_________________ on Error Sending Packet");
        e.printStackTrace();
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.w(TAG, "onStartConnectToUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.w(TAG, "onConnectedToUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.w(TAG, "onConnectionClosedForUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.w(TAG, "onDisconnectedFromUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.w(TAG, "onDisconnectedTimeoutFromUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.w(TAG, "onConnectionFailedWithUser() called with: " + "qbrtcSession = [" + qbrtcSession + "], integer = [" + integer + "]");
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        Log.w(TAG, "onError() called with: " + "qbrtcSession = [" + qbrtcSession + "], e = [" + e + "]");
    }

}
