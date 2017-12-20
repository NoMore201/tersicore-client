package com.evenless.tersicore.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Cover extends RealmObject {
    @PrimaryKey
    public byte[] hash;

    @Required
    public String artist;

    @Required
    public String album;

    @Required
    public byte[] cover;
}
