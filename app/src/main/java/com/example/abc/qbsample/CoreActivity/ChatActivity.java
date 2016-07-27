package com.example.abc.qbsample.CoreActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abc.qbsample.Adapters.MessageListAdapter;
import com.example.abc.qbsample.Bean.ChatContent;
import com.example.abc.qbsample.Listeners.INetworkListener;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.NetworkUtils;
import com.example.abc.qbsample.Utils.QBLogin;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ChatActivity extends BaseActivity implements INetworkListener, TextWatcher,EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {
    QBUser currentUser, selectedUser;
    QBDialog currentDialog;

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(message);

    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(message, emojicon);

    }

    EmojiconEditText message;
    ImageView sendMessage;
    QBPrivateChatManager chatManager;
    QBMessageListener<QBPrivateChat> privateChatQBMessageListener;
    public StickyListHeadersListView messageListView;
    MessageListAdapter adapter;
    ArrayList<ChatContent> chatHistory = new ArrayList<>();
    RelativeLayout friendReqLayout;
    Button btnAccept, btnDecline, btnCamera, btnGallery;
    TextView reqMessage;
    QBRoster roster;
    private BroadcastReceiver networkReceiver = getReceiver();
    private boolean internetAvailable = false;
    private Boolean messageFetched = false;
    private static final String TAG = ChatActivity.class.getSimpleName();
    private String ExtraMSG;
    private boolean isDialogCreated = false;
    Dialog pickImageDialog;
    private Bitmap captured_image;
    private FrameLayout emojicons;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
