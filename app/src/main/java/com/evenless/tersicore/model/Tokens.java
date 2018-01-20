package com.evenless.tersicore.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Tokens extends RealmObject {

    @PrimaryKey
    public String server;
    public String token;


}
