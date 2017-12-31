package com.evenless.tersicore.model;

import android.graphics.Bitmap;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Cover extends RealmObject {

    @PrimaryKey
    public String hash;

    public String artist;

    public String album;

    @Required
    public byte[] cover;
}
