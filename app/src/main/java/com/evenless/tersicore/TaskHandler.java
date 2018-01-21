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
import com.evenless.tersicore.interfaces.LoginTaskListener;
import com.evenless.tersicore.interfaces.ServerStatusTaskListener;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.PlaylistFake;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackSuggestion;
import com.evenless.tersicore.model.User;
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
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class TaskHandler {
    private static final String TAG = "TaskHandler";
    private static final String API_BASE_URL = "https://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "b6570587abc105bc286cf227cabbba50";
    private static final String API_SHARED_SECRET = "ca91306bbff831733d675dfc6e556b77";
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
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), ALL_TRACKS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getLatestTracks(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/tracks/latest");
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), TRACKS_LATEST);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getPlaylists(ApiRequestTaskListener listener,
                                 String server) throws MalformedURLException
    {
        URL url = new URL(server + "/playlists");
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), PLAYLISTS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getMessages(ApiRequestExtraTaskListener listener,
                                    String server) throws MalformedURLException
    {
        URL url = new URL(server + "/messages");
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), MESSAGES);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getUsers(ApiRequestExtraTaskListener listener,
                                   String server) throws MalformedURLException
    {
        URL url = new URL(server + "/users");
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), USERS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getPlaylist(ApiRequestTaskListener listener,
                                    String server, String id) throws MalformedURLException
    {
        URL url = new URL(server + "/playlists/" + id);
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), PLAYLIST_SINGLE);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getSuggestions(ApiRequestTaskListener listener,
                                    String server) throws MalformedURLException
    {
        URL url = new URL(server + "/suggestions");
        ApiGetTask task = new ApiGetTask(listener, url, DataBackend.getToken(server), SUGGESTIONS);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void getArtistImageFromApi(ImageRequestTaskListener listener,
                                 String query,
                                 int id) throws
            MalformedURLException,
            UnsupportedEncodingException
    {
        URL url = new URL(buildArtistUrl(query));
        ImageGetTask task = new ImageGetTask(listener, id, query, "", url);
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
                null,
                url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getCover(CoverRetrieveTaskListener listener,
                                Track track, String server, int id)
    {
        String url = server + "/stream/" + track.resources.get(0).uuid;
        CoverRetrieveTask task = new CoverRetrieveTask(listener, track, url, DataBackend.getToken(server), id);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void isServerRunning(ServerStatusTaskListener listener,
                                       URL serverUrl)
    {
        ServerStatusTask task = new ServerStatusTask(listener, serverUrl, "");
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
                DataBackend.getToken(server),
                data,
                listener);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void setPlaylist(String server,
                                   ApiPostTaskListener listener,
                                   Playlist playlist) throws MalformedURLException {
        URL serverUrl = new URL(server + "/playlists");
        Gson gson = new Gson();
        String data = gson.toJson(new PlaylistFake(playlist.id, playlist.name, playlist.uploader, playlist.tracks), PlaylistFake.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_PLAYLIST,
                DataBackend.getToken(server),
                data,
                listener);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void setUser(String server,
                                   ApiPostTaskListener listener,
                                   User u) throws MalformedURLException {
        /*
        URL serverUrl = new URL(server + "/users");
        Gson gson = new Gson();
        String data = gson.toJson(u, User.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_USERS,
                DataBackend.getToken(server),
                data,
                listener);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);*/
    }

    public static void sendMessage(String server,
                                   ApiPostTaskListener listener,
                                   EmailType mail) throws MalformedURLException {
        URL serverUrl = new URL(server + "/playlists");
        Gson gson = new Gson();
        String data = gson.toJson(mail, EmailType.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_MESSAGE,
                DataBackend.getToken(server),
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

    public static void Login(User temp, String server, ApiPostTaskListener tt) throws MalformedURLException {
        URL serverUrl = new URL(server + "/login");
        Gson gson = new Gson();
        String data = gson.toJson(temp, User.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_LOGIN,
                DataBackend.getToken(server),
                data,
                tt);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static void deletePlaylist(String server, Playlist playlist) throws MalformedURLException {
        URL serverUrl = new URL(server + "/playlists");
        Gson gson = new Gson();
        String data = gson.toJson(new PlaylistFake(playlist.id, playlist.name, playlist.uploader, null), PlaylistFake.class);
        GenericPostTask task = new GenericPostTask(serverUrl,
                GenericPostTask.POST_PLAYLIST,
                DataBackend.getToken(server),
                data,
                null);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}
