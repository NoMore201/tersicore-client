package com.evenless.tersicore;

import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.interfaces.ApiPostTaskListener;
import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.interfaces.ApiRequestExtraTaskListener;
import com.evenless.tersicore.interfaces.CoverDownloadTaskListener;
import com.evenless.tersicore.interfaces.CoverRetrieveTaskListener;
import com.evenless.tersicore.interfaces.FileDownloadTaskListener;
import com.evenless.tersicore.interfaces.ImageRequestTaskListener;
import com.evenless.tersicore.interfaces.ServerStatusTaskListener;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackSuggestion;
import com.evenless.tersicore.tasks.ApiGetTask;
import com.evenless.tersicore.tasks.CoverDownloadTask;
import com.evenless.tersicore.tasks.CoverRetrieveTask;
import com.evenless.tersicore.tasks.FileDownloadTask;
import com.evenless.tersicore.tasks.FileRemoveTask;
import com.evenless.tersicore.tasks.GenericPostTask;
import com.evenless.tersicore.tasks.ImageGetTask;
import com.evenless.tersicore.tasks.ServerStatusTask;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TaskHandler {
    private static final String TAG = "TaskHandler";
    private static final String API_BASE_URL = "https://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "b6570587abc105bc286cf227cabbba50";
    private static final String API_SHARED_SECRET = "ca91306bbff831733d675dfc6e556b77";
    private static final String TERSICORE_TOKEN =
            "0651863bf5d902262b17c4621ec340544ff016752543d99a92d7d22872d8a455";
    public static final int ALL_TRACKS = 0;
    public static final int TRACKS_LATEST = 1;
    public static final int PLAYLISTS = 2;
    public static final int PLAYLIST_SINGLE = 3;
    public static final int SUGGESTIONS = 4;
    public static final int USERS = 5;
    public static final int MESSAGES = 6;

    /*
      GET REQUESTS
     */

    public static void getTracks(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/tracks");
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, ALL_TRACKS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getLatestTracks(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/tracks/latest");
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, TRACKS_LATEST);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getPlaylists(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/playlists");
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, PLAYLISTS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getMessages(ApiRequestExtraTaskListener listener,
                                    String server) throws MalformedURLException
    {
        URL url = new URL(server + "/messages");
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, MESSAGES);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getUsers(ApiRequestExtraTaskListener listener,
                                   String server) throws MalformedURLException
    {
        URL url = new URL(server + "/users");
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, USERS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getPlaylist(ApiRequestTaskListener listener,
                                    String server, String id) throws MalformedURLException
    {
        URL url = new URL(server + "/playlists/" + id);
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, PLAYLIST_SINGLE);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getSuggestions(ApiRequestTaskListener listener,
                                    String server) throws MalformedURLException
    {
        URL url = new URL(server + "/suggestions");
        ApiGetTask task = new ApiGetTask(listener, url, TERSICORE_TOKEN, SUGGESTIONS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getArtistImageFromApi(ImageRequestTaskListener listener,
                                 String query,
                                 int id) throws
            MalformedURLException,
            UnsupportedEncodingException
    {
        URL url = new URL(buildArtistUrl(query));
        ImageGetTask task = new ImageGetTask(listener, id, query, TERSICORE_TOKEN, url);
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
        ImageGetTask task = new ImageGetTask(listener,
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

    /*
      POST REQUESTS
     */

    public static void setSuggestion(String server,
                                     ApiPostTaskListener listener,
                                     TrackSuggestion suggestion) throws MalformedURLException {
        URL serverUrl = new URL(server + "/suggestions");
        Gson gson = new Gson();
        String data = gson.toJson(suggestion, TrackSuggestion.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_SUGGESTION,
                TERSICORE_TOKEN,
                data,
                listener);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void setPlaylist(String server,
                                   ApiPostTaskListener listener,
                                   Playlist playlist) throws MalformedURLException {
        URL serverUrl = new URL(server + "/playlists");
        Gson gson = new Gson();
        String data = gson.toJson(playlist, Playlist.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_PLAYLIST,
                TERSICORE_TOKEN,
                data,
                listener);
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

    public static void downloadFile(String link, String key, String format, FileDownloadTaskListener listener, String id)
            throws MalformedURLException {
        URL url = new URL(link);
        FileDownloadTask task = new FileDownloadTask(url, key, format, listener, id);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void removeFile(String key, String format)
            throws MalformedURLException {
        FileRemoveTask task = new FileRemoveTask(key, format);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void removeAllFiles() {
        FileRemoveTask task = new FileRemoveTask("ALL", null);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
