package com.example.abc.qbsample.Listeners;

import com.example.abc.qbsample.Adapters.MessageListAdapter;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;

/**
 * Created by abc on 6/8/2016.
 */
public abstract class IFileQBEntityCallback implements QBEntityCallback<QBFile> {
    public int qbImagePosition;
    public MessageListAdapter.MessageHolder holder;
}
