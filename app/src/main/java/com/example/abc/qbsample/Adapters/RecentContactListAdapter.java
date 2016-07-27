package com.example.abc.qbsample.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.TimeUtils;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by abc on 5/13/2016.
 */
public class RecentContactListAdapter extends ArrayAdapter<QBDialog> {
    Context mContext;
    ArrayList<QBDialog> contact_data;
    SparseArray<QBUser> userList;
    LayoutInflater inflater;
    QBUser currentUser;
    public static final String TAG = "RecentContactList";

    public RecentContactListAdapter(Context mContext, int resourceId, ArrayList<QBDialog> arrayList, SparseArray userlist, QBUser currentUser) {
        super(mContext, resourceId, arrayList);
        this.mContext = mContext;
        this.contact_data = arrayList;
        this.userList = userlist;
        this.currentUser = currentUser;

        inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

    }

    Holder holder;
    QBDialog list_item;
    QBUser user;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        list_item = getItem(position);

        if (row == null) {
            row = inflater.inflate(R.layout.row_contact_list, null);
            holder = new Holder();
            holder.userImage = (ImageView) row.findViewById(R.id.user_image);
            holder.username = (TextView) row.findViewById(R.id.username);
            holder.lastMessage = (TextView) row.findViewById(R.id.user_last_massage);
            holder.lastseen = (TextView) row.findViewById(R.id.user_last_seen);
            holder.midTitle = (TextView) row.findViewById(R.id.midTitle);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        user = getUser(list_item);
        if (Helper.getUserImage(user.getLogin()) != null) {
            holder.midTitle.setVisibility(View.GONE);
            holder.userImage.setImageBitmap(Helper.getUserImage(user.getLogin()));
        } else {
            UserCustomData customData = Helper.getCustomObject(user.getCustomData());
            if (customData != null && customData.getProfile_picture_url() != null) {
                holder.midTitle.setVisibility(View.GONE);
                Picasso.with(mContext)
                        .load(customData.getProfile_picture_url())
                        .error(R.drawable.ic_profile_image)
                        .placeholder(R.drawable.ic_profile_image)
                        .into(holder.userImage);
            } else {
                holder.midTitle.setVisibility(View.VISIBLE);
                int random = (int) (Math.random() * 10) % Helper.randomColorCodes.length;
                holder.userImage.getBackground().setColorFilter(Color.parseColor(Helper.randomColorCodes[random]), PorterDuff.Mode.ADD);
                holder.midTitle.setText(user.getLogin().toUpperCase().substring(0, 2));
                holder.midTitle.setTextColor(Color.WHITE);
            }

        }


        if (user != null) {
            holder.username.setText(user.getLogin());
        }
        holder.lastMessage.setText(list_item.getLastMessage());
        holder.lastseen.setText(getTime(list_item.getLastMessageDateSent() * 1000));

        return row;
    }
    private String getTime(long time){
        Date currentDate = new Date();
        Date srcDate = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String srcDateStr = format.format(new Date(time));
        String currentDateStr = format.format(currentDate);

        //Log.w(TAG, "Current Date is : : : "+currentDateStr+"\n Source Date is : : : "+srcDateStr );
        if(currentDate.getYear()==srcDate.getYear()){
            //System.out.println("current Date year :: "+currentDate.getYear()+"\nsource date year :: "+srcDate.getYear());
            if(currentDate.getMonth()==srcDate.getMonth()){
                //System.out.println("current Date Month :: "+currentDate.getMonth()+"\nsource date Month :: "+srcDate.getMonth());
                if(currentDate.getDay()==srcDate.getDay()){
                    //System.out.println("current Date Day :: "+currentDate.getDay()+"\nsource date Day :: "+srcDate.getDay());
                    return TimeUtils.getTime(time);
                }
                else if(currentDate.getDay()-srcDate.getDay()==1){
                    return "Yesterday";
                }else{
                    return srcDateStr;
                }
            }
        }
        return srcDateStr;
    }
    private QBUser getUser(QBDialog dialog) {
        QBUser user = null;
        for (Integer userId : dialog.getOccupants()) {
            if (userId != currentUser.getId()) {
                user = (QBUser) userList.get(userId);
                if (user != null) {
                    return user;
                }
            }
        }
        return user;
    }

    private class Holder {
        ImageView userImage;
        TextView lastMessage, username, lastseen, midTitle;
    }
}
