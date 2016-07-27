package com.example.abc.qbsample.Bean;


import android.graphics.Bitmap;

import com.quickblox.chat.model.QBChatMessage;

import java.io.File;

/**
 * Created by abc on 6/8/2016.
 */
public class ChatContent {
    private QBChatMessage chatMessage;

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public QBChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(QBChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    private File image;

    public Boolean getStartLoader() {
        return startLoader;
    }

    public void setStartLoader(Boolean startLoader) {
        this.startLoader = startLoader;
    }

    private Boolean startLoader = false;
    public ChatContent(QBChatMessage chatMessage,File image){
        this.chatMessage = chatMessage;
        this.image = image;
    }
}
