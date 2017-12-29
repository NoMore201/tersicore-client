package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Track;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by McPhi on 10/12/2017.
 */

public class ArtistsActivity  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ApiRequestTaskListener,
        MediaPlayerServiceListener {

    private List<String> listArtists;
    private MediaPlayerService mService;
    private Context ctx = this;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mService.setMediaPlayerServiceListener((MediaPlayerServiceListener) ctx);
            if (mService.getCurrentPlaylist().size() == 0) {
                FloatingActionButton asd = findViewById(R.id.floatingActionButton);
                CoordinatorLayout.LayoutParams temp = (CoordinatorLayout.LayoutParams) asd.getLayoutParams();
                temp.bottomMargin = 112;
                asd.setLayoutParams(temp);
                findViewById(R.id.asd2).setVisibility(View.GONE);
            } else {
                FloatingActionButton asd = findViewById(R.id.floatingActionButton);
                CoordinatorLayout.LayoutParams temp = (CoordinatorLayout.LayoutParams) asd.getLayoutParams();
                temp.bottomMargin = 300;
                asd.setLayoutParams(temp);
                findViewById(R.id.asd2).setVisibility(View.VISIBLE);
                PlayerInterface.UpdateTrack(findViewById(R.id.asd2), mService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (DataBackend.getArtists().size() != 0) {
                listArtists= DataBackend.getArtists();
            } else
                try {
                    TaskHandler.getTracks(this, PreferencesHandler.getServer(this));
                } catch (Exception e) {
                    listArtists = new ArrayList<>();
                }
        } catch (Exception e){
            Log.e("ArtistsActivity", e.getMessage());
        }
        setContentView(R.layout.activity_main4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        findViewById(R.id.floatingActionButton).setVisibility(View.GONE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_artists);

        ListView lsv = findViewById(R.id.listart);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listArtists );
        lsv.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {
                Intent asd = new Intent(v.getContext(), SingleArtistActivity.class);
                asd.putExtra("EXTRA_ARTIST", listArtists.get(position));
                v.getContext().startActivity(asd);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_albums) {
            Intent asd = new Intent(this, AlbumsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_artists) {

        } else if (id == R.id.nav_dj) {

        } else if (id == R.id.nav_home) {
            Intent asd = new Intent(this, SearchActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_playlists) {
            Intent asd = new Intent(this, PlaylistsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_songs) {
            Intent asd = new Intent(this, TracksActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_view) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestComplete(String response, Exception e) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            DataBackend.insertTracks(new ArrayList<>(Arrays.asList(new Gson().fromJson(response, Track[].class))));
            listArtists = DataBackend.getArtists();
            ListView lsv = findViewById(R.id.listart);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    listArtists );
            lsv.setAdapter(arrayAdapter);
        }
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        PlayerInterface.UpdateTrack(findViewById(R.id.asd2), mService);
    }

    @Override
    public void onPlaylistComplete() {
        PlayerInterface.setStop(findViewById(R.id.asd2));
    }

    @Override
    public void onCoverFetched(Track track, int id) {

    }

    @Override
    public void onPlaybackError(Exception exception) {
        PlayerInterface.setStop(findViewById(R.id.asd2));
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
}
