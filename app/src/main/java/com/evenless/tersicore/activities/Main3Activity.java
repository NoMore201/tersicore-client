package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.evenless.tersicore.view.NonScrollableListView;
import com.google.gson.Gson;

public class Main3Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ApiRequestTaskListener, SearchView.OnQueryTextListener,
        MediaPlayerServiceListener {

    public static final String[] playOptions = {"Play now (Destroy queue)", "Play now (Maintain queue)", "Add To Playlist (Coda)", "Play After"};

    private Track[] listTracks = new Track[0];
    private ArrayList<Track> listTracksFiltered = new ArrayList<>();
    private ArrayList<Album> listAlbums = new ArrayList<>();
    private ArrayList<String> listArtists = new ArrayList<>();
    private Map<String, Bitmap> artistsCover;
    private Context ctx = this;
    private boolean mBound=false;
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerViewAlbums;
    private int page = 0;
    private MediaPlayerService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setMediaPlayerServiceListener((MediaPlayerServiceListener) ctx);
            artistsCover = mService.artistsCover;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound=false;
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
            Log.e("Main3Activity", e.getMessage());
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

        TextView smTracks = findViewById(R.id.tvsmlist);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.LayoutManager mLayoutManagerAlbum = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = findViewById(R.id.artistlistview);
        mRecyclerViewAlbums = findViewById(R.id.coverlistview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerViewAlbums.setLayoutManager(mLayoutManagerAlbum);

        final EditText editit = findViewById(R.id.searchone);
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

        smTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editit = findViewById(R.id.searchone);
                boolean brokeTracks = false;
                for (int i = (3 + page*10); i < listTracks.length; i++) {
                    if (asd(listTracks[i], editit.getText().toString()) && !listTracksFiltered.contains(listTracks[i]))
                        if(listTracksFiltered.size()>2 + ((page+1) *10)) {
                            brokeTracks = true;
                            page ++;
                            break;
                        } else
                            listTracksFiltered.add(listTracks[i]);
                }

                if(!brokeTracks)
                    v.setVisibility(View.GONE);

                updateList();
            }
        });

        NonScrollableListView lsv = findViewById(R.id.listtr);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {
                Track[] temp = new Track[1];
                temp[0] = listTracksFiltered.get(position);
                mService.playNow(temp);
                Intent dd = new Intent(ctx, MainActivity.class);
                startActivity(dd);
            }
        });

        lsv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                final int position = pos;
                final Track[] temp = {listTracksFiltered.get(position)};
                if(mBound) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(playOptions, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent dd = new Intent(ctx, MainActivity.class);
                                    switch (which){
                                        case 0: mService.updatePlaylist(temp);  startActivity(dd); break;
                                        case 1: mService.playNow(temp); startActivity(dd); break;
                                        case 2: mService.addToPlaylist(temp); startActivity(dd); break;
                                        case 3: mService.playAfter(temp); startActivity(dd); break;
                                        default: break;
                                    }
                                }
                            });
                    builder.create().show();
                } else {
                    //Alert service not bound yet
                    Log.i("Home", "Service not bound yet");
                }
                return true;
            }
        });

        navigationView.setCheckedItem(R.id.nav_home);
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
            Intent asd = new Intent(this, AlbumsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_artists) {
            Intent asd = new Intent(this, ArtistsActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_dj) {

        } else if (id == R.id.nav_home) {

        } else if (id == R.id.nav_playlists) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_songs) {
            Intent asd = new Intent(this, TracksActivity.class);
            startActivity(asd);
        } else if (id == R.id.nav_view) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newT) {
        listTracksFiltered = new ArrayList<>();
        listAlbums = new ArrayList<>();
        listArtists = new ArrayList<>();
        page=0;
        if(newT.length()!=0) {
            boolean brokeTracks = false;
            for (int i = 0; i < listTracks.length; i++) {
                if (asd(listTracks[i], newT)) {
                    if(listTracksFiltered.size()>2) {
                        brokeTracks = true;
                        break;
                    } else {
                        listTracksFiltered.add(listTracks[i]);
                    }
                }
            }
            for (int i = 0; i < listTracks.length; i++) {
                if (albumSD(listTracks[i], newT) && !isParsedAlbum(listTracks[i])){
                        Track temp = listTracks[i];
                        if(temp.album_artist!=null)
                            listAlbums.add(new Album(temp.album, temp.album_artist));
                        else
                            listAlbums.add(new Album(temp.album, temp.artist));
                    }
            }
            for (int i = 0; i < listTracks.length; i++) {
                if (artistSD(listTracks[i], newT) && !listArtists.contains(listTracks[i].artist))
                        listArtists.add(listTracks[i].artist);
            }
            TextView t1 = findViewById(R.id.tvArtists);
            if(listArtists.size()>0){
                t1.setVisibility(View.VISIBLE);
            } else {
                t1.setVisibility(View.GONE);
            }
            t1 = findViewById(R.id.tvAlbums);
            if(listAlbums.size()>0){
                t1.setVisibility(View.VISIBLE);
            } else {
                t1.setVisibility(View.GONE);
            }
            t1 = findViewById(R.id.tvtracks);
            if(listTracksFiltered.size()>0)
                t1.setVisibility(View.VISIBLE);
            else
                t1.setVisibility(View.GONE);
        } else {
            TextView t1 = findViewById(R.id.tvArtists);
            t1.setVisibility(View.GONE);
            t1 = findViewById(R.id.tvAlbums);
            t1.setVisibility(View.GONE);
            t1 = findViewById(R.id.tvtracks);
            t1.setVisibility(View.GONE);
        }
        updateList();
        mRecyclerView.setAdapter(new MyListAdapter(listArtists, artistsCover, MyListAdapter.ARTIST_STATE));
        mRecyclerViewAlbums.setAdapter(new MyListAdapter(listAlbums, artistsCover, MyListAdapter.ALBUMS_STATE));
        return false;
    }

    private boolean isParsedAlbum(Track temp) {
        Album asd;
        if(temp.album_artist!=null)
            asd = new Album(temp.album, temp.album_artist);
        else
            asd = new Album(temp.album, temp.artist);

        return listAlbums.contains(asd);
    }

    private void updateList() {
        NonScrollableListView lsv = findViewById(R.id.listtr);
        ArrayAdapter<Track> arrayAdapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                listTracksFiltered ){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                text1.setText(listTracksFiltered.get(position).toString());
                text1.setMaxLines(1);
                text2.setText("from " + listTracksFiltered.get(position).album);
                text2.setMaxLines(1);
                return view;
            }
        };
        lsv.setAdapter(arrayAdapter);
    }

    public boolean asd(Track s, String t){
        String newText = t.toLowerCase();
        return (s.title!=null && (s.title.toLowerCase().startsWith(newText) || s.title.toLowerCase().contains(" " + newText)));
    }

    public boolean artistSD(Track s, String t){
        String newText = t.toLowerCase();
        return (s.artist!=null && (s.artist.toLowerCase().startsWith(newText)|| s.artist.toLowerCase().contains(" " + newText)));
    }

    public boolean albumSD(Track s, String t){
        String newText = t.toLowerCase();
        return (s.album!=null && (s.album.toLowerCase().startsWith(newText)|| s.album.toLowerCase().contains(" " + newText))) ;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        //Update future Miniplayer
    }

    @Override
    public void onPlaylistComplete() {
        //Update future Miniplayer
    }

    @Override
    public void onCoverFetched(Track tr, int id) {
        Bitmap image = BitmapFactory.decodeByteArray(
                    tr.resources.get(0).cover_data, 0,
                    tr.resources.get(0).cover_data.length);
        if(id==MyListAdapter.ARTIST_STATE){
            artistsCover.put(tr.artist, image);
            int tempid=listArtists.indexOf(tr.artist);
            if(tempid!=-1)
                mRecyclerView.getAdapter().notifyItemChanged(tempid);
        } else if (id==MyListAdapter.ALBUMS_STATE){
            artistsCover.put(tr.album + tr.artist, image);
            int tempid = listAlbums.indexOf(tr);
            if(tempid!= -1)
                mRecyclerViewAlbums.getAdapter().notifyItemChanged(tempid);
        }
    }

    @Override
    public void onPlaybackError(Exception exception) {
        //Update future Miniplayer
    }

    @Override
    public void onPlaybackProgressUpdate(int currentMilliseconds) {
        //Update future Miniplayer
    }

    @Override
    public void onRequestComplete(String response, Exception e) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            listTracks = new Gson().fromJson(response, Track[].class);
            DataBackend.addTracks(new ArrayList<>(Arrays.asList(listTracks)));
        }
    }
}
