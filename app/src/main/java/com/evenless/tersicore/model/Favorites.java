package com.evenless.tersicore.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Favorites extends RealmObject {

    @PrimaryKey
    public String uuid;
}
