package com.evenless.tersicore.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.interfaces.ApiPostTaskListener;
import com.evenless.tersicore.interfaces.CoverDownloadTaskListener;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.interfaces.FileDownloadTaskListener;
import com.evenless.tersicore.interfaces.ImageRequestTaskListener;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.MyListAdapter;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.TrackSuggestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SingleAlbumActivity  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MediaPlayerServiceListener, ImageRequestTaskListener, CoverDownloadTaskListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        FileDownloadTaskListener, ApiPostTaskListener{

    private static final String TAG = "SingleAlbumActivity";
    private List<Track> listTracks;
    private String albumName;
    private String artist;
    private boolean mBound = false;
    private boolean isPlayer = false;
    private MediaPlayerService mService;
    private Context ctx = this;
    private static int choice = 0;
    private int downloadedCount = 0;
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
                if(isPlayer) {
                    RelativeLayout asd = findViewById(R.id.albumListLayout);
                    asd.setMinimumHeight(asd.getHeight() + 200);
                    isPlayer=false;
                } else
                    isPlayer=true;
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

        listTracks=DataBackend.getTracks(artist, albumName);
        if(listTracks!=null)
            updateList();

        ToggleButton lik = findViewById(R.id.likeButt);
        lik.setChecked(DataBackend.checkFavorite(new Album(albumName, artist)));
        lik.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Album a = new Album(albumName, artist);
                DataBackend.updateFavorite(a, isChecked);
                if(isChecked)
                    try {
                        for(String ss : PreferencesHandler.getServer(ctx))
                        TaskHandler.setSuggestion(ss,
                                (ApiPostTaskListener) ctx, new TrackSuggestion(albumName, artist, PreferencesHandler.getUsername(ctx)));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
            }
        });

        Switch asd = navigationView.getMenu().findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcharr);
        asd.setChecked(PreferencesHandler.getOffline(this));
        asd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesHandler.setOffline(ctx, isChecked);
                finish();
                startActivity(getIntent());
            }
        });

        findViewById(R.id.removeButt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(SingleAlbumActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            356);

                } else {
                    for(Track tt : listTracks)
                        for (TrackResources tr : tt.resources)
                            if(tr.isDownloaded) {
                                Track trt = DataBackend.removeOfflineTrack(tt, tr.uuid);
                                if(mService.getCurrentPlaylist().contains(tt))
                                    mService.setDownloaded(trt);
                            }
                    listTracks=DataBackend.getTracks(artist, albumName);
                    v.setVisibility(View.GONE);
                    findViewById(R.id.downloadButt).setVisibility(View.VISIBLE);
                }
            }
        });
        findViewById(R.id.downloadButt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(SingleAlbumActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            355);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(MediaPlayerService.playOptions, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    findViewById(R.id.downloadButt).setVisibility(View.GONE);
                                    findViewById(R.id.progressani).setVisibility(View.VISIBLE);
                                    for(Track tt : listTracks) {
                                        TrackResources res = MediaPlayerService.checkTrackResourceByPreference(tt, which, false);
                                        if(!res.isDownloaded){
                                            downloadedCount++;
                                            try {
                                                mService.downloadFile(res, tt.uuid, (FileDownloadTaskListener) ctx);
                                            } catch (Exception e) {
                                                Toast.makeText(ctx, "Some files have not been downloaded correctly", Toast.LENGTH_LONG).show();
                                                findViewById(R.id.downloadButt).setVisibility(View.VISIBLE);
                                                findViewById(R.id.progressani).setVisibility(View.GONE);
                                            }
                                        }
                                    }
                                }
                            });
                    builder.show();
                }
            }
        });
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
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
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

    private void updateList() {
        ListView lsv = findViewById(R.id.albumScrollableList);
        TextView aln = findViewById(R.id.albumname);
        TextView arn = findViewById(R.id.artistname);
        TextView totr = findViewById(R.id.tottracks);
        TextView totd = findViewById(R.id.totduration);
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
                findViewById(R.id.separator).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(listTracks.size()>0 && listTracks.get(0).genre!=null){
            TextView g = findViewById(R.id.genre);
            g.setText(listTracks.get(0).genre);
        }
        if(listTracks.size()>0){
            totr.setText(listTracks.size() + " Tracks");
            long duration = getTotalDurationMs();
            if(duration!=0)
                totd.setText(MainActivity.parseDuration(duration));
            boolean hasBeenDownloaded = true;
            for(Track t : listTracks)
                if(!t.hasBeenDownloaded()){
                    hasBeenDownloaded=false;
                    break;
                }
            if(hasBeenDownloaded){
               findViewById(R.id.removeButt).setVisibility(View.VISIBLE);
               findViewById(R.id.downloadButt).setVisibility(View.GONE);
            }
        }
        ArrayAdapter<Track> arrayAdapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                listTracks) {

            @SuppressLint("SetTextI18n")
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                Track temp = listTracks.get(position);
                if(temp.track_number!=null)
                    text1.setText(temp.track_number + " - " + temp.title);
                else
                    text1.setText(temp.title);
                if(temp.duration!=0){
                    TextView text2 = view.findViewById(android.R.id.text2);
                    text2.setText("Duration: " + MainActivity.parseDuration((long)temp.duration*1000));
                }
                if(position==listTracks.size()-1) {
                    ScrollView main = (ScrollView) findViewById(R.id.mainScrollAlbumView).getParent();
                    main.scrollTo(0, 0);
                    if(isPlayer) {
                        RelativeLayout asd = findViewById(R.id.albumListLayout);
                        asd.setMinimumHeight(asd.getHeight() + 200);
                        isPlayer=false;
                    }
                }
                return view;
            }
        };
        lsv.setAdapter(arrayAdapter);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(this);

        lsv.setOnItemLongClickListener(this);

        findViewById(R.id.playbutt).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Play options")
                        .setItems(AlertDialogTrack.playOptions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                choice=which;
                                findViewById(R.id.playbutt).callOnClick();
                            }});
                builder.show();
                return false;
            }
        });

        findViewById(R.id.playbutt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(choice==4){
                    AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
                    bd.setTitle("Playlists");
                    final List<Playlist> asd = DataBackend.getMyPlaylists(PreferencesHandler.getUsername(ctx));
                    CharSequence[] data = new CharSequence[asd.size()+1];
                    data[0]="NEW PLAYLIST";
                    int i = 1;
                    for(Playlist p : asd) {
                        data[i] = p.name;
                        i++;
                    }
                    bd.setItems(data, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(which==0){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                builder.setTitle("Name the New Playlist");
                                final EditText input = new EditText(ctx);
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AlertDialogTrack.updatePlaylistOnServer(ctx,
                                                DataBackend.createNewPlaylist(input.getText().toString(),
                                                        listTracks, PreferencesHandler.getUsername(ctx)));
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            }
                            else
                                AlertDialogTrack.updatePlaylistOnServer(ctx,
                                        DataBackend.addToPlaylist(asd.get(which-1), listTracks));
                        }});
                    bd.create().show();
                    choice=0;
                } else{
                    String[] pop = new String[5];
                    for(int i=0; i<MediaPlayerService.playOptions.length; i++)
                        pop[i]=MediaPlayerService.playOptions[i];
                    pop[4]="OFFLINE Version (if available)";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(pop, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<Integer, Integer> resfav = new HashMap<>();
                                    Intent dd = new Intent(ctx, MainActivity.class);
                                    for(int i=0; i<listTracks.size(); i++) {
                                        Track t = listTracks.get(i);
                                        resfav.put(i, t.resources.indexOf(MediaPlayerService.checkTrackResourceByPreference(t, which, which!=4)));
                                    }
                                    switch (choice){
                                        case 0: mService.updatePlaylist(listTracks, 0, false, resfav); startActivity(dd); break;
                                        // play now keep queue
                                        case 1: mService.seekToTrack(mService.append(listTracks, resfav)); startActivity(dd); break;
                                        // add to current playlist
                                        case 2: mService.append(listTracks, resfav); startActivity(dd); break;
                                        // play after
                                        case 3: mService.appendAfterCurrent(listTracks, resfav); startActivity(dd); break;
                                        default: break;
                                    }
                                    choice=0;
                                }
                            });
                    builder.show();
                }
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

    private long getTotalDurationMs() {
        long temp=0;
        for (Track t : listTracks){
            if(t.duration==0)
                return 0;
            temp=temp+(long)t.duration;
        }
        return temp*1000;
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
            ex.printStackTrace();
        } else try {
            JSONObject tempJson = new JSONObject(result);
            JSONArray tmp = tempJson.getJSONObject("album").getJSONArray("image");
            if(listTracks.get(0).genre==null) {
                TextView g = findViewById(R.id.genre);
                g.setText(tempJson.getJSONObject("album").getJSONObject("tags").getJSONArray("tag").getJSONObject(0).getString("name"));
            }
            final String link = tmp.getJSONObject(3).getString("#text");
            TaskHandler.downloadCover(link, 0, key, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnCoverDownloaded(Bitmap result, int mState, String key) {
        ImageView temp = findViewById(R.id.coverAlbum);
        temp.setImageBitmap(result);
        DataBackend.insertCover(artist, albumName, MyListAdapter.ConvertToByteArray(result));
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
    public void OnFileDownloaded(String key, String id) {
        if(key==null){
            ImageButton d = findViewById(R.id.downloadButt);
            Toast.makeText(ctx, "Some files have not been downloaded correctly. Try another preference!", Toast.LENGTH_LONG).show();
            d.setVisibility(View.VISIBLE);
            findViewById(R.id.progressani).setVisibility(View.GONE);
            listTracks = DataBackend.getTracks(artist, albumName);
        } else {
            downloadedCount--;
            Track ins = DataBackend.insertOfflineTrack(key, id);
            for(int i=0; i< listTracks.size(); i++)
                if(listTracks.get(i).uuid.equals(id)) {
                    listTracks.remove(i);
                    listTracks.add(i, ins);
                }
            if(downloadedCount==0) {
                findViewById(R.id.progressani).setVisibility(View.GONE);
                findViewById(R.id.removeButt).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 355: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ImageButton d = findViewById(R.id.downloadButt);
                    d.callOnClick();

                } else {
                    Toast.makeText((Context) ctx, "Cannot download files without permissions", Toast.LENGTH_LONG).show();
                }
            }
            case 356: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ImageButton d = findViewById(R.id.removeButt);
                    d.callOnClick();

                } else {
                    Toast.makeText((Context) ctx, "Cannot remove files without permissions", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    public void onRequestComplete(int requestType, Exception e, String result) {
        if(e==null && requestType==3)
            Toast.makeText(ctx, "Album Suggested to friends", Toast.LENGTH_SHORT).show();
    }
}