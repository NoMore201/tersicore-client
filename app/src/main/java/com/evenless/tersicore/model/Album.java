package com.evenless.tersicore.model;


public class Album {
    public String name;
    public String artist;

    public Album (String n, String a){
        name = n;
        artist = a;
    }

    @Override
    public String toString() {
        return artist + " - " + name;
    }

    @Override
    public boolean equals(Object a){
        Album temp = (Album) a;
        return name.equals(temp.name) && artist.equals(temp.artist);
    }
}
