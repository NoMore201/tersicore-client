package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.CoverRetrieveTaskListener;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.exceptions.InvalidUrlException;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;

import java.net.MalformedURLException;

public class MediaPlayerExampleActivity extends AppCompatActivity
        implements MediaPlayerServiceListener,
        ApiRequestTaskListener
{
    private static final String TAG = "ExampleActivity";

    private MediaPlayerService mService;
    private boolean mBound;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player_example);
        try {
            TaskHandler.getTracks(this);
        } catch (MalformedURLException e) {
            Log.e(TAG, "onCreate: malformed url", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }


    public void onClickStop(View v) {
        if (mBound) {
            mService.pause();
        }
    }

    public void onClickPlay(View v) {
        if (mBound) {
            mService.play();
        }
    }

    public void onClickInit(View v) {
        if (mBound) {
            mService.setMediaPlayerServiceListener(this);
        }
        Track track1 = new Track();
        TrackResources temp = new TrackResources();
        TrackResources[] tempArray = new TrackResources[1];
        temp.path = "https://upload.wikimedia.org/wikipedia/en/0/09/Blue_%28Da_Ba_Dee%29_sample.ogg";
        tempArray[0] = temp;
        track1.resources = tempArray;
        temp = new TrackResources();
        tempArray = new TrackResources[1];
        temp.path = "https://upload.wikimedia.org/wikipedia/en/0/0a/Kate_Bush_-_Running_Up_That_Hill.ogg";
        tempArray[0] = temp;
        Track track2 = new Track();
        track2.resources = tempArray;

        Track[] list = {track1, track2};

        if (mBound) {
            mService.updatePlaylist(list);
        }
    }

    @Override
    public void onPlaylistComplete() {
        //do something
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        Log.d("TAG", "onNewTrackPlaying: " + newTrack.resources[0].path);
    }

    @Override
    public void onPlaybackError(Exception exception) {
        if (exception.getClass().equals(InvalidUrlException.class)) {
            Log.e("TAG", "onPlaybackError: invalid url" );
        }
    }

    @Override
    public void onApiRequestError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onRequestComplete(String response) {
        TextView view = findViewById(R.id.textView4);
        view.append(response);
    }

    @Override
    public void onCoverFetched(Track track) {
        boolean check = track.resources[0].cover_data != null;
        Log.d(TAG, "onCoverFetched: " + check);
    }
}
