package com.evenless.tersicore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evenless.tersicore.exceptions.MediaPlayerException;
import com.evenless.tersicore.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.RealmResults;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        CoverRetrieveTaskListener
{
    private static final String TAG = "MediaPlayerService";
    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayerServiceListener mListener;
    private ArrayList<Track> mCurrentPlaylist;
    private int mCurrentIndex;
    private Timer mCurrentTimer = new Timer();

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

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
        mCurrentPlaylist = new ArrayList<>();
        mCurrentIndex=-1;

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        mMediaPlayer.setAudioAttributes(attributes);

        try {
            WifiManager service = ((WifiManager) getApplicationContext()
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
    }

    public void setMediaPlayerServiceListener(MediaPlayerServiceListener listener) {
        mListener = listener;
    }

    public void updatePlaylist(Track[] tracks) {
        mCurrentPlaylist = new ArrayList<>(Arrays.asList(tracks));
        mCurrentIndex = 0;
        updateState();
    }

    public void updatePlaylist(List<Track> tracks) {
        mCurrentPlaylist = new ArrayList<>(tracks);
        mCurrentIndex = 0;
        updateState();
    }

    public void playNow(Track[] tracks) {
        mCurrentPlaylist.addAll(mCurrentIndex + 1, new ArrayList<>(Arrays.asList(tracks)));
        seekToTrack(mCurrentIndex + 1);
    }

    public void playAfter(Track[] tracks) {
        mCurrentPlaylist.addAll(mCurrentIndex + 1, new ArrayList<>(Arrays.asList(tracks)));
    }

    public void addToPlaylist(Track[] tracks) {
        mCurrentPlaylist.addAll(new ArrayList<>(Arrays.asList(tracks)));
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

    public void seekToTrack(int index) {
        mCurrentIndex = index;
        updateState();
    }

    public void seekTo(int milliseconds) {
        mMediaPlayer.seekTo(milliseconds);
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
    public void onCoverRetrieveComplete(Track track, byte[] cover, int id) {
        if(cover==null)
            cover=new byte[0];
        if(mCurrentPlaylist!=null) {
            int index = mCurrentPlaylist.indexOf(track);
            if (index != -1 && track.resources != null && track.resources.size() != 0) {
                Track updated = DataBackend.updateTrackCover(track.uuid, cover);
                mCurrentPlaylist.set(index, updated);
                mListener.onCoverFetched(updated, id);
                if (updated.album != null && cover.length!=0) {
                    // update all tracks of the same album
                    RealmResults<Track> tracksWithSameAlbum =
                            DataBackend.getTracksByAlbum(track.artist, track.album);
                    for (Track t : tracksWithSameAlbum) {
                        if (t.resources != null &&
                                t.resources.size() != 0) {
                            DataBackend.updateTrackCover(t.uuid, cover);
                        }
                    }
                }
            }
        } else {
            Track updated = DataBackend.updateTrackCover(track.uuid, cover);
            mListener.onCoverFetched(updated, id);
        }
    }

    private void playlistCompleted() {
        if (mListener != null) {
            mListener.onPlaylistComplete();
        }
        // set index to the beginning of playlist
        // but don't start playing
        mCurrentIndex=0;
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
            final Track current = mCurrentPlaylist.get(mCurrentIndex);
            try {
                newTrackPlaying(current);
                mMediaPlayer.setDataSource(PreferencesHandler.getServer(this) +
                        "/stream/" +
                        current.resources.get(0).uuid);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                playbackError(e);
            }
        }
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
                        Message a = new Message();
                        a.arg1=mediaPlayerInstance.getCurrentPosition();
                        mHandler.sendMessage(a);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, 0, 1000);
    }

    public void fetchCover(Track track, int id) {
        try {
            Track tmp = DataBackend.getTracks().where().equalTo("uuid", track.uuid).findFirst();
            if (tmp.resources.get(0).cover_data == null) {
                TaskHandler.getCover(this, track, PreferencesHandler.getServer(this), id);
            } else if (tmp.resources.get(0).cover_data.length!=0){
                mListener.onCoverFetched(tmp, id);
            }
        } catch (NullPointerException e){
            Log.i(TAG, "Something is NULL");
        }
    }
}
