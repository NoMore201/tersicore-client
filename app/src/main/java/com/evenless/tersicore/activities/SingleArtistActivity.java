package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.MyListAdapter;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by McPhi on 10/12/2017.
 */

public class SingleArtistActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MediaPlayerServiceListener,
        ViewTreeObserver.OnWindowFocusChangeListener{

    private List<Album> listAlbums;
    private String artist;
    private MediaPlayerService mService;
    private Context ctx = this;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBound = true;
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mService.setMediaPlayerServiceListener((MediaPlayerServiceListener) ctx);
            Log.d("DEBUG", "EZ onServiceConnected");
            createList();
            if (mService.getCurrentPlaylist().size() == 0) {
                FloatingActionButton asd = findViewById(R.id.floatingShuffle);
                CoordinatorLayout.LayoutParams temp = (CoordinatorLayout.LayoutParams) asd.getLayoutParams();
                temp.bottomMargin = 112;
                asd.setLayoutParams(temp);
                findViewById(R.id.asd2).setVisibility(View.INVISIBLE);
            } else {
                FloatingActionButton fab = findViewById(R.id.floatingShuffle);
                CoordinatorLayout.LayoutParams temp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                temp.bottomMargin = 260;
                fab.setLayoutParams(temp);
                View v = findViewById(R.id.asd2);
                v.setVisibility(View.VISIBLE);
                View asd = findViewById(R.id.coverAlbumArtist);
                ConstraintLayout.LayoutParams x = (ConstraintLayout.LayoutParams) asd.getLayoutParams();
                if(x.bottomMargin<80)
                    x.bottomMargin = 160;
                asd.setLayoutParams(x);
                PlayerInterface.UpdateTrack(v, mService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
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
        setContentView(R.layout.activity_artist);

        artist = getIntent().getStringExtra("EXTRA_ARTIST");

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

        Switch asd = navigationView.getMenu().findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcharr);
        asd.setChecked(PreferencesHandler.getOffline(this));
        asd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHandler.setOffline(ctx, isChecked);
            finish();
            startActivity(new Intent(this, SearchActivity.class));
        });

        FloatingActionButton er = findViewById(R.id.floatingShuffle);
        er.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Intent dd = new Intent(v.getContext(), MainActivity.class);
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Play options")
                        .setItems(MediaPlayerService.playOptions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mService.updatePlaylist(DataBackend.getTracks(artist),0, true);
                                mService.replacementChoice=which;
                                startActivity(dd);
                            }});
                builder.show();
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

        listAlbums = DataBackend.getAlbums(artist);
        if(listAlbums!=null)
            createList();

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
            Intent asd = new Intent(this, PlaylistsActivity.class);
            startActivity(asd);
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
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_artists);
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

    private void createList() {
        RecyclerView.LayoutManager mLayoutManagerAlbum = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView mRecyclerViewAlbums = findViewById(R.id.coverAlbumArtist);
        mRecyclerViewAlbums.setLayoutManager(mLayoutManagerAlbum);
        mRecyclerViewAlbums.setAdapter(new MyListAdapter(new ArrayList(listAlbums), MyListAdapter.ARTALB_STATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        RecyclerView mRecyclerViewAlbums = findViewById(R.id.coverAlbumArtist);
        mRecyclerViewAlbums.setAdapter(null);
        if (mBound) {
            unbindService(mConnection);
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

    @Override
    public void onPreparedPlayback() {
        PlayerInterface.setPlay(findViewById(R.id.asd2));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View aa = findViewById(R.id.asd2);
        if(aa!=null && mService!=null && aa.getVisibility()==View.VISIBLE && hasFocus) {
            if (mService.isPlaying()) {
                PlayerInterface.setPlay(aa);
            } else {
                PlayerInterface.setStop(aa);
            }
        }
    }
}
