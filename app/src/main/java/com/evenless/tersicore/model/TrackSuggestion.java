package com.evenless.tersicore.model;

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
     * @param id
     * @param uuid
     * @param album
     * @param artist
     * @param title
     */
    public TrackSuggestion (String id, String uuid, String album, String artist, String title ){
        this.id = id;
        this.uuid = uuid;
        this.album = album;
        this.artist = artist;
        this.title = title;
    }

    /**
     * Constructor for album
     * @param id
     * @param album
     * @param artist
     */
    public TrackSuggestion(String id, String album, String artist) {
        this.id = id;
        this.album = album;
        this.artist = artist;
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
