package com.evenless.tersicore.model;

import android.graphics.Bitmap;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Track extends RealmObject {
    @PrimaryKey
    public String uuid;
    public String album;
    public String album_artist;
    public boolean compilation;
    public String date;
    public String disc_number;
    public String title;
    public String track_number;
    public RealmList<TrackResources> resources;
    public String artist;
    public int total_tracks;
    public int total_discs;
    public String label;
    public String isrc;
    public int duration;
    public String genre;
    public Date playedIn;

    public boolean updateCover(byte[] cover) {
        for (TrackResources tr : resources)
            if(tr.cover_data==null || tr.cover_data.length==0)
                tr.cover_data = cover;
            else
                return false;

        return true;
    }

    public byte[] getCover(){
        for (TrackResources tr : resources)
            if(tr.cover_data!=null && tr.cover_data.length!=0)
                return tr.cover_data;

        return null;
    }

    public boolean hasResources() {
        return resources != null && resources.size() > 0;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }

    public boolean hasBeenDownloaded() {
        for (TrackResources tr : resources)
            if(tr.isDownloaded)
                return true;

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            Track t = (Track) obj;
            return this.uuid.equals(t.uuid);
        } catch (Exception e){
            return false;
        }
    }

}
