package com.evenless.tersicore.tasks;

import com.evenless.tersicore.ImageRequestTaskListener;

import java.net.URL;

public class ImageRequestTask extends GenericRequestTask {
    private ImageRequestTaskListener mListener;
    private int mId;
    private String mQuery;

    public ImageRequestTask(ImageRequestTaskListener listener,
                            int id,
                            String query,
                            String token,
                            URL url) {
        super(url, token);
        mListener = listener;
        mId = id;
        mQuery = query;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        mListener.onImgRequestComplete(result, mId, mQuery, e);
    }
}
