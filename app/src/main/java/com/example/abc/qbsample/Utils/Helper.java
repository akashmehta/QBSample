
package com.example.abc.qbsample.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.abc.qbsample.CoreActivity.LoginActivity;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.gcm.GooglePlayServicesHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quickblox.auth.QBAuth;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by abc on 5/3/2016.
 */
public class Helper {

    public static final String APP_ID = "40101";
    public static final String AUTH_KEY = "Kqh-wtPYecWOXLp";
    public static final String AUTH_SECRET = "YtQEsDFab7qcdbB";
    public static final String ACCOUNT_KEY = "pWy2oxhYCsUPvqehkeRy";
    public static final String SENDER_ID = "1067088977023";
    public static final String EXTRA_GCM_MESSAGE = "message";
    public static final String ACTION_NEW_GCM_EVENT = "new-push-event";
    public static final String requestMessage = "isRequestMessage";
    public static final String opponentID = "opponentId";
    public static final String chatType = "chatType";
    public static final String typeAudio = "audioChat";
    public static final String typeVideo = "videoChat";
    public static final String currentUserStr = "currentUser";
    public static QBRTCSession receivedSession =null ;
    public static String Action;
    public static String Action_Login = "Login";
    public static final String StartCall = "StartCall";
    public static final String AcceptCall = "AcceptCall";
    public static final String CallStatusKey = "CallStatus";
    public static Boolean isCallStarted = false;
    public static final String IncomingCallFragment = "IncommingCallFragment";
    public static int PICK_IMAGE_REQUEST = 5001;
    public static int CAMERA_REQUEST = 1888;
    public static QBUser currentUser;
    public static final String FileCacheName = "QBSampleCache";
    public static final String[] randomColorCodes = {"#f44336", "#9c27b0", "#3f51b5", "#2196f3", "#cddc39", "#009688", "#4caf50", "#827717", "#795548", "#607d8b"};

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {

            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                //Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static final int dp_padding = 18;
    public static final int cp_padding = 4;
    Dialog sample_dialog;
    TextView dialog_title;
    Button btnPositive, btnNegative;
    private final String SharedPreferenceTitle = "QBSamplePrefs", current_user = "currentUser";
    public static final String hang_up_contentMsg[] = {
            "Are you sure you want to Hang up?"
            , "No"
            , "Yes"};

    public static final String rec_audio_confirm[] = {
            "Send recorded file to user?", "Cancle", "Send"
    };

    public static final String image_send_list[] = {
            "Choose Image from ..."
            ,   "Camera"
            ,   "Gallery"
    };

    public static final String gender_list[] = {
            "Gender",
            "male",
            "female"
    };

    public static final String city_list[] = {
            "City",
            "Ahmedabad",
            "Bhuj",
            "Surat"
    };

    public static final String state_list[] = {
            "State",
            "Gujarat",
            "Punjab",
            "Karnatak"
    };

    public static final String country_list[] = {
            "Country",
            "India",
            "U.S.",
            "U.K."
    };

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String getAttachmentFileName(String fileId){return "Photo_"+fileId;}
    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mEditor;
    private static final String SharedPrefsTitle = "QBSampleSharedPrefs";
    private static String json;

    /**
     * @param mContext
     * @param key
     * @param messageList
     * @see SharedPreferences .This methods are used to save user chat and retrieve it.
     */
    public static void saveChat(Context mContext, String key, ArrayList<QBChatMessage> messageList) {
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        json = gson.toJson(messageList, ArrayList.class);
        mEditor.putString(key, json);
        mEditor.commit();
    }

    public static ArrayList<QBChatMessage> getSavedChat(Context mContext, String key) {
        try {
            Type type = new TypeToken<ArrayList<QBChatMessage>>() {
            }.getType();
            mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
            json = mPrefs.getString(key, null);
            ArrayList<QBChatMessage> chatList = gson.fromJson(json, type);
            System.out.println("size of chatlist is.............. : " + chatList.size());
            if (chatList != null) {
                return chatList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<QBChatMessage>();
    }

    /*-----------------------------*/
    public static final String recentDialogList = "recentDialogList";
    public static final String recentUserListKey = "recentUserListKey";
    public static final String recentUserListUserValue = "recentUserListValue";
    /**
     * This method will save user dialog list and user list
     *
     * @param mContext
     * @param userList
     */
    public static void saveRecentMessageUser(Context mContext, SparseArray<QBUser> userList, int currentUserId) {

        Type intArraytype = new TypeToken<Integer[]>(){}.getType();
        Type objArrayType = new TypeToken<QBUser[]>(){}.getType();

        Integer[] userIdKey = new Integer[userList.size()];
        QBUser[] userAr = new QBUser[userList.size()];

        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        for(int i = 0 ; i<userList.size() ; i++){
            userIdKey[i] = userList.keyAt(i);
            userAr[i] = userList.valueAt(i);
        }

        String userListStrKey = gson.toJson(userIdKey, intArraytype);
        String userListStrValue = gson.toJson(userAr, objArrayType);

        mEditor.putString(recentUserListKey + currentUserId, userListStrKey);
        mEditor.putString(recentUserListUserValue + currentUserId, userListStrValue);

        mEditor.commit();
    }

    public static void saveRecentMessageDialog(Context mContext,ArrayList<QBDialog> dialogList,int currentUserId){
        Type arrayType = new TypeToken<ArrayList<QBDialog>>(){}.getType();
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        String dialogListStr = gson.toJson(dialogList, arrayType);
        mEditor.putString(recentDialogList + currentUserId, dialogListStr);
        mEditor.commit();

    }

    public static ArrayList<QBDialog> getRecentDialogs(Context mContext, int currentUserId) {
        try {
            Type arrayType = new TypeToken<ArrayList<QBDialog>>() {
            }.getType();
            mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
            String dialogListStr = mPrefs.getString(recentDialogList + currentUserId, null);
            ArrayList<QBDialog> dialogList = gson.fromJson(dialogListStr, arrayType);
            if(dialogList!=null){
                return dialogList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public static SparseArray<QBUser> getRecentUserList(Context mContext, int currentUserId) {
        SparseArray<QBUser> recentUserList = new SparseArray<>();
        try {
            Type intArraytype = new TypeToken<Integer[]>(){}.getType();
            Type objArrayType = new TypeToken<QBUser[]>(){}.getType();

            mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);

            String userListStrKey = mPrefs.getString(recentUserListKey + currentUserId, null);
            String userListStrValue = mPrefs.getString(recentUserListUserValue + currentUserId, null);

            Integer key[] = gson.fromJson(userListStrKey, intArraytype);
            QBUser value[] = gson.fromJson(userListStrValue,objArrayType);

            for (int i = 0 ; i < key.length ; i ++ ) {
                recentUserList.put(key[i],value[i]);
            }
            if(recentUserList!=null){
                return recentUserList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recentUserList;
    }

    public static void updateRecentMessageDialog(Context mContext,ArrayList<QBDialog> qbDialogs,int currentUserId){
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle,mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mEditor.remove(recentDialogList);
        saveRecentMessageDialog(mContext,qbDialogs,currentUserId);
    }

    public static void updateRecentUserList(Context mContext,SparseArray<QBUser> qbUsers , int currentUserId){
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle,mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mEditor.remove(recentUserListKey);
        mEditor.remove(recentUserListUserValue);
        saveRecentMessageUser(mContext,qbUsers,currentUserId);
    }

    /*--------------------------------------------------------------------*/
    public static final String friendListDialogTxt = "friendListDialog";
    public static final String friendListUserTxt = "friendListUser";
    /**
     * This method will save Friend List
     *
     * @param mContext
     */
    public static void saveFriendListUsers(Context mContext, ArrayList<QBUser> userList, int currentUserId) {
        Type userArrayType = new TypeToken<ArrayList<QBUser>>(){}.getType();
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        String userListStr = gson.toJson(userList,userArrayType);
        mEditor.putString(friendListUserTxt + currentUserId , userListStr);
        mEditor.commit();
    }

    public static void saveFriendListDialog(Context mContext ,ArrayList<QBDialog> friendList,int currentUserId){
        Type dialogArrayType = new TypeToken<ArrayList<QBDialog>>(){}.getType();
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        String friendListStr = gson.toJson(friendList, dialogArrayType);
        mEditor.putString(friendListDialogTxt + currentUserId, friendListStr);
        mEditor.commit();
    }

    public static ArrayList<QBDialog> getFriendDialogList(Context mContext, int currentUserId) {
        try {
            Type arrayType = new TypeToken<ArrayList<QBDialog>>() {
            }.getType();
            mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
            String friendListStr = mPrefs.getString(friendListDialogTxt + currentUserId, null);
            ArrayList<QBDialog> dialogList = gson.fromJson(friendListStr, arrayType);
            if(dialogList!=null){
                return dialogList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public static ArrayList<QBUser> getFriendUserList(Context mContext , int currentUserId){
        try{
            Type arrayType = new TypeToken<ArrayList<QBUser>>(){}.getType();
            mPrefs = mContext.getSharedPreferences(SharedPrefsTitle, mContext.MODE_PRIVATE);
            String friendListStr = mPrefs.getString(friendListUserTxt + currentUserId, null);
            ArrayList<QBUser> userList = gson.fromJson(friendListStr, arrayType);
            if(userList!=null){
                return userList;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public static void updateFriendDialogList(Context mContext, ArrayList<QBDialog> dialogs,int currentUserId){
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle,mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mEditor.remove(friendListDialogTxt);
        saveFriendListDialog(mContext,dialogs,currentUserId);
    }
    public static void updateFriendUserList(Context mContext, ArrayList<QBUser> users,int currentUserId){
        mPrefs = mContext.getSharedPreferences(SharedPrefsTitle,mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mEditor.remove(friendListUserTxt);
        saveFriendListUsers(mContext,users,currentUserId);
    }
    /*------------------------------------------------------------------------------------------*/



    public static String getOpponentKey(int id) {
        return "Opponentchat" + id;
    }

    public static final void SignOut(final Activity currentActivity) {
        QBAuth.deleteSession(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        currentActivity.finish();
                        GooglePlayServicesHelper googlePlayServicesHelper = new GooglePlayServicesHelper();
                        if(googlePlayServicesHelper.checkPlayServicesAvailable()){
                            googlePlayServicesHelper.unregisterFromGcm(Helper.SENDER_ID);
                        }
                        Helper.getHelperInstance().setCurrentUser(currentActivity,null);
                        Intent it = new Intent(currentActivity, LoginActivity.class);
                        currentActivity.startActivity(it);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        e.printStackTrace();
                    }
                });

            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
            }
        });


    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Activity activity, Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        System.out.println("________THE HEIGHT OF BITMAP IS : " + bitmap.getHeight() + " __________ the Width of Bitmap is : " + bitmap.getWidth());
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                Math.min(bitmap.getWidth() / 2, bitmap.getHeight() / 2), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        if (bitmap.getWidth() > (bitmap.getHeight() * 1.5)) {
            canvas.rotate(90, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        }
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    public static final String path = Environment.getExternalStorageDirectory()+"/Pictures/QBSampleImages/";
    public static final String messagePath = Environment.getExternalStorageDirectory()+"/Picture/QBSampleImages/ChatMessageAttachments";

    public static File checkUserImage(String fileName){
        File f = new File(messagePath+"/"+fileName+".jpg");
        if(f.exists()){
            return f;
        }
        return null;
    }

    public static void copyUserImage(Bitmap image,String fileName){
        File f = new File(messagePath);
        if(!f.exists()){
            f.mkdirs();
        }
        File f1 = new File(messagePath +"/"+ fileName + ".jpg");
        try {
            FileOutputStream fout = new FileOutputStream(f1);
            image.compress(Bitmap.CompressFormat.JPEG,100,fout);
            fout.close();
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void copyUserImage(File srcFile,String fileName){
        Bitmap image = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
        copyUserImage(image,fileName);
    }
    public static void saveUserImage(Bitmap image,String name){
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        File f1 = new File(path + name + ".jpg");
        try {
            FileOutputStream fout = new FileOutputStream(f1);
            image.compress(Bitmap.CompressFormat.JPEG,100,fout);
            fout.close();
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Bitmap getUserImage(String name){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap image = BitmapFactory.decodeFile(path + name + ".jpg",options);
        return image;
    }


    public static final String chooseImageDialog[] = {"Choose Image from ... ", "Camera", "Gallery"};

    private static Gson gson = new Gson();

    public static String getJsonString(UserCustomData customData) {
        Gson gson = new Gson();
        String Json = gson.toJson(customData);
        return Json;
    }

    public static UserCustomData getCustomObject(String json) {
        Gson gson = new Gson();
        UserCustomData obj = gson.fromJson(json, UserCustomData.class);
        return obj;
    }

    public static Helper getHelperInstance() {
        return helperInstance;
    }

    private final static Helper helperInstance = new Helper();

    public Dialog loaderDialog(Activity activity){
        final Dialog dialog = new Dialog(activity,R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loader);
        return dialog;
    }

    public Dialog createDialog(Activity activity, Boolean setNegativeOption, String... massage) {
        sample_dialog = new Dialog(activity);
        sample_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sample_dialog.setContentView(R.layout.dialog_sample);
        dialog_title = (TextView) sample_dialog.findViewById(R.id.dialog_title);
        dialog_title.setText(massage[0]);
        btnNegative = (Button) sample_dialog.findViewById(R.id.negative_option);
        btnNegative.setText(massage[1]);
        if (setNegativeOption) {
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sample_dialog.dismiss();
                }
            });
        }
        btnPositive = (Button) sample_dialog.findViewById(R.id.positive_option);
        btnPositive.setText(massage[2]);
        return sample_dialog;
    }

    public void setCurrentUser(Context mContext, QBUser user) {
        String json = gson.toJson(user);
        mPrefs = mContext.getSharedPreferences(SharedPreferenceTitle, mContext.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mEditor.putString(current_user, json);
        mEditor.commit();
    }

    public QBUser getCurrentUser(Context mContext) {
        try{
            mPrefs = mContext.getSharedPreferences(SharedPreferenceTitle, mContext.MODE_PRIVATE);
            QBUser user = gson.fromJson(mPrefs.getString(current_user, ""), QBUser.class);
            return user;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isCameraFront(int deviceId) {
        Camera.CameraInfo cameraInfo = getCameraInfo(deviceId);

        return (cameraInfo != null && cameraInfo.facing == 1);
    }

    public static Camera.CameraInfo getCameraInfo(int deviceId) {

        Camera.CameraInfo info = null;

        try {
            info = new Camera.CameraInfo();
            Camera.getCameraInfo(deviceId, info);
        } catch (Exception var3) {
            var3.printStackTrace();
            info = null;
        }
        return info;
    }

}
