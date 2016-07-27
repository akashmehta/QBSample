package com.example.abc.qbsample.CoreActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.abc.qbsample.Adapters.ContactPagerAdapter;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.gcm.GooglePlayServicesHelper;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.quickblox.users.model.QBUser;

public class ContactListActivity extends BaseActivity implements View.OnClickListener{
    ViewPager contactList;
    ContactPagerAdapter adapter ;
    public QBUser currentUser;
    PagerSlidingTabStrip tabStrip;
    public String ExtraMSG;
    public Boolean isUserLogin = false;
    GooglePlayServicesHelper googlePlayServicesHelper;
    private FragmentManager fm;
    public static final String TAG = ContactListActivity.class.getSimpleName();
    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Helper.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Receiving event " + Helper.ACTION_NEW_GCM_EVENT + " with data: " + message);
        }
    };
    private void registerReceiver() {
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(Helper.ACTION_NEW_GCM_EVENT));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.search_user:
                FragmentTransaction ft = fm.beginTransaction();
                //ft.add();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initConfig();
        initSessionCallback();
    }
    FloatingActionMenu menuLabelsRight;
    FloatingActionButton btnSearchUser;
    private static final String PREF_GCM_REG_ID = "registration_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        menuLabelsRight = (FloatingActionMenu) findViewById(R.id.menu_labels_right);
        menuLabelsRight.hideMenuButton(false);
        int delay = 400;
        Handler mUiHandler = new Handler();
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                menuLabelsRight.showMenuButton(true);
            }
        }, delay);

        btnSearchUser = (FloatingActionButton) findViewById(R.id.search_user);
        btnSearchUser.setOnClickListener(this);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            googlePlayServicesHelper.registerForGcm(Helper.SENDER_ID);
        }
        registerReceiver();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentUser = (QBUser) getIntent().getSerializableExtra("CurrentUser");
        ExtraMSG = getIntent().getExtras().getString("ExtraMSG");
        if(ExtraMSG!=null && ExtraMSG.equals("error")){
            Toast.makeText(ContactListActivity.this, "cant login", Toast.LENGTH_SHORT).show();
            isUserLogin = false;
        }else{
            Toast.makeText(ContactListActivity.this, "login succesfull", Toast.LENGTH_SHORT).show();
            isUserLogin = false;
        }
        contactList =(ViewPager) findViewById(R.id.contact_pager);
        tabStrip = (PagerSlidingTabStrip) findViewById(R.id.slidingTab);
        fm = getSupportFragmentManager();
        adapter = new ContactPagerAdapter(fm, tabStrip, contactList);
        contactList.setAdapter(adapter);

        tabStrip.setViewPager(contactList);
        tabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabStrip.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    Activity getActivity() {
        return ContactListActivity.this;
    }

    @Override
    QBUser currentUser() {
        return currentUser;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_list,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
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
        /*removeConfig();
        removeSessionCallback();*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*Intent service = new Intent(this, GetCallService.class);
        startService(service);*/
    }
}
