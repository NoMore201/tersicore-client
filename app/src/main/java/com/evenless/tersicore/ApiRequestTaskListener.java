package com.evenless.tersicore;

public interface ApiRequestTaskListener {
    void onRequestComplete(String response);
    void onApiRequestError(Exception e);

    void onImgRequestComplete(String result, int id);
}
