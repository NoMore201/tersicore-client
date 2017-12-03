package com.evenless.tersicore;

import android.os.AsyncTask;

import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.tasks.ApiRequestTask;
import com.evenless.tersicore.tasks.CoverRetrieveTask;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class TaskHandler {
    private static final String API_BASE_URL = "http://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "b6570587abc105bc286cf227cabbba50";
    private static final String API_SHARED_SECRET = "ca91306bbff831733d675dfc6e556b77";

    private static String getApiUrl(String relativeUrl){
        return API_BASE_URL + relativeUrl;   }

    public static void getTracks(ApiRequestTaskListener listener, String server) throws MalformedURLException {
        URL url = new URL("http://" + server + "/tracks");
        ApiRequestTask task = new ApiRequestTask(listener);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
    }

    public static void getImages(ApiRequestTaskListener listener, String query, int id) throws MalformedURLException, UnsupportedEncodingException {
        URL url = new URL(getApiUrl("?method=artist.getinfo&artist="+ URLEncoder.encode(query,"utf-8") +
                "&api_key=" + API_KEY + "&format=json"));
        ApiRequestTask task = new ApiRequestTask(listener, id);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public static void getCoversWeb(ApiRequestTaskListener listener, String ar, String al, int id) throws MalformedURLException, UnsupportedEncodingException {
        URL url = new URL(getApiUrl("?method=album.getinfo&artist="+ URLEncoder.encode(ar,"utf-8") +
                        "&album="+ URLEncoder.encode(al,"utf-8") + "&api_key=" + API_KEY + "&format=json"));
        ApiRequestTask task = new ApiRequestTask(listener, id);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public static void getCover(CoverRetrieveTaskListener listener,
                                Track track, String server, int id)
    {
        String url = "http://" + server + "/stream/" + track.resources.get(0).uuid;
        CoverRetrieveTask task = new CoverRetrieveTask(listener, track, id);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
    }
}
