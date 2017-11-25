package com.evenless.tersicore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evenless.tersicore.exceptions.MediaPlayerException;
import com.evenless.tersicore.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        CoverRetrieveTaskListener
{
    private static final String TAG = "MediaPlayerService";
    private static String STREAM_URL = "http://casa.izzo.li:8888/stream/";

    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayerServiceListener mListener;
    private ArrayList<Track> mCurrentPlaylist;
    private int mCurrentIndex;

    public enum SkipDirection { SKIP_FORWARD, SKIP_BACKWARD }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

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
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        try {
            mWifiLock = ((WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        } catch (NullPointerException e) {
            Log.w(TAG, "onCreate: unable to lock Wifi", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mWifiLock.release();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    public void setMediaPlayerServiceListener(MediaPlayerServiceListener listener) {
        mListener = listener;
    }

    public void updatePlaylist(Track[] tracks) {
        mCurrentPlaylist = new ArrayList<>(Arrays.asList(tracks));
        fetchAllCovers();
        mCurrentIndex = 0;
        updateState();
    }

    public void play() {
        mMediaPlayer.start();
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void skip(SkipDirection direction) {
        if (direction == SkipDirection.SKIP_FORWARD) {
            mCurrentIndex += 1;
        }
        if (direction == SkipDirection.SKIP_BACKWARD) {
            mCurrentIndex -= 1;
        }
        updateState();
    }

    public void seekTo(int index) {
        mCurrentIndex = index;
        updateState();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public ArrayList<Track> getCurrentPlaylist() {
        return mCurrentPlaylist;
    }

    public int getCurrentTrackIndex() {
        return mCurrentIndex;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared: called");
        mediaPlayer.start();
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
    public void onCoverRetrieveComplete(Track track, byte[] cover) {
        int index = mCurrentPlaylist.indexOf(track);
        if (index != -1) {
            mCurrentPlaylist.get(index).resources[0].cover_data = cover;
            mListener.onCoverFetched(mCurrentPlaylist.get(index));
        }
    }

    private void playlistCompleted() {
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

    private void updateState() {
        mMediaPlayer.reset();
        if (mCurrentIndex >= mCurrentPlaylist.size()) {
            playlistCompleted();
        } else {
            Track current = mCurrentPlaylist.get(mCurrentIndex);
            try {
                newTrackPlaying(current);
                mMediaPlayer.setDataSource(STREAM_URL +  current.resources[0].uuid);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                playbackError(e);
            }
        }
    }

    private void fetchAllCovers() {
        if (mCurrentPlaylist.size() == 0) {
            return;
        }
        for (Track t: mCurrentPlaylist) {
            TaskHandler.getCover(this, t);
        }
    }
}
