package com.evenless.tersicore;

import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.tasks.ApiRequestTask;
import com.evenless.tersicore.tasks.CoverRetrieveTask;
import com.evenless.tersicore.tasks.ImageRequestTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class TaskHandler {
    private static final String TAG = "TaskHandler";
    private static final String API_BASE_URL = "https://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "b6570587abc105bc286cf227cabbba50";
    private static final String API_SHARED_SECRET = "ca91306bbff831733d675dfc6e556b77";

    private static String getApiUrl(String relativeUrl){
        return API_BASE_URL + relativeUrl;   }

    public static void getTracks(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL("https://" + server + "/tracks");
        ApiRequestTask task = new ApiRequestTask(listener, url);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getArtistImageFromApi(ApiRequestTaskListener listener,
                                 String query,
                                 int id) throws
            MalformedURLException,
            UnsupportedEncodingException
    {
        URL url = new URL(buildArtistUrl(query));
        ApiRequestTask task = new ImageRequestTask(listener, id, url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getAlbumImageFromApi(ApiRequestTaskListener listener,
                                    String artist,
                                    String album,
                                    int id) throws
            MalformedURLException,
            UnsupportedEncodingException
    {
        URL url = new URL(buildAlbumUrl(artist, album));
        ApiRequestTask task = new ImageRequestTask(listener, id, url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getCover(CoverRetrieveTaskListener listener,
                                Track track, String server, int id)
    {
        String url = "https://" + server + "/stream/" + track.resources.get(0).uuid;
        CoverRetrieveTask task = new CoverRetrieveTask(listener, track, url, id);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static boolean checkServerIsRunning(String serverUrl) {
        HttpsURLConnection connection = null;
        String result = null;
        StringBuilder sb;
        try {
            URL url = new URL("https://" + serverUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            result = sb.toString();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result != null && result.contains("Tersicore");
    }

    private static String buildArtistUrl(String artist) throws UnsupportedEncodingException {
        return API_BASE_URL +
                "?method=artist.getinfo&artist=" +
                URLEncoder.encode(artist, "utf-8") +
                "&api_key=" + API_KEY + "&format=json";
    }

    private static String buildAlbumUrl(String artist, String album) throws UnsupportedEncodingException {
        return API_BASE_URL +
                "?method=album.getinfo&artist=" +
                URLEncoder.encode(artist, "utf-8") +
                "&album=" + URLEncoder.encode(album, "utf-8") +
                "&api_key=" + API_KEY + "&format=json";
    }

}
