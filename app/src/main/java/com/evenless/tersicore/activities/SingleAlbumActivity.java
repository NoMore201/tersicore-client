package com.evenless.tersicore.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.CoverDownloadTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.ImageRequestTaskListener;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Cover;
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
import java.util.Locale;

public class SingleAlbumActivity  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ApiRequestTaskListener,
        MediaPlayerServiceListener, ImageRequestTaskListener, CoverDownloadTaskListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

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
            mService.setMediaPlayerServiceListener((MediaPlayerServiceListener) ctx);
            mBound=true;
            if (mService.getCurrentPlaylist().size() == 0) {
                findViewById(R.id.asd2).setVisibility(View.GONE);
            } else {
                View v = findViewById(R.id.asd2);
                v.setVisibility(View.VISIBLE);
                RelativeLayout asd = findViewById(R.id.listalbumR);
                asd.setMinimumHeight(asd.getHeight() + v.getHeight());
                PlayerInterface.UpdateTrack(v, mService);
            }
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
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artist = getIntent().getStringExtra("EXTRA_ARTIST");
        albumName = getIntent().getStringExtra("EXTRA_ALBUM");

        setContentView(R.layout.activity_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(albumName!=null)
            toolbar.setTitle(albumName);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        Intent intent;
        switch (id) {
            case R.id.nav_albums:
                intent = new Intent(this, AlbumsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_artists:
                intent = new Intent(this, ArtistsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_home:
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_playlists:
                intent = new Intent(this, PlaylistsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_songs:
                intent = new Intent(this, TracksActivity.class);
                startActivity(intent);
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
    public void onRequestComplete(String response, Exception e) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            DataBackend.insertTracks(new ArrayList<>(Arrays.asList(new Gson().fromJson(response, Track[].class))));
            listTracks=DataBackend.getTracks(artist, albumName);
            updateList();
        }
    }

    private void updateList() {
        ListView lsv = findViewById(R.id.listalbum);
        TextView aln = findViewById(R.id.albumname);
        TextView arn = findViewById(R.id.artistname);
        TextView y = findViewById(R.id.year);
        aln.setText(albumName);
        arn.setText(artist);
        if (listTracks.size() > 0 && listTracks.get(0).date != null) {
            SimpleDateFormat format =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
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
                listTracks) {

            @SuppressLint("SetTextI18n")
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                Track temp = listTracks.get(position);
                if (temp.track_number.contains("/")) {
                    text1.setText(temp.track_number.substring(0, temp.track_number.indexOf("/"))
                            + ". " + listTracks.get(position).title);
                } else {
                    text1.setText(temp.track_number + ". " + listTracks.get(position).title);
                }
                if(position==listTracks.size()-1) {
                    ScrollView main = (ScrollView) findViewById(R.id.mainScrollAlbumView).getParent();
                    main.scrollTo(0, 0);
                }
                return view;
            }
        };
        lsv.setAdapter(arrayAdapter);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(this);

        lsv.setOnItemLongClickListener(this);

        findViewById(R.id.playbutt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.updatePlaylist(listTracks, 0, false);
                Intent dd = new Intent(ctx, MainActivity.class);
                startActivity(dd);
            }
        });
        Cover asd = DataBackend.getCover(artist, albumName);
        if(asd==null)
            try {
                TaskHandler.getAlbumImageFromApi(this, artist, albumName, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        else{
            ImageView temp = findViewById(R.id.coverAlbum);
            temp.setImageBitmap(BitmapFactory.decodeByteArray(asd.cover,0,asd.cover.length));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
        if(mBound) {
            AlertDialogTrack.CreateDialogTrack(ctx, listTracks.get(pos), mService);
        } else {
            //Alert service not bound yet
            Log.i(TAG, "Service not bound yet");
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mService.updatePlaylist(listTracks, i, false);
        Intent dd = new Intent(ctx, MainActivity.class);
        startActivity(dd);
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
}