package com.evenless.tersicore;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MyReceiver extends IntentService {
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            MediaPlayerService mService = binder.getService();
            mService.pause();
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
        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
