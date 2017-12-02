package com.evenless.tersicore;

import android.util.Log;

import com.evenless.tersicore.model.Track;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

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
        List<Track> tracks = getTracks();
        ArrayList<String> result = new ArrayList<>();
        for (Track t : tracks) {
            if (!result.contains(t.album_artist)) {
                result.add(t.album_artist);
            }
        }
        return result;
    }

    public static List<Track> getTracks() {
        return getInstance().where(Track.class).findAll();
    }

    public static List<Track> getTracksByArtist(String artist) {
        return getInstance().where(Track.class).equalTo("album_artist", artist).findAll();
    }

    public static List<Track> getTracksByAlbum(String album) {
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
