package com.evenless.tersicore;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class TersicoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("tersicore.realm").build();
        Realm.setDefaultConfiguration(config);
    }

}
