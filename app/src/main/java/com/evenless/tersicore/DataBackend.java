package com.evenless.tersicore;

import android.support.annotation.NonNull;

import com.evenless.tersicore.model.Album;
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

    public static List<Album> getAlbums() {
        RealmResults<Track> unique = getInstance().where(Track.class)
                .distinct("album");
        ArrayList<Album> result = new ArrayList<>();
        for (Track t : unique) {
            if(t.album!=null)
                if (t.album_artist != null) {
                    result.add(new Album(t.album, t.album_artist));
                } else
                    result.add(new Album(t.album, t.artist));
        }
        return result;
    }

    public static RealmResults<Track> getTracks() {
        return getInstance().where(Track.class).findAll();
    }

    public static Track getTrackByUuid(String uuid) {
        return getInstance().where(Track.class)
                .equalTo("uuid", uuid)
                .findFirst();
    }

    public static List<Album> getAlbumsByArtist(@NonNull String artist) {
        RealmResults<Track> unique = getInstance().where(Track.class)
                .distinct("album", "album_artist");
        ArrayList<Album> result = new ArrayList<>();
        for (Track t : unique) {
            if(t.album!=null)
                if (t.album_artist!=null && t.album_artist.equalsIgnoreCase(artist)) {
                    result.add(new Album(t.album, t.album_artist));
                } else if (t.artist!=null && t.artist.equalsIgnoreCase(artist))
                    result.add(new Album(t.album, t.artist));
        }
        return result;
    }

    public static RealmResults<Track> getTracksByArtist(String artist) {
        return getInstance().where(Track.class).equalTo("artist", artist).findAll();
    }

    public static RealmResults<Track> getTracksByAlbum(String artist, String album) {
        return getInstance().where(Track.class)
                .equalTo("artist", artist)
                .equalTo("album", album)
                .findAllSorted("track_number");
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
        return track;
    }

    private static Realm getInstance() {
        return Realm.getDefaultInstance();
    }
}
