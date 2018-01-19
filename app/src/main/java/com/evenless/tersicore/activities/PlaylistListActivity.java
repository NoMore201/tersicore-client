package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.ItemAdapter;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;
import java.util.List;

/**
 * Created by McPhi on 10/12/2017.
 */

public class PlaylistListActivity extends AppCompatActivity{

    private static final String TAG = "TracksActivity";
    private List<Track> listTracks;
    private boolean mBound = false;
    private MediaPlayerService mService;
    private Context ctx = this;
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
                    else if(fromPosition!=toPosition)
                        listTracks=DataBackend.modifyPlaylistPosition(fromPosition, toPosition, pid.id);
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
                        else
                            listTracks = DataBackend.deleteFromPlaylist(it, pid.id);
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
            aa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mService.updatePlaylist(listTracks, 0, false);
                    Intent dd = new Intent(ctx, MainActivity.class);
                    startActivity(dd);
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

    private long getTotalDurationMs() {
        long temp=0;
        for (Track t : listTracks){
            if(t.duration==0)
                return 0;
            temp=temp+(long)t.duration;
        }
        return temp*1000;
    }
}
