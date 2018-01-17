package com.evenless.tersicore.model;

public class TrackSuggestion {

    public String uuid;
    public String album;
    public String artist;
    public String title;
    public String username;

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
        if(title!=null)
            return artist + " - " + title;
        else
            return artist + " - " + album;
    }
}
