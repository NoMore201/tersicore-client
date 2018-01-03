package com.evenless.tersicore.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.MyListAdapter;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.evenless.tersicore.view.NonScrollableListView;
import com.google.gson.Gson;

import io.realm.RealmList;

public class SearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ApiRequestTaskListener,
        SearchView.OnQueryTextListener,
        MediaPlayerServiceListener,
        AdapterView.OnItemLongClickListener,
        AdapterView.OnItemClickListener {

    private static final String TAG = "SearchActivity";

    private Track[] listTracks = new Track[0];
    private ArrayList<Track> listTracksFiltered = new ArrayList<>();
    private ArrayList<Album> listAlbums = new ArrayList<>();
    private ArrayList<String> listArtists = new ArrayList<>();
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
            if (mService.getCurrentPlaylist().size() == 0) {
                findViewById(R.id.asd2).setVisibility(View.GONE);
            } else {
                View v = findViewById(R.id.asd2);
                v.setVisibility(View.VISIBLE);
                LinearLayout asd = findViewById(R.id.linearLayout4);
                ConstraintLayout.LayoutParams x = (ConstraintLayout.LayoutParams) asd.getLayoutParams();
                x.bottomMargin = x.bottomMargin + v.getHeight();
                asd.setLayoutParams(x);
                PlayerInterface.UpdateTrack(v, mService);
            }
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
            Log.e(TAG, e.getMessage());
        }
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView smTracks = findViewById(R.id.tvsmlist);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false);
        RecyclerView.LayoutManager mLayoutManagerAlbum = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false);

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
                    Log.e("SearchActivity", e.getMessage());
                }
            }
        });

        smTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editit = findViewById(R.id.searchone);
                boolean brokeTracks = false;
                for (int i = (3 + page*10); i < listTracks.length; i++) {
                    if (checkTrackByTitle(listTracks[i], editit.getText().toString()) && !listTracksFiltered.contains(listTracks[i]))
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
        lsv.setOnItemClickListener(this);

        lsv.setOnItemLongClickListener(this);

        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mService.seekToTrack(mService.append(listTracksFiltered.get(i)));
        Intent dd = new Intent(ctx, MainActivity.class);
        startActivity(dd);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
        if(mBound) {
            AlertDialogTrack.CreateDialogTrack(ctx, listTracksFiltered.get(pos), mService);
        } else {
            //Alert service not bound yet
            Log.i("Home", "Service not bound yet");
        }
        return true;
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
            } else
                super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_favorite:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                ImageButton asdasd = drawer.findViewById(R.id.showmail);
                asdasd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent sdsd = new Intent(ctx, EmailsActivity.class);
                        startActivity(sdsd);
                    }
                });
                drawer.openDrawer(GravityCompat.END);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        listTracksFiltered = new ArrayList<>();
        listAlbums = new ArrayList<>();
        listArtists = new ArrayList<>();
        page=0;
        if(query.length()!=0) {
            for (Track t : listTracks) {
                if (listTracksFiltered.size() > 2) {
                    break;
                }
                if (checkTrackByTitle(t, query)) {
                    listTracksFiltered.add(t);
                }
            }
            for (Track t: listTracks) {
                if (checkTrackByAlbum(t, query) && !isParsedAlbum(t)) {
                    if (t.album_artist != null) {
                        listAlbums.add(new Album(t.album, t.album_artist));
                    } else {
                        listAlbums.add(new Album(t.album, t.artist));
                    }
                }
            }
            for (Track t : listTracks) {
                if (checkTrackByArtist(t, query) && !listArtists.contains(t.artist)) {
                    listArtists.add(t.artist);
                }
            }

            TextView t1 = findViewById(R.id.tvArtists);
            if (listArtists.size()>0) {
                t1.setVisibility(View.VISIBLE);
            } else {
                t1.setVisibility(View.GONE);
            }
            t1 = findViewById(R.id.tvAlbums);
            if (listAlbums.size()>0) {
                t1.setVisibility(View.VISIBLE);
            } else {
                t1.setVisibility(View.GONE);
            }
            t1 = findViewById(R.id.tvtracks);
            if (listTracksFiltered.size()>0)
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
        mRecyclerView.setAdapter(new MyListAdapter(listArtists, MyListAdapter.ARTIST_STATE));
        mRecyclerViewAlbums.setAdapter(new MyListAdapter(listAlbums, MyListAdapter.ALBUMS_STATE));
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

            @SuppressLint("SetTextI18n")
            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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

    public boolean checkTrackByTitle(Track s, String t){
        String newText = t.toLowerCase();
        return (s.title!=null && (s.title.toLowerCase().startsWith(newText) || s.title.toLowerCase().contains(" " + newText)));
    }

    public boolean checkTrackByArtist(Track s, String t){
        String newText = t.toLowerCase();
        return (s.artist!=null && (s.artist.toLowerCase().startsWith(newText)|| s.artist.toLowerCase().contains(" " + newText)));
    }

    public boolean checkTrackByAlbum(Track s, String t){
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
    public void onCoverFetched(Track tr, int id) {
        Bitmap image = BitmapFactory.decodeByteArray(
                    tr.resources.get(0).cover_data, 0,
                    tr.resources.get(0).cover_data.length);
        if(id==MyListAdapter.ARTIST_STATE){
            int tempid=listArtists.indexOf(tr.artist);
            if(tempid!=-1)
                mRecyclerView.getAdapter().notifyItemChanged(tempid);
        } else if (id==MyListAdapter.ALBUMS_STATE){
            int tempid = listAlbums.indexOf(tr);
            if(tempid!= -1)
                mRecyclerViewAlbums.getAdapter().notifyItemChanged(tempid);
        }
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
            DataBackend.insertTracks(new ArrayList<>(Arrays.asList(listTracks)));
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
    public void onPlaybackError(Exception exception) {
        PlayerInterface.setStop(findViewById(R.id.asd2));
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
}
