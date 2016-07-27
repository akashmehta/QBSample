package com.example.abc.qbsample.CoreFragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.abc.qbsample.CoreActivity.CallActivity;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.RTCGLVideoView;

import org.webrtc.VideoRenderer;

import java.util.List;
import java.util.Map;

/**
 * Created by abc on 5/4/2016.
 */
public class VideoViewFragment extends Fragment implements QBRTCClientVideoTracksCallbacks, View.OnClickListener {
    private RTCGLVideoView localVideoView, remoteView;
    ImageView manageAudio, switchCamera, audio_chat;
    CallActivity currentActivity;
    String callStatus;
    private RelativeLayout rlTitleLayout;
    private TextView opponentName;
    private static final String TAG = VideoViewFragment.class.getSimpleName();
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.currentActivity = (CallActivity)activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("#####____________  is Call started is called");
        currentActivity.removeVideoTrackCallbacksListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_chat, null);
        rlTitleLayout = (RelativeLayout) rootView.findViewById(R.id.video_chat_title_layout);
        opponentName = (TextView) rootView.findViewById(R.id.opponentName);

        rlTitleLayout.getBackground().setAlpha(80);
        localVideoView = (RTCGLVideoView) rootView.findViewById(R.id.localView);
        localVideoView.setVisibility(View.VISIBLE);

        remoteView = (RTCGLVideoView) rootView.findViewById(R.id.remoteView);
        remoteView.setVisibility(View.VISIBLE);

        manageAudio = (ImageView) rootView.findViewById(R.id.mice);
        manageAudio.setOnClickListener(this);
        switchCamera = (ImageView) rootView.findViewById(R.id.camera);
        switchCamera.setOnClickListener(this);
        audio_chat = (ImageView) rootView.findViewById(R.id.audio_chat);
        audio_chat.setOnClickListener(this);
        //currentActivity.getCurrentSession().getMediaStreamManager().setVideoEnabled(true);
        currentActivity.addVideoTrackCallbacksListener(this);
        //Log.w(TAG, "onCreateView: _________________________ is call started flag is : "+Helper.isCallStarted );
        if(!Helper.isCallStarted){
            Helper.isCallStarted = true;
            callStatus = getArguments().getString(Helper.CallStatusKey);
            if (callStatus.equals(Helper.StartCall)) {
                Map<String,String> userInfo = currentActivity.getCurrentSession().getUserInfo();
                userInfo.put(Helper.chatType,Helper.typeVideo);
                currentActivity.getCurrentSession().startCall(userInfo);
            } else {
                currentActivity.getCurrentSession().acceptCall(currentActivity.getCurrentSession().getUserInfo());
            }
        }
        List<Integer> opponentList = currentActivity.getCurrentSession().getOpponents();
        Integer caller_id = currentActivity.getCurrentSession().getCallerID();

        if(currentActivity.isCallReceived){
            QBUsers.getUser(caller_id, new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle bundle) {
                    opponentName.setVisibility(View.VISIBLE);
                    opponentName.setText("Call from : " + user.getLogin());
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
            Log.e(TAG, " ##_______________##  The User is : " + caller_id);
            for (Integer opponent : opponentList) {
                Log.e(TAG," ##_______________## opponent : " + opponent);
            }
        }else{
            for (Integer opponent : opponentList){
                Log.e(TAG," ##_______________## opponent : "+opponent);
                QBUsers.getUser(opponent, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle bundle) {
                        Log.w(TAG, "onSuccess() called with: " + "user = [" + user + "], bundle = [" + bundle + "]");
                        opponentName.setVisibility(View.VISIBLE);
                        opponentName.setText("Calling to : " + user.getLogin());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mice:

                if (currentActivity.getCurrentSession().getMediaStreamManager().isAudioEnabled()) {
                    System.out.println("-------------------disabling audio");
                    currentActivity.getCurrentSession().getMediaStreamManager().setAudioEnabled(false);
                    manageAudio.setImageResource(R.drawable.ic_mic_off);
                } else {
                    System.out.println("--------------------enabling audio");
                    currentActivity.getCurrentSession().getMediaStreamManager().setAudioEnabled(true);
                    manageAudio.setImageResource(R.drawable.ic_mic);
                }
                break;

            case R.id.camera:
                // TODO hear null pointer exception occured
                if (Helper.isCameraFront(currentActivity.getCurrentSession().getMediaStreamManager().getCurrentCameraId())) {
                    System.out.println("current camera mode is :::::::::::::::::::::: Camera Front");
                    switchCamera.setImageResource(R.drawable.ic_camera_rear);
                } else {
                    System.out.println("current camera mode is :::::::::::::::::::::: Camera Back");
                    switchCamera.setImageResource(R.drawable.ic_camera_front);
                }

                currentActivity.getCurrentSession().getMediaStreamManager().switchCameraInput(new Runnable() {
                    @Override
                    public void run() {}
                });
                break;

            case R.id.audio_chat:
                currentActivity.getCurrentSession().getMediaStreamManager().setVideoEnabled(false);
                if(callStatus.equals(Helper.StartCall)){
                    currentActivity.switchToStartAudioCall();
                }else{
                    currentActivity.switchToAcceptAudioCall();
                }
                break;
        }
    }

    private void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(remoteRenderer ?
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.MAIN) :
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.SECOND)));
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {
        fillVideoView(localVideoView, qbrtcVideoTrack, true);
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        fillVideoView(remoteView, qbrtcVideoTrack, true);
    }
}
