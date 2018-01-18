package com.evenless.tersicore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
    private static final String mToken = "0651863bf5d902262b17c4621ec340544ff016752543d99a92d7d22872d8a455";


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

    public int getCurrentprogress(){return currentprogress;}

    public int getCurrentResource(){return res;}

    // CurrentPlaylist index - Selected Resources. Not Required!
    private Map<Integer, Integer> resNumb;

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
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mCurrentPlaylist = new ArrayList<>();
        mCurrentPlaylistSorted = new ArrayList<>();
        resNumb = new HashMap<>();
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
        resNumb.remove(mCurrentPlaylist.indexOf(it));
    }

    public int getPreferredRes(Track tr) {
        if(tr.resources.size()==1)
            return 0;

        int ind=mCurrentPlaylist.indexOf(tr);
        Integer aa = resNumb.get(ind);
        if(aa!=null)
            return aa;
        else
            //consider preferences!
            return 0;
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
            if(!isShuffle) {
                Integer res = resNumb.get(fromPosition);
                if (res != null) {
                    resNumb.remove(fromPosition);
                    resNumb.put(toPosition, res);
                }
            }
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
    public void updatePlaylist(List<Track> tracks, int position, boolean random, Map<Integer, Integer> res) {
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
    public int appendAfterCurrent(List<Track> tracks, Map<Integer,Integer> res) {
        //TODO: check behaviour
        if(isShuffle){
            isShuffle=false;
            mCurrentIndex = mCurrentPlaylist.indexOf(getCurrentPlaylist().get(mCurrentIndex));
        }
        mCurrentPlaylist.addAll(mCurrentIndex + 1, new ArrayList<>(tracks));
        for (Integer i : res.keySet()) {
            Track temp = tracks.get(i);
            resNumb.put(mCurrentPlaylist.indexOf(temp), res.get(i));
        }
        return mCurrentIndex + 1;
    }

    /**
     * Append specified list of tracks after the current index
     * @param track Track to insert
     * @param res Favorite res for track
     * @return index of the inserted track
     */
    public int appendAfterCurrent(Track track, int res) {
        //TODO: check behaviour
        if(isShuffle){
            isShuffle=false;
            mCurrentIndex = mCurrentPlaylist.indexOf(getCurrentPlaylist().get(mCurrentIndex));
        }
        mCurrentPlaylist.add(mCurrentIndex + 1, track);
        resNumb.put(mCurrentIndex + 1, res);
        return mCurrentIndex + 1;
    }

    /**
     * Append specified list of tracks after the current index
     * @param track Track to insert
     * @return index of the inserted track
     */
    public int appendAfterCurrent(Track track) {
        //TODO: check behaviour
        if(isShuffle){
            isShuffle=false;
            mCurrentIndex = mCurrentPlaylist.indexOf(getCurrentPlaylist().get(mCurrentIndex));
        }
        mCurrentPlaylist.add(mCurrentIndex + 1, track);
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
    public int append(List<Track> tracks, Map<Integer, Integer> res) {
        mCurrentPlaylist.addAll(new ArrayList<>(tracks));
        mCurrentPlaylistSorted.addAll(new ArrayList<>(tracks));
        for (Integer i : res.keySet()) {
            Track temp = tracks.get(i);
            resNumb.put(mCurrentPlaylist.indexOf(temp), res.get(i));
        }
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
        resNumb.put(mCurrentPlaylist.size()-1, res);
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
        if(index!=mCurrentIndex) {
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
        return mCurrentIndex;
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
        if (track.hasResources()) {
            if (track.resources.get(0).cover_data == null) {
                TaskHandler.getCover(this, track, PreferencesHandler.getServer(this), id);
            } else {
                mListener.onCoverFetched(track, id);
            }
        }
    }

    private void sortPlaylist() {
        mCurrentPlaylistSorted = new ArrayList<>(mCurrentPlaylist);
        long seed = System.nanoTime();
        Collections.shuffle(mCurrentPlaylistSorted, new Random(seed));
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
        currentprogress=0;
        mMediaPlayer.reset();
        if (mCurrentIndex >= getCurrentPlaylist().size()) {
            playlistCompleted();
        } else {
            final Track current = getCurrentPlaylist().get(mCurrentIndex);
            res = getPreferredRes(current);
            try {
                newTrackPlaying(current);
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("AUTH", mToken);
                String url = PreferencesHandler.getServer(this) +
                        "/stream/" +
                        current.resources.get(res).uuid;
                mMediaPlayer.setDataSource(getApplicationContext(),
                        Uri.parse(url),
                        headers);
                mMediaPlayer.prepareAsync();
                DataBackend.setDate(current);
            } catch (IOException e) {
                e.printStackTrace();
                playbackError(e);
            }
        }
    }
}
