package com.evenless.tersicore;


import java.net.URL;

public interface ServerStatusTaskListener {
    void onServerStatusCheck(URL originalUrl, boolean running);
}
