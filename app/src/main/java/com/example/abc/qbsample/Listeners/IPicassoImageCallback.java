package com.example.abc.qbsample.Listeners;

        import com.example.abc.qbsample.Adapters.MessageListAdapter;
        import com.quickblox.chat.model.QBAttachment;
        import com.quickblox.core.QBEntityCallback;
        import com.squareup.picasso.Callback;
        import com.squareup.picasso.Target;

        import java.io.InputStream;

/**
 * Created by abc on 6/8/2016.
 */
public abstract class IPicassoImageCallback implements Callback{
    public abstract int getQbImagePosition();
    public abstract String getAttachmentId();
    /*public abstract MessageListAdapter.MessageHolder getMessageHolder();*/
}
