package com.evenless.tersicore.model;

import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id;
    public boolean online;
    public String last_track;
    public String avatar;
    public String password;
    public ArrayList<String> servers;

    public User (){}

    public User(String id, boolean online){
        this.id=id;
        this.online=online;
    }

    public User(String id, String playing){
        this.id=id;
        this.last_track=playing;
        this.online=true;
    }


    @Override
    public String toString() {
        return id;
    }

    public byte[] getAvatar() {
        String s = avatar;
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return Base64.decode(data, Base64.DEFAULT);
    }

    @Override
    public boolean equals(Object obj) {
        try {
            User t = (User) obj;
            return this.id.equals(t.id);
        } catch (Exception e){
            return false;
        }
    }

}
