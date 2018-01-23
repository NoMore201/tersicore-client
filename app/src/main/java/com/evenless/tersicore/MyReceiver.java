package com.evenless.tersicore;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class MyReceiver extends IntentService {
    private String action;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            MediaPlayerService mService = binder.getService();
            if(action.equals("play"))
                mService.play();
            else if (action.equals("pause"))
                mService.pause();
            else if (action.equals("forward"))
                mService.skip(MediaPlayerService.SKIP_FORWARD);
            else if (action.equals("backward"))
                mService.skip(MediaPlayerService.SKIP_BACKWARD);
            unbindService(mConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    public MyReceiver() {
        super("MyReceiver");
    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyReceiver(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent ii) {
        if(ii != null && ii.getAction() != null)
            action=ii.getAction();
        else
            action="";
        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
