package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Arrays;
import com.google.gson.Gson;

public class Main3Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ApiRequestTaskListener, SearchView.OnQueryTextListener {

    Track[] listTracks = new Track[0];
    ArrayList<Track> listTracksFiltered = new ArrayList<>();

    Context ctx = this;
    private MediaPlayerService mService;
    private boolean mBound=false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (DataBackend.getTracks().size() != 0) {
                listTracks = new Track[DataBackend.getTracks().size()];
                DataBackend.getTracks().toArray(listTracks);
            } else
                try {
                    TaskHandler.getTracks(this, PreferencesHandler.getServer(this));
                } catch (Exception e) {
                    listTracks = new Track[0];
                }
        } catch (Exception e){
            Log.e("Player", e.getMessage());
        }
        setContentView(R.layout.activity_main3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<Track> arrayAdapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_1,
                listTracksFiltered );

        EditText editit = findViewById(R.id.searchone);
        editit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    onQueryTextChange(s.toString());
                } catch (Exception e) {
                    Log.e("Main3Activity", e.getMessage());
                }
            }
        });

        ListView lsv = findViewById(R.id.listtr);
        lsv.setAdapter(arrayAdapter);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {
                if(mBound) {
                    Track[] temp = new Track[1];
                    temp[0] = listTracksFiltered.get(position);
                    mService.updatePlaylist(temp);
                    Intent asd = new Intent(ctx, MainActivity.class);
                    startActivity(asd);
                } else {
                    //Alert service not bound yet
                    Log.i("Home", "Service not bound yet");
                }
            }
        });
        lsv.setAdapter(arrayAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_favorite:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_albums) {
            // Handle the camera action
        } else if (id == R.id.nav_artists) {

        } else if (id == R.id.nav_dj) {

        } else if (id == R.id.nav_home) {

        } else if (id == R.id.nav_playlists) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_songs) {

        } else if (id == R.id.nav_view) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*@Override
    public void onNewTrackPlaying(Track newTrack) {
        // Update miniplayer
    }

    @Override
    public void onPlaylistComplete() {
        //Hide Miniplayer
    }

    @Override
    public void onCoverFetched(Track track) {
        //Update Miniplayer
    }

    @Override
    public void onPlaybackError(Exception exception) {
        //Alert?
    }*/

    @Override
    public void onRequestComplete(String response) {
        Log.i("Main3Activity", response.substring(0,100));
        try {
            listTracks = new Gson().fromJson(response, Track[].class);
            DataBackend.addTracks(new ArrayList<>(Arrays.asList(listTracks)));
        } catch (Exception e) {
            Log.e("Main3Activity", e.getMessage());
        }
        Log.i("Main3Activity", listTracks[0].toString());
    }

    @Override
    public void onApiRequestError(Exception e) {
        //Alert?
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newT) {
        listTracksFiltered = new ArrayList<>();
        if(newT.length()!=0) {
            for (int i = 0; i < listTracks.length; i++)
                if (asd(listTracks[i], newT))
                    listTracksFiltered.add(listTracks[i]);
        }
        ListView lsv = findViewById(R.id.listtr);
        ArrayAdapter<Track> arrayAdapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_1,
                listTracksFiltered );
        lsv.setAdapter(arrayAdapter);
        return false;
    }

    public boolean asd(Track s, String t){
        String newText = t.toLowerCase();
        return (s.title!=null && s.title.toLowerCase().contains(newText)) || (s.album!=null && s.album.toLowerCase().contains(newText)) ||
                (s.artist!=null && s.artist.toLowerCase().contains(newText));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }
}
