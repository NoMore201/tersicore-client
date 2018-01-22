package com.evenless.tersicore;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;
import com.evenless.tersicore.activities.MainActivity;
import com.evenless.tersicore.exceptions.MediaPlayerException;
import com.evenless.tersicore.interfaces.CoverRetrieveTaskListener;
import com.evenless.tersicore.interfaces.FileDownloadTaskListener;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        CoverRetrieveTaskListener
{
    private static final String TAG = "MediaPlayerService";
    public static final String[] playOptions = {
            "Higher Bitrate Ever",
            "Lossless Bitrate",
            "Higher lossy bitrate",
            "Lower Bitrate"
    };

    public static TrackResources checkTrackResourceByPreference(Track tt, int w, boolean preferDownloaded, boolean isDataProt) {
        TrackResources res = tt.resources.get(0);
        int which;
        if(w==6 && replacementChoice!=-1)
            which=replacementChoice;
        else
            which=w;
        if(tt.resources.size()!=1 && which!=6) {
            int bitr = tt.resources.get(0).bitrate;
            for (int i=1; i<tt.resources.size(); i++){
                int currbit = tt.resources.get(i).bitrate;
                if((!preferDownloaded || tt.resources.get(i).isDownloaded || !tt.resources.get(0).isDownloaded) &&
                        ((which==0 && currbit>bitr) || (which==3 && currbit<bitr) ||
                                (which==1 && ((bitr>1411200 && currbit<bitr) || (bitr<1411200 && currbit<1411200 && currbit>bitr))) ||
                                (which==2 && ((bitr>350000 && currbit<bitr) || (bitr<350000 && currbit<350000 && currbit>bitr))))) {
                    res = tt.resources.get(i);
                    bitr=currbit;
                }
            }
        }
        if(which==3 && res.bitrate>350000 && isDataProt && !res.isDownloaded && !checkIsCached(res))
            res=null;
        return res;
    }

    public boolean isOfflineT() {
        return getCurrentPlaylist().get(mCurrentIndex).resources.get(res).isDownloaded;
    }

    public void downloadCurrentFile(FileDownloadTaskListener ctx) throws MalformedURLException {
        TrackResources tr = getCurrentPlaylist().get(mCurrentIndex).resources.get(res);
        if(tr!=null) {
            String urlfile = tr.server +
                    "/stream/" + tr.uuid;
            if(proxy.isCached(urlfile))
                urlfile=url;

            TaskHandler.downloadFile(urlfile, tr.uuid, tr.codec, ctx, getCurrentPlaylist().get(mCurrentIndex).uuid);
        }
    }

    public void downloadFile(TrackResources tr, String uuid, FileDownloadTaskListener ctx) throws MalformedURLException {
        if(tr!=null) {
            String urlfile = PreferencesHandler.getServer(this) +
                    "/stream/" + tr.uuid;
            if(proxy.isCached(urlfile))
                urlfile=proxy.getProxyUrl(urlfile);
            TaskHandler.downloadFile(urlfile, tr.uuid, tr.codec, ctx, uuid);
        }
    }

    public static boolean hasBeenCached(Track t) {
        for(TrackResources tr : t.resources)
            if(checkIsCached(tr))
                return true;

        return false;
    }


    public enum SkipDirection { SKIP_FORWARD, SKIP_BACKWARD }
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayerServiceListener mListener;
    private ArrayList<Track> mCurrentPlaylist;
    private ArrayList<Track> mCurrentPlaylistSorted;
    private int mCurrentIndex;
    private Timer mCurrentTimer = new Timer();
    private boolean isShuffle = false;
    private Integer res;
    private int currentprogress;
    private String url;
    private MediaSession mSes;
    private PendingIntent mPlay;
    private PendingIntent mPause;
    private PendingIntent mBack;
    private PendingIntent mForw;
    public static int replacementChoice;
    private Context ccx = this;

    public int getCurrentprogress(){return currentprogress;}

    public int getCurrentResource(){
        if(res!=-1 && getCurrentTrackIndex()==mCurrentIndex)
            return res;
        else
            return 0;
    }

    // CurrentPlaylist index - Selected Resources. Not Required!
    private Map<String, Integer> resNumb;

    private static HttpProxyCacheServer proxy;

    /*
     * Override methods
     */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setWakeMode(ccx, PowerManager.PARTIAL_WAKE_LOCK);
        mSes = new MediaSession(this, "TAG1");
        mSes.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mCurrentPlaylist = new ArrayList<>();
        mCurrentPlaylistSorted = new ArrayList<>();
        changeProxyCacheSize(PreferencesHandler.getCacheSize(this));
        resNumb = new HashMap<>();
        mCurrentIndex=-1;
        Intent iAction1 = new Intent(this, MyReceiver.class);
        iAction1.setAction("pause");
        mPause = PendingIntent.getService(
                this,
                0,
                iAction1,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        replacementChoice = -1;
        iAction1.setAction("play");
        mPlay = PendingIntent.getService(
                this,
                0,
                iAction1,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        iAction1.setAction("forward");
        mForw = PendingIntent.getService(
                this,
                0,
                iAction1,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        iAction1.setAction("backward");
        mBack = PendingIntent.getService(
                this,
                0,
                iAction1,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        mMediaPlayer.setAudioAttributes(attributes);

        try {
            WifiManager service = ((WifiManager) ccx
                    .getSystemService(Context.WIFI_SERVICE));
            if (service != null) {
                mWifiLock = service.createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
            }

        } catch (NullPointerException e) {
            Log.w(TAG, "onCreate: unable to lock Wifi", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        for (String ss : PreferencesHandler.getServer(this))
            try {
                TaskHandler.setUser(ss, null, new User(PreferencesHandler.getUsername(this), false));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.cancel(9876);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.cancel(9876);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared: called");
        mediaPlayer.start();
        mListener.onPreparedPlayback();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion: called");
        mCurrentIndex += 1;
        updateState();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        playbackError(new MediaPlayerException());
        return true;
    }

    @Override
    public void onCoverRetrieveComplete(Track track, byte[] cover, int id, Exception e) {
        if (cover == null || e!=null) {
            cover = new byte[0];
        } else {
            String temp = track.album_artist;
            if(temp==null)
                temp=track.artist;
            if(DataBackend.getCover(temp, track.album)==null)
                DataBackend.insertCover(temp, track.album, cover);
        }
        Track updated = DataBackend.updateTrackCover(track.uuid, cover);
        if(updated!=null) {
            if (mCurrentPlaylist != null) {
                int index = mCurrentPlaylist.indexOf(track);
                if (index != -1 && track.resources != null && track.resources.size() != 0) {
                    mCurrentPlaylist.set(index, updated);
                    //DataBackend.insertCover(updated.album_artist, updated.album, cover);
                }
            }
            if (mCurrentPlaylistSorted != null) {
                int index = mCurrentPlaylistSorted.indexOf(track);
                if (index != -1 && track.resources != null && track.resources.size() != 0) {
                    mCurrentPlaylistSorted.set(index, updated);
                }
            }
            if (updated.album != null) {
                String tr;
                if (track.album_artist != null)
                    tr = track.album_artist;
                else
                    tr = track.artist;

                // update all tracks of the same album
                ArrayList<Track> tracksWithSameAlbum =
                        DataBackend.getTracks(tr, track.album);
                for (Track t : tracksWithSameAlbum) {
                    if (t.resources != null &&
                            t.resources.size() > id) {
                        DataBackend.updateTrackCover(t.uuid, cover);
                    }
                }
            }
            mListener.onCoverFetched(updated, id);
        }
    }

    /*
     * Custom methods
     */

    public void deleteFromPlaylist(Track it) {
        if(mCurrentIndex==getCurrentPlaylist().indexOf(it))
            skip(SkipDirection.SKIP_FORWARD);
        getCurrentPlaylist().remove(it);
        if(!getCurrentPlaylist().contains(it))
            resNumb.remove(it.uuid);
    }

    private int getPreferredRes(Track tr, Context ctx) {
        Integer aa = resNumb.get(tr.uuid);

        if(tr.resources.size()==1 && aa!=null && aa!=-1)
            return 0;
        else if(aa!=null)
            return aa;
        else {
            TrackResources temp = checkTrackResourceByPreference(tr,
                    PreferencesHandler.getPreferredQuality(ctx), PreferencesHandler.getPreferOffline(ctx),
                    PreferencesHandler.getDataProtection(ctx));
            if(temp!=null)
                return tr.resources.indexOf(temp);
            else
                return -1;
        }
    }

    public void reset() {
        mMediaPlayer.reset();
        mCurrentPlaylist=new ArrayList<>();
        mCurrentPlaylistSorted=new ArrayList<>();
        currentprogress=0;
        mCurrentIndex=0;
        mCurrentTimer.cancel();
        isShuffle=false;
        res=0;
    }

    public boolean getRepeat() {return mMediaPlayer.isLooping();}

    public void setRepeat(boolean r) {mMediaPlayer.setLooping(r);}

    public boolean getShuffle() {return isShuffle;}

    public void toggleShuffle(){
        isShuffle = !isShuffle;
        if (isShuffle) {
            sortPlaylist();
            Track temp = mCurrentPlaylist.get(mCurrentIndex);
            mCurrentPlaylistSorted.remove(temp);
            mCurrentPlaylistSorted.add(0, temp);
            mCurrentIndex=0;
        } else {
            mCurrentIndex = mCurrentPlaylist.indexOf(mCurrentPlaylistSorted.get(mCurrentIndex));
        }
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void changePlaylistPosition(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            if (mCurrentIndex == fromPosition)
                mCurrentIndex = toPosition;
            else if (fromPosition > mCurrentIndex && toPosition <= mCurrentIndex)
                mCurrentIndex++;
            else if (fromPosition < mCurrentIndex && toPosition >= mCurrentIndex)
                mCurrentIndex--;
            Track temp = getCurrentPlaylist().get(fromPosition);
            getCurrentPlaylist().remove(fromPosition);
            getCurrentPlaylist().add(toPosition, temp);
        }
    }

    public void setMediaPlayerServiceListener(MediaPlayerServiceListener listener) {
        mListener = listener;
    }

    /**
     * Update the current playlist with the specified list, choosing to shuffle it or not
     * @param tracks Tracks to be played
     * @param position Position to start playing from (valid only for normal playback)
     * @param random Whether to start shuffling playlist or not
     */
    public void updatePlaylist(List<Track> tracks, int position, boolean random) {
        if (position >= tracks.size()) {
            throw new IndexOutOfBoundsException("Position exceed list size");
        }
        mCurrentPlaylist = new ArrayList<>(tracks);
        // initialize sorted playlist with a copy of the normal one. After calling
        // updatePlaylist or toggleShuffle, it will be correctly processed
        mCurrentPlaylistSorted = new ArrayList<>(tracks);
        resNumb = new HashMap<>();
        if (random) {
            sortPlaylist();
            isShuffle = true;
            mCurrentIndex = 0;
        } else {
            isShuffle = false;
            mCurrentIndex = position;
        }
        updateState();
    }

    /**
     * Update the current playlist with the specified list, choosing to shuffle it or not
     * @param tracks Tracks to be played
     * @param position Position to start playing from (valid only for normal playback)
     * @param random Whether to start shuffling playlist or not
     * @param res Favorite resources for all, or part of the list
     */
    public void updatePlaylist(List<Track> tracks, int position, boolean random, Map<String, Integer> res) {
        if (position >= tracks.size()) {
            throw new IndexOutOfBoundsException("Position exceed list size");
        }
        mCurrentPlaylist = new ArrayList<>(tracks);
        // initialize sorted playlist with a copy of the normal one. After calling
        // updatePlaylist or toggleShuffle, it will be correctly processed
        mCurrentPlaylistSorted = new ArrayList<>(tracks);
        if (random) {
            sortPlaylist();
            isShuffle = true;
            mCurrentIndex = 0;
        } else {
            isShuffle = false;
            mCurrentIndex = position;
        }
        resNumb = res;
        updateState();
    }

    /**
     * Append specified list of tracks after the current index
     * @param tracks List of tracks to insert
     * @return starting index of the inserted list
     */
    public int appendAfterCurrent(List<Track> tracks) {
        //TODO: check behaviour
        if(isShuffle){
            isShuffle=false;
            mCurrentIndex = mCurrentPlaylist.indexOf(getCurrentPlaylist().get(mCurrentIndex));
        }
        mCurrentPlaylist.addAll(mCurrentIndex + 1, new ArrayList<>(tracks));
        return mCurrentIndex + 1;
    }

    /**
     * Append specified list of tracks after the current index
     * @param tracks List of tracks to insert
     * @param res List of the favorite resources for all, or part of the list
     * @param res List of the favorite resources for all, or part of the list
     * @return starting index of the inserted list
     */
    public int appendAfterCurrent(List<Track> tracks, Map<String,Integer> res) {
        //TODO: check behaviour
        if(isShuffle){
            isShuffle=false;
            mCurrentIndex = mCurrentPlaylist.indexOf(getCurrentPlaylist().get(mCurrentIndex));
        }
        mCurrentPlaylist.addAll(mCurrentIndex + 1, new ArrayList<>(tracks));

        resNumb.putAll(res);
        return mCurrentIndex + 1;
    }

    /**
     * Append the provided Track list at the end of the playlist
     * @param tracks List of Track to be appended
     * @return starting index of the list
     */
    public int append(List<Track> tracks) {
        mCurrentPlaylist.addAll(new ArrayList<>(tracks));
        mCurrentPlaylistSorted.addAll(new ArrayList<>(tracks));
        return mCurrentPlaylist.size() - tracks.size();
    }

    /**
     * Append the provided Track list at the end of the playlist
     * @param tracks List of Track to be appended
     * @return starting index of the list
     */
    public int append(List<Track> tracks, Map<String, Integer> res) {
        mCurrentPlaylist.addAll(new ArrayList<>(tracks));
        mCurrentPlaylistSorted.addAll(new ArrayList<>(tracks));
        resNumb.putAll(res);
        return mCurrentPlaylist.size() - tracks.size();
    }

    /**
     * Append the provided Track at the end of the playlist
     * @param track Track to be appended
     * @param res favorite resource for Track
     * @return index of the inserted Track
     */
    public int append(Track track, int res) {
        mCurrentPlaylist.add(track);
        mCurrentPlaylistSorted.add(track);
        resNumb.put(track.uuid, res);
        return mCurrentPlaylist.size() - 1;
    }

    /**
     * Append the provided Track at the end of the playlist
     * @param track Track to be appended
     * @return index of the inserted Track
     */
    public int append(Track track) {
        mCurrentPlaylist.add(track);
        mCurrentPlaylistSorted.add(track);
        return mCurrentPlaylist.size() - 1;
    }

    public void play() {
        if(getCurrentTrackIndex()==0 && currentprogress==0 && !mMediaPlayer.isPlaying())
            updateState();
        else {
            mMediaPlayer.start();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9876, createNotification(true, getCurrentPlaylist().get(getCurrentTrackIndex())));
        }
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
            notificationManager.notify(9876, createNotification(false, getCurrentPlaylist().get(getCurrentTrackIndex())));
        }
    }

    public void skip(SkipDirection direction) {
        if (direction == SkipDirection.SKIP_FORWARD && mCurrentIndex<getCurrentPlaylist().size()-1) {
            Log.i(TAG, mCurrentIndex + " Skipped");
            mCurrentIndex += 1;
            updateState();
        } else if (direction == SkipDirection.SKIP_BACKWARD && mCurrentIndex>0) {
            Log.i(TAG, mCurrentIndex + " Back");
            mCurrentIndex -= 1;
            updateState();
        } else
            playlistCompleted();

    }

    public void seekToTrack(int index) {
        if(index!=mCurrentIndex && index<getCurrentPlaylist().size()) {
            Log.i(TAG, "Seek to " + index);
            mCurrentIndex = index;
            updateState();
        }
    }

    public void seekTo(int milliseconds) {
        mMediaPlayer.seekTo(milliseconds);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public ArrayList<Track> getCurrentPlaylist() {
        if(isShuffle)
            return mCurrentPlaylistSorted;
        else
            return mCurrentPlaylist;
    }

    public int getCurrentTrackIndex() {
        if(mCurrentIndex<getCurrentPlaylist().size())
            return mCurrentIndex;
        else
            return 0;
    }

    public void callTimer(Handler h){
        final Handler mHandler = h;
        final MediaPlayer mediaPlayerInstance = mMediaPlayer;
        mCurrentTimer.cancel();
        mCurrentTimer = new Timer();
        mCurrentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (mMediaPlayer.isPlaying()) {
                        currentprogress=mediaPlayerInstance.getCurrentPosition();
                        Message a = new Message();
                        a.arg1 = mediaPlayerInstance.getCurrentPosition();
                        mHandler.sendMessage(a);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, 0, 1000);
    }

    public void fetchCover(Track track, int id) {
        TrackResources tr = track.resources.get(id);
        if (tr.cover_data == null) {
            TaskHandler.getCover(this, track, tr.server, id);
        } else {
            mListener.onCoverFetched(track, id);
        }

    }

    private void sortPlaylist() {
        mCurrentPlaylistSorted = new ArrayList<>(mCurrentPlaylist);
        long seed = System.nanoTime();
        Collections.shuffle(mCurrentPlaylistSorted, new Random(seed));
    }

    private void playlistCompleted() {
        // set index to the beginning of playlist
        // but don't start playing
        res=0;
        mCurrentIndex=0;
        mMediaPlayer.reset();
        mCurrentTimer.cancel();
        newTrackPlaying(getCurrentPlaylist().get(0));
        if (mListener != null) {
            mListener.onPlaylistComplete();
        }
    }

    private void newTrackPlaying(Track current) {
        if (mListener != null) {
            mListener.onNewTrackPlaying(current);
        }
    }

    private void playbackError(Exception e) {
        if (mListener != null) {
            mListener.onPlaybackError(e);
        }
    }

    public void updateState() {
        currentprogress=0;
        if (mCurrentIndex >= getCurrentPlaylist().size()) {
            playlistCompleted();
        } else {
            Track current = getCurrentPlaylist().get(mCurrentIndex);
            res = getPreferredRes(current, ccx);
            if (res >= 0 ) {
                newTrackPlaying(current);
                mMediaPlayer.reset();
                TrackResources trr = current.resources.get(res);
                if (trr.isDownloaded)
                    url = Environment.getExternalStorageDirectory() + "/TersicoreMusic/" + trr.uuid + "." + trr.codec;
                else
                    url = proxy.getProxyUrl(trr.server + "/stream/" + trr.uuid);
                try {
                    mMediaPlayer.setDataSource(ccx, Uri.parse(url));
                } catch (IOException e) {
                    if (trr.isDownloaded)
                        try {
                            DataBackend.removeOfflineTrack(current, trr.uuid);
                            url = proxy.getProxyUrl(trr.server + "/stream/" + trr.uuid);
                            mMediaPlayer.setDataSource(ccx, Uri.parse(url));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    else
                        e.printStackTrace();
                }
                mMediaPlayer.prepareAsync();
                Notification noti = createNotification(true, current);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(9876, noti);
                DataBackend.setDate(current);
                if (!PreferencesHandler.getOffline(this))
                    for (String ss : PreferencesHandler.getServer(this))
                        try {
                            TaskHandler.setUser(ss, null,
                                    new User(PreferencesHandler.getUsername(this), current.toString()));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
            } else {
                skip(SkipDirection.SKIP_FORWARD);
            }
        }
    }

    //Size in MB
    public void changeProxyCacheSize(int size) {
        proxy = new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(size * 1024 * 1024)
                .build();
    }

    public static boolean checkIsCached(TrackResources t){
        return proxy.isCached(t.server + "/stream/" + t.uuid);
    }

    private Bitmap getCover(Track tr){
        byte[] cov = tr.getCover();

        // convert the byte array to a bitmap
        if(cov!= null && cov.length!=0)
            return BitmapFactory.decodeByteArray(cov, 0, cov.length);
        else {
            String art;
            if(tr.album_artist!=null)
                art=tr.album_artist;
            else
                art=tr.artist;
            Cover asd = DataBackend.getCover(art, tr.album);
            if(asd!=null)
                return BitmapFactory.decodeByteArray(asd.cover,0,asd.cover.length);
            else
                return BitmapFactory.decodeResource(this.getResources(), R.drawable.nocover);
        }
    }

    public Notification createNotification(boolean isPlayed, Track current){
        Notification.Builder xd = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(current.title)
                .setContentIntent(PendingIntent.getActivity(
                        this,
                        0,
                        new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .setDeleteIntent(PendingIntent.getActivity(
                        this,
                        0,
                        new Intent(this, CloseService.class),
                        PendingIntent.FLAG_CANCEL_CURRENT
                ))
                .setContentText(current.artist + " - " + current.album);
        xd.addAction(new Notification.Action(android.R.drawable.ic_media_previous,
                "Skip Backward", mBack));
        if(!isPlayed)
            xd.addAction(new Notification.Action(android.R.drawable.ic_media_play,
                    "Play", mPlay));
        else
            xd.addAction(new Notification.Action(android.R.drawable.ic_media_pause,
                    "Play", mPause));
        xd.addAction(new Notification.Action(android.R.drawable.ic_media_next,
                "Skip Forward", mForw));
        xd.setStyle(new Notification.MediaStyle()
                .setMediaSession(mSes.getSessionToken()).setShowActionsInCompactView(1));
        xd.setVisibility(Notification.VISIBILITY_PUBLIC);
        xd.setLargeIcon(getCover(current));
        return xd.build();
    }


}
