package com.evenless.tersicore;

public interface ApiRequestTaskListener {
    void onRequestComplete(String response, Exception e);
    void onLatestRequestComplete(String response, Exception e);
}
