package com.evenless.tersicore.model;

public class TrackSuggestion {

    public String uuid;
    public String album;
    public String artist;
    public String title;

    public TrackSuggestion(){

    };

    public TrackSuggestion(String u, String a, String ar, String t){
        uuid=u;
        album=a;
        artist=ar;
        title=t;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}
