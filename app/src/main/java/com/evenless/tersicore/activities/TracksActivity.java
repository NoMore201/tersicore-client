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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by McPhi on 10/12/2017.
 */

public class TracksActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MediaPlayerServiceListener,
        ViewTreeObserver.OnWindowFocusChangeListener {

    private static final String TAG = "TracksActivity";
    private List<Track> listTracks;
    private String artist;
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
            if (mService.getCurrentPlaylist().size() == 0) {
                FloatingActionButton asd = findViewById(R.id.floatingActionButton);
                RelativeLayout.LayoutParams temp = (RelativeLayout.LayoutParams) asd.getLayoutParams();
                temp.bottomMargin = 112;
                asd.setLayoutParams(temp);
                findViewById(R.id.miniplayer).setVisibility(View.INVISIBLE);
            } else {
                View v = findViewById(R.id.miniplayer);
                FloatingActionButton fab = findViewById(R.id.floatingActionButton);
                RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) fab.getLayoutParams();
                if(params.bottomMargin<150)
                    params.bottomMargin = params.bottomMargin + v.getHeight();
                fab.setLayoutParams(params);
                v.setVisibility(View.VISIBLE);
                ListView asd = findViewById(R.id.listart);
                ConstraintLayout.LayoutParams x = (ConstraintLayout.LayoutParams) asd.getLayoutParams();
                x.bottomMargin=v.getHeight();
                asd.setLayoutParams(x);
                PlayerInterface.UpdateTrack(v, mService);
            }
            NavigationView navigationView = findViewById(R.id.nav_view);
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
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artist = getIntent().getStringExtra("EXTRA_ARTIST");

        setContentView(R.layout.activity_main4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (artist != null)
            toolbar.setTitle(artist);
        else
            toolbar.setTitle("Songs");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (artist == null)
            listTracks = DataBackend.getTracks();
        else
            listTracks = DataBackend.getTracks(artist);

        updateList();

        Switch asd = navigationView.getMenu().findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcharr);
        asd.setChecked(PreferencesHandler.getOffline(this));
        asd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHandler.setOffline(ctx, isChecked);
            finish();
            startActivity(new Intent(this, SearchActivity.class));
        });

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
            if(artist!=null) {
                listTracks = DataBackend.getTracks();
                artist=null;
                updateList();
                Toolbar temp = findViewById(R.id.toolbar);
                temp.setTitle("Songs");
            }
        } else if (id == R.id.nav_view) {

        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        if(artist==null)
            navigationView.setCheckedItem(R.id.nav_songs);
        else
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

    private void updateList() {
        ListView lsv = findViewById(R.id.listart);
        ArrayAdapter<Track> arrayAdapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                listTracks) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                text1.setText(listTracks.get(position).toString());
                text2.setText("from " + listTracks.get(position).album);
                return view;
            }
        };
        lsv.setAdapter(arrayAdapter);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                final List<Track> temp = new ArrayList<>();
                temp.add(listTracks.get(position));
                boolean askResource = PreferencesHandler.getPreferredQuality(ctx) == 6;
                if(!askResource) {
                    mService.seekToTrack(mService.append(temp));
                    Intent dd = new Intent(ctx, MainActivity.class);
                    startActivity(dd);
                }
                else{
                    AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
                    bd.setTitle("Resources");
                    CharSequence[] data = new CharSequence[temp.get(0).resources.size()];
                    int i=0;
                    for(TrackResources p : temp.get(0).resources) {
                        String toShow = "";
                        if(p.isDownloaded)
                            toShow += "Offline\n";
                        else
                            toShow += p.server + "\n";
                        data[i]=toShow + p.codec + " " + p.sample_rate / 1000 +
                                "Khz " + p.bitrate / 1000 + "kbps";
                        i++;
                    }
                    bd.setItems(data, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int w) {
                            mService.seekToTrack(mService.append(temp.get(0), w));
                            Intent dd = new Intent(ctx, MainActivity.class);
                            startActivity(dd);
                        }
                    });
                    bd.show();
                }
            }
        });

        lsv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                if (mBound) {
                    AlertDialogTrack.CreateDialogTrack(ctx, listTracks.get(pos), mService);
                } else {
                    //Alert service not bound yet
                    Log.i(TAG, "Service not bound yet");
                }
                return true;
            }
        });

        findViewById(R.id.floatingActionButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent dd = new Intent(v.getContext(), MainActivity.class);
                        mService.updatePlaylist(listTracks, 0, true);
                        startActivity(dd);
                    }
                }
        );
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
