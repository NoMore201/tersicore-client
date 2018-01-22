package com.evenless.tersicore;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import com.evenless.tersicore.model.User;

import java.net.MalformedURLException;

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
