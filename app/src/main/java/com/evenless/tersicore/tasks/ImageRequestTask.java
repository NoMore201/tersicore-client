package com.evenless.tersicore.tasks;

import com.evenless.tersicore.ApiRequestTaskListener;

import java.net.URL;

public class ImageRequestTask extends GenericRequestTask {
    private ApiRequestTaskListener mListener;
    private int mId;

    public ImageRequestTask(ApiRequestTaskListener listener,
                            int id,
                            URL url) {
        super(url);
        mListener = listener;
        mId = id;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        mListener.onImgRequestComplete(result, mId);
    }
}
