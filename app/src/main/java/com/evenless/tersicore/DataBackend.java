package com.evenless.tersicore;

import android.support.annotation.NonNull;

import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Track;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

public class DataBackend {

    /**
     * Save tracks into the database
     * @param tracks list of track informations to save
     */
    public static void insertTracks(List<Track> tracks) {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(tracks);
        realm.commitTransaction();
    }

    /**
     * Save a single track into the database
     * @param track track information to save
     */
    public static void insertTracks(Track track) {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(track);
        realm.commitTransaction();
    }

    /**
     * Get a list of all Artists contained in the collection
     * @return List of strings representing artists found
     */
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

    /**
     * Get all tracks saved in the collection
     * @return list of Track
     */
    public static RealmResults<Track> getTracks() {
        return getInstance().where(Track.class).findAll();
    }

    /**
     * Get a list of all Albums contained in the collection
     * @return list of Album
     */
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

    /**
     * Get all albums of the given artist
     * @param artist artist of the albums we want
     * @return list of Album
     */
    public static List<Album> getAlbums(@NonNull String artist) {
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

    /**
     * Get a single track
     * @param uuid uuid of the Track to retrieve
     * @return corresponding Track or null
     */
    public static Track getTrack(@NonNull String uuid) {
        return getInstance().where(Track.class)
                .equalTo("uuid", uuid)
                .findFirst();
    }

    /**
     * Get all tracks of a specific artist
     * @param artist used to filter tracks
     * @return list of Track
     */
    public static RealmResults<Track> getTracks(@NonNull String artist) {
        return getInstance().where(Track.class).equalTo("artist", artist).findAll();
    }

    /**
     * Get all tracks of a specific artist and album
     * @param artist
     * @param album
     * @return
     */
    public static RealmResults<Track> getTracks(@NonNull String artist, @NonNull String album) {
        return getInstance().where(Track.class)
                .equalTo("artist", artist)
                .equalTo("album", album)
                .findAllSorted("track_number");
    }

    public static void insertCover(String artist, String album, byte[] cover)
    throws RealmException
    {
        Realm realm = getInstance();
        realm.beginTransaction();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RealmException("Unable to hash data bytes");
        }
        Cover toInsert = new Cover();
        toInsert.hash = digest.digest(cover);
        toInsert.cover = cover;
        toInsert.artist = artist;
        toInsert.album = album;
        realm.copyToRealmOrUpdate(toInsert);
        realm.commitTransaction();
    }

    public static Cover getCover(byte[] hash) {
        return getInstance()
                .where(Cover.class)
                .equalTo("hash", hash)
                .findFirst();
    }

    public static Cover getCover(String artist, String album) {
        return getInstance()
                .where(Cover.class)
                .equalTo("artist", artist)
                .equalTo("album", album)
                .findFirst();
    }

    /**
     * Update cover data for Track with the given uuid
     * @param uuid ID of the track to update
     * @param cover cover data
     * @return updated track
     */
    public static Track updateTrackCover(@NonNull String uuid, byte[] cover) {
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
