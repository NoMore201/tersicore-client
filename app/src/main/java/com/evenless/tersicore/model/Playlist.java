package com.evenless.tersicore.model;

import android.provider.ContactsContract;

import com.evenless.tersicore.DataBackend;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Playlist extends RealmObject {
    @PrimaryKey
    public String id;
    public String name;
    public String uploader;
    public boolean favorite;
    public String date_added;
    public RealmList<String> tracks;

    public Playlist(){
        favorite = false;
        tracks = new RealmList<>();
    }

    public Playlist(String n, String u){
        name = n;
        uploader = u;
        favorite = false;
        id = n + u;
        tracks = new RealmList<>();
    }

    public List<Track> getTrackObjects() {
        List<Track> out = new ArrayList<>();
        List<String> toR = new ArrayList<>();
        for (String s: tracks) {
            Track t = DataBackend.getTrack(s);
            if(t!=null)
                out.add(t);
            else
                toR.add(s);
        }
        if(toR.size()>0)
            DataBackend.deleteNullsFromPlaylist(this,toR);
        return out;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            Playlist t = (Playlist) obj;
            return this.id.equals(t.id);
        } catch (Exception e){
            return false;
        }
    }
}
