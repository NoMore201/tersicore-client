package com.evenless.tersicore.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EmailType extends RealmObject {

    @PrimaryKey
    public String id;
    public String to;
    public String from;
    public String date;
    public String object;
    public String msg;
    public String songuuid;
    public String album;
    public String artist;
    public String server;

}
