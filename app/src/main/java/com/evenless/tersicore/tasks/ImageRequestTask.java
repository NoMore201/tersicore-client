package com.evenless.tersicore.tasks;

import com.evenless.tersicore.ApiRequestTaskListener;

import java.net.URL;

public class ImageRequestTask extends ApiRequestTask {
    private int mId;

    public ImageRequestTask(ApiRequestTaskListener listener,
                            int id,
                            URL url) {
        super(listener, url);
        mId = id;
    }

    @Override
    protected void notifyResult(String result) {
        super.mListener.onImgRequestComplete(result, mId);
    }
}
