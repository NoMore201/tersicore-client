package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.R;
import com.evenless.tersicore.exceptions.InvalidUrlException;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.view.SquareImageView;

import java.util.HashMap;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class MainActivity extends AppCompatActivity
    implements MediaPlayerServiceListener {

    private MediaPlayerService mService;
    private boolean mBound;
    private final MediaPlayerServiceListener ctx = this;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setMediaPlayerServiceListener(ctx);
            Track track1 = new Track();
            TrackResources temp = new TrackResources();
            TrackResources[] tempArray = new TrackResources[1];
            temp.path = "http://casa.izzo.li:8888/stream/881f51d2f478418fb9b595623528c55b";
            temp.uuid = "881f51d2f478418fb9b595623528c55b";
            tempArray[0] = temp;
            track1.resources = tempArray;
            track1.title="Oh Bella!";
            track1.album_artist="Flume";
            temp = new TrackResources();
            tempArray = new TrackResources[1];
            temp.path = "http://casa.izzo.li:8888/stream/af49dd2f3ef04b1f8687e092d0a83bc0";
            temp.uuid = "af49dd2f3ef04b1f8687e092d0a83bc0";
            tempArray[0] = temp;
            Track track2 = new Track();
            track2.resources = tempArray;
            track2.title="OMG";
            track2.album_artist="Sconosciuto";
            Track[] list = {track1, track2};
            mService.updatePlaylist(list);
            PagerContainer container = (PagerContainer) findViewById(R.id.pager_container);
            final ViewPager pager = container.getViewPager();
            pager.setAdapter(new MainActivity.MyPagerAdapter());
            pager.setClipChildren(false);
            pager.setOffscreenPageLimit(15);
            boolean showTransformer = getIntent().getBooleanExtra("showTransformer",true);
            if(showTransformer){
                new CoverFlow.Builder()
                        .with(pager)
                        .scale(0.3f)
                        .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                        .spaceSize(0f)
                        .build();
            }else{
                pager.setPageMargin(30);
            }
            final TextView tv_song = (TextView) findViewById(R.id.tv_song);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mService.seekTo(position);
                    tv_song.setText(mService.getCurrentPlaylist().get(position).title);
                    RelativeLayout relativeLayout = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                    ViewCompat.setElevation(relativeLayout.getRootView(), 8.0f);
                    Palette palette = Palette.from(getCover(mService.getCurrentPlaylist().get(position))).generate();
                    setStatusBar(palette);
                    TextView tv_artist = findViewById(R.id.tv_artist);
                    tv_artist.setText(mService.getCurrentPlaylist().get(position).album_artist);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
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
    public void onCoverFetched(Track tr){
        PagerContainer container = findViewById(R.id.pager_container);
        ViewPager pager = container.getViewPager();
        pager.setAdapter(new MainActivity.MyPagerAdapter());
        pager.setCurrentItem(mService.getCurrentTrackIndex(), false);
    }

    public void onClickPlay(View v) {
        if (mBound) {
            if(mService.isPlaying()) {
                ImageButton vie = (ImageButton) v;
                vie.setImageResource(R.drawable.ic_play);
                mService.pause();
            }
            else {
                ImageButton vie = (ImageButton) v;
                vie.setImageResource(R.drawable.ic_pause);
                mService.play();
            }
        }
    }

    public void onClickForward(View v) {
        int temp = mService.getCurrentTrackIndex() + 1;
        if(temp<mService.getCurrentPlaylist().size()){
            PagerContainer container = findViewById(R.id.pager_container);
            ViewPager pager = container.getViewPager();
            pager.setCurrentItem(temp, true);
        }
    }

    public void onClickBackward(View v) {
        if(mService.getCurrentTrackIndex()!=0){
            PagerContainer container = findViewById(R.id.pager_container);
            ViewPager pager = container.getViewPager();
            pager.setCurrentItem(mService.getCurrentTrackIndex() - 1, true);
        }
    }

    @Override
    public void onPlaylistComplete() {
        //do something
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        Log.d("TAG", "onNewTrackPlaying: " + newTrack.resources[0].path);
        ImageButton vie = findViewById(R.id.playbutton);
        vie.setImageResource(R.drawable.ic_pause);
        TextView tv_song = findViewById(R.id.tv_song);
        tv_song.setText(newTrack.title);
        TextView tv_artist = findViewById(R.id.tv_artist);
        tv_artist.setText(newTrack.album_artist);
    }

    @Override
    public void onPlaybackError(Exception exception) {
        if (exception.getClass().equals(InvalidUrlException.class)) {
            Log.e("TAG", "onPlaybackError: invalid url" );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private Bitmap getCover(Track tr){
        // convert the byte array to a bitmap
        if(tr.resources[0].cover_data != null)
            return BitmapFactory.decodeByteArray(tr.resources[0].cover_data, 0, tr.resources[0].cover_data.length);
         else
            return BitmapFactory.decodeResource(this.getResources(), R.drawable.nocover);
    }


    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_cover,null);
            SquareImageView imageView = view.findViewById(R.id.image_cover);
            imageView.setImageBitmap(getCover(mService.getCurrentPlaylist().get(position)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return mService.getCurrentPlaylist().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }
    public void setStatusBar(Palette palette){
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Palette.Swatch vibrant = palette.getDominantSwatch();
            if (vibrant != null) {
                window.setStatusBarColor(vibrant.getRgb());
            }

        }
    }

}
