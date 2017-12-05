package com.evenless.tersicore.tasks;

import com.evenless.tersicore.ApiRequestTaskListener;

import java.net.URL;

public class ApiRequestTask extends GenericRequestTask {
    private ApiRequestTaskListener mListener;

    public ApiRequestTask(ApiRequestTaskListener listener,
                          URL url) {
        super(url);
        mListener = listener;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        super.notifyResult(result, e);
        if (result == null) {
            mListener.onApiRequestError(e);
        } else {
            mListener.onRequestComplete(result);
        }
    }
}
