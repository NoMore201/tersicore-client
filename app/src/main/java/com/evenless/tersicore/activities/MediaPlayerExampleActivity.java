package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.R;

import java.util.ArrayList;

public class MediaPlayerExampleActivity extends AppCompatActivity {

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
        ArrayList<String> list = new ArrayList<>();
        list.add("https://upload.wikimedia.org/wikipedia/en/0/09/Blue_%28Da_Ba_Dee%29_sample.ogg");
        list.add("https://upload.wikimedia.org/wikipedia/en/0/0a/Kate_Bush_-_Running_Up_That_Hill.ogg");

        if (mBound) {
            mService.updatePlaylist(list);
        }
    }
}
