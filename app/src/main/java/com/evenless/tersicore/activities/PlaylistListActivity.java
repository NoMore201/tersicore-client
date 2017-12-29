package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.ItemAdapter;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.google.gson.Gson;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.crosswall.lib.coverflow.core.PagerContainer;

/**
 * Created by McPhi on 10/12/2017.
 */

public class PlaylistListActivity extends AppCompatActivity{

    private static final String TAG = "TracksActivity";
    private List<Track> listTracks;
    private String artist;
    private boolean mBound = false;
    private MediaPlayerService mService;
    private Context ctx = this;
    private Playlist pid = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound=true;
            String p = getIntent().getStringExtra("EXTRA_PLAYLIST_ID");
            if(p==null)
                listTracks = mService.getCurrentPlaylist();
            else{
                pid=DataBackend.getPlaylist(p);
                listTracks = pid.tracks;
            }
            DragListView mDragListView = (DragListView) findViewById(R.id.dragPlaylist);
            mDragListView.setDragListListener(new DragListView.DragListListener() {
                @Override
                public void onItemDragStarted(int position) {

                }

                @Override
                public void onItemDragging(int itemPosition, float x, float y) {

                }

                @Override
                public void onItemDragEnded(int fromPosition, int toPosition) {
                    if(pid==null)
                        mService.changePlaylistPosition(fromPosition, toPosition);
                    else
                        listTracks=DataBackend.modifyPlaylistPosition(fromPosition, toPosition, pid.id);
                }
            });

            mDragListView.setLayoutManager(new LinearLayoutManager(ctx));
            ItemAdapter listAdapter = new ItemAdapter(listTracks, R.layout.list_item, R.id.image, false);
            mDragListView.setAdapter(listAdapter, true);
            mDragListView.setCanDragHorizontally(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound=false;
        }

    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artist = getIntent().getStringExtra("EXTRA_ARTIST");

        setContentView(R.layout.playlist_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Playing List");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
