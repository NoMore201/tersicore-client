package com.evenless.tersicore.tasks;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.TaskHandler;

import java.net.URL;

public class ApiRequestTask extends GenericRequestTask {
    private ApiRequestTaskListener mListener;
    private int mState;

    public ApiRequestTask(ApiRequestTaskListener listener,
                          URL url, int state) {
        super(url);
        mListener = listener;
        mState=state;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        super.notifyResult(result, e);
        if(mState== TaskHandler.ALL_TRACKS)
            mListener.onRequestComplete(result, e);
        else
            mListener.onLatestRequestComplete(result,e);
    }
}
