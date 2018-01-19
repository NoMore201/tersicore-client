package com.evenless.tersicore.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Playlist extends RealmObject {
    @PrimaryKey
    public String id;
    public String name;
    public String uploader;
    public boolean favorite;
    public RealmList<Track> tracks;

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
}
