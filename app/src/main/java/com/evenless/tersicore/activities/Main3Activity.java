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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Track;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.evenless.tersicore.view.NonScrollableListView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main3Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ApiRequestTaskListener, SearchView.OnQueryTextListener,
        MediaPlayerServiceListener{

    private Track[] listTracks = new Track[0];
    private  ArrayList<Track> listTracksFiltered = new ArrayList<>();
    private ArrayList<Track> listAlbums = new ArrayList<>();
    private  ArrayList<String> listArtists = new ArrayList<>();
    private Map<String, Bitmap> artistsCover;
    private static final String[] playOptions = {"Play now (Destroy queue)", "Play now (Maintain queue)", "Add To Playlist (Coda)", "Play After"};
    private Context ctx = this;
    private MediaPlayerService mService;
    private boolean mBound=false;
    private int page = 0;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setMediaPlayerServiceListener((MediaPlayerServiceListener) ctx);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistsCover = new HashMap<String, Bitmap>() {};
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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

        TextView smArtists = findViewById(R.id.tvsmartist);
        TextView smAlbums = findViewById(R.id.tvsmalbum);
        TextView smTracks = findViewById(R.id.tvsmlist);

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

        smArtists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                EditText editit = findViewById(R.id.searchone);
                listArtists = new ArrayList<>();
                for (int i = 0; i < listTracks.length; i++) {
                    if (artistSD(listTracks[i], editit.getText().toString()) && !listArtists.contains(listTracks[i].artist))
                        listArtists.add(listTracks[i].artist);
                }
                updateArtists();
            }
        });

        smAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                EditText editit = findViewById(R.id.searchone);
                listAlbums = new ArrayList<>();
                for (int i = 0; i < listTracks.length; i++) {
                    if (albumSD(listTracks[i], editit.getText().toString()) && !isParsedAlbum(listTracks[i].album))
                        listAlbums.add(listTracks[i]);
                }
                updateAlbums();
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
                                        case 1: mService.updatePlaylist(temp); startActivity(dd); break;
                                        case 2: mService.addToPlaylist(temp); break;
                                        case 3: mService.playAfter(temp); break;
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

    @Override
    public void onRequestComplete(String response) {
        try {
            listTracks = new Gson().fromJson(response, Track[].class);
            DataBackend.addTracks(new ArrayList<>(Arrays.asList(listTracks)));
        } catch (Exception e) {
            Log.e("Main3Activity", e.getMessage());
        }
    }

    @Override
    public void onApiRequestError(Exception e) {
        //Alert?
    }

    @Override
    public void onImgRequestComplete(String result, int id) {
        final ImageView temp = findViewById(id);
        if(temp!=null){
            try {
                JSONObject tempJson = new JSONObject(result);
                if(tempJson.has("artist")) {
                    JSONArray tmp = tempJson.getJSONObject("artist").getJSONArray("image");
                    final String link = tmp.getJSONObject(2).getString("#text");
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(link).getContent());
                    temp.setAlpha(0f);
                    temp.setImageBitmap(bitmap);
                    artistsCover.put(tempJson.getJSONObject("artist").getString("name"), bitmap);
                } else {
                    JSONArray tmp = tempJson.getJSONObject("album").getJSONArray("image");
                    final String link = tmp.getJSONObject(2).getString("#text");
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(link).getContent());
                    temp.setAlpha(0f);
                    temp.setImageBitmap(bitmap);
                    String key = tempJson.getJSONObject("album").getString("name") + tempJson.getJSONObject("album").getString("artist");
                    artistsCover.put(key, bitmap);
                }
             temp.animate().alpha(1f);
            } catch (JSONException e) {
                Log.e("Main3Activity", e.getMessage());
            } catch (MalformedURLException e) {
                Log.e("Main3Activity", e.getMessage());
            } catch (IOException e) {
                Log.e("Main3Activity", e.getMessage());
            } catch (Exception e) {
                Log.e("Main3Activity", e.getMessage());
            }
        }
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
        View v = findViewById(R.id.paddingbot);
        page=0;
        if(newT.length()!=0) {
            v.setVisibility(View.VISIBLE);
            boolean brokeTracks = false;
            boolean brokeArt = false;
            boolean brokeAlb = false;
            for (int i = 0; i < listTracks.length; i++) {
                if (asd(listTracks[i], newT)) {
                    if(listTracksFiltered.size()>2) {
                        brokeTracks = true;
                        break;
                    } else
                        listTracksFiltered.add(listTracks[i]);
                }
            }
            for (int i = 0; i < listTracks.length; i++) {
                if (albumSD(listTracks[i], newT) && !isParsedAlbum(listTracks[i].album))
                    if(listAlbums.size()>2) {
                        brokeAlb=true;
                        break;
                    } else
                        listAlbums.add(listTracks[i]);
            }
            for (int i = 0; i < listTracks.length; i++) {
                if (artistSD(listTracks[i], newT) && !listArtists.contains(listTracks[i].artist))
                    if(listArtists.size()>2) {
                        brokeArt=true;
                        break;
                    } else
                        listArtists.add(listTracks[i].artist);
            }
            TextView t1 = findViewById(R.id.tvArtists);
            TextView t2 = findViewById(R.id.tvsmartist);
            LinearLayout l1 = findViewById(R.id.linearArtists);
            if(listArtists.size()>0){
                t1.setVisibility(View.VISIBLE);
                if(brokeArt) {
                    t2.setVisibility(View.VISIBLE);
                } else {
                    t2.setVisibility(View.GONE);
                }
                l1.setVisibility(View.VISIBLE);
                updateArtists();
            } else {
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
                l1.setVisibility(View.GONE);
            }
            t1 = findViewById(R.id.tvAlbums);
            t2 = findViewById(R.id.tvsmalbum);
            l1 = findViewById(R.id.linearAlbums);
            if(listAlbums.size()>0){
                t1.setVisibility(View.VISIBLE);
                if(brokeAlb) {
                    t2.setVisibility(View.VISIBLE);
                } else {
                    t2.setVisibility(View.GONE);
                }
                l1.setVisibility(View.VISIBLE);
                updateAlbums();
            } else {
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
                l1.setVisibility(View.GONE);
            }
            t1 = findViewById(R.id.tvtracks);
            t2 = findViewById(R.id.tvsmlist);
            if(listTracksFiltered.size()>0){
                t1.setVisibility(View.VISIBLE);
                if(brokeTracks) {
                    t2.setVisibility(View.VISIBLE);
                } else {
                    t2.setVisibility(View.GONE);
                }
            } else {
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
            }
        } else {
            TextView t1 = findViewById(R.id.tvArtists);
            TextView t2 = findViewById(R.id.tvsmartist);
            LinearLayout l1 = findViewById(R.id.linearArtists);
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            l1.setVisibility(View.GONE);
            t1 = findViewById(R.id.tvAlbums);
            t2 = findViewById(R.id.tvsmalbum);
            l1 = findViewById(R.id.linearAlbums);
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            l1.setVisibility(View.GONE);
            t1 = findViewById(R.id.tvtracks);
            t2 = findViewById(R.id.tvsmlist);
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
        }
        updateList();
        return false;
    }

    private boolean isParsedAlbum(String listTrack) {
        for(int i=0; i<listAlbums.size(); i++){
            if (listAlbums.get(i).album.equals(listTrack)){
                return true;
            }
        }
        return false;
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

    private void updateAlbums() {
        LinearLayout l1 = findViewById(R.id.linearAlbums);
        l1.removeAllViewsInLayout();
        LinearLayout l2 = createLinearLayout();
        for (int i = 0; i < listAlbums.size(); i++) {
            if(i%3==0 && i!=0){
                l1.addView(l2);
                l2 = createLinearLayout();
            }
            LinearLayout temp = new LinearLayout(ctx);
            temp.setOrientation(LinearLayout.VERTICAL);
            temp.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            ));
            ImageView timg = new ImageView(ctx);
            ViewGroup.MarginLayoutParams imgparam = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timg.setLayoutParams(imgparam);
            timg.setCropToPadding(true);
            timg.setPadding(5,0,5,0);
            timg.setId(View.generateViewId());
            timg.setImageBitmap(getAlbumCover(listAlbums.get(i), timg.getId()));
            timg.setAdjustViewBounds(true);
            temp.addView(timg);
            TextView ttxt = new TextView(ctx);
            imgparam = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imgparam.setMargins(0, 0, 10, 0);
            ttxt.setLayoutParams(imgparam);
            ttxt.setText(listAlbums.get(i).album);
            temp.addView(ttxt);
            l2.addView(temp);
        }
        l1.addView(l2);
    }

    private Bitmap getArtistImage(String s, int id) {
        if(artistsCover.containsKey(s))
            return artistsCover.get(s);
        else {
            try {
                TaskHandler.getImages(this, s, id);
            } catch (Exception e) {
                Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return BitmapFactory.decodeResource(this.getResources(), R.drawable.nocover);
        }
    }

    private Bitmap getAlbumCover(Track tr, int id){
        if(artistsCover.containsKey(tr.album + tr.artist))
            return artistsCover.get(tr.album + tr.artist);
        else {
            try {
                TaskHandler.getCoversWeb(this, tr.artist, tr.album, id);
            } catch (Exception e) {
                Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return BitmapFactory.decodeResource(this.getResources(), R.drawable.nocover);
        }
    }

    private LinearLayout createLinearLayout(){
        LinearLayout temp = new LinearLayout(ctx);
        temp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        temp.setWeightSum(3);
        temp.setOrientation(LinearLayout.HORIZONTAL);
        return temp;
    }

    private void updateArtists() {
        LinearLayout l1 = findViewById(R.id.linearArtists);
        l1.removeAllViewsInLayout();
        LinearLayout l2 = createLinearLayout();
        for (int i = 0; i < listArtists.size(); i++) {
            if(i%3==0 && i!=0){
                    l1.addView(l2);
                    l2 = createLinearLayout();
            }
            LinearLayout temp = new LinearLayout(ctx);
            temp.setOrientation(LinearLayout.VERTICAL);
            temp.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            ));
            ImageView timg = new ImageView(ctx);
            ViewGroup.MarginLayoutParams imgparam = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timg.setLayoutParams(imgparam);
            timg.setCropToPadding(true);
            timg.setPadding(5,0,5,0);
            timg.setId(View.generateViewId());
            timg.setImageBitmap(getArtistImage(listArtists.get(i), timg.getId()));
            timg.setAdjustViewBounds(true);
            temp.addView(timg);
            TextView ttxt = new TextView(ctx);
            imgparam = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imgparam.rightMargin=10;
            ttxt.setLayoutParams(imgparam);
            ttxt.setText(listArtists.get(i));
            temp.addView(ttxt);
            l2.addView(temp);
        }
        l1.addView(l2);
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
        ImageView img = findViewById(id);
        if(tr.resources.get(0).cover_data!=null && img!=null) {
            img.setImageBitmap(BitmapFactory.decodeByteArray(
                    tr.resources.get(0).cover_data, 0,
                    tr.resources.get(0).cover_data.length));
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

}
