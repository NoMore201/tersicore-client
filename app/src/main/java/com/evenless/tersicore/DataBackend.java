package com.evenless.tersicore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

public class DataBackend {

    private static final String TAG = "DataBackend";

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
     * Save a single playlist into the database
     * @param p playlist information to save
     */
    public static void insertPlaylist(Playlist p) {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(p);
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
    public static ArrayList<Track> getTracks(@NonNull String artist) {
        RealmResults<Track> result = getInstance().where(Track.class).equalTo("artist", artist).findAll();
        ArrayList<Track> toReturn = new ArrayList<>(result);
        orderByTrack(toReturn);
        return toReturn;
    }

    /**
     * Get all tracks of a specific artist and album
     * @param artist
     * @param album
     * @return
     */
    public static ArrayList<Track> getTracks(@NonNull String artist, @NonNull String album) {
        RealmResults<Track> result =  getInstance().where(Track.class)
                .equalTo("artist", artist)
                .equalTo("album", album)
                .findAll();
        ArrayList<Track> toReturn = new ArrayList<>(result);
        orderByTrack(toReturn);
        return toReturn;
    }

    public static void orderByTrack(ArrayList<Track> list) {
        Collections.sort(list, new Comparator<Track>() {
            @Override
            public int compare(Track first, Track second) {
                int firstTrackNumber;
                if (first.track_number.contains("/")) {
                    String tmp = first.track_number.substring(0, first.track_number.indexOf("/"));
                    firstTrackNumber = Integer.parseInt(tmp);
                } else {
                    firstTrackNumber = Integer.parseInt(first.track_number);
                }
                int secondTrackNumber;
                if (second.track_number.contains("/")) {
                    String tmp = second.track_number.substring(0, second.track_number.indexOf("/"));
                    secondTrackNumber = Integer.parseInt(tmp);
                } else {
                    secondTrackNumber = Integer.parseInt(second.track_number);
                }
                return firstTrackNumber - secondTrackNumber;
            }
        });
    }

    /**
     * Insert cover data into the database, and index it with the MD5 of its data
     * @param artist string representing the artist
     * @param album string representing album or null if it's an artist image
     * @param cover cover bytes data
     * @throws RealmException when it's not possible to calculate hash
     */
    public static void insertCover(String artist, String album, byte[] cover)
    throws RealmException
    {
        Realm realm = getInstance();
        realm.beginTransaction();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RealmException("Unable to hash data bytes");
        }
        Cover toInsert = new Cover();
        byte[] hash = digest.digest(cover);
        toInsert.hash = new String(hash);
        Log.d(TAG, "insertCover: " + toInsert.hash);
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

    public static List<Playlist> getPlaylists() {
        ArrayList<Playlist> result = new ArrayList<>();
        for (Playlist p : getInstance().where(Playlist.class).findAll()){
            result.add(p);
        }
        return result;
    }

    public static Playlist getPlaylist(String pid) {
        return getInstance().where(Playlist.class).equalTo("id", pid).findFirst();
    }

    //Not Working
    public static RealmList<Track> modifyPlaylistPosition(int fromPosition, int toPosition, String id) {
        Realm realm = getInstance();
        realm.beginTransaction();
        RealmList<Track> listTracks = realm.where(Playlist.class)
                .equalTo("id", id)
                .findFirst().tracks;
        Track temp = listTracks.get(fromPosition);
        listTracks.remove(temp);
        listTracks.add(toPosition, temp);
        realm.commitTransaction();
        return listTracks;
    }

    public static RealmList<Track> deleteFromPlaylist(Track it, String id) {
        Realm realm = getInstance();
        realm.beginTransaction();
        RealmList<Track> listTracks = realm.where(Playlist.class)
                .equalTo("id", id)
                .findFirst().tracks;
        listTracks.remove(it);
        realm.commitTransaction();
        return listTracks;
    }
}
