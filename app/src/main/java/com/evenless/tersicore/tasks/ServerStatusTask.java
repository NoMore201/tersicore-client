package com.evenless.tersicore.tasks;

import com.evenless.tersicore.ServerStatusTaskListener;

import java.net.URL;

public class ServerStatusTask extends GenericRequestTask {

    private ServerStatusTaskListener mListener;

    public ServerStatusTask(ServerStatusTaskListener listener,
                            URL serverUrl,
                            String token) {
        super(serverUrl, token);
        mListener = listener;
    }

    @Override
    protected void notifyResult(String result, Exception e) {
        if (result != null && result.contains("Tersicore")) {
            mListener.onServerStatusCheck(mUrl, true);
        } else {
            mListener.onServerStatusCheck(mUrl, false);
        }
    }
}
