package com.evenless.tersicore;

import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Track;

import me.crosswall.lib.coverflow.core.PagerContainer;

/**
 * Created by McPhi on 21/12/2017.
 */

public class PlayerInterface {
    public static void UpdateTrack(View v, MediaPlayerService mService){
        Track tra = mService.getCurrentPlaylist().get(mService.getCurrentTrackIndex());
        TextView tv_song = v.findViewById(R.id.minititle);
        tv_song.setText(tra.title);
        TextView tv_artist = v.findViewById(R.id.miniartist);
        if(tra.artist!=null)
            tv_artist.setText(tra.artist);
        else
            tv_artist.setText(tra.album_artist);
        if(mService.isPlaying()){
            ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
            vie.setImageResource(R.drawable.ic_pause);
        } else {
            ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
            vie.setImageResource(R.drawable.ic_play);
        }
        ImageView temp = v.findViewById(R.id.minicover);
        if(tra.resources.get(0).cover_data!= null && tra.resources.get(0).cover_data.length!=0)
            temp.setImageBitmap(BitmapFactory.decodeByteArray(
                    tra.resources.get(0).cover_data, 0,
                    tra.resources.get(0).cover_data.length));
        else {
            String art;
            if(tra.album_artist!=null)
                art=tra.album_artist;
            else
                art=tra.artist;
            Cover asd = DataBackend.getCover(art, tra.album);
            if(asd!=null)
                temp.setImageBitmap(BitmapFactory.decodeByteArray(asd.cover,0,asd.cover.length));
            else
                temp.setImageBitmap(BitmapFactory.decodeResource(v.getContext().getResources(), R.drawable.nocover));
        }
    }

    public static void onClickPlay(View v, MediaPlayerService mService) {
        if(mService.isPlaying()) {
            ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
            vie.setImageResource(R.drawable.ic_play);
            mService.pause();
        }
        else {
            ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
            vie.setImageResource(R.drawable.ic_pause);
            mService.play();
        }
    }

    public static void onClickForward(View v, MediaPlayerService mService) {
        mService.skip(MediaPlayerService.SkipDirection.SKIP_FORWARD);
    }

    public static void onClickBackward(View v, MediaPlayerService mService) {
        mService.skip(MediaPlayerService.SkipDirection.SKIP_BACKWARD);
    }

    public static void setStop(View v){
        ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
        vie.setImageResource(R.drawable.ic_play);
    }

    public static void setPlay(View v){
        ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
        vie.setImageResource(R.drawable.ic_pause);
    }
}
