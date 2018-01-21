package com.evenless.tersicore.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EmailType extends RealmObject {

    @PrimaryKey
    public String id;
    public String recipient;
    public String sender;
    public String date;
    public String object;
    public String msg;
    public String songuuid;
    public String album;
    public String artist;
    public String server;
    public boolean isRead;

    @Override
    public boolean equals(Object obj) {
        try {
            EmailType t = (EmailType) obj;
            return this.id.equals(t.id);
        } catch (Exception e){
            return false;
        }
    }
}
