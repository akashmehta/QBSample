package com.example.abc.qbsample.CoreFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.abc.qbsample.Adapters.AllContactListAdapter;
import com.example.abc.qbsample.CoreActivity.ContactListActivity;
import com.example.abc.qbsample.Listeners.INetworkListener;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.NetworkUtils;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by abc on 5/13/2016.
 */
public class AllContactListFragment extends Fragment implements INetworkListener{
    ListView contactList;
    AllContactListAdapter adapter;
    ProgressBar center_loader;
    ArrayList<QBUser> user_list = new ArrayList();
    ContactListActivity activity;
    TextView err_txt;
    private QBRequestGetBuilder requestBuilder;
    private QBPrivateChatManager chatManager;
    QBPagedRequestBuilder pagedRequestBuilder;
    ArrayList<Integer> userIds = new ArrayList<>();
    QBMessageListener privateChatQBMessageListener;
    QBRoster roster;
    private static final String TAG  = AllContactListFragment.class.getSimpleName();
    private BroadcastReceiver networkReceiver = getReceiver() ;
    private boolean internetAvailable = false;
    private String ExtraMSG;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ContactListActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contects, null);
        this.ExtraMSG = activity.ExtraMSG;
        contactList = (ListView) v.findViewById(R.id.contact_list);
        center_loader = (ProgressBar) v.findViewById(R.id.center_loader);
        err_txt = (TextView) v.findViewById(R.id.error_txt);
        contactList.setVisibility(View.GONE);
        center_loader.setVisibility(View.VISIBLE);

        chatManager = QBChatService.getInstance().getPrivateChatManager();
        privateChatQBMessageListener = new QBMessageListener<QBPrivateChat>() {
            @Override
            public void processMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
                System.out.println("_______ The message is : " + qbChatMessage);
            }

            @Override
            public void processError(QBPrivateChat qbPrivateChat, QBChatException e, QBChatMessage qbChatMessage) {

            }
        };
        requestBuilder = new QBRequestGetBuilder();
        pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int i) {

            }
        };
        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
        if(roster!=null){
            setAllFriendList();
        }else{
            Log.d(TAG, "onCreateView: THe user is currently not logged in..........");
        }
        registerReceiver();
        return v;
    }
    private void setAllFriendList(){
        Iterator<QBRosterEntry> it = roster.getEntries().iterator();
        while (it.hasNext()) {
            QBRosterEntry entry = it.next();
            userIds.add(entry.getUserId());
        }

        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> qbDialogs, Bundle bundle) {
                QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        for (QBUser user :
                                users) {
                            System.out.println("________________THE CURRENT USER ID IS : " + user.getId());
                            if (!user.getLogin().equals(activity.currentUser.getLogin()) && !userIds.contains(user.getId())) {
                                user_list.add(user);
                            }
                        }
                        contactList.setVisibility(View.VISIBLE);
                        center_loader.setVisibility(View.GONE);
                        adapter = new AllContactListAdapter(activity, R.layout.fragment_contects, user_list);
                        contactList.setAdapter(adapter);

                        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                final Dialog sendMessageDialog = new Dialog(activity);
                                sendMessageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                sendMessageDialog.setContentView(R.layout.dialog_send_request);
                                Button send = (Button) sendMessageDialog.findViewById(R.id.sendMessageBtn);
                                final EditText reqMessage = (EditText) sendMessageDialog.findViewById(R.id.dialog_message);
                                send.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int userID = user_list.get(position).getId();
                                        sendMessage(reqMessage.getText().toString(), user_list.get(position).getId());
                                        if (roster.contains(userID)) {
                                            try {
                                                roster.subscribe(userID);
                                            } catch (SmackException.NotConnectedException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            try {
                                                roster.createEntry(userID, null);
                                            } catch (XMPPException e) {
                                                e.printStackTrace();
                                            } catch (SmackException.NotLoggedInException e) {
                                                e.printStackTrace();
                                            } catch (SmackException.NotConnectedException e) {
                                                e.printStackTrace();
                                            } catch (SmackException.NoResponseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        sendMessageDialog.dismiss();
                                    }
                                });
                                sendMessageDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        errors.printStackTrace();
                        err_txt.setVisibility(View.VISIBLE);
                        contactList.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }
    private void sendMessage(String message, Integer selectedUserId) {
        try {
            QBChatMessage chatMessage = new QBChatMessage();
            if (message == null || message.equals("")) {
                chatMessage.setBody("Hi there ! accept Request");
            } else {
                chatMessage.setBody(message);
            }
            chatMessage.setSenderId(activity.currentUser.getId());
            chatMessage.setProperty("save_to_history", "1");
            chatMessage.setProperty(Helper.requestMessage, "true");
            QBPrivateChat privateChat = chatManager.getChat(selectedUserId);
            if (privateChat == null) {
                privateChat = chatManager.createChat(selectedUserId, privateChatQBMessageListener);
            }
            privateChat.sendMessage(chatMessage);
            QBChatService.deleteMessage(chatMessage.getId(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    System.out.println("message deleted");
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BroadcastReceiver getReceiver() {
        BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = NetworkUtils.getConnectionStatus(context);
                if(networkStatus == NetworkUtils.NOT_CONNECTED){
                    internetAvailable = false;
                    onInternetDisconnected();
                } else if(networkStatus == NetworkUtils.MOBILE){
                    internetAvailable = true;
                    onInternetConnected();
                } else if(networkStatus == NetworkUtils.WIFI){
                    internetAvailable = true;
                    onInternetConnected();
                }
            }
        };
        return networkChangeReceiver;
    }

    @Override
    public void registerReceiver() {
        activity.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void unregisterReceiver() {
        activity.unregisterReceiver(networkReceiver);
    }

    @Override
    public void onInternetConnected() {

    }

    @Override
    public void onInternetDisconnected() {

    }
    @Override
      public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }
}