//        menu.findItem(R.id.video_chat).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        return true;
    }

    ArrayList<QBDialog> dbRecentDialog;
    SparseArray<QBUser> dbRecentUser;

    /**
     * This method will use to delete chat history , dialog and user
     */
    private void deleteDialog() {

        /*
             portion to delete chat
         */
        String msgIds[] = new String[chatHistory.size()];
        for (int i = 0; i < chatHistory.size(); i++) {
            msgIds[i] = chatHistory.get(i).getChatMessage().getId();
        }
        if (msgIds.length > 0) {
            QBChatService.deleteMessages(new HashSet(Arrays.asList(msgIds)), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Toast.makeText(ChatActivity.this, "All messages are deleted", Toast.LENGTH_LONG).show();
                    chatHistory.clear();
                    Helper.saveChat(ChatActivity.this, Helper.getOpponentKey(selectedUser.getId()), new ArrayList<QBChatMessage>());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {
                    Toast.makeText(ChatActivity.this, "Error while deletig messages", Toast.LENGTH_LONG).show();
                }
            });

        }
        /*-----------------------------------------------------------------------------*/
        System.out.println("...................THE current user id is : " + currentUser.getId());

        /*
            This portion is used to delete user
         */

        dbRecentUser = Helper.getRecentUserList(ChatActivity.this, currentUser.getId());
        dbRecentUser.remove(selectedUser.getId());
        Helper.updateRecentUserList(ChatActivity.this, dbRecentUser, currentUser.getId());

        /*
            This portion is used to delete dialog
         */
        dbRecentDialog = Helper.getRecentDialogs(ChatActivity.this, currentUser.getId());
        chatManager.deleteDialog(currentDialog.getDialogId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Toast.makeText(ChatActivity.this, "Dialog is deleted", Toast.LENGTH_LONG).show();
                for (int i = 0; i < dbRecentDialog.size(); i++) {
                    if (dbRecentDialog.get(i).getDialogId().equals(currentDialog.getDialogId())) {
                        Log.e(TAG, "onSuccess: dbRecentDialog is : " + dbRecentDialog + "\n____ current Dialog" + currentDialog);
                        dbRecentDialog.remove(i);
                    }
                }
                Helper.updateRecentMessageDialog(ChatActivity.this, dbRecentDialog, currentUser.getId());
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.delete_chat:
                deleteDialog();
                break;
            case R.id.remove_user:
                try {
                    if (roster != null) {
                        roster.unsubscribe(selectedUser.getId());
                        /*
                            delete and update friend user list
                         */
                        ArrayList<QBUser> dbUserList = Helper.getFriendUserList(ChatActivity.this, currentUser.getId());
                        for (int i = 0; i < dbUserList.size(); i++) {
                            if (dbUserList.get(i).getId() == selectedUser.getId()) {
                                dbUserList.remove(i);
                            }
                        }
                        Helper.updateFriendUserList(ChatActivity.this, dbUserList, currentUser.getId());
                        /*
                            delete and update dialog list
                         */
                        ArrayList<QBDialog> dbUserDialog = Helper.getFriendDialogList(ChatActivity.this, currentUser.getId());
                        if (currentDialog != null) {
                            for (int i = 0; i < dbUserDialog.size(); i++) {
                                if (currentDialog.getDialogId().equals(dbUserDialog.get(i).getDialogId())) {
                                    dbUserDialog.remove(i);
                                }
                            }
                            Helper.updateFriendDialogList(ChatActivity.this, dbUserDialog, currentUser.getId());
                        }
                    }
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "User is not subscribed", Toast.LENGTH_LONG);
                }
                break;
            case R.id.add_fav:
                Toast.makeText(ChatActivity.this, "Perform add to favorite operation", Toast.LENGTH_SHORT).show();
                break;
            case R.id.edit_contact:
                Toast.makeText(ChatActivity.this, "Perform Edit contact operation", Toast.LENGTH_SHORT).show();
                break;
            case R.id.view_profile:
                Toast.makeText(ChatActivity.this, "Perform view profile operation", Toast.LENGTH_SHORT).show();
                break;
            case R.id.block_contact:
                Toast.makeText(ChatActivity.this, "Perform block contact operation", Toast.LENGTH_SHORT).show();
                break;

            case R.id.video_chat:
                Intent it = new Intent(ChatActivity.this, CallActivity.class);
                it.putExtra(Helper.opponentID, selectedUser.getId());
                it.putExtra(Helper.chatType, Helper.typeVideo);
                it.putExtra(Helper.currentUserStr, currentUser);
                startActivity(it);
                break;
            case R.id.audio_chat:
                it = new Intent(ChatActivity.this, CallActivity.class);
                it.putExtra(Helper.opponentID, selectedUser.getId());
                it.putExtra(Helper.chatType, Helper.typeAudio);
                it.putExtra(Helper.currentUserStr, currentUser);
                startActivity(it);
                break;
            case R.id.sign_out:
                Helper.SignOut(this);
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //removeSessionCallback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSessionCallback();
        Log.i(TAG, "onStart: called____________________");
        if (Helper.Action != null && Helper.Action.equals(Helper.Action_Login)) {
            Log.e(TAG, "onStart: __________________________ Activity call from call activity");
            Helper.Action = null;
            QBChatService.getInstance().login(Helper.getHelperInstance().getCurrentUser(this), new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {
                    Log.i(TAG, "onStart: User Logged in");

                }

                @Override
                public void onError(QBResponseException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public BroadcastReceiver getReceiver() {
        BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = NetworkUtils.getConnectionStatus(context);
                if (networkStatus == NetworkUtils.NOT_CONNECTED) {
                    internetAvailable = false;
                    onInternetDisconnected();
                } else if (networkStatus == NetworkUtils.MOBILE) {
                    internetAvailable = true;
                    onInternetConnected();
                } else if (networkStatus == NetworkUtils.WIFI) {
                    internetAvailable = true;
                    onInternetConnected();
                }
            }
        };
        return networkChangeReceiver;
    }

    @Override
    public void registerReceiver() {
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void unregisterReceiver() {
        try {
            unregisterReceiver(networkReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInternetConnected() {
        if (!messageFetched) {
            if (ExtraMSG != null && ExtraMSG.equals("error")) {
                new QBLogin(Helper.getHelperInstance().getCurrentUser(this)) {
                    @Override
                    public void OnLoginSuccessfull() {
                        ExtraMSG = "success";
                        getDialogMessages();
                    }

                    @Override
                    public void OnLoginError() {
                        this.error.printStackTrace();
                    }
                };
            } else {
                getDialogMessages();
            }
        }
    }

    @Override
    public void onInternetDisconnected() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /*if (count == 0) {
            sendFile.setVisibility(View.VISIBLE);
            sendMessage.setVisibility(View.GONE);

        } else {
            sendFile.setVisibility(View.GONE);
            sendMessage.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private class BtnClickListener implements View.OnClickListener {
        Intent it;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.accept_req:
                    try {
                        if (roster != null) {
                            roster.confirmSubscription(selectedUser.getId());
                            /*
                                TODO Hear new user will be added when current user accepts the request
                             */
                            ArrayList<QBUser> friendUserList = Helper.getFriendUserList(ChatActivity.this, currentUser.getId());
                            friendUserList.add(selectedUser);
                            Helper.updateFriendUserList(ChatActivity.this, friendUserList, currentUser.getId());
                        }
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotLoggedInException e) {
                        e.printStackTrace();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.decline_req:
                    try {
                        if (roster != null) {
                            roster.reject(selectedUser.getId());
                        }
                        deleteDialog();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.positive_option:
                    // TODO operation for pick Image from Gallery
                    pickImageDialog.dismiss();
                    it = new Intent();
                    it.setType("image/*");
                    it.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(it, "Select File"), Helper.PICK_IMAGE_REQUEST);
                    break;
                case R.id.negative_option:
                    // TODO operation for pick Image from Camera
                    pickImageDialog.dismiss();
                    it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(it, Helper.CAMERA_REQUEST);
                    break;
                case R.id.stickers_btn:
                    if(stickers_btn.getTag().equals(TEXT)){
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        emojicons.setVisibility(View.VISIBLE);
                        message.setUseSystemDefault(true);
                        setEmojiconFragment(true);
                        stickers_btn.setTag(EMOJI);
                    }else {
                        setEmojiconFragment(false);
                        emojicons.setVisibility(View.GONE);
                        stickers_btn.setTag(TEXT);
                    }

                    break;
                case R.id.image_attachment:
                    pickImageDialog = Helper.getHelperInstance().createDialog(ChatActivity.this,false,Helper.chooseImageDialog);
                    btnCamera = (Button)pickImageDialog.findViewById(R.id.negative_option);
                    btnCamera.setOnClickListener(this);
                    btnGallery = (Button) pickImageDialog.findViewById(R.id.positive_option);
                    btnGallery.setOnClickListener(this);
                    pickImageDialog.show();
                    break;
            }
            friendReqLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {

        if(emojicons.getVisibility()==View.VISIBLE){
            emojicons.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    QBPrivateChatManager privateCHatManager;

    private ArrayList<QBDialog> updateDialog(ArrayList<QBDialog> srcDialog) {
        for (int i = 0; i < srcDialog.size(); i++) {
            if (srcDialog.get(i).getDialogId().equals(currentDialog.getDialogId())) {
                currentDialog.setLastMessage(chatHistory.get(chatHistory.size() - 1).getChatMessage().getBody());
                currentDialog.setLastMessageDateSent(chatHistory.get(chatHistory.size() - 1).getChatMessage().getDateSent());
                srcDialog.set(i, currentDialog);
            }
        }
        return srcDialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ArrayList<QBChatMessage> chatMessages = new ArrayList<>();
        chatMessages.clear();
        for (ChatContent content : chatHistory) {
            chatMessages.add(content.getChatMessage());
        }
        Helper.saveChat(this, Helper.getOpponentKey(selectedUser.getId()), chatMessages);
        // TODO solve ArrayIndex out of bound
        if (chatHistory.size() > 0) {
            currentDialog.setLastMessage(chatHistory.get(chatHistory.size() - 1).getChatMessage().getBody());
            currentDialog.setLastMessageDateSent(chatHistory.get(chatHistory.size() - 1).getChatMessage().getDateSent());
            Helper.updateRecentMessageDialog(this, updateDialog(Helper.getRecentDialogs(this, currentUser.getId())), currentUser.getId());
        }

        unregisterReceiver();
    }

    private File imageFile;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == Helper.PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                captured_image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageFile = new File(Helper.getRealPathFromURI(this, imageUri));

            } else if (requestCode == Helper.CAMERA_REQUEST) {
                captured_image = (Bitmap) data.getExtras().get("data");
                Uri tempUri = Helper.getImageUri(getApplicationContext(), captured_image);
                imageFile = new File(Helper.getRealPathFromURI(this, tempUri));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imageFile != null) {
            sendMessage(null, imageFile);

        }
    }

    Bitmap opponentImage;
    int random = (int) (Math.random() * 10) % Helper.randomColorCodes.length;
    Toolbar toolbar;
    BtnClickListener clickListener;
    RelativeLayout chatContent;
    ImageView stickers_btn ;
    private static final String TEXT = "TEXT";
    private static final String EMOJI = "EMOJI";
    InputMethodManager inputManager;
    private void addItem(QBChatMessage message){
        ChatContent content = new ChatContent(message,null);
        if(content.getChatMessage().getAttachments() != null && content.getChatMessage().getAttachments().size() > 0){
            content.setStartLoader(true);
        }
        chatHistory.add(content);
    }
    FloatingActionMenu menuLabelsRight;
    FloatingActionButton image_attachment;
    boolean btn_flag = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        clickListener = new BtnClickListener();
        menuLabelsRight = (FloatingActionMenu) findViewById(R.id.menu_labels_right);
        menuLabelsRight.hideMenuButton(false);

        image_attachment = (FloatingActionButton) findViewById(R.id.image_attachment);
        image_attachment.setOnClickListener(clickListener);

        inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        chatContent = (RelativeLayout) findViewById(R.id.chat_content);

        ExtraMSG = getIntent().getExtras().getString("ExtraMSG");
        currentUser = (QBUser) getIntent().getSerializableExtra("currentUser");
        Helper.currentUser = currentUser;
        selectedUser = (QBUser) getIntent().getSerializableExtra("selectedUser");
        Log.w(TAG, "onCreate: Current user is : ______________________" + selectedUser.getLogin());
        ab.setTitle(selectedUser.getLogin());
        currentDialog = (QBDialog) getIntent().getSerializableExtra("currentChatDialog");

       /* userImage = (ImageView) toolbar.findViewById(R.id.userImage);
        midTitle = (TextView) toolbar.findViewById(R.id.midTitle);
        userStatus = (ImageView) toolbar.findViewById(R.id.UserStatus);*/

        friendReqLayout = (RelativeLayout) findViewById(R.id.acceptReqLayout);
        btnAccept = (Button) findViewById(R.id.accept_req);
        btnDecline = (Button) findViewById(R.id.decline_req);
        reqMessage = (TextView) findViewById(R.id.requestText);
        messageListView = (StickyListHeadersListView) findViewById(R.id.chat_history_list);
        emojicons = (FrameLayout) findViewById(R.id.emojicons);
        stickers_btn = (ImageView) findViewById(R.id.stickers_btn);
        stickers_btn.setTag(TEXT);
        stickers_btn.setOnClickListener(clickListener);
        try {
            chatHistory.clear();
            for (QBChatMessage message : Helper.getSavedChat(this, String.valueOf(Helper.getOpponentKey(selectedUser.getId())))) {
                addItem(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        opponentImage = Helper.getUserImage(selectedUser.getLogin());

        adapter = new MessageListAdapter(this, R.layout.activity_chat, chatHistory, opponentImage, currentUser, selectedUser);
        messageListView.setAdapter(adapter);
        messageListView.setLongClickable(true);
        messageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;

                try {
                    int count = messageListView.getCount();
                    if (scrollState == SCROLL_STATE_IDLE) {
                        if ((messageListView.getLastVisiblePosition() >= count - threshold)) {
                            if(btn_flag){
                                btn_flag = false;
                                int delay = 400;
                                Handler mUiHandler = new Handler();
                                mUiHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        menuLabelsRight.showMenuButton(true);
                                    }
                                }, delay);
                            }
                            else{
                                btn_flag = true;
                            }
                        }
                    }
                } catch (NullPointerException nullpointer) {
                    nullpointer.printStackTrace();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if((totalItemCount-firstVisibleItem)!=visibleItemCount){
                    Log.e(TAG, "onScroll: __________ gather required postition" );
                    int delay = 400;
                    Handler mUiHandler = new Handler();
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            menuLabelsRight.hideMenuButton(true);
                        }
                    }, delay);
                }
            }
        });
        /*QBChatService.getInstance().addConnectionListener(this);*/
        message = (EmojiconEditText) findViewById(R.id.messageBox);
        message.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    if(emojicons.getVisibility()==View.VISIBLE){
                        emojicons.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        sendMessage = (ImageView) findViewById(R.id.sendMsg);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = message.getText().toString();
                if (!messageTxt.equals("") && roster != null) {
                    message.setText("");
                    sendMessage(messageTxt, null);
                }

            }
        });

        QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int i) {
            }
        };
        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
        try {
            QBPresence selectedUserPresence = roster.getPresence(selectedUser.getId());
            Log.w(TAG, "onCreate: Selected User presenece is : __________ " + selectedUserPresence);
            Log.w(TAG, "onCreate: Selected User presenece status is : __________ " + selectedUserPresence.getStatus());
            if (selectedUserPresence.getType().equals(QBPresence.Type.online)) {
                Log.w(TAG, "onCreate: Selected User status is : __________ online");
                //userStatus.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            } else if (selectedUserPresence.getType().equals(QBPresence.Type.offline)) {
                Log.w(TAG, "onCreate: Selected User status is : __________ offline");
                //userStatus.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
            }
        } catch (Exception e) {
           // userStatus.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        }

        Log.w(TAG, "onCreate: The roster is : _________ " + roster);
        if (roster != null) {
            roster.addRosterListener(new QBRosterListener() {
                @Override
                public void entriesDeleted(Collection<Integer> collection) {
                    System.out.println("Entry is deleted");
                    if (collection.contains(selectedUser.getId())) {
                        System.out.println(",,,,,,,,,,,,,,selected entry is deleted");
                        Toast.makeText(ChatActivity.this, "Current User is Unsubscribed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void entriesAdded(Collection<Integer> collection) {
                    if (collection.contains(selectedUser.getId())) {
                        System.out.println("User is Added...........");
                    }
                }

                @Override
                public void entriesUpdated(Collection<Integer> collection) {
                }

                @Override
                public void presenceChanged(final QBPresence qbPresence) {
                    Log.w(TAG, "presenceChanged: The status is changed of : ________________________ " + qbPresence.getUserId());
                    Log.w(TAG, "presenceChanged: The status is  ________________________ " + qbPresence.getStatus());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (qbPresence.getType().equals(QBPresence.Type.online)) {
                                Log.w(TAG, "onCreate: Selected User status is : __________ online");
                                //userStatus.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                            } else if (qbPresence.getType().equals(QBPresence.Type.offline)) {
                                Log.w(TAG, "onCreate: Selected User status is : __________ offline");
                                //userStatus.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                            }

                        }
                    });

                }
            });

        }
        chatManager = QBChatService.getInstance().getPrivateChatManager();
        if (currentDialog != null) {
            isDialogCreated = true;
            getDialogMessages();
        } else {
            isDialogCreated = false;

        }

        privateChatQBMessageListener = new QBMessageListener<QBPrivateChat>() {
            @Override
            public void processMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
                System.out.println("_______ The message is : " + qbChatMessage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDialogCreated) {
                            chatManager.createDialog(selectedUser.getId(), new QBEntityCallback<QBDialog>() {
                                @Override
                                public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                                    ChatActivity.this.currentDialog = qbDialog;
                                    addItem(qbChatMessage);
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            addItem(qbChatMessage);
                            adapter.notifyDataSetChanged();
                        }

                    }
                });

            }

            @Override
            public void processError(QBPrivateChat qbPrivateChat, QBChatException e, QBChatMessage qbChatMessage) {
            }
        };
        QBPrivateChatManagerListener privateChatManagerListener = new QBPrivateChatManagerListener() {
            @Override
            public void chatCreated(QBPrivateChat qbPrivateChat, boolean b) {
            }
        };
        privateCHatManager = QBChatService.getInstance().getPrivateChatManager();
        if (privateCHatManager != null) {
            privateCHatManager.addPrivateChatManagerListener(privateChatManagerListener);
            chatManager.createChat(selectedUser.getId(), privateChatQBMessageListener);
        }
        registerReceiver();

    }

    @Override
    Activity getActivity() {
        return ChatActivity.this;
    }

    @Override
    QBUser currentUser() {
        return currentUser;
    }

    /* private void createDialog(){

         }*/
    private void getDialogMessages() {
        messageFetched = true;
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        QBChatService.getDialogMessages(currentDialog, requestGetBuilder, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(final ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                Boolean isFriendRequest = false, isInFriendList = false;
                String reqMessageStr = "";
                for (QBChatMessage chatMessage : qbChatMessages) {
                    if (chatMessage.getProperty(Helper.requestMessage) != null && chatMessage.getProperty(Helper.requestMessage).equals("true")) {
                        isFriendRequest = true;
                        reqMessageStr = chatMessage.getBody();
                        QBChatService.deleteMessage(chatMessage.getId(), new QBEntityCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid, Bundle bundle) {
                                System.out.println("Message is deleted.......................");
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                if (isFriendRequest) {
                    friendReqLayout.setVisibility(View.VISIBLE);

                    btnAccept.setOnClickListener(clickListener);
                    btnDecline.setOnClickListener(clickListener);
                    reqMessage.setText(reqMessageStr);
                } else {
                    if (qbChatMessages.size() > chatHistory.size()) {
                        if (!isDialogCreated) {

                            chatManager.createDialog(selectedUser.getId(), new QBEntityCallback<QBDialog>() {
                                @Override
                                public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                                    ChatActivity.this.currentDialog = qbDialog;
                                    int diff = qbChatMessages.size() - chatHistory.size();
                                    for (QBChatMessage message : qbChatMessages.subList(qbChatMessages.size() - diff, qbChatMessages.size())) {
                                        addItem(message);
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            int diff = qbChatMessages.size() - chatHistory.size();
                            for (QBChatMessage message : qbChatMessages.subList(qbChatMessages.size() - diff, qbChatMessages.size())) {
                                addItem(message);
                            }
                            adapter.notifyDataSetChanged();
                        }


                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
            }
        });
    }

    public QBPrivateChat privateChat;
    private ChatContent content;
    private void sendMessage(final String message, final File imageFile) {

        try {
            if (!isDialogCreated) {

                chatManager.createDialog(selectedUser.getId(), new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        ChatActivity.this.currentDialog = qbDialog;
                        dbRecentDialog = Helper.getRecentDialogs(ChatActivity.this, currentUser.getId());
                        dbRecentDialog.add(0, qbDialog);

                        dbRecentUser = Helper.getRecentUserList(ChatActivity.this, currentUser.getId());
                        dbRecentUser.put(selectedUser.getId(), selectedUser);

                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setBody(message);
                        chatMessage.setSenderId(currentUser.getId());
                        chatMessage.setProperty("save_to_history", "1");
                        chatMessage.setDateSent(System.currentTimeMillis() / 1000);
                        privateChat = chatManager.getChat(selectedUser.getId());
                        if (privateChat == null) {
                            privateChat = chatManager.createChat(selectedUser.getId(), privateChatQBMessageListener);
                        }
                        content = new ChatContent(chatMessage,imageFile);
                        content.setStartLoader(true);
                        chatHistory.add(content);
                        adapter.notifyDataSetChanged();
                        if (imageFile == null) {
                            try {
                                privateChat.sendMessage(chatMessage);
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(message);
                chatMessage.setSenderId(currentUser.getId());
                chatMessage.setProperty("save_to_history", "1");
                chatMessage.setDateSent(System.currentTimeMillis() / 1000);
                privateChat = chatManager.getChat(selectedUser.getId());
                if (privateChat == null) {
                    privateChat = chatManager.createChat(selectedUser.getId(), privateChatQBMessageListener);
                }
                content = new ChatContent(chatMessage,imageFile);
                content.setStartLoader(true);
                chatHistory.add(content);
                adapter.notifyDataSetChanged();
                if (imageFile == null) {
                    privateChat.sendMessage(chatMessage);

                }
            }

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
}
