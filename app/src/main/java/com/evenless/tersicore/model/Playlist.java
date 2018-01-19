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
        for (String s: tracks) {
            out.add(DataBackend.getTrack(s));
        }
        return out;
    }
}
