package com.evenless.tersicore.model;

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
    public int disc_number;
    public String title;
    public int track_number;
    public RealmList<TrackResources> resources;
    public String artist;
    public int total_tracks;
    public int total_discs;
    public String label;
    public String isrc;
}
