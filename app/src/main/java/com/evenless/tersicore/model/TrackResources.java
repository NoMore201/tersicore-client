package com.evenless.tersicore.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TrackResources extends RealmObject {
    @PrimaryKey
    public String uuid;

    public int bitrate;
    public String codec;
    public int sample_rate;
    public byte[] cover_data;
}
