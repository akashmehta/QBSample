package com.example.abc.qbsample.CoreFragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.abc.qbsample.CoreActivity.CallActivity;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.skyfishjy.library.RippleBackground;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by abc on 6/13/2016.
 */
public class AudioCallFragment extends Fragment implements View.OnClickListener , AppRTCAudioManager.OnAudioManagerStateListener{
    CallActivity currentActivity;
    int opponentId;
    ImageView callerImage, text_chat, video_call, call_end, audio_call, speaker;
    ProgressBar callerImage_loader;
    String chatType;
    TextView time_text;
    AppRTCAudioManager audioManager;
    private String TAG = AudioCallFragment.class.getSimpleName();
    String callStatus;
    private TextView username;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.currentActivity = (CallActivity) activity;
    }
    private AudioStreamReceiver audioStreamReceiver;
    IntentFilter intentFilter;
    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
    }
    @Override
    public void onStart() {
        super.onStart();
        currentActivity.registerReceiver(audioStreamReceiver, intentFilter);
        Log.w(TAG, "onCreateView: _________________________ is call started flag is : " + Helper.isCallStarted);
        if (!Helper.isCallStarted) {

            Helper.isCallStarted = true;
            if (callStatus.equals(Helper.StartCall)) {
                Map<String,String> userInfo = currentActivity.getCurrentSession().getUserInfo();
                userInfo.put(Helper.chatType,Helper.typeAudio);
                currentActivity.getCurrentSession().startCall(userInfo);
            } else {
                currentActivity.getCurrentSession().acceptCall(currentActivity.getCurrentSession().getUserInfo());
            }
        }
        Log.w(TAG, "onStart() called with: " + "");
    }

    @Override
    public void onStop() {
        super.onStop();
        currentActivity.unregisterReceiver(audioStreamReceiver);
        Log.w(TAG, "onStop() called with: " + "");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_chat:
                break;
            case R.id.video_call:
                currentActivity.fm.beginTransaction().remove(this).commit();

                if(callStatus.equals(Helper.StartCall)){
                    currentActivity.switchToStartVideoCall();
                }else{
                    currentActivity.switchToAcceptVideoCall();
                }
                break;
            case R.id.end_call:
                currentActivity.ringtonePlayer.stop();
                final Dialog hang_up_dialog = Helper.getHelperInstance().createDialog(currentActivity,true,Helper.hang_up_contentMsg);
                Button positiveBtn = (Button) hang_up_dialog.findViewById(R.id.positive_option);
                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hang_up_dialog.dismiss();

                        currentActivity.currentSession.hangUp(currentActivity.userInfo);
                        // TODO current session is hanging up<><><><>
                        currentActivity.destroyCurrentSession();
                        Helper.isCallStarted = false;
                        currentActivity.finish();
                    }
                });
                hang_up_dialog.show();
                break;
            case R.id.audio_call:
                if (currentActivity.getCurrentSession().getMediaStreamManager().isAudioEnabled()) {
                    System.out.println("-------------------disabling audio");
                    currentActivity.getCurrentSession().getMediaStreamManager().setAudioEnabled(false);
                    audio_call.setImageResource(R.drawable.voice_mute);
                } else {
                    System.out.println("--------------------enabling audio");
                    currentActivity.getCurrentSession().getMediaStreamManager().setAudioEnabled(true);
                    audio_call.setImageResource(R.drawable.voice);
                }
                break;
            case R.id.speaker:
                if (audioManager.getSelectedAudioDevice().equals(AppRTCAudioManager.AudioDevice.EARPIECE)) {
                    audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                    speaker.setAlpha(1.0f);
                } else {
                    audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                    speaker.setAlpha(0.5f);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    int minute = -1,sec = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_chat,null);
        currentActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        username = (TextView)view.findViewById(R.id.username);
        audioManager = AppRTCAudioManager.create(currentActivity, this);
        audioManager.init();
        callStatus = getArguments().getString(Helper.CallStatusKey);

        List<Integer> opponentList = currentActivity.getCurrentSession().getOpponents();
        Integer caller_id = currentActivity.getCurrentSession().getCallerID();

        this.opponentId = currentActivity.opponentId;
        this.chatType = currentActivity.chatType;

        RippleBackground profile_Img_layout = (RippleBackground) view.findViewById(R.id.user_image);
        profile_Img_layout.startRippleAnimation();

        time_text = (TextView) view.findViewById(R.id.time_text);

        final DecimalFormat decimalFormat = new DecimalFormat("00");
        CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(sec % 60 == 0){
                    minute++;
                }

                time_text.setText(decimalFormat.format(minute)+":"+decimalFormat.format(sec % 60));
                sec ++ ;
            }

            public void onFinish() {

            }
        };
        newtimer.start();

        text_chat = (ImageView) view.findViewById(R.id.text_chat);
        text_chat.setOnClickListener(this);

        video_call = (ImageView) view.findViewById(R.id.video_call);
        video_call.setOnClickListener(this);

        call_end = (ImageView) view.findViewById(R.id.end_call);
        call_end.setOnClickListener(this);

        audio_call = (ImageView) view.findViewById(R.id.audio_call);
        audio_call.setOnClickListener(this);

        speaker = (ImageView) view.findViewById(R.id.speaker);
        speaker.setOnClickListener(this);

        callerImage = (ImageView)view.findViewById(R.id.callerImage);
        callerImage_loader = (ProgressBar) view.findViewById(R.id.callerImage_loader);

        if(!currentActivity.isCallReceived){
            for(Integer opponent:opponentList){
                opponentId = opponent;
            }
        }else{
            opponentId = caller_id;
        }
        if(!Helper.isCallStarted){
            Helper.isCallStarted = true;
            callStatus = getArguments().getString(Helper.CallStatusKey);
            if (callStatus.equals(Helper.StartCall)) {
                Map<String,String> userInfo = currentActivity.getCurrentSession().getUserInfo();
                userInfo.put(Helper.chatType,Helper.typeAudio);
                currentActivity.getCurrentSession().startCall(userInfo);
            }
        }
        QBUsers.getUser(opponentId, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                username.setVisibility(View.VISIBLE);
                username.setText(user.getLogin());
                UserCustomData userCustomData = Helper.getCustomObject(user.getCustomData());
                if(userCustomData!=null){
                    Picasso.with(currentActivity)
                            .load(userCustomData.getProfile_picture_url())
                            .error(R.drawable.image_not_found)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    callerImage.setImageBitmap(Helper.getRoundedCornerBitmap(bitmap));
                                    callerImage_loader.setVisibility(View.GONE);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    Bitmap icon = BitmapFactory.decodeResource(currentActivity.getResources(),
                                            R.drawable.image_not_found);
                                    callerImage.setImageBitmap(icon);
                                    callerImage.setPadding(Helper.dp_padding, Helper.dp_padding, Helper.dp_padding, Helper.dp_padding);
                                    callerImage_loader.setVisibility(View.GONE);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    callerImage_loader.setVisibility(View.VISIBLE);
                                }
                            });
                }
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
                callerImage_loader.setVisibility(View.GONE);
                callerImage.setImageResource(R.drawable.image_not_found);
            }
        });

        return view;
    }

    @Override
    public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
        Log.w(TAG, "onAudioChangedState() called with: " + "audioDevice = [" + audioDevice + "]");

    }
}
