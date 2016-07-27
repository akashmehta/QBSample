package com.example.abc.qbsample.CoreFragments;

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.abc.qbsample.Adapters.AllContactListAdapter;
import com.example.abc.qbsample.CoreActivity.ChatActivity;
import com.example.abc.qbsample.CoreActivity.ContactListActivity;
import com.example.abc.qbsample.Listeners.INetworkListener;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.NetworkUtils;
import com.example.abc.qbsample.Utils.QBLogin;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by abc on 5/19/2016.
 */
public class FriendContactListFragment extends Fragment implements ConnectionListener,INetworkListener{
    ListView contactList ;
    AllContactListAdapter adapter;
    ProgressBar center_loader;
    //ArrayList<QBUser> user_list;
    ArrayList<QBUser> dbUser;
    ArrayList<QBDialog> dbDialog;
    private static final String TAG = FriendContactListFragment.class.getSimpleName();
    ContactListActivity activity;
    TextView err_txt;
    private QBRequestGetBuilder requestBuilder1;
    QBPagedRequestBuilder requestBuilder;
    QBPagedRequestBuilder pagedRequestBuilder;
    ArrayList<Integer> userIds;
    QBRoster roster;
    private BroadcastReceiver networkReceiver = getReceiver() ;
    private boolean internetAvailable = false;
    private String ExtraMSG;
    QBSubscriptionListener subscriptionListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity =(ContactListActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contects,null);
        contactList = (ListView) v.findViewById(R.id.contact_list);
        center_loader = (ProgressBar) v.findViewById(R.id.center_loader);
        err_txt = (TextView) v.findViewById(R.id.error_txt);
        this.ExtraMSG = activity.ExtraMSG;

        dbUser = Helper.getFriendUserList(activity,activity.currentUser.getId());
        dbDialog = Helper.getFriendDialogList(activity,activity.currentUser.getId());

        if(dbUser.size()==0){
            contactList.setVisibility(View.GONE);
            center_loader.setVisibility(View.VISIBLE);
        }
        adapter = new AllContactListAdapter(activity, R.layout.fragment_contects, dbUser);
        contactList.setAdapter(adapter);
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(activity, ChatActivity.class);
                it.putExtra("currentUser", activity.currentUser);
                it.putExtra("selectedUser", dbUser.get(position));
                for (QBDialog dialog : dbDialog) {
                    if (dialog.getOccupants().contains(dbUser.get(position).getId())) {
                        it.putExtra("currentChatDialog", dialog);
                    }
                }
                it.putExtra("ExtraMSG", ExtraMSG);
                activity.startActivity(it);
            }
        });
        requestBuilder1= new QBRequestGetBuilder();
        pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int i) {

            }
        };
        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,subscriptionListener);
        userIds= new ArrayList<>();

        if(roster!=null){
            UpdateContactList();
        } else{
            Log.d(TAG, "onCreateView: User is not Logged in currently : __________________");
        }
        registerReceiver();
        return v;
    }
    private void UpdateContactList(){
        Iterator<QBRosterEntry> it = roster.getEntries().iterator();
        while (it.hasNext()){
            QBRosterEntry entry = it.next();
            userIds.add(entry.getUserId());
        }
        requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);

        QBChatService.getChatDialogs(null, requestBuilder1, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> qbDialogs, Bundle bundle) {
                QBUsers.getUsersByIDs(userIds, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        contactList.setVisibility(View.VISIBLE);
                        center_loader.setVisibility(View.GONE);
                        int diff;
                        if(qbDialogs.size()>dbDialog.size()){
                            diff= qbDialogs.size() - dbDialog.size();
                            System.out.println("________________THE length of qbDialogs = " + qbDialogs.size() +
                                    "_______________THE length of dbDialog = " + dbDialog.size() + "" +
                                    "_______________THE length of qbUsers = " + qbUsers.size() + "" +
                                    "_______________THE length of dbUsers = " + dbUser.size());

                            dbDialog.addAll(qbDialogs.subList(qbDialogs.size() - diff, qbDialogs.size()));
                        }
                        if(qbUsers.size()>dbUser.size()){
                            diff = qbUsers.size() - dbUser.size();
                            dbUser.addAll(qbUsers.subList(qbUsers.size() - diff,qbUsers.size()));
                        }
                        adapter.notifyDataSetChanged();
                        Helper.saveFriendListUsers(activity, dbUser, activity.currentUser.getId());
                        Helper.saveFriendListDialog(activity,qbDialogs,activity.currentUser.getId());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        e.printStackTrace();
                        err_txt.setVisibility(View.VISIBLE);
                        contactList.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void connected(XMPPConnection xmppConnection) {
        Log.d(TAG, "connected: THE CONNECTION IS ESTABLISHED---------------------------");
    }

    @Override
    public void authenticated(XMPPConnection xmppConnection, boolean b) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int i) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

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
        Log.e(TAG, "onInternetConnected: THE Internet is connected successfully,............");
        if(ExtraMSG.equals("error")){
            if(roster==null){
                QBLogin qbLogin = new QBLogin(Helper.getHelperInstance().getCurrentUser(activity)) {
                    @Override
                    public void OnLoginSuccessfull() {
                        ExtraMSG = "success";
                        activity.isUserLogin = true;
                        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,subscriptionListener);
                        UpdateContactList();
                    }

                    @Override
                    public void OnLoginError() {
                        this.error.printStackTrace();
                    }
                };
            }else{
                roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,subscriptionListener);
                UpdateContactList();
            }
        }
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
