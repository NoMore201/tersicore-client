package com.evenless.tersicore;

import android.os.AsyncTask;

import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.tasks.ApiRequestTask;
import com.evenless.tersicore.tasks.CoverDownloadTask;
import com.evenless.tersicore.tasks.CoverRetrieveTask;
import com.evenless.tersicore.tasks.ImageRequestTask;
import com.evenless.tersicore.tasks.ServerStatusTask;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class TaskHandler {
    private static final String TAG = "TaskHandler";
    private static final String API_BASE_URL = "https://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "b6570587abc105bc286cf227cabbba50";
    private static final String API_SHARED_SECRET = "ca91306bbff831733d675dfc6e556b77";
    private static final String TERSICORE_TOKEN = "0651863bf5d902262b17c4621ec340544ff016752543d99a92d7d22872d8a455";
    public static final int ALL_TRACKS = 0;
    public static final int TRACKS_LATEST = 1;
    public static final int PLAYLISTS = 2;
    public static final int PLAYLIST_SINGLE = 3;
    public static final int SUGGESTIONS = 4;

    public static void getTracks(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/tracks");
        ApiRequestTask task = new ApiRequestTask(listener, url, TERSICORE_TOKEN, ALL_TRACKS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getLatestTracks(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/tracks/latest");
        ApiRequestTask task = new ApiRequestTask(listener, url, TERSICORE_TOKEN, TRACKS_LATEST);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getPlaylists(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/playlists");
        ApiRequestTask task = new ApiRequestTask(listener, url, TERSICORE_TOKEN, PLAYLISTS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getPlaylist(ApiRequestTaskListener listener,
                                    String server, String id) throws MalformedURLException
    {
        URL url = new URL(server + "/playlists/" + id);
        ApiRequestTask task = new ApiRequestTask(listener, url, TERSICORE_TOKEN, PLAYLIST_SINGLE);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getSuggestions(ApiRequestTaskListener listener,
                                    String server) throws MalformedURLException
    {
        URL url = new URL(server + "/suggestions");
        ApiRequestTask task = new ApiRequestTask(listener, url, TERSICORE_TOKEN, SUGGESTIONS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getArtistImageFromApi(ImageRequestTaskListener listener,
                                 String query,
                                 int id) throws
            MalformedURLException,
            UnsupportedEncodingException
    {
        URL url = new URL(buildArtistUrl(query));
        ImageRequestTask task = new ImageRequestTask(listener, id, query, TERSICORE_TOKEN, url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getAlbumImageFromApi(ImageRequestTaskListener listener,
                                    String artist,
                                    String album,
                                    int id) throws
            MalformedURLException,
            UnsupportedEncodingException
    {
        URL url = new URL(buildAlbumUrl(artist, album));
        ImageRequestTask task = new ImageRequestTask(listener,
                id,
                artist+"<!!"+album,
                TERSICORE_TOKEN,
                url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getCover(CoverRetrieveTaskListener listener,
                                Track track, String server, int id)
    {
        String url = server + "/stream/" + track.resources.get(0).uuid;
        CoverRetrieveTask task = new CoverRetrieveTask(listener, track, url, TERSICORE_TOKEN, id);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void isServerRunning(ServerStatusTaskListener listener,
                                       URL serverUrl)
    {
        ServerStatusTask task = new ServerStatusTask(listener, serverUrl, TERSICORE_TOKEN);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
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

    public static void downloadCover(String link, int state, String key, CoverDownloadTaskListener listener)
            throws MalformedURLException {
        URL url = new URL(link);
        CoverDownloadTask task = new CoverDownloadTask(url, state, key, listener);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
