package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.CoverDownloadTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.ImageRequestTaskListener;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Track;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by McPhi on 10/12/2017.
 */

public class SingleAlbumActivity  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ApiRequestTaskListener,
        MediaPlayerServiceListener, ImageRequestTaskListener, CoverDownloadTaskListener {

    private static final String TAG = "TracksActivity";
    private List<Track> listTracks;
    private String albumName;
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
            mBound=true;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artist = getIntent().getStringExtra("EXTRA_ARTIST");
        albumName = getIntent().getStringExtra("EXTRA_ALBUM");

        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(albumName!=null)
            toolbar.setTitle(albumName);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_songs);

        try {
            if (DataBackend.getArtists().size() != 0) {
                listTracks=DataBackend.getTracks(artist, albumName);
                updateList();
            } else
                try {
                    TaskHandler.getTracks(this, PreferencesHandler.getServer(this));
                } catch (Exception e) {
                    listTracks = new ArrayList<>();
                }
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
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
            listTracks=DataBackend.getTracks(artist, albumName);
            updateList();
        }
    }

    private void updateList(){
        ListView lsv = findViewById(R.id.listalbum);
        TextView aln = findViewById(R.id.albumname);
        TextView arn = findViewById(R.id.artistname);
        TextView y = findViewById(R.id.year);
        aln.setText(albumName);
        arn.setText(artist);
        if(listTracks.size()>0 && listTracks.get(0).date!=null) {
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            try {
                Date date = format.parse(listTracks.get(0).date);
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                y.setText(year + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<Track> arrayAdapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listTracks ){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                Track temp = listTracks.get(position);
                text1.setText(temp.track_number.substring(0, temp.track_number.indexOf("/"))
                        + ". " + listTracks.get(position).title);
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
                mService.updatePlaylist(listTracks, position);
                Intent dd = new Intent(ctx, MainActivity.class);
                startActivity(dd);
            }
        });

        lsv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                final int position = pos;
                final Track[] temp = {listTracks.get(position)};
                if(mBound) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(Main3Activity.playOptions, new DialogInterface.OnClickListener() {
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
                    Log.i(TAG, "Service not bound yet");
                }
                return true;
            }
        });

        findViewById(R.id.playbutt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.updatePlaylist(listTracks, 0);
                Intent dd = new Intent(ctx, MainActivity.class);
                startActivity(dd);
            }
        });

        try {
            TaskHandler.getAlbumImageFromApi(this, artist, albumName, 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {

    }

    @Override
    public void onPlaylistComplete() {

    }

    @Override
    public void onCoverFetched(Track track, int id) {

    }

    @Override
    public void onPlaybackError(Exception exception) {

    }

    @Override
    public void onPlaybackProgressUpdate(int currentMilliseconds) {

    }

    @Override
    public void onImgRequestComplete(String result, int state, String key, Exception ex) {
        if(ex!=null){
            Log.e(TAG, ex.getMessage());
        } else try {
            JSONObject tempJson = new JSONObject(result);
            JSONArray tmp = tempJson.getJSONObject("album").getJSONArray("image");
            TextView g = findViewById(R.id.genre);
            g.setText(tempJson.getJSONObject("album").getJSONObject("tags").getJSONArray("tag").getJSONObject(0).getString("name"));
            final String link = tmp.getJSONObject(3).getString("#text");
            TaskHandler.downloadCover(link, 0, key, this);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void OnCoverDownloaded(Bitmap result, int mState, String key) {
        ImageView temp = findViewById(R.id.coverAlbum);
        temp.setImageBitmap(result);
    }
}