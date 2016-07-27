package com.example.abc.qbsample.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abc.qbsample.Bean.ChatContent;
import com.example.abc.qbsample.CoreActivity.ChatActivity;
import com.example.abc.qbsample.Listeners.IFileQBEntityCallback;
import com.example.abc.qbsample.Listeners.IPicassoImageCallback;
import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.TimeUtils;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by abc on 5/17/2016.
 */
public class MessageListAdapter extends ArrayAdapter<ChatContent> implements StickyListHeadersAdapter, View.OnLongClickListener {
    Activity activity;
    ArrayList<ChatContent> chatList;
    LayoutInflater inflater;
    QBUser currentUser, selectedUser;
    Bitmap opponentImage;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_MESSAGE = 0;
    int random = (int) (Math.random() * 10) % Helper.randomColorCodes.length;
    public static final String TAG = MessageListAdapter.class.getSimpleName();

    public MessageListAdapter(final Activity activity, int resource, ArrayList<ChatContent> objects, Bitmap opponentImage, QBUser... currentUser) {
        super(activity, resource, objects);
        this.activity = activity;
        this.chatList = objects;
        this.currentUser = currentUser[0];
        this.selectedUser = currentUser[1];
        this.inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
        this.opponentImage = opponentImage;


    }

    @Override
    public int getItemViewType(int position) {
        ChatContent content = getItem(position);
        if (content.getImage() != null || (content.getChatMessage().getAttachments() != null && content.getChatMessage().getAttachments().size() > 0))
            return TYPE_IMAGE;
        else return TYPE_MESSAGE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        HeaderViewHolder header_holder;
        if (row == null) {
            row = inflater.inflate(R.layout.row_chat_headerview, null);
            header_holder = new HeaderViewHolder();
            header_holder.headerTitle = (TextView) row.findViewById(R.id.chat_header_title);
            row.setTag(header_holder);
        } else {
            header_holder = (HeaderViewHolder) row.getTag();
        }
        QBChatMessage chatMessage = chatList.get(position).getChatMessage();
        header_holder.headerTitle.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));
        return row;
    }

    private void setOpponentImage(final MessageHolder holder, QBChatMessage message) {
        if (opponentImage != null) {
            holder.midTitle.setVisibility(View.GONE);
            holder.opponentImageview.setImageBitmap(opponentImage);
        } else {
            UserCustomData customData = Helper.getCustomObject(selectedUser.getCustomData());
            if (customData != null && customData.getProfile_picture_url() != null) {

                Picasso.with(activity)
                        .load(customData.getProfile_picture_url())
                        .error(R.drawable.ic_profile_image)
                        .placeholder(R.drawable.ic_profile_image)
                        .into(holder.opponentImageview, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.midTitle.setVisibility(View.GONE);
                                opponentImage = ((BitmapDrawable) holder.opponentImageview.getDrawable()).getBitmap();
                                Helper.saveUserImage(opponentImage, selectedUser.getLogin());
                            }

                            @Override
                            public void onError() {
                                holder.midTitle.setVisibility(View.VISIBLE);
                                holder.opponentImageview.getBackground().setColorFilter(Color.parseColor(Helper.randomColorCodes[random]), PorterDuff.Mode.ADD);
                                holder.midTitle.setText(selectedUser.getLogin().toUpperCase().substring(0, 2));
                                holder.midTitle.setTextColor(Color.WHITE);
                            }
                        });
            } else {
                holder.midTitle.setVisibility(View.VISIBLE);
                holder.opponentImageview.getBackground().setColorFilter(Color.parseColor(Helper.randomColorCodes[random]), PorterDuff.Mode.ADD);
                holder.midTitle.setText(selectedUser.getLogin().toUpperCase().substring(0, 2));
                holder.midTitle.setTextColor(Color.WHITE);
            }
        }
        HandleUi(holder, message);
    }

    private MessageHolder getHolder(View row) {
        MessageHolder holder = new MessageHolder();
        holder.chatComponents = (LinearLayout) row.findViewById(R.id.chatMessageLayout);
        holder.messageBg = (RelativeLayout) row.findViewById(R.id.messageBg);
        holder.time = (TextView) row.findViewById(R.id.chatBoxTime);
        holder.midTitle = (TextView) row.findViewById(R.id.midTitle);
        holder.opponentImageview = (ImageView) row.findViewById(R.id.user_image);
        holder.userImageLayout = (RelativeLayout) row.findViewById(R.id.user_image_layout);
        holder.llMessage = (LinearLayout) row.findViewById(R.id.llMessage);
        return holder;
    }

    private View getMessageView(int position, View convertView) {
        View row = convertView;
        final MessageHolder holder;
        QBChatMessage message = getItem(position).getChatMessage();
        if (row == null) {
            row = inflater.inflate(R.layout.row_chat_message, null);
            holder = getHolder(row);
            holder.chatMessage = (EmojiconTextView) row.findViewById(R.id.chatTextMessage);
            holder.chatMessage.setOnLongClickListener(this);
            holder.chatMessage.setUseSystemDefault(true);
            row.setTag(holder);
        } else {
            holder = (MessageHolder) row.getTag();
        }
        setOpponentImage(holder, message);
        holder.chatMessage.setText(message.getBody());
        holder.time.setText(TimeUtils.getTime(message.getDateSent() * 1000));
        return row;

    }

    private View getImageView(final int position, View convertView) {
        ChatContent content = getItem(position);
        File image = content.getImage();
        final QBChatMessage message = content.getChatMessage();
        View row = convertView;
        final MessageHolder holder;
        if (row == null) {
            row = inflater.inflate(R.layout.row_chat_image, null);
            holder = getHolder(row);
            holder.imageMessage = (ImageView) row.findViewById(R.id.chatImageMessage);
            holder.imageLoader = (ProgressBar) row.findViewById(R.id.imageLoader);
            row.setTag(holder);
        } else {
            holder = (MessageHolder) row.getTag();
        }
        setOpponentImage(holder, message);

        /*if (getItem(position).getImageBitmap() != null) {
            holder.imageMessage.setImageBitmap(getItem(position).getImageBitmap());
        } else*/ if (image != null) {

            if (getItem(position).getStartLoader()) {
                Log.w(TAG, "getImageView: ______ Image Absolute Path is : " + image.getAbsolutePath());
                //Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());

                holder.imageMessage.setImageURI(Uri.fromFile(image));
                //getItem(position).setImageBitmap(myBitmap);

                IFileQBEntityCallback callback = new IFileQBEntityCallback() {

                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        message.setProperty("save_to_history", "1");
                        QBAttachment attachment = new QBAttachment("photo");
                        attachment.setId(qbFile.getId().toString());
                        attachment.setUrl(qbFile.getPrivateUrl());
                        attachment.setName(Helper.getAttachmentFileName(qbFile.getId().toString()));
                        message.addAttachment(attachment);
                        Helper.copyUserImage(
                                getItem(qbImagePosition).getImage()
                                , Helper.getAttachmentFileName(qbFile.getId().toString())
                        );
                        holder.imageLoader.setVisibility(View.GONE);

                        try {
                            ((ChatActivity) activity).privateChat.sendMessage(message);
                            notifyDataSetChanged();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        e.printStackTrace();
                    }
                };

                getItem(position).setStartLoader(false);
                callback.qbImagePosition = position;
                callback.holder = holder;
                holder.imageLoader.setVisibility(View.VISIBLE);
                QBContent.uploadFileTask(image, false, null, callback);
            }

        } else {
            if (content.getChatMessage().getAttachments() != null && content.getChatMessage().getAttachments().size() > 0) {
                Iterator<QBAttachment> itr = content.getChatMessage().getAttachments().iterator();
                final QBAttachment attachment = itr.next();
                Log.w(TAG, "getImageView: __________________________ ID Of attachment is : " + attachment.getId());
                File f = Helper.checkUserImage(Helper.getAttachmentFileName(attachment.getId().toString()));
                if (f != null) {
                    holder.imageMessage.setImageURI(Uri.fromFile(f));
                } else {
                        holder.imageLoader.setVisibility(View.VISIBLE);
                        Log.w(TAG, "getImageView: ________________________ attachment URL is : " + attachment.getUrl());
                        Picasso
                                .with(activity)
                                .load(attachment.getUrl())
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.image_not_found)
                                .into(holder.imageMessage,new IPicassoImageCallback() {
                                    @Override
                                    public void onSuccess() {
                                            holder.imageLoader.setVisibility(View.GONE);
                                            Bitmap bitmap = ((BitmapDrawable)holder.imageMessage.getDrawable()).getBitmap();
                                            notifyDataSetChanged();
                                            //getItem(getQbImagePosition()).setStartLoader(false);
                                            Helper.copyUserImage(bitmap, Helper.getAttachmentFileName(getAttachmentId()));

                                    }

                                    @Override
                                    public void onError() {

                                    }

                                    @Override
                                    public int getQbImagePosition() {
                                        return position;
                                    }

                                    @Override
                                    public String getAttachmentId() {
                                        return attachment.getId();
                                    }

                                });
                    //}
                }
            }
        }

        holder.time.setText(TimeUtils.getTime(message.getDateSent() * 1000));
        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_IMAGE) {
            Log.e(TAG, "getView: _________________________   TYPE IS : TYPE_IMAGE");
            return getImageView(position, convertView);
        } else {
            Log.e(TAG, "getView: _________________________   TYPE IS : TYPE_MESSAGE");
            return getMessageView(position, convertView);
        }
    }

    private void HandleUi(MessageHolder holder, QBChatMessage message) {
        if (String.valueOf(currentUser.getId()).equals(String.valueOf(message.getSenderId()))) {
            holder.chatComponents.setGravity(Gravity.RIGHT);
            holder.llMessage.setGravity(Gravity.RIGHT);
            holder.messageBg.setBackground(activity.getResources().getDrawable(R.drawable.right_box));
            holder.userImageLayout.setVisibility(View.GONE);
            holder.messageBg.setGravity(Gravity.RIGHT);
            holder.time.setGravity(Gravity.RIGHT);
            if (holder.chatMessage != null) {
                holder.chatMessage.setTextColor(Color.WHITE);
            }

        } else {
            holder.chatComponents.setGravity(Gravity.LEFT);
            holder.llMessage.setGravity(Gravity.LEFT);
            holder.messageBg.setBackground(activity.getResources().getDrawable(R.drawable.left_box));
            holder.userImageLayout.setVisibility(View.VISIBLE);
            holder.messageBg.setGravity(Gravity.LEFT);
            holder.time.setGravity(Gravity.LEFT);
            if (holder.chatMessage != null) {
                holder.chatMessage.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position).getChatMessage();
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.chatTextMessage:
                TextView messageView = (TextView) v;
                ClipboardManager manager = (ClipboardManager) activity.getSystemService(activity.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("messageText", messageView.getText().toString());
                manager.setPrimaryClip(clipData);
                Toast.makeText(activity, "Text Copied", Toast.LENGTH_SHORT).show();
                break;
        }

        return false;
    }


    public class MessageHolder {
        LinearLayout chatComponents, llMessage;
        RelativeLayout messageBg, userImageLayout;
        EmojiconTextView chatMessage;
        public TextView time, midTitle;
        public ImageView opponentImageview, imageMessage;
        ProgressBar imageLoader;
    }

    private class HeaderViewHolder {
        TextView headerTitle;
    }

}
