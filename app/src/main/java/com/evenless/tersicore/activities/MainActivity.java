package com.evenless.tersicore.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.MediaPlayerServiceListener;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.exceptions.InvalidUrlException;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.view.SquareImageView;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import io.realm.RealmList;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class MainActivity extends AppCompatActivity
    implements MediaPlayerServiceListener {
    private static final String TAG = "MainActivity";

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
            mService.callTimer(mHandler);
            PagerContainer container = (PagerContainer) findViewById(R.id.pager_container);
            Toolbar toolbar = findViewById(R.id.toolbar2);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            final ViewPager pager = container.getViewPager();
            pager.setAdapter(new MainActivity.MyPagerAdapter());
            pager.setClipChildren(false);
            pager.setOffscreenPageLimit(4);
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
            Track tr = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
            final TextView tv_song = (TextView) findViewById(R.id.tv_song);
            tv_song.setText(tr.title);
            RelativeLayout relativeLayout = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
            ViewCompat.setElevation(relativeLayout.getRootView(), 8.0f);
            Palette palette = Palette.from(getCover(tr)).generate();
            setStatusBar(palette);
            TextView tv_artist = findViewById(R.id.tv_artist);
            ImageButton vie = findViewById(R.id.playbutton);
            tv_artist.setText(tr.artist);
            toolbar.setSubtitle(PreferencesHandler.getServer((Context) ctx));
            toolbar.setTitle(tr.resources.get(0).codec + " " + tr.resources.get(0).bitrate/1000 + "kbps");
            ToggleButton toggleR = (ToggleButton) findViewById(R.id.toggleRepeat);
            toggleR.setChecked(mService.getRepeat());
            toggleR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mService.setRepeat(isChecked);
                }
            });
            ToggleButton toggleS = (ToggleButton) findViewById(R.id.toggleShuffle);
            toggleS.setChecked(mService.getShuffle());
            toggleS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked!=mService.getShuffle()){
                        mService.toggleShuffle();
                        pager.setAdapter(new MainActivity.MyPagerAdapter());
                        pager.setCurrentItem(mService.getCurrentTrackIndex());
                    }
                }
            });
            if(mService.isPlaying())
                vie.setImageResource(R.drawable.ic_play);
            else
                vie.setImageResource(R.drawable.ic_pause);
            SeekBar tv_seek=findViewById(R.id.tv_seek);
            tv_seek.setOnSeekBarChangeListener(new seekListener());
            pager.setCurrentItem(mService.getCurrentTrackIndex(), true);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mService.seekToTrack(position);
                    Track tra = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
                    tv_song.setText(tra.title);
                    RelativeLayout relativeLayout = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                    ViewCompat.setElevation(relativeLayout.getRootView(), 8.0f);
                    Palette palette = Palette.from(getCover(tra)).generate();
                    setStatusBar(palette);
                    Toolbar toolbar = findViewById(R.id.toolbar2);
                    toolbar.setSubtitle(PreferencesHandler.getServer((Context) ctx));
                    toolbar.setTitle(tra.resources.get(0).codec + " " + tra.resources.get(0).sample_rate/1000 +
                            "Khz " + tra.resources.get(0).bitrate/1000 + "kbps");
                    TextView tv_artist = findViewById(R.id.tv_artist);
                    if(tra.artist!=null)
                        tv_artist.setText(tra.artist);
                    else
                        tv_artist.setText(tra.album_artist);
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
    public void onCoverFetched(Track tr, int id){
        PagerContainer container = findViewById(R.id.pager_container);
        ViewPager pager = container.getViewPager();
        pager.setAdapter(new MainActivity.MyPagerAdapter());
        pager.setCurrentItem(mService.getCurrentTrackIndex(), false);
    }

    private String parseDuration(long durationMs){
        long duration = durationMs / 1000;
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);
        if (h == 0) {
            return m + ":" + String.format("%02d",s);
        } else {
            return h + ":" + String.format("%02d",m) + ":" + String.format("%02d",s);
        }
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

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            onPlaybackProgressUpdate(msg.arg1);
        }
    };

    @Override
    public void onPlaylistComplete() {
        //do something
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        ImageButton vie = findViewById(R.id.playbutton);
        vie.setImageResource(R.drawable.ic_pause);
        TextView tv_song = findViewById(R.id.tv_song);
        tv_song.setText(newTrack.title);
        TextView tv_artist = findViewById(R.id.tv_artist);
        tv_artist.setText(newTrack.album_artist);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setSubtitle(PreferencesHandler.getServer((Context) ctx));
        toolbar.setTitle(newTrack.resources.get(0).codec + " " + newTrack.resources.get(0).bitrate/1000 + "kbps");
    }

    @Override
    public void onPlaybackProgressUpdate(int currentMilliseconds) {
        long durat = mService.getDuration();
        boolean result = durat!=0;
        // update seekbar
        if(result){
            TextView tv_currentms = findViewById(R.id.tv_current_time);
            tv_currentms.setText(parseDuration(currentMilliseconds));
            SeekBar tv_seek = findViewById(R.id.tv_seek);
            tv_seek.setMax(0);
            tv_seek.setMax((int) durat);
            tv_seek.setProgress(currentMilliseconds);
            TextView fullT = findViewById(R.id.tv_full_time);
            fullT.setText(parseDuration(durat));
        }
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
        if(tr.resources.get(0).cover_data!= null && tr.resources.get(0).cover_data.length!=0)
            return BitmapFactory.decodeByteArray(
                    tr.resources.get(0).cover_data, 0,
                    tr.resources.get(0).cover_data.length);
        else {
            mService.fetchCover(tr, 0);
            return BitmapFactory.decodeResource(this.getResources(), R.drawable.nocover);
        }
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

    private class seekListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            mService.seekTo(progress);
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
