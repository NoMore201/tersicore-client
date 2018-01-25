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
import android.widget.ImageView;
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
import java.util.Set;

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
                    final Track it = pt.second;
                    if(swipedDirection == ListSwipeItem.SwipeDirection.RIGHT) {
                        if (pid == null) {
                            mService.deleteFromPlaylist(it);
                        }
                        else {
                            listTracks = DataBackend.deleteFromPlaylist(it, pid.id);
                            updatePlaylistOnServer();
                        }
                        mDragListView.getAdapter().removeItem(mDragListView.getAdapter().getPositionForItem(pt));
                    }
                    else if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                        if (pid == null) {
                            mService.seekToTrack(mService.getCurrentPlaylist().indexOf(it));
                            finish();
                            startActivity(new Intent(ctx, MainActivity.class));
                        }
                        else {
                            boolean askResource = PreferencesHandler.getPreferredQuality(ctx) == 6;
                            if(!askResource) {
                                mService.updatePlaylist(listTracks, listTracks.indexOf(it), false);
                            }
                            else{
                                AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
                                bd.setTitle("Resources");
                                CharSequence[] data = new CharSequence[it.resources.size()];
                                int k=0;
                                for(TrackResources p : it.resources) {
                                    String toShow = "";
                                    if(p.isDownloaded)
                                        toShow += "Offline\n";
                                    else
                                        toShow += p.server + "\n";
                                    data[k]=toShow + p.codec + " " + p.sample_rate / 1000 +
                                            "Khz " + p.bitrate / 1000 + "kbps";
                                    k++;
                                }
                                bd.setItems(data, (dialog, w) -> {
                                    Map<String, Integer> resfav = new HashMap<>();
                                    TrackResources d = it.resources.get(w);
                                    for(int k1 = 0; k1 <listTracks.size(); k1++) {
                                        Track temp = listTracks.get(k1);
                                        int index=0;
                                        int bitrate=temp.resources.get(0).bitrate;
                                        for (int j=1; j<temp.resources.size(); j++)
                                            if(Math.abs(bitrate-d.bitrate)>Math.abs(temp.resources.get(j).bitrate-d.bitrate)){
                                                index=j;
                                                bitrate=temp.resources.get(j).bitrate;
                                            }
                                        resfav.put(temp.uuid, index);
                                    }
                                    mService.updatePlaylist(listTracks, listTracks.indexOf(it), false, resfav);
                                });
                                bd.show();
                            }
                            Intent dd = new Intent(ctx, MainActivity.class);
                            startActivity(dd);
                        }
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
        if(pid.uploader.equals(PreferencesHandler.getUsername(ctx)))
            try {
                Playlist temp = DataBackend.getPlaylist(pid.id);
                List<String> servers = PreferencesHandler.getServer(ctx);
                for(String ss : servers)
                    TaskHandler.setPlaylist(ss, null, temp);
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
            findViewById(R.id.deletePlaylist).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Cancel Playlist");
                    builder.setMessage("Are you sure you want to cancel the playlist?");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(pid.uploader.equals(PreferencesHandler.getUsername(ctx)))
                                try {
                                    for(String ss : PreferencesHandler.getServer(ctx))
                                        TaskHandler.deletePlaylist(ss, pid);
                                    DataBackend.deletePlaylist(pid);
                                    finish();
                                } catch (MalformedURLException e) {
                                    Toast.makeText(ctx, "There can be errors in synchronizing with server", Toast.LENGTH_LONG).show();
                                }
                            else
                                Toast.makeText(ctx, "You can't delete a playlist that is not yours!", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    builder.show();
                }
            });
            ImageButton aa = findViewById(R.id.playbutt);
            aa.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Play options")
                            .setItems(AlertDialogTrack.playOptionsPlus, new DialogInterface.OnClickListener() {
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
                                    Map<String, Integer> resfav = new HashMap<>();
                                    Intent dd = new Intent(ctx, MainActivity.class);
                                    for(int i=0; i<listTracks.size(); i++) {
                                        Track t = listTracks.get(i);
                                        TrackResources res = MediaPlayerService.checkTrackResourceByPreference(t, which, which!=4,
                                                PreferencesHandler.getDataProtection(ctx));
                                        if(res!=null)
                                            resfav.put(t.uuid, t.resources.indexOf(res));
                                        else
                                            resfav.put(t.uuid, -1);
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
                                    DataBackend.removeOfflineTrack(tt, tr.uuid);

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
                                            TrackResources res = MediaPlayerService.checkTrackResourceByPreference(tt, which, false,
                                                    PreferencesHandler.getDataProtection(ctx));
                                            if(res==null) {
                                                downloadedCount++;
                                                if(downloadedCount==1) {
                                                    Toast.makeText(ctx, "Data Protection is Active: Flac not downloaded", Toast.LENGTH_LONG).show();
                                                    findViewById(R.id.downloadButt).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.progressani3).setVisibility(View.GONE);
                                                }
                                            } else if(!res.isDownloaded){
                                                downloadedCount++;
                                                try {
                                                    mService.downloadFile(res, tt.uuid, (FileDownloadTaskListener) ctx);
                                                } catch (Exception e) {
                                                    Toast.makeText(ctx, "Some files have not been downloaded correctly", Toast.LENGTH_LONG).show();
                                                    findViewById(R.id.downloadButt).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.progressani3).setVisibility(View.GONE);
                                                }
                                            }
                                        }
                                    }
                                });
                        builder.show();
                    }
                }
            });
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
    }

    @Override
    public void onBackPressed() {
        if (pid == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            super.onBackPressed();
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
