package com.evenless.tersicore;

import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.tasks.ApiRequestTask;
import com.evenless.tersicore.tasks.CoverRetrieveTask;

import java.net.MalformedURLException;
import java.net.URL;

public class TaskHandler {

    private static String TRACKS_URL = "http://casa.izzo.li:8888/tracks";
    private static String STREAM_URL = "http://casa.izzo.li:8888/stream";

    public static void getTracks(ApiRequestTaskListener listener) throws MalformedURLException {
        URL url = new URL(TRACKS_URL);
        ApiRequestTask task = new ApiRequestTask(listener);
        task.execute(url);
    }

    public static void getCover(CoverRetrieveTaskListener listener,
                                Track track)
    {
        String url = STREAM_URL + "/" + track.resources[0].uuid;
        CoverRetrieveTask task = new CoverRetrieveTask(listener, track);
        task.execute(url);
    }
}
