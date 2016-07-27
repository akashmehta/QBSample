package com.example.abc.qbsample.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abc on 5/18/2016.
 */
public class AllContactListAdapter extends ArrayAdapter<QBUser> {
    Context mContext;
    ArrayList userList;
    LayoutInflater inflater;
    public AllContactListAdapter(Context context, int resource, ArrayList<QBUser> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.userList = objects;
        inflater =(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        QBUser opponent = getItem(position);
        Holder holder;
        if(row==null){
            row = inflater.inflate(R.layout.row_contact_list,null);
            holder =new Holder();
            holder.userImage = (ImageView) row.findViewById(R.id.user_image);
            holder.username = (TextView) row.findViewById(R.id.username);
            holder.midTitle = (TextView) row.findViewById(R.id.midTitle);
            row.findViewById(R.id.user_last_seen).setVisibility(View.GONE);
            row.findViewById(R.id.user_last_massage).setVisibility(View.GONE);
            row.setTag(holder);
        }else{
            holder = (Holder) row.getTag();
        }
        holder.username.setText(opponent.getLogin());
        UserCustomData data = Helper.getCustomObject(opponent.getCustomData());
        if (data != null && data.getProfile_picture_url() != null) {
            holder.midTitle.setVisibility(View.GONE);
            Picasso.with(mContext)
                    .load(data.getProfile_picture_url())
                    .error(R.drawable.ic_profile_image)
                    .placeholder(R.drawable.ic_profile_image)
                    .into(holder.userImage);
        } else {
            holder.midTitle.setVisibility(View.VISIBLE);
            int random = (int) (Math.random() * 10) % Helper.randomColorCodes.length;
            /*holder.userImage.setBackgroundColor(Color.parseColor(Helper.randomColorCodes[random]));*/
            holder.userImage.getBackground().setColorFilter(Color.parseColor(Helper.randomColorCodes[random]), PorterDuff.Mode.ADD);
            holder.midTitle.setText(opponent.getLogin().toUpperCase().substring(0, 2));
            holder.midTitle.setTextColor(Color.WHITE);
        }
        return row;
    }

    private class Holder {
        ImageView userImage;
        TextView lastMessage, username, lastseen, midTitle;
    }
}
