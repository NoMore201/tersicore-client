package com.evenless.tersicore;

import com.evenless.tersicore.model.Track;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class DataBackend {
    public static void addTracks(List<Track> tracks, Realm.Transaction.OnSuccess onSuccessCallback,
                                 Realm.Transaction.OnError onErrorCallback) {
        final List<Track> toAdd = tracks;
        getInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(toAdd);
            }
        }, onSuccessCallback, onErrorCallback);
    }

    public static void addTrack(Track track, Realm.Transaction.OnSuccess onSuccessCallback,
                                Realm.Transaction.OnError onErrorCallback) {
        final Track toAdd = track;
        getInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(toAdd);
            }
        }, onSuccessCallback, onErrorCallback);
    }

    public static RealmResults<Track> getTracks() {
        return getInstance().where(Track.class).findAll();
    }

    public static RealmResults<Track> getTracksByArtist(String artist) {
        return getInstance().where(Track.class).equalTo("album_artist", artist).findAll();
    }

    public static void customUpdate(Realm.Transaction transaction) {
        getInstance().executeTransactionAsync(transaction);
    }

    public static void customUpdateSync(Realm.Transaction transaction) {
        getInstance().executeTransaction(transaction);
    }

    private static Realm getInstance() {
        return Realm.getDefaultInstance();
    }
}
