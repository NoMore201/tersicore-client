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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Track;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;

/**
 * Created by McPhi on 10/12/2017.
 */

public class AlbumsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MediaPlayerServiceListener,
        ViewTreeObserver.OnWindowFocusChangeListener
{

    private List<Album> listAlbums;
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
                findViewById(R.id.miniplayer).setVisibility(View.INVISIBLE);
            } else {
                FloatingActionButton floating = findViewById(R.id.floatingActionButton);
                View miniplayer = findViewById(R.id.miniplayer);
                RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) floating.getLayoutParams();
                if(params.bottomMargin<150)
                    params.bottomMargin = params.bottomMargin + miniplayer.getHeight();
                floating.setLayoutParams(params);
                miniplayer.setVisibility(View.VISIBLE);
                ListView list = findViewById(R.id.listart);
                ConstraintLayout.LayoutParams x =
                        (ConstraintLayout.LayoutParams) list.getLayoutParams();
                x.bottomMargin = miniplayer.getHeight();
                list.setLayoutParams(x);
                PlayerInterface.UpdateTrack(findViewById(R.id.miniplayer), mService);
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
    protected void onResume() {
        super.onResume();
        listAlbums = DataBackend.getAlbums();
        if (listAlbums != null)
            createList();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_albums);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.floatingActionButton).setOnClickListener(
                v -> {
                    final Intent dd = new Intent(v.getContext(), MainActivity.class);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(MediaPlayerService.playOptions, (dialog, which) -> {
                                mService.updatePlaylist(DataBackend.getTracks(),0, true);
                                mService.replacementChoice=which;
                                startActivity(dd);
                            });
                    builder.show();
                }
        );
        Switch asd = navigationView.getMenu()
                .findItem(R.id.app_bar_switch)
                .getActionView()
                .findViewById(R.id.switcharr);
        asd.setChecked(PreferencesHandler.getOffline(this));
        asd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHandler.setOffline(ctx, isChecked);
            finish();
            startActivity(getIntent());
        });
    }

    private void createList() {
        ListView lsv = findViewById(R.id.listart);
        ArrayAdapter<Album> arrayAdapter = new ArrayAdapter<Album>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                listAlbums ){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                text1.setText(listAlbums.get(position).name);
                text2.setText("by " + listAlbums.get(position).artist);
                return view;
            }
        };
        lsv.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {
                Intent asd = new Intent(v.getContext(), SingleAlbumActivity.class);
                asd.putExtra("EXTRA_ARTIST", listAlbums.get(position).artist);
                asd.putExtra("EXTRA_ALBUM", listAlbums.get(position).name);
                v.getContext().startActivity(asd);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_albums:
                break;
            case R.id.nav_artists:
                startActivity(new Intent(this, ArtistsActivity.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.nav_playlists:
                startActivity(new Intent(this, PlaylistsActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_songs:
                startActivity(new Intent(this, TracksActivity.class));
                break;
            case R.id.nav_view:
                break;
            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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

    @Override
    public void onPreparedPlayback() {
        PlayerInterface.setPlay(findViewById(R.id.miniplayer));
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
