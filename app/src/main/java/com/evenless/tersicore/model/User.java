package com.evenless.tersicore.model;

import android.util.Base64;

public class User {
    public String id;
    public boolean online;
    public String lastTrack;
    public String avatar;
    public String password;

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

}
