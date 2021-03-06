package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PlaylistSingleAdapter;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.User;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

/**
 * Created by McPhi on 10/12/2017.
 */

public class PlaylistsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MediaPlayerServiceListener, ApiRequestTaskListener,
        ViewTreeObserver.OnWindowFocusChangeListener
{

    private static final String TAG = "PlaylistsActivity";
    private List<Playlist> listPlaylists;
    private boolean mBound = false;
    private MediaPlayerService mService;
    private Context ctx = this;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mService.setMediaPlayerServiceListener((MediaPlayerServiceListener) ctx);
            mBound = true;
            FloatingActionButton fab = findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.GONE);
            if (mService.getCurrentPlaylist().size() == 0) {
                findViewById(R.id.miniplayer).setVisibility(View.INVISIBLE);
            } else {
                View v = findViewById(R.id.miniplayer);
                v.setVisibility(View.VISIBLE);
                ListView asd = findViewById(R.id.listart);
                ConstraintLayout.LayoutParams x = (ConstraintLayout.LayoutParams) asd.getLayoutParams();
                x.bottomMargin=160;
                asd.setLayoutParams(x);
                PlayerInterface.UpdateTrack(v, mService);
            }
            NavigationView navigationView = findViewById(R.id.nav_view);
            if(navigationView!=null)
                navigationView.setCheckedItem(R.id.nav_playlists);
            try {
                List<String> servers = PreferencesHandler.getServer(ctx);
                for(String ss : servers)
                    TaskHandler.getPlaylists((ApiRequestTaskListener) ctx, ss);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mService.setMediaPlayerServiceListener(null);
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

        setContentView(R.layout.activity_main4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Playlists");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Switch asd = navigationView.getMenu().findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcharr);
        asd.setChecked(PreferencesHandler.getOffline(this));
        asd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHandler.setOffline(ctx, isChecked);
            finish();
            startActivity(new Intent(this, SearchActivity.class));
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        listPlaylists = DataBackend.getPlaylists(PreferencesHandler.getUsername(this));
        updateList();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_playlists);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_albums) {
            Intent asd = new Intent(this, AlbumsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_artists) {
            Intent asd = new Intent(this, ArtistsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_home) {
            Intent asd = new Intent(this, SearchActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_playlists) {

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_songs) {
            Intent asd = new Intent(this, TracksActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_view) {

        }


        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void updateList() {
        ListView lsv = findViewById(R.id.listart);
        PlaylistSingleAdapter arrayAdapter = new PlaylistSingleAdapter(
                this,
                R.layout.playlists,
                listPlaylists);

        lsv.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Intent asd = new Intent(v.getContext(), PlaylistListActivity.class);
                asd.putExtra("EXTRA_PLAYLIST_ID", listPlaylists.get(position).id);
                startActivity(asd);
            }
        });
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        PlayerInterface.UpdateTrack(findViewById(R.id.miniplayer), mService);
    }

    @Override
    public void onPlaylistComplete() {
        PlayerInterface.setStop(findViewById(R.id.miniplayer));
    }

    @Override
    public void onCoverFetched(Track track, int id) {

    }

    @Override
    public void onPlaybackError(Exception exception) {
        PlayerInterface.setStop(findViewById(R.id.miniplayer));
    }

    @Override
    public void onPlaybackProgressUpdate(int currentMilliseconds) {

    }

    public void onClickPlay(View v) {
        PlayerInterface.onClickPlay(v, mService);
    }

    public void onClickForward(View v) {
        PlayerInterface.onClickForward(v, mService);
    }

    public void onClickBackward(View v) {
        PlayerInterface.onClickBackward(v, mService);
    }

    public void onClickPlayer(View v) {
        Intent dd = new Intent(this, MainActivity.class);
        startActivity(dd);
    }

    @Override
    public void onPreparedPlayback() {
        PlayerInterface.setPlay(findViewById(R.id.miniplayer));
    }

    @Override
    public void onRequestComplete(String response, Exception e, String asd) {

    }

    @Override
    public void onLatestRequestComplete(String response, Exception e) {

    }

    @Override
    public void onPlaylistSingleRequestComplete(String result, Exception e) {

    }

    @Override
    public void onPlaylistsRequestComplete(String result, Exception e) {
        Playlist[] allp = new Gson().fromJson(result, Playlist[].class);
        for (Playlist p : allp)
            DataBackend.insertPlaylist(p);
        listPlaylists=DataBackend.getPlaylists(PreferencesHandler.getUsername(ctx));
        updateList();
    }

    @Override
    public void onSuggestionsRequestComplete(String result, Exception e) {

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View aa = findViewById(R.id.miniplayer);
        if(aa!=null && mService!=null && aa.getVisibility()==View.VISIBLE && hasFocus) {
            if (mService.isPlaying()) {
                PlayerInterface.setPlay(aa);
            } else {
                PlayerInterface.setStop(aa);
            }
        }
    }
}
