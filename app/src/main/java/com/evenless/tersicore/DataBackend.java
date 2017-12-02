package com.evenless.tersicore;

import android.util.Log;

import com.evenless.tersicore.model.Track;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class DataBackend {

    public static void addTracks(List<Track> tracks) {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(tracks);
        realm.commitTransaction();
    }

    public static void addTrack(Track track) {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(track);
        realm.commitTransaction();
    }

    public static List<String> getArtists() {
        RealmResults<Track> unique = getInstance().where(Track.class)
                .distinct("artist");
        ArrayList<String> result = new ArrayList<>();
        for (Track t : unique) {
            if (t.artist != null) {
                result.add(t.artist);
            }
        }
        return result;
    }

    public static List<String> getAlbums() {
        RealmResults<Track> unique = getInstance().where(Track.class)
                .distinct("album");
        ArrayList<String> result = new ArrayList<>();
        for (Track t : unique) {
            if (t.album != null) {
                result.add(t.album);
            }
        }
        return result;
    }

    public static RealmResults<Track> getTracks() {
        return getInstance().where(Track.class).findAll();
    }

    public static RealmResults<Track> getTracksByArtist(String artist) {
        return getInstance().where(Track.class).equalTo("album_artist", artist).findAll();
    }

    public static RealmResults<Track> getTracksByAlbum(String album) {
        return getInstance().where(Track.class).equalTo("album", album).findAll();
    }

    public static Track updateTrackCover(String uuid, byte[] cover) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Track track = realm.where(Track.class)
                .equalTo("uuid", uuid)
                .findFirst();
        if (track != null) {
            track.updateCover(cover);
        }
        realm.commitTransaction();
        Log.d("DataBackend", "updateTrackCover: track cover = length" + track.resources.get(0).cover_data.length);
        return track;
    }

    private static Realm getInstance() {
        return Realm.getDefaultInstance();
    }
}
