package com.example.abc.qbsample.ShareFile;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.R;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SendAudioVideomsg extends AppCompatActivity {
    ImageView audio_chat_rec;
    ProgressBar audio_chat_loader;
    Button playFile,stopFile ;
    String output_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QBSampleFiles/Audio";
    MediaPlayer m;
    ProgressBar uploadProgressBar;
    Dialog d;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_audio_videomsg);
        playFile = (Button) findViewById(R.id.play_file);
        stopFile = (Button) findViewById(R.id.stop_file);
        uploadProgressBar = (ProgressBar) findViewById(R.id.uploadprogressBar);

        audio_chat_rec = (ImageView) findViewById(R.id.audio_rec);
        audio_chat_loader = (ProgressBar) findViewById(R.id.audio_chat_loader);
        audio_chat_loader.setVisibility(View.GONE);

        final MediaRecorder myAudioRecorder=new MediaRecorder();

        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        Long tsLong = System.currentTimeMillis()/1000;
        final String ts = tsLong.toString();
        File f = new File(output_path);
        if(!f.exists()){
            f.mkdirs();
        }
        output_path = output_path+"/rec"+ts+".3gp";
        myAudioRecorder.setOutputFile(output_path);

        audio_chat_rec.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        System.out.println("_____________rec button is pressed : ");
                        audio_chat_loader.setVisibility(View.VISIBLE);
                        try {
                            myAudioRecorder.prepare();
                            myAudioRecorder.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        System.out.println("_____________rec button is released : ");
                        audio_chat_loader.setVisibility(View.GONE);
                        d = Helper.getHelperInstance().createDialog(SendAudioVideomsg.this,true, Helper.rec_audio_confirm);
                        Button confirm = (Button)d.findViewById(R.id.positive_option);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                d.dismiss();
                                myAudioRecorder.stop();
                                myAudioRecorder.release();
                                File file = new File(output_path);

                                Boolean isPublic = true;

                                uploadProgressBar.setVisibility(View.VISIBLE);
                                QBContent.uploadFileTask(file, isPublic, null, new QBEntityCallback<QBFile>() {
                                    @Override
                                    public void onSuccess(QBFile qbFile, Bundle params) {
                                        uid = qbFile.getUid();
                                        // TODO send push notification to user
                                        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
                                        userIds.add(12599880);
                                        QBEvent event = new QBEvent();
                                        event.setUserIds(userIds);
                                        event.setEnvironment(QBEnvironment.DEVELOPMENT);
                                        event.setNotificationType(QBNotificationType.PUSH);
                                        event.setPushType(QBPushType.GCM);

                                        HashMap<String, Object> data = new HashMap();
                                        data.put("data.message", uid);
                                        data.put("data.type", "download_uid");
                                        event.setMessage(data);

                                        QBPushNotifications.createEvent(event, new QBEntityCallback<QBEvent>() {
                                            @Override
                                            public void onSuccess(QBEvent qbEvent, Bundle args) {
                                                // sent
                                                System.out.println("Push Notification sent successfully notification massage is : \n" + qbEvent.getMessage());
                                            }

                                            @Override
                                            public void onError(QBResponseException errors) {
                                                errors.printStackTrace();
                                            }
                                        });
                                        System.out.println("__________________THE UID IS : " + qbFile.getUid());
                                    }

                                    @Override
                                    public void onError(QBResponseException error) {
                                        error.printStackTrace();
                                    }
                                }, new QBProgressCallback() {
                                    @Override
                                    public void onProgressUpdate(int progress) {
                                        uploadProgressBar.setProgress(progress);
                                    }
                                });


                                playFile.setVisibility(View.VISIBLE);
                                playFile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        m = new MediaPlayer();
                                        try{
                                            m.setDataSource(output_path);
                                            m.prepare();
                                            m.start();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        stopFile.setVisibility(View.VISIBLE);
                                        stopFile.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                m.stop();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        d.show();
                        break;
                }
                return true;
            }
        });
    }
}
