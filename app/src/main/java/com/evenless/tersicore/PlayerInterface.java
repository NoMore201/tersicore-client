package com.evenless.tersicore;

import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
        }
        ImageView temp = v.findViewById(R.id.minicover);
        if(tra.resources.get(0).cover_data!= null && tra.resources.get(0).cover_data.length!=0)
            temp.setImageBitmap(BitmapFactory.decodeByteArray(
                    tra.resources.get(0).cover_data, 0,
                    tra.resources.get(0).cover_data.length));
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
        int temp = mService.getCurrentTrackIndex() + 1;
        if(temp<mService.getCurrentPlaylist().size()){
            mService.seekToTrack(temp);
        }
    }

    public static void onClickBackward(View v, MediaPlayerService mService) {
        if(mService.getCurrentTrackIndex()!=0){
            mService.seekToTrack(mService.getCurrentTrackIndex()-1);
        }
    }

    public static void setStop(View v){
        ImageButton vie = (ImageButton) v.findViewById(R.id.miniplay);
        vie.setImageResource(R.drawable.ic_play);
    }
}
