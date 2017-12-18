package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.MyListAdapter;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Track;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by McPhi on 10/12/2017.
 */

public class SingleArtistActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ApiRequestTaskListener {

    private List<Album> listAlbums;
    private String artist;
    private RecyclerView mRecyclerViewAlbums;
    private MediaPlayerService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            createList();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artist = getIntent().getStringExtra("EXTRA_ARTIST");

        setContentView(R.layout.activity_artist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(artist);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton asd = findViewById(R.id.floatingShuffle);
        asd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent dd = new Intent(v.getContext(), MainActivity.class);
                mService.playRandom(DataBackend.getTracksByArtist(artist));
                startActivity(dd);
            }
        });

        Button alltracksButton = findViewById(R.id.alltracksButton);
        alltracksButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent asd = new Intent(v.getContext(), TracksActivity.class);
                asd.putExtra("EXTRA_ARTIST", artist);
                startActivity(asd);
            }
        });

        try {
            if (DataBackend.getArtists().size() != 0) {
                listAlbums = DataBackend.getAlbumsByArtist(artist);
            } else
                try {
                    TaskHandler.getTracks(this, PreferencesHandler.getServer(this));
                } catch (Exception e) {
                    listAlbums = new ArrayList<>();
                }
        } catch (Exception e) {
            Log.e("ArtistsActivity", e.getMessage());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_albums) {
            Intent asd = new Intent(this, AlbumsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_artists) {
            Intent asd = new Intent(this, ArtistsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_dj) {

        } else if (id == R.id.nav_home) {
            Intent asd = new Intent(this, Main3Activity.class);
            startActivity(asd);
        } else if (id == R.id.nav_playlists) {

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
        if (e != null) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            DataBackend.addTracks(new ArrayList<>(Arrays.asList(new Gson().fromJson(response, Track[].class))));
            listAlbums = DataBackend.getAlbumsByArtist(artist);
            createList();
        }
    }

    private void createList() {
        RecyclerView.LayoutManager mLayoutManagerAlbum = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewAlbums = findViewById(R.id.coverAlbumArtist);
        mRecyclerViewAlbums.setLayoutManager(mLayoutManagerAlbum);
        mRecyclerViewAlbums.setAdapter(new MyListAdapter(new ArrayList(listAlbums), mService.artistsCover, MyListAdapter.ARTALB_STATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }
}
