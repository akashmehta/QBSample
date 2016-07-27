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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.abc.qbsample.Adapters.RecentContactListAdapter;
import com.example.abc.qbsample.CoreActivity.ChatActivity;
import com.example.abc.qbsample.CoreActivity.ContactListActivity;
import com.example.abc.qbsample.Listeners.INetworkListener;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.NetworkUtils;
import com.example.abc.qbsample.Utils.QBLogin;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abc on 5/13/2016.
 */
public class RecentContactListFragment extends Fragment implements INetworkListener {
    ListView contactList;
    RecentContactListAdapter adapter;
    ProgressBar center_loader;
    ContactListActivity activity;
    TextView err_txt;
    private QBRequestGetBuilder requestBuilder;
    ArrayList<QBDialog> dbDialogs;
    SparseArray dbUserList;
    private boolean internetAvailable = false;
    private BroadcastReceiver networkReceiver = getReceiver();
    private String ExtraMSG ;
    private boolean startFragment = false;
    public static final String TAG = RecentContactListFragment.class.getSimpleName();
    private QBPrivateChatManager privateChatManager;
    private QBMessageListener privateChatManagerListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ContactListActivity) activity;
        Log.d(TAG, "onAttach() called with: " + "currentActivity = [" + activity + "]");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called with: " + "");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called with: " + "_____startFragment : "+startFragment);
        if(adapter!=null && !startFragment){
            dbDialogs.clear();
            dbUserList.clear();

            Log.e(TAG, "onResume: size of recent dialog list Is ::: " + Helper.getRecentDialogs(activity, activity.currentUser.getId()));
            Log.e(TAG, "onResume: size of recent user list Is ::: " + Helper.getRecentUserList(activity, activity.currentUser.getId()));
            dbDialogs .addAll(Helper.getRecentDialogs(activity, activity.currentUser.getId())) ;
            SparseArray tempUserList = Helper.getRecentUserList(activity,activity.currentUser.getId());
            for(int i =0 ;i<tempUserList.size();i++){
                dbUserList.put(tempUserList.keyAt(i),tempUserList.valueAt(i));
            }

            if(dbDialogs.size()==0){
                contactList.setVisibility(View.GONE);
                center_loader.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called with: " + "");
    }

    @Override
    public void onStop() {
        super.onStop();
        startFragment =false;
        Log.d(TAG, "onStop() called with: " + "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView() called with: " + "");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called with: " + "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        startFragment = true;
        Log.d(TAG, "onCreateView() called with: " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");

        View v = inflater.inflate(R.layout.fragment_contects, null);
        contactList = (ListView) v.findViewById(R.id.contact_list);
        center_loader = (ProgressBar) v.findViewById(R.id.center_loader);
        err_txt = (TextView) v.findViewById(R.id.error_txt);
        this.ExtraMSG = activity.ExtraMSG;
        requestBuilder = new QBRequestGetBuilder();

        dbDialogs = Helper.getRecentDialogs(activity, activity.currentUser.getId());
        dbUserList = Helper.getRecentUserList(activity, activity.currentUser.getId());
        if(dbDialogs.size()==0){
            contactList.setVisibility(View.GONE);
            center_loader.setVisibility(View.VISIBLE);
        }
        adapter = new RecentContactListAdapter(activity, R.layout.fragment_contects, dbDialogs, dbUserList, activity.currentUser);
        contactList.setAdapter(adapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(activity, ChatActivity.class);
                it.putExtra("currentUser", activity.currentUser);
                it.putExtra("currentChatDialog", dbDialogs.get(position));
                QBUser user = null;
                for (Integer uid : dbDialogs.get(position).getOccupants()) {
                    if (uid != activity.currentUser.getId()) {
                        if (dbUserList.get(uid) != null) {
                            user = (QBUser) dbUserList.get(uid);
                        }
                    }
                }
                System.out.println("___________________Intent selected User is : " + user);
                it.putExtra("selectedUser", user);
                if(ExtraMSG!=null){
                    it.putExtra("ExtraMSG",ExtraMSG);
                }
                activity.startActivity(it);
            }
        });
        if(ExtraMSG!=null && !ExtraMSG.equals("error")){
            getChatDialogs();
        }
        registerReceiver();
        return v;
    }
    private void getChatDialogs(){
        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> qbDialogs, Bundle bundle) {

                List<Integer> opponentList = new ArrayList();
                for (QBDialog dialog :
                        qbDialogs) {
                    for (Integer userId : dialog.getOccupants()) {
                        if (userId != activity.currentUser.getId()) {
                            opponentList.add(userId);
                        }
                    }
                }
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(opponentList.size(), 1);
                QBUsers.getUsersByIDs(opponentList, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(final ArrayList<QBUser> qbUsers, Bundle bundle) {
                        contactList.setVisibility(View.VISIBLE);
                        center_loader.setVisibility(View.GONE);

                        //TODO Saving recent chat to mobile database
                        if (dbDialogs.size() < qbDialogs.size()) {
                            int diff = qbDialogs.size() - dbDialogs.size();
                            int userdiff = qbUsers.size() - dbUserList.size();

                            dbDialogs.addAll(qbDialogs.subList(qbDialogs.size() - diff, qbDialogs.size()));
                            for (int i = qbUsers.size() - userdiff; i < qbUsers.size(); i++) {
                                if (!String.valueOf(qbUsers.get(i).getId()).equals(String.valueOf(activity.currentUser.getId()))) {
                                    dbUserList.put(qbUsers.get(i).getId(), qbUsers.get(i));
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Helper.saveRecentMessageUser(activity, dbUserList, activity.currentUser.getId());
                            Helper.saveRecentMessageDialog(activity,qbDialogs,activity.currentUser.getId());
                        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called with: " + "");
        unregisterReceiver();
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
            if(activity.isUserLogin){
                getChatDialogs();
            }else{
                QBLogin login = new QBLogin(Helper.getHelperInstance().getCurrentUser(activity)) {
                    @Override
                    public void OnLoginSuccessfull() {
                        ExtraMSG = "success";
                        activity.isUserLogin = true;
                        getChatDialogs();
                    }

                    @Override
                    public void OnLoginError() {
                        this.error.printStackTrace();
                    }
                };

            }
        }

    }

    @Override
    public void onInternetDisconnected() {

    }
}
