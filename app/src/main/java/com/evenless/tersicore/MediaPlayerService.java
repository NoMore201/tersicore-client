package com.evenless.tersicore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener
{
    public static final String ACTION_PLAY = "com.evenless.tersicore.PLAY";
    public static final String ACTION_STOP = "com.evenless.tersicore.STOP";

    private MediaPlayer mMediaPlayer = null;
    private WifiManager.WifiLock mWifiLock;
    private String testUrl = "https://upload.wikimedia.org/wikipedia/en/0/09/Blue_%28Da_Ba_Dee%29_sample.ogg";

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mMediaPlayer.setDataSource(testUrl);
        } catch (IOException e) {
            Log.e("ERROR", "onCreate: unable to fetch url");
            e.printStackTrace();
        }

        mWifiLock = ((WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DEBUG", "onStartCommand: " + intent.getAction());
        if (intent.getAction().equals(ACTION_PLAY) &&
            !mMediaPlayer.isPlaying())
        {
            mMediaPlayer.prepareAsync();
        }
        if (intent.getAction().equals(ACTION_STOP)) {
            mMediaPlayer.stop();
        }
        return START_STICKY;
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
     * Listeners
     */

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
