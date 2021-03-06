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
    public long duration;
    public String date_added;
    public String server;
    public boolean isDownloaded;

    @Override
    public boolean equals(Object obj) {
        try {
            TrackResources t = (TrackResources) obj;
            return this.uuid.equals(t.uuid);
        } catch (Exception e){
            return false;
        }
    }
}
