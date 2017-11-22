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

import com.evenless.tersicore.exceptions.InvalidUrlException;
import com.evenless.tersicore.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayerServiceListener mListener;
    private ArrayList<Track> mCurrentPlaylist;
    private int mCurrentIndex;

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

        mWifiLock = ((WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
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

    public ArrayList<Track> getCurrentPlaylist() {
        return mCurrentPlaylist;
    }

    private void updateState() {
        mMediaPlayer.reset();
        if (mCurrentIndex >= mCurrentPlaylist.size()) {
            mListener.onPlaylistComplete();
        } else {
            Track current = mCurrentPlaylist.get(mCurrentIndex);
            try {
                mListener.onNewTrackPlaying(current);
                mMediaPlayer.setDataSource(current.resources[0].path);
                mMediaPlayer.prepareAsync();
                mCurrentIndex += 1;
            } catch (IOException e) {
                e.printStackTrace();
                mListener.onPlaybackError(new InvalidUrlException());
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("DEBUG", "onCompletion: triggered");
        updateState();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}
