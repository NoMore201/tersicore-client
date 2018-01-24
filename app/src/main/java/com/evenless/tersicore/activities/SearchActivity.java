package com.evenless.tersicore.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.MyUsersListAdapter;
import com.evenless.tersicore.interfaces.ApiRequestExtraTaskListener;
import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.MyListAdapter;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Track;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.TrackSuggestion;
import com.evenless.tersicore.model.User;
import com.evenless.tersicore.view.NonScrollableListView;
import com.google.gson.Gson;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ApiRequestExtraTaskListener,
        SearchView.OnQueryTextListener,
        MediaPlayerServiceListener,
        AdapterView.OnItemLongClickListener,
        AdapterView.OnItemClickListener,
        ViewTreeObserver.OnWindowFocusChangeListener {

    private static final String TAG = "SearchActivity";

    private List<Track> listTracks = new ArrayList<>();
    private ArrayList<Track> listTracksFiltered = new ArrayList<>();
    private ArrayList<Album> listAlbums = new ArrayList<>();
    private ArrayList<Album> listRecentAlbums = new ArrayList<>();
    private ArrayList<Album> listRecentUpAlbums = new ArrayList<>();
    private ArrayList<String> listArtists = new ArrayList<>();
    private ArrayList<TrackSuggestion> listSugg = new ArrayList<>();
    public static ArrayList<User> users = new ArrayList<>();
    private int newMessages = 0;
    private Context ctx = this;
    private boolean mBound=false;
    private RecyclerView mRecyclerView;
    private User me;
    private Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
    private Runnable workRunnable;
    private RecyclerView mRecyclerViewAlbums;
    private RecyclerView mRecyclerViewAlbumsRecent;
    private Gson gson = new Gson();
    private int page = 0;
    private boolean isFirstTime=false;
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
                findViewById(R.id.asd2).setVisibility(View.INVISIBLE);
            } else {
                View v = findViewById(R.id.asd2);
                v.setVisibility(View.VISIBLE);
                LinearLayout asd = findViewById(R.id.linearLayout4);
                ConstraintLayout.LayoutParams x = (ConstraintLayout.LayoutParams) asd.getLayoutParams();
                if(x.bottomMargin<200)
                    x.bottomMargin = x.bottomMargin + 200;
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
        mRecyclerViewAlbumsRecent = findViewById(R.id.coverlistrecentview);

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
                    handler.removeCallbacks(workRunnable);
                    workRunnable = () -> onQueryTextChange(s.toString());
                    handler.postDelayed(workRunnable, 200 /*delay*/);
                } catch (Exception e) {
                    Log.e("SearchActivity", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        PreferencesHandler.setOffline(this, PreferencesHandler.getOffline(ctx));

        smTracks.setOnClickListener(v -> {
            EditText editit1 = findViewById(R.id.searchone);
            boolean brokeTracks = false;
            for (int i = (3 + page*10); i < listTracks.size(); i++) {
                if (checkTrackByTitle(listTracks.get(i), editit1.getText().toString()) && !listTracksFiltered.contains(listTracks.get(i)))
                    if(listTracksFiltered.size()>2 + ((page+1) *10)) {
                        brokeTracks = true;
                        page ++;
                        break;
                    } else
                        listTracksFiltered.add(listTracks.get(i));
            }

            if(!brokeTracks)
                v.setVisibility(View.GONE);

            updateList();
        });

        NonScrollableListView lsv = findViewById(R.id.listtr);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(this);

        lsv.setOnItemLongClickListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        Switch asd = navigationView.getMenu().findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcharr);
        asd.setChecked(PreferencesHandler.getOffline(this));
        asd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHandler.setOffline(ctx, isChecked);
            finish();
            startActivity(new Intent(this, SearchActivity.class));
        });
        List<String> servers = PreferencesHandler.getServer(ctx);
        if (DataBackend.isFirstTime() && !PreferencesHandler.offline)
            try {
                isFirstTime=true;
                TaskHandler.getTracks((ApiRequestExtraTaskListener) ctx, servers.get(0));
                PreferencesHandler.setLastUpdate(ctx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(PreferencesHandler.offline)
            listTracks=DataBackend.findAllOffline();
        else
            DataBackend.getInstance().where(Track.class).findAllAsync().addChangeListener(callback);
    }

    private OrderedRealmCollectionChangeListener<RealmResults<Track>> callback = (tracks, changeSet) -> {
        RealmResults<Track> listTracksParsed = tracks;
        listTracks = listTracksParsed;
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

        boolean askResource = PreferencesHandler.getPreferredQuality(ctx) == 6;
        if(!askResource) {
            mService.seekToTrack(mService.append(listTracksFiltered.get(i)));
            Intent dd = new Intent(ctx, MainActivity.class);
            startActivity(dd);
        }else{
            AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
            bd.setTitle("Resources");
            CharSequence[] data = new CharSequence[listTracksFiltered.get(i).resources.size()];
            int j=0;
            for(TrackResources p : listTracksFiltered.get(i).resources) {
                String toShow = "";
                if(p.isDownloaded)
                    toShow += "Offline\n";
                else
                    toShow += p.server + "\n";
                data[j]=toShow + p.codec + " " + p.sample_rate / 1000 +
                        "Khz " + p.bitrate / 1000 + "kbps";
                j++;
            }
            bd.setItems(data, (dialog, w) -> {
                mService.seekToTrack(mService.append(listTracksFiltered.get(i), w));
                Intent dd = new Intent(ctx, MainActivity.class);
                startActivity(dd);
            });
            bd.show();
        }
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
    protected void onResume() {
        super.onResume();
        DataBackend.getInstance().executeTransactionAsync(realm -> {
            List<Track> result = realm.where(Track.class).
                    isNotNull("playedIn")
                    .findAllSorted("playedIn", Sort.DESCENDING);

            if(PreferencesHandler.getOffline(ctx))
                result=DataBackend.findAllOffline(result);

            ArrayList<Album> toReturn = new ArrayList<>();
            for(Track t : result){
                String temp;
                if(t.album_artist==null)
                    temp=t.artist;
                else
                    temp=t.album_artist;

                Album a = new Album(t.album, temp);
                if(!toReturn.contains(a))
                    toReturn.add(a);

                if(toReturn.size()>9)
                    break;
            }
            listRecentAlbums=toReturn;
            SearchActivity.this.runOnUiThread(() -> {
                RecyclerView.LayoutManager mLayoutManagerAlbumRecent = new LinearLayoutManager(ctx,
                        LinearLayoutManager.HORIZONTAL,
                        false);
                mRecyclerViewAlbumsRecent.setLayoutManager(mLayoutManagerAlbumRecent);
                if(listRecentAlbums.size()<1)
                    findViewById(R.id.textView2).setVisibility(View.GONE);
                else
                    mRecyclerViewAlbumsRecent.setAdapter(new MyListAdapter(listRecentAlbums, MyListAdapter.ALBUMS_STATE));
            });
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_home);
        if(!PreferencesHandler.getOffline(this))
            try {
                newMessages=0;
                users=new ArrayList<>();
                for(String ss : PreferencesHandler.getServer(ctx)) {
                    TaskHandler.getTracksFromNow(this, ss, PreferencesHandler.getLastUpdate(this));
                    TaskHandler.getMessages(this, ss);
                    TaskHandler.getUsers(this, ss);
                    TaskHandler.setUser(ss, null,
                            new User(PreferencesHandler.getUsername(this), true));
                }
                PreferencesHandler.setLastUpdate(this);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
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
            //Handles the Top Right button
            case R.id.action_favorite:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                ImageButton asdasd = drawer.findViewById(R.id.showmail);
                asdasd.setOnClickListener(v -> {
                    Intent sdsd = new Intent(ctx, EmailsActivity.class);
                    startActivity(sdsd);
                });
                if(newMessages>0) {
                    drawer.findViewById(R.id.roundcircle).setVisibility(View.VISIBLE);
                    TextView ssss = drawer.findViewById(R.id.counter);
                    ssss.setText(newMessages + "");
                } else
                    drawer.findViewById(R.id.roundcircle).setVisibility(View.GONE);
                ListView mListView = findViewById(R.id.friendslist);
                mListView.setAdapter(new MyUsersListAdapter(ctx, R.layout.frienditem, users));
                if(PreferencesHandler.getOffline(this))
                    drawer.findViewById(R.id.onlineimgmy).setVisibility(View.GONE);
                else
                    drawer.findViewById(R.id.onlineimgmy).setVisibility(View.VISIBLE);
                ImageView myimg = drawer.findViewById(R.id.myimg);
                if(me!=null) {
                    byte[] avatar = me.getAvatar();
                    if (avatar != null && avatar.length != 0)
                        myimg.setImageBitmap(
                                MyUsersListAdapter.getRoundedShape(BitmapFactory.decodeByteArray(avatar, 0, avatar.length)));
                    TextView meme = drawer.findViewById(R.id.myname);
                    meme.setText(me.id);
                }
                try {
                    newMessages=0;
                    for(String ss : PreferencesHandler.getServer(ctx)) {
                        TaskHandler.getMessages(this, ss);
                        TaskHandler.getUsers(this, ss);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                drawer.openDrawer(GravityCompat.END);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void getMyUser(User[] usersS, String server) {
        String user = PreferencesHandler.getUsername(this);
        for(int i=0; i<usersS.length; i++) {
            User temp = usersS[i];
            if (temp.id.equals(user))
                me = temp;
            else {
                if (!users.contains(temp)) {
                    temp.servers=new ArrayList<>();
                    temp.servers.add(server);
                    users.add(temp);
                } else {
                    int ind = users.indexOf(temp);
                    User temp2 = users.get(ind);
                    boolean modified = false;
                    if(temp.online && !temp2.online){
                        temp2.online=true;
                        temp2.last_track=temp.last_track;
                        modified=true;
                    }
                    if(!temp2.servers.contains(server)){
                        temp2.servers.add(server);
                        modified=true;
                    }
                    if(modified){
                        users.remove(ind);
                        users.add(ind, temp2);
                    }
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
            for (Album t: DataBackend.getAlbums()) {
                if (checkTrackByAlbum(t, query)) {
                    listAlbums.add(t);
                }
            }
            for (String t : DataBackend.getArtists()) {
                if (checkTrackByArtist(t, query)) {
                    listArtists.add(t);
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

    public boolean checkTrackByArtist(String s, String t){
        String newText = t.toLowerCase();
        return s.toLowerCase().startsWith(newText)|| s.toLowerCase().contains(" " + newText);
    }

    public boolean checkTrackByAlbum(Album s, String t){
        String newText = t.toLowerCase();
        return s.name.toLowerCase().startsWith(newText)|| s.name.toLowerCase().contains(" " + newText) ;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecyclerView.setAdapter(null);
        mRecyclerViewAlbums.setAdapter(null);
        mRecyclerViewAlbumsRecent.setAdapter(null);
        RecyclerView mRecyclerV = findViewById(R.id.coverlistuploadedview);
        mRecyclerV.setAdapter(null);
        mRecyclerV = findViewById(R.id.suggestionsview);
        mRecyclerV.setAdapter(null);
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    public void onCoverFetched(Track tr, int id) {}

    @Override
    public void onPlaybackProgressUpdate(int currentMilliseconds) {

    }

    @Override
    public void onRequestComplete(String response, Exception e, String token) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            String s = DataBackend.getServer(token);
            Track[] listT = gson.fromJson(response, Track[].class);
            if(!isFirstTime && listT!=null) {
                ArrayList<Track> news=new ArrayList<>();
                for(Track t : news)
                    if(t!=null) {
                        listTracks.add(t);
                        news.add(t);
                    }
                DataBackend.insertTracks(news, s);
            } else if (isFirstTime) {
                isFirstTime=false;
                listTracks = DataBackend.insertTracks(new ArrayList<>(Arrays.asList(listT)), s);
            }try {
                TaskHandler.getLatestTracks(this, s);
                TaskHandler.getSuggestions(this, s);
            } catch (Exception ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLatestRequestComplete(String response, Exception e) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            Track[] temporary = gson.fromJson(response, Track[].class);
            if (temporary.length>0) {
                for (Track t : temporary){
                    Album temp;
                    if (t.album_artist != null)
                        temp = new Album(t.album, t.album_artist);
                    else
                        temp = new Album(t.album, t.artist);
                    if(!listRecentUpAlbums.contains(temp))
                        listRecentUpAlbums.add(temp);
                }
                RecyclerView.LayoutManager mLayoutM = new LinearLayoutManager(ctx,
                        LinearLayoutManager.HORIZONTAL,
                        false);

                RecyclerView mRecyclerV = findViewById(R.id.coverlistuploadedview);
                mRecyclerV.setLayoutManager(mLayoutM);
                findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                mRecyclerV.setAdapter(new MyListAdapter(listRecentUpAlbums, MyListAdapter.ALBUMS_STATE));
            }
        }
    }

    @Override
    public void onPlaylistSingleRequestComplete(String result, Exception e) {
        // never called
    }

    @Override
    public void onPlaylistsRequestComplete(String result, Exception e) {
        // never called
    }

    @Override
    public void onSuggestionsRequestComplete(String result, Exception e) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (result != null){
            TrackSuggestion[] temporary = gson.fromJson(result, TrackSuggestion[].class);
            for(TrackSuggestion t : temporary)
                if(!listSugg.contains(t))
                    listSugg.add(t);
            if (temporary.length>0) {
                RecyclerView.LayoutManager mLayoutM = new LinearLayoutManager(ctx,
                        LinearLayoutManager.HORIZONTAL,
                        false);
                RecyclerView mRecyclerV = findViewById(R.id.suggestionsview);
                mRecyclerV.setLayoutManager(mLayoutM);
                findViewById(R.id.textView1).setVisibility(View.VISIBLE);
                mRecyclerV.setAdapter(new MyListAdapter(listSugg, MyListAdapter.SUGGESTIONS_STATE));
            }
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

    @Override
    public void onMessagesRequestComplete(String response, Exception e) {
        if(e!=null)
            e.printStackTrace();
        else{
            EmailType[] allMessages = gson.fromJson(response, EmailType[].class);
            for(EmailType m : allMessages){
                EmailType duo = DataBackend.getMessage(m.id);
                if(duo==null || !duo.isRead){
                    if(duo==null)
                        DataBackend.insertMessage(m);

                    if(m.recipient.equals(PreferencesHandler.getUsername(ctx)))
                        newMessages++;
                }
            }
        }
        View onlinround = findViewById(R.id.roundcircle);
        if(onlinround!=null && newMessages>0) {
            onlinround.setVisibility(View.VISIBLE);
            TextView ssss = findViewById(R.id.counter);
            ssss.setText(newMessages + "");
        } else if(onlinround!=null)
            onlinround.setVisibility(View.GONE);
    }

    @Override
    public void onUsersRequestComplete(String response, Exception e, String token) {
        if(e==null) {
            getMyUser(gson.fromJson(response, User[].class), DataBackend.getServer(token));
        } else {
            e.printStackTrace();
        }
    }

    public static User getUser(String sender) {
        for (User u : users)
            if(u.id.equals(sender))
                return u;
        return null;
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
