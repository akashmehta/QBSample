package com.example.abc.qbsample.CoreFragments;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
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
import com.skyfishjy.library.RippleBackground;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by abc on 6/13/2016.
 */
public class IncomingCallFragment extends Fragment implements View.OnClickListener{
    CallActivity activity;
    int opponentId;
    ImageView callerImage,   call_end, call_receive;
    ProgressBar callerImage_loader;
    String chatType;
    TextView username;
    RippleBackground profile_Img_layout;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (CallActivity) activity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.end_call:
                activity.ringtonePlayer.stop();
                activity.currentSession.hangUp(activity.userInfo);
                // TODO current session is hanging up<><><><>
                activity.destroyCurrentSession();
                Helper.isCallStarted = false;
                activity.finish();
                break;
            case R.id.receive_call:

                if(chatType.equals(Helper.typeAudio)){
                    activity.ringtonePlayer.stop();
                    activity.fm.beginTransaction().remove(this).commit();
                    activity.hangUp.setVisibility(View.VISIBLE);
                    activity.switchToAcceptAudioCall();
                }else if(chatType.equals(Helper.typeVideo)){
                    activity.ringtonePlayer.stop();
                    activity.fm.beginTransaction().remove(this).commit();
                    activity.hangUp.setVisibility(View.VISIBLE);
                    activity.switchToAcceptVideoCall();
                }
                break;
        }
    }

    private void setView(View v,Boolean active){
        if(active){
            v.setClickable(true);
            v.setAlpha(1f);
            v.setOnClickListener(this);
        }else{
            v.setClickable(false);
            v.setAlpha(0.5f);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incoming_call,null);
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        this.opponentId = activity.opponentId;
        this.chatType = activity.chatType;

        profile_Img_layout = (RippleBackground) view.findViewById(R.id.user_image);
        profile_Img_layout.startRippleAnimation();
        username = (TextView)view.findViewById(R.id.username);

        call_end = (ImageView) view.findViewById(R.id.end_call);
        setView(call_end,true);

        call_receive = (ImageView) view.findViewById(R.id.receive_call);
        setView(call_receive,true);

        callerImage = (ImageView)view.findViewById(R.id.callerImage);
        callerImage_loader = (ProgressBar) view.findViewById(R.id.callerImage_loader);
        QBUsers.getUser(opponentId, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                username.setVisibility(View.VISIBLE);
                username.setText(user.getLogin());
                UserCustomData userCustomData = Helper.getCustomObject(user.getCustomData());
                if(userCustomData!=null){
                    Picasso.with(activity)
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
                                    Bitmap icon = BitmapFactory.decodeResource(activity.getResources(),
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
}
