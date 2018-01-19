package com.evenless.tersicore.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v4.util.Pair;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.ItemAdapter;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.FileDownloadTaskListener;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by McPhi on 10/12/2017.
 */

public class PlaylistListActivity extends AppCompatActivity
implements FileDownloadTaskListener{

    private static final String TAG = "PlaylistListActivity";
    private List<Track> listTracks;
    private boolean mBound = false;
    private MediaPlayerService mService;
    private Context ctx = this;
    private int choice=0;
    private int downloadedCount=0;
    private Playlist pid = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound=true;

            if(pid==null)
                listTracks = mService.getCurrentPlaylist();

            final DragListView mDragListView = (DragListView) findViewById(R.id.dragPlaylist);
            mDragListView.setDragListListener(new DragListView.DragListListener() {
                @Override
                public void onItemDragStarted(int position) {

                }

                @Override
                public void onItemDragging(int itemPosition, float x, float y) {

                }

                @Override
                public void onItemDragEnded(int fromPosition, int toPosition) {
                    if(pid==null)
                        mService.changePlaylistPosition(fromPosition, toPosition);
                    else if(fromPosition!=toPosition) {
                        listTracks = DataBackend.modifyPlaylistPosition(fromPosition, toPosition, pid.id);
                        updatePlaylistOnServer();
                    }
                }
            });

            mDragListView.setLayoutManager(new LinearLayoutManager(ctx));
            ItemAdapter listAdapter = new ItemAdapter(listTracks, R.layout.list_item, R.id.image, false);
            mDragListView.setAdapter(listAdapter, true);
            mDragListView.setCanDragHorizontally(false);
            mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListener() {
                @Override
                public void onItemSwipeStarted(ListSwipeItem item) {

                }

                @Override
                public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                    Pair<Long, Track> pt =  (Pair<Long, Track>) item.getTag();
                    Track it = pt.second;
                    if(swipedDirection == ListSwipeItem.SwipeDirection.RIGHT) {
                        if (pid == null)
                            mService.deleteFromPlaylist(it);
                        else {
                            listTracks = DataBackend.deleteFromPlaylist(it, pid.id);
                            updatePlaylistOnServer();
                        }
                        mDragListView.getAdapter().removeItem(mDragListView.getAdapter().getPositionForItem(pt));
                    }
                    else if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                        if (pid == null)
                            mService.seekToTrack(mService.getCurrentPlaylist().indexOf(it));
                        else
                            mService.updatePlaylist(listTracks, listTracks.indexOf(it), false);
                        Intent dd = new Intent(ctx, MainActivity.class);
                        startActivity(dd);
                    }
                }

                @Override
                public void onItemSwiping(ListSwipeItem item, float swipedDistanceX) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound=false;
        }

    };

    private void updatePlaylistOnServer() {
        try {
            Playlist temp = DataBackend.getPlaylist(pid.id);
            TaskHandler.setPlaylist(PreferencesHandler.getServer(ctx), null, temp);
        } catch (MalformedURLException e) {
            Toast.makeText(ctx, "There can be errors in synchronizing with server", Toast.LENGTH_LONG).show();
        } catch (Exception e){
            //e.printStackTrace();
        }
    }

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
        String p = getIntent().getStringExtra("EXTRA_PLAYLIST_ID");
        Toolbar toolbar;
        if(p!=null){
            setContentView(R.layout.playlist_list_alternative);
            toolbar = (Toolbar) findViewById(R.id.toolbar2);
            pid=DataBackend.getPlaylist(p);
            listTracks = pid.getTrackObjects();
            toolbar.setTitle(pid.name);
            TextView tt = findViewById(R.id.playupload);
            tt.setText("Uploaded By " + pid.uploader);
            ImageButton aa = findViewById(R.id.playbutt);
            aa.setOnLongClickListener(new View.OnLongClickListener() {
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

            aa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                        // Add to Playlist
                                        case 4: AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
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
                                        default: break;
                                    }
                                    choice=0;
                                }
                            });
                    builder.show();
                }
            });
            ToggleButton tb = findViewById(R.id.likeButt);
            tb.setChecked(pid.favorite);
            tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DataBackend.setPlaylistFavorite(pid.id, isChecked);
                }
            });
            TextView totd = findViewById(R.id.totduration);
            long duration = getTotalDurationMs();
            if(duration!=0)
                totd.setText("Duration: " + MainActivity.parseDuration(duration));
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
        } else {
            setContentView(R.layout.playlist_list);
            toolbar = (Toolbar) findViewById(R.id.toolbar2);
            toolbar.setTitle("Playing List");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.removeButt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(PlaylistListActivity.this,
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
                    listTracks=pid.getTrackObjects();
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

                    ActivityCompat.requestPermissions(PlaylistListActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            355);

                } else {
                    final ImageButton d = (ImageButton) v;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(MediaPlayerService.playOptions, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    d.setVisibility(View.GONE);
                                    findViewById(R.id.progressani3).setVisibility(View.VISIBLE);
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
    public void OnFileDownloaded(String key, String id) {
        if(key==null){
            ImageButton d = findViewById(R.id.downloadButt);
            Toast.makeText(ctx, "Some files have not been downloaded correctly. Try another preference!", Toast.LENGTH_LONG).show();
            d.setVisibility(View.VISIBLE);
            findViewById(R.id.progressani3).setVisibility(View.GONE);
            listTracks = pid.getTrackObjects();
        } else {
            downloadedCount--;
            Track ins = DataBackend.insertOfflineTrack(key, id);
            for(int i=0; i< listTracks.size(); i++)
                if(listTracks.get(i).uuid.equals(id)) {
                    listTracks.remove(i);
                    listTracks.add(i, ins);
                }
            if(downloadedCount==0) {
                findViewById(R.id.progressani3).setVisibility(View.GONE);
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
}
