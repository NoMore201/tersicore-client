package com.evenless.tersicore.interfaces;

public interface ImageRequestTaskListener {
    void onImgRequestComplete(String result, int state, String key, Exception e);
}
