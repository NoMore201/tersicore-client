package com.evenless.tersicore.model;

import com.evenless.tersicore.PreferencesHandler;

import java.util.UUID;

public class TrackSuggestion {

    // suggestion id
    public String id;
    // referring track uuid
    public String uuid;
    public String album;
    public String artist;
    public String title;
    public String username;

    public TrackSuggestion() {}

    /**
     * Constructor for a single track
     * @param uuid
     * @param album
     * @param artist
     * @param title
     */
    public TrackSuggestion (String uuid, String album, String artist, String title, String username){
        this.id = UUID.randomUUID().toString();
        this.uuid = uuid;
        this.album = album;
        this.artist = artist;
        this.title = title;
        this.username = username;
    }

    /**
     * Constructor for album
     * @param album
     * @param artist
     */
    public TrackSuggestion(String album, String artist, String username) {
        this.id = UUID.randomUUID().toString();
        this.album = album;
        this.artist = artist;
        this.username = username;
    }

    @Override
    public String toString() {
        if(title!=null)
            return artist + " - " + title;
        else
            return artist + " - " + album;
    }

    public boolean isTrack() {
        return uuid != null;
    }
}
