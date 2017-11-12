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

import java.io.IOException;
import java.util.ArrayList;


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    private final IBinder mBinder = new LocalBinder();
    private ArrayList<String> mCurrentPlaylist = new ArrayList<>();

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

    /*
     * Implementation
     */

    public void updatePlaylist(ArrayList<String> playlist) {
        mCurrentPlaylist = playlist;
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

    private void updateState() {
        mMediaPlayer.reset();
        if (!mCurrentPlaylist.isEmpty()) {
            // fetch and play next song in the list
            Log.d("DEBUG", "updateState: triggered");
            try {
                mMediaPlayer.setDataSource(mCurrentPlaylist.remove(0));
            } catch (IOException e) {
                Log.e("ERROR", "updateState: cannot open url");
                e.printStackTrace();
            }
            mMediaPlayer.prepareAsync();
        }
    }

    /*
     * Listeners
     */

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
