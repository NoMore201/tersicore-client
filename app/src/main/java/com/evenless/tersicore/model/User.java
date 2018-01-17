package com.evenless.tersicore.model;


public class User {
    public String id;
    public boolean online;
    public String lastTrack;
    public byte[] avatar;

    @Override
    public String toString() {
        return id;
    }

}
