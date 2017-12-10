package com.evenless.tersicore;

public interface ImageRequestTaskListener {
    void onImgRequestComplete(String result, int state, String key, Exception e);
}
