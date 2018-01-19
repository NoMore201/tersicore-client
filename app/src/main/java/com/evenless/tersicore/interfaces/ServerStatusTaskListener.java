package com.evenless.tersicore.interfaces;


import java.net.URL;

public interface ServerStatusTaskListener {
    void onServerStatusCheck(URL originalUrl, boolean running);
}
