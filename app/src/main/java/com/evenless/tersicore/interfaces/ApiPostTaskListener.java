package com.evenless.tersicore.interfaces;

public interface ApiPostTaskListener {
    void onRequestComplete(int requestType, Exception e, String result);
}
