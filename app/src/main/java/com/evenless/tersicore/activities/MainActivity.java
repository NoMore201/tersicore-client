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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.ApiPostTaskListener;
import com.evenless.tersicore.interfaces.FileDownloadTaskListener;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.exceptions.InvalidUrlException;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.TrackSuggestion;
import com.evenless.tersicore.view.SquareImageView;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class MainActivity extends AppCompatActivity
    implements MediaPlayerServiceListener,
        FileDownloadTaskListener, ApiPostTaskListener,
        ViewTreeObserver.OnWindowFocusChangeListener
{
    private static final String TAG = "MainActivity";
    private final String [] shareOptions = {"Send a mail in Tersicore", "External App"};
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
            String uuidnow = getIntent().getStringExtra("EXTRA_UUID");
            if(uuidnow!=null) {
                Track toPlay=DataBackend.getTrack(uuidnow);
                if(toPlay==null){
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setMessage("You don't have this track in your servers");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "BACK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    onBackPressed();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    final List<Track> temp = new ArrayList<>();
                    temp.add(toPlay);
                    boolean askResource = PreferencesHandler.getPreferredQuality((Context) ctx) == 6;
                    if(!askResource) {
                        int asd = mService.append(temp);
                        PagerContainer container = findViewById(R.id.pager_container);
                        final ViewPager pager = container.getViewPager();
                        pager.setAdapter(new MainActivity.MyPagerAdapter());
                        pager.setCurrentItem(mService.getCurrentTrackIndex());
                        mService.seekToTrack(asd);
                        if(mService.getCurrentPlaylist().size()==1) {
                            mService.updateState();
                            getIntent().putExtra("EXTRA_UUID", (String) null);
                        }
                    }
                    else{
                        AlertDialog.Builder bd = new AlertDialog.Builder((Context) ctx);
                        bd.setTitle("Resources");
                        CharSequence[] data = new CharSequence[temp.get(0).resources.size()];
                        int i=0;
                        for(TrackResources p : temp.get(0).resources) {
                            String toShow = "";
                            if(p.isDownloaded)
                                toShow += "Offline\n";
                            else
                                toShow += p.server + "\n";
                            data[i]=toShow + p.codec + " " + p.sample_rate / 1000 +
                                    "Khz " + p.bitrate / 1000 + "kbps";
                            i++;
                        }
                        bd.setItems(data, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int w) {
                                int asd = mService.append(temp.get(0),w);
                                PagerContainer container = findViewById(R.id.pager_container);
                                final ViewPager pager = container.getViewPager();
                                pager.setAdapter(new MainActivity.MyPagerAdapter());
                                pager.setCurrentItem(mService.getCurrentTrackIndex());
                                mService.seekToTrack(asd);
                                if(mService.getCurrentPlaylist().size()==1) {
                                    mService.updateState();
                                    getIntent().putExtra("EXTRA_UUID", (String) null);
                                    recreate();
                                }
                            }
                        });
                        bd.show();
                    }
                }
            }
            if(mService.getCurrentPlaylist().size()>0) {
                mService.callTimer(mHandler);
                PagerContainer container = findViewById(R.id.pager_container);
                final Toolbar toolbar = findViewById(R.id.toolbar2);
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
                boolean showTransformer = getIntent().getBooleanExtra("showTransformer", true);
                if (showTransformer) {
                    new CoverFlow.Builder()
                            .with(pager)
                            .scale(0.3f)
                            .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                            .spaceSize(0f)
                            .build();
                } else {
                    pager.setPageMargin(30);
                }
                final Track tr = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
                int curres = mService.getCurrentResource();
                final TextView tv_song = findViewById(R.id.tv_song);
                tv_song.setText(tr.title);
                RelativeLayout relativeLayout = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                ViewCompat.setElevation(relativeLayout.getRootView(), 8.0f);
                Palette palette = Palette.from(getCover(tr)).generate();
                setStatusBar(palette);
                TextView tv_artist = findViewById(R.id.tv_artist);
                ImageButton vie = findViewById(R.id.playbutton);
                tv_artist.setText(tr.artist);
                TextView tv_currentms = findViewById(R.id.tv_current_time);
                tv_currentms.setText(parseDuration(mService.getCurrentprogress()));
                TextView fullT = findViewById(R.id.tv_full_time);
                fullT.setText(parseDuration((long) tr.duration * 1000));
                toolbar.setTitle(tr.resources.get(curres).codec + " " + tr.resources.get(curres).sample_rate / 1000 +
                        "Khz " + tr.resources.get(curres).bitrate / 1000 + "kbps");
                ToggleButton toggleR = (ToggleButton) findViewById(R.id.toggleRepeat);
                toggleR.setChecked(mService.getRepeat());
                toggleR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mService.setRepeat(isChecked);
                    }
                });
                ToggleButton toggleS = findViewById(R.id.toggleShuffle);
                toggleS.setChecked(mService.getShuffle());
                toggleS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked != mService.getShuffle()) {
                            mService.toggleShuffle();
                            pager.setAdapter(new MainActivity.MyPagerAdapter());
                            pager.setCurrentItem(mService.getCurrentTrackIndex());
                        }
                    }
                });
                if (mService.isPlaying())
                    vie.setImageResource(R.drawable.ic_pause);
                else
                    vie.setImageResource(R.drawable.ic_play);
                ToggleButton lik = findViewById(R.id.tb_love);
                lik.setChecked(DataBackend.checkFavorite(tr));
                lik.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Track t = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
                        DataBackend.updateFavorite(t , isChecked);
                        if(isChecked)
                            try {
                                List<String> servers = PreferencesHandler.getServer((Context) ctx);
                                TrackSuggestion temps = new TrackSuggestion(t.uuid, t.album, t.artist, t.title, PreferencesHandler.getUsername((Context) ctx));
                                for(String ss : servers)
                                    TaskHandler.setSuggestion(ss,
                                            (ApiPostTaskListener) ctx,
                                            temps);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                    }
                });
                SeekBar tv_seek = findViewById(R.id.tv_seek);
                tv_seek.setOnSeekBarChangeListener(new seekListener());
                tv_seek.setMax(tr.duration * 1000);
                tv_seek.setProgress(mService.getCurrentprogress());
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    tv_seek.setThumb(null);
                }
                pager.setCurrentItem(mService.getCurrentTrackIndex(), true);
                pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        mService.seekToTrack(position);
                        final Track tra = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
                        tv_song.setText(tra.title);
                        RelativeLayout relativeLayout = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                        ViewCompat.setElevation(relativeLayout.getRootView(), 8.0f);
                        Palette palette = Palette.from(getCover(tra)).generate();
                        setStatusBar(palette);
                        Toolbar toolbar = findViewById(R.id.toolbar2);
                        int curres = mService.getCurrentResource();
                        toolbar.setTitle(tra.resources.get(curres).codec + " " + tra.resources.get(curres).sample_rate / 1000 +
                                "Khz " + tra.resources.get(curres).bitrate / 1000 + "kbps");
                        TextView tv_artist = findViewById(R.id.tv_artist);
                        if (tra.artist != null)
                            tv_artist.setText(tra.artist);
                        else
                            tv_artist.setText(tra.album_artist);

                        TextView tv_currentms = findViewById(R.id.tv_current_time);
                        tv_currentms.setText(parseDuration(mService.getCurrentprogress()));
                        SeekBar tv_seek = findViewById(R.id.tv_seek);
                        tv_seek.setMax(0);
                        tv_seek.setProgress(0);
                        TextView fullT = findViewById(R.id.tv_full_time);
                        if (tra.duration == 0)
                            fullT.setText("-:-");
                        else
                            fullT.setText(parseDuration((long) tra.duration * 1000));
                        ToggleButton lik = findViewById(R.id.tb_love);
                        lik.setChecked(DataBackend.checkFavorite(tra));
                        findViewById(R.id.progressani2).setVisibility(View.GONE);
                        ImageButton download = findViewById(R.id.downloadButt2);
                        if(mService.isOfflineT()) {
                            download.setVisibility(View.GONE);
                            toolbar.setSubtitle("OFFLINE");
                            findViewById(R.id.removeButt2).setVisibility(View.VISIBLE);
                        }
                        else {
                            download.setVisibility(View.VISIBLE);
                            toolbar.setSubtitle(tra.resources.get(mService.getCurrentResource()).server);
                            findViewById(R.id.removeButt2).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                findViewById(R.id.playlistshow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent asd = new Intent(v.getContext(), PlaylistListActivity.class);
                        startActivity(asd);
                    }
                });
                findViewById(R.id.playlistshare).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder((Context) ctx);
                        builder.setTitle("Share your song")
                                .setItems(shareOptions, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Track temp = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
                                        switch (which) {
                                            case 0:
                                                AlertDialog.Builder builder = new AlertDialog.Builder((Context) ctx);
                                                final String[] usersName = new String[SearchActivity.users.size()];
                                                for (int i=0;i<SearchActivity.users.size();i++)
                                                    usersName[i]=SearchActivity.users.get(i).id;
                                                builder.setTitle("Select User")
                                                        .setItems(usersName, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int sel) {
                                                                Intent sendMail = new Intent((Context) ctx, SendMail.class);
                                                                sendMail.putExtra("EXTRA_UUID", temp.uuid);
                                                                sendMail.putExtra("EXTRA_CONTACT_NAME", usersName[sel]);
                                                                startActivity(sendMail);
                                                            }
                                                        });
                                                builder.show();
                                                break;
                                            case 1:
                                                Intent sendIntent = new Intent();
                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                sendIntent.putExtra(Intent.EXTRA_TEXT, "Currently Playing: " +
                                                        temp.title + " by " + temp.artist + " on #tersicore");
                                                sendIntent.setType("text/plain");
                                                startActivity(sendIntent);
                                                break;
                                        }
                                    }
                                });
                        builder.show();
                    }
                });
                ImageButton download = findViewById(R.id.downloadButt2);
                ImageButton remove = findViewById(R.id.removeButt2);
                findViewById(R.id.progressani2).setVisibility(View.GONE);
                if(mService.isOfflineT()) {
                    download.setVisibility(View.GONE);
                    toolbar.setSubtitle("OFFLINE");
                    remove.setVisibility(View.VISIBLE);
                }
                else {
                    toolbar.setSubtitle(tr.resources.get(mService.getCurrentResource()).server);
                    download.setVisibility(View.VISIBLE);
                    remove.setVisibility(View.GONE);
                }
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(v.getContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        355);

                        } else {
                            v.setVisibility(View.GONE);
                            findViewById(R.id.progressani2).setVisibility(View.VISIBLE);
                            try {
                                mService.downloadCurrentFile((FileDownloadTaskListener) ctx);
                            } catch (MalformedURLException e) {
                                findViewById(R.id.progressani2).setVisibility(View.GONE);
                                v.setVisibility(View.VISIBLE);
                                Toast.makeText((Context) ctx,
                                        "Error in downloading file from server",
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(v.getContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    356);

                        } else {
                            Track temp = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
                            DataBackend.removeOfflineTrack(temp,
                                    temp.resources.get(mService.getCurrentResource()).uuid);
                            toolbar.setSubtitle(temp.resources.get(mService.getCurrentResource()).server);
                            v.setVisibility(View.GONE);
                            findViewById(R.id.downloadButt2).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(mService!=null && hasFocus) {
            if (mService.isPlaying()) {
                ((ImageButton) findViewById(R.id.playbutton))
                        .setImageResource(R.drawable.ic_pause);
            } else {
                ((ImageButton) findViewById(R.id.playbutton))
                        .setImageResource(R.drawable.ic_play);
            }
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
    public void onCoverFetched(Track tr, int id){
        PagerContainer container = findViewById(R.id.pager_container);
        ViewPager pager = container.getViewPager();
        pager.setAdapter(new MainActivity.MyPagerAdapter());
        pager.setCurrentItem(mService.getCurrentTrackIndex(), false);
    }

    public static String parseDuration(long durationMs){
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
        ImageButton vie = findViewById(R.id.playbutton);
        vie.setImageResource(R.drawable.ic_play);
        PagerContainer container = findViewById(R.id.pager_container);
        ViewPager pager = container.getViewPager();
        pager.setCurrentItem(mService.getCurrentTrackIndex(), true);
    }

    @Override
    public void onNewTrackPlaying(Track newTrack) {
        PagerContainer container = findViewById(R.id.pager_container);
        ViewPager pager = container.getViewPager();
        pager.setCurrentItem(mService.getCurrentPlaylist().indexOf(newTrack), true);
    }

    @Override
    public void onPlaybackProgressUpdate(int currentMilliseconds) {
        long durat = mService.getDuration();
        if(durat==0)
            durat = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex()).duration*1000;
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferencesHandler.offline=PreferencesHandler.getOffline(this);
    }

    private Bitmap getCover(Track tr){
        byte[] cov = getEmbeddedCover(tr);

        // convert the byte array to a bitmap
        if(cov!= null && cov.length!=0)
            return BitmapFactory.decodeByteArray(cov, 0, cov.length);
        else {
            String art;
            if(tr.album_artist!=null)
                art=tr.album_artist;
            else
                art=tr.artist;
            Cover asd = DataBackend.getCover(art, tr.album);
            if(asd!=null)
                return BitmapFactory.decodeByteArray(asd.cover,0,asd.cover.length);
            else
                return BitmapFactory.decodeResource(this.getResources(), R.drawable.nocover);
        }
    }

    private byte[] getEmbeddedCover(Track tr){
        for(TrackResources r : tr.resources)
            if(r.cover_data!=null && r.cover_data.length!=0)
                return r.cover_data;
            else if(r.cover_data==null)
                mService.fetchCover(tr, tr.resources.indexOf(r));
        return new byte[0];
    }

    @Override
    public void OnFileDownloaded(String key, String id) {
        if(key!=null) {
            DataBackend.insertOfflineTrack(key, id);
            if (mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex()).uuid.compareTo(id) == 0) {
                findViewById(R.id.progressani2).setVisibility(View.GONE);
                findViewById(R.id.downloadButt2).setVisibility(View.GONE);
                Toolbar toolbar = findViewById(R.id.toolbar2);
                toolbar.setSubtitle("OFFLINE");
                findViewById(R.id.removeButt2).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.downloadButt2).setVisibility(View.VISIBLE);
            findViewById(R.id.progressani2).setVisibility(View.GONE);
            Toast.makeText((Context) ctx, "There was some errors in downloading file", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestComplete(int requestType, Exception e, String result) {
        if(e==null && requestType==3)
            Toast.makeText((Context) ctx, "Track Suggested to friends", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onPreparedPlayback() {
        ImageButton vie = findViewById(R.id.playbutton);
        vie.setImageResource(R.drawable.ic_pause);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 355: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ImageButton d = findViewById(R.id.downloadButt2);
                    d.callOnClick();

                } else {
                    Toast.makeText((Context) ctx, "Cannot download file without permissions", Toast.LENGTH_LONG).show();
                }
            }
            case 356: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ImageButton d = findViewById(R.id.removeButt2);
                    d.callOnClick();

                } else {
                    Toast.makeText((Context) ctx, "Cannot remove file without permissions", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

}
