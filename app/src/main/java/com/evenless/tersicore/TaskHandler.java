package com.evenless.tersicore;

import android.util.Log;

import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.tasks.ApiRequestTask;
import com.evenless.tersicore.tasks.CoverRetrieveTask;

import java.net.MalformedURLException;
import java.net.URL;

public class TaskHandler {

    public static void getTracks(ApiRequestTaskListener listener, String server) throws MalformedURLException {
        URL url = new URL("http://" + server + "/tracks");
        ApiRequestTask task = new ApiRequestTask(listener);
        task.execute(url);
    }

    public static void getCover(CoverRetrieveTaskListener listener,
                                Track track, String server)
    {
        String url = "http://" + server + "/stream/" + track.resources[0].uuid;
        CoverRetrieveTask task = new CoverRetrieveTask(listener, track);
        task.execute(url);
    }
}
