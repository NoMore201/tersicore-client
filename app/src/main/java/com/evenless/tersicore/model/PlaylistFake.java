package com.evenless.tersicore.model;

import com.evenless.tersicore.DataBackend;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PlaylistFake {

    public String id;
    public String name;
    public String uploader;
    public ArrayList<String> tracks;

    public PlaylistFake(){

    }

    public PlaylistFake(String id, String name, String uploader, RealmList<String> tracks){
        this.id = id;
        this.name=name;
        this.uploader=uploader;
        if(tracks!=null) {
            this.tracks = new ArrayList<>();
            this.tracks.addAll(tracks);
        }
    }
}
