package com.evenless.tersicore;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Favorites;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.TrackSuggestion;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
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

    public static void updateFavorite(Track t, boolean isFav) {
        Realm realm = getInstance();
        realm.beginTransaction();
        if(isFav) {
            Favorites temp = new Favorites();
            temp.uuid=t.uuid;
            realm.copyToRealmOrUpdate(temp);
        }
        else
            try {
                realm.where(Favorites.class).and().equalTo("uuid", t.uuid).findFirst().deleteFromRealm();
            } catch (Exception e){
                Log.i(TAG,"No Favorite Found");
            }
        realm.commitTransaction();
    }

    public static void updateFavorite(Album a, boolean isFav) {
        Realm realm = getInstance();
        realm.beginTransaction();
        if(isFav) {
            Favorites temp = new Favorites();
            temp.uuid=a.name + a.artist;
            realm.copyToRealmOrUpdate(temp);
        }
        else
            try {
                realm.where(Favorites.class).equalTo("uuid", a.name + a.artist).findFirst().deleteFromRealm();
            } catch (Exception e){
                Log.i(TAG,"No Favorite Found");
            }
        realm.commitTransaction();
    }

    public static boolean checkFavorite(Album a) {
        Realm realm = getInstance();
        return realm.where(Favorites.class).equalTo("uuid", a.name + a.artist).findFirst()!=null;
    }

    public static boolean checkFavorite(Track t) {
        Realm realm = getInstance();
        Log.i(TAG, "Checking track favorite " + t.title);
        return realm.where(Favorites.class).equalTo("uuid", t.uuid).findFirst()!=null;
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

        ArrayList<String> result = new ArrayList<>();
        if(PreferencesHandler.offline){
            ArrayList<Track> asd = findAllOffline(null);
            for (Track t : asd)
                if(t.artist!=null && !result.contains(t.artist))
                    result.add(t.artist);
        } else {
            RealmResults<Track> unique;
            unique = getInstance().where(Track.class)
                    .distinct("artist");
            for (Track t : unique) {
                if (t.artist != null) {
                    result.add(t.artist);
                }
            }
        }
        return result;
    }

    private static ArrayList<Track> findAllOffline(RealmResults<Track> ttr) {
        if(ttr==null)
            ttr = getInstance().where(Track.class).findAll();
        ArrayList <Track> offlineTracks = new ArrayList<>();
        for (Track t : ttr)
            if(t.hasBeenDownloaded() || MediaPlayerService.hasBeenCached(t))
                offlineTracks.add(t);
        return offlineTracks;
    }

    /**
     * Get all tracks saved in the collection
     * @return list of Track
     */
    public static List<Track> getTracks() {
        if(PreferencesHandler.offline)
            return findAllOffline(null);
        else
            return getInstance().where(Track.class).findAll();
    }

    /**
     * Get a list of all Albums contained in the collection
     * @return list of Album
     */
    public static List<Album> getAlbums() {
        ArrayList<Album> result = new ArrayList<>();
        if(PreferencesHandler.offline){
            ArrayList<Track> asd = findAllOffline(null);
            for (Track t : asd)
                if(t.album!=null) {
                    Album n;
                    if (t.album_artist != null) {
                        n = new Album(t.album, t.album_artist);
                    } else
                        n = new Album(t.album, t.artist);
                    if(!result.contains(n))
                        result.add(n);
                }
        } else {
            RealmResults<Track> unique = getInstance().where(Track.class)
                    .distinct("album");
            for (Track t : unique) {
                if(t.album!=null)
                    if (t.album_artist != null) {
                        result.add(new Album(t.album, t.album_artist));
                    } else
                        result.add(new Album(t.album, t.artist));
            }
        }
        return result;
    }

    /**
     * Get all albums of the given artist
     * @param artist artist of the albums we want
     * @return list of Album
     */
    public static List<Album> getAlbums(@NonNull String artist) {
        ArrayList<Album> result = new ArrayList<>();
        if(PreferencesHandler.offline){
            ArrayList<Track> asd = findAllOffline(null);
            for (Track t : asd)
                if(t.album!=null) {
                    Album n=null;
                    if (t.album_artist != null && t.album_artist.equalsIgnoreCase(artist)) {
                        n = new Album(t.album, t.album_artist);
                    } else if(t.artist!=null && t.artist.equalsIgnoreCase(artist))
                        n = new Album(t.album, t.artist);
                    if(n!=null && !result.contains(n))
                        result.add(n);
                }
        } else {
            RealmResults<Track> unique = getInstance().where(Track.class)
                    .distinct("album", "album_artist");
            for (Track t : unique) {
                if(t.album!=null)
                    if (t.album_artist!=null && t.album_artist.equalsIgnoreCase(artist)) {
                        result.add(new Album(t.album, t.album_artist));
                    } else if (t.artist!=null && t.artist.equalsIgnoreCase(artist))
                        result.add(new Album(t.album, t.artist));
            }
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
     * @param artist string representing the artist
     * @return list of Track
     */
    public static ArrayList<Track> getTracks(@NonNull String artist) {
        RealmResults<Track> result = getInstance().where(Track.class).equalTo("artist", artist).findAll();
        if(PreferencesHandler.offline)
            return findAllOffline(result);
        else
            return new ArrayList<>(result);
    }

    /**
     * Get all Albums played lately
     * @return list of Track
     */
    public static ArrayList<Album> getLastTracks() {
        List<Track> result = getInstance().where(Track.class).
                isNotNull("playedIn").findAllSorted("playedIn", Sort.DESCENDING);

        if(PreferencesHandler.offline)
            result=findAllOffline((RealmResults<Track>) result);

        ArrayList<Album> toReturn = new ArrayList<>();
        for(Track t : result){
            String temp;
            if(t.album_artist==null)
                temp=t.artist;
            else
                temp=t.album_artist;

            Album a = new Album(t.album, temp);
            if(!toReturn.contains(a))
                toReturn.add(a);

            if(toReturn.size()>9)
                break;
        }
        return toReturn;
    }

    /**
     * Get all tracks of a specific artist and album
     * @param artist string representing the artist
     * @param album string representing the album
     * @return
     */
    public static ArrayList<Track> getTracks(@NonNull String artist, @NonNull String album) {
        List<Track> result =  getInstance().where(Track.class).beginGroup()
                .equalTo("artist", artist).or().equalTo("album_artist", artist)
                .endGroup().equalTo("album", album)
                .findAll();
        if(PreferencesHandler.offline)
            result=findAllOffline((RealmResults<Track>) result);
        ArrayList<Track> toReturn = new ArrayList<>(result);
        orderByTrack(toReturn);
        return toReturn;
    }

    public static void orderByTrack(ArrayList<Track> list) {
        Collections.sort(list, new Comparator<Track>() {
            @Override
            public int compare(Track first, Track second) {
                if(first.disc_number==null || second.disc_number==null || first.disc_number.equals(second.disc_number))
                    if(first.track_number!=null && second.track_number!=null)
                        return Integer.parseInt(first.track_number) - Integer.parseInt(second.track_number);
                    else
                        return first.title.compareTo(second.title);
                else
                    return Integer.parseInt(first.disc_number) - Integer.parseInt(second.disc_number);
            }
        });
    }

    /**
     * Insert cover data into the database
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
        Cover toInsert = new Cover();
        toInsert.hash = artist+album;
        toInsert.cover = cover;
        toInsert.artist = artist;
        toInsert.album = album;
        realm.copyToRealmOrUpdate(toInsert);
        realm.commitTransaction();
    }

    public static void setDate(Track t){
        Realm realm = getInstance();
        realm.beginTransaction();
        t.playedIn = new Date();
        realm.copyToRealmOrUpdate(t);
        realm.commitTransaction();
    }

    public static Cover getCover(String artist, String album) {
        return getInstance()
                .where(Cover.class)
                .equalTo("artist", artist)
                .equalTo("album", album)
                .findFirst();
    }

    public static List<Cover> getCovers() {
        return getInstance()
                .where(Cover.class)
                .findAll();
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
        boolean isUpdated=false;
        if (track != null) {
            isUpdated=track.updateCover(cover);
        }
        realm.commitTransaction();
        return isUpdated ? track : null;
    }

    private static Realm getInstance() {
        return Realm.getDefaultInstance();
    }

    public static List<Playlist> getPlaylists() {
        return getInstance().where(Playlist.class).findAll();
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

    public static void createNewPlaylist(String name, List<Track> toPlay) {
        Playlist p = new Playlist(name, "me");
        p.tracks.addAll(toPlay);
        insertPlaylist(p);
    }

    public static void addToPlaylist(Playlist playlist, List<Track> toPlay) {
        Realm realm = getInstance();
        realm.beginTransaction();
        RealmList<Track> listTracks = realm.where(Playlist.class)
                .equalTo("id", playlist.id)
                .findFirst().tracks;
        listTracks.addAll(toPlay);
        realm.commitTransaction();
    }

    public static void setPlaylistFavorite(String id, boolean isChecked) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Playlist p = realm.where(Playlist.class)
                .equalTo("id", id).findFirst();
        p.favorite = isChecked;
        realm.commitTransaction();
    }

    public static ArrayList<TrackSuggestion> getTracksCopied() {
        ArrayList<TrackSuggestion> temp = new ArrayList<>();
        for (Track t : getTracks())
            temp.add(new TrackSuggestion(t.uuid, t.album, getTrackArtist(t), t.title));
        return temp;
    }

    private static String getTrackArtist(Track t) {
        if (t.album_artist != null)
            return t.album_artist;
        else
            return t.artist;
    }

    public static void removeAll() {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.delete(Track.class);
        realm.commitTransaction();
    }

    public static Track insertOfflineTrack(String key, String id) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Track t = getTrack(id);
        for (TrackResources r : t.resources)
            if(r.uuid.compareTo(key)==0)
                r.isDownloaded=true;
        realm.insertOrUpdate(t);
        realm.commitTransaction();
        return t;
    }

    public static Track removeOfflineTrack(Track trr, String key) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Track t = getTrack(trr.uuid);
        for (TrackResources r : t.resources)
            if(r.uuid.compareTo(key)==0) {
                try {
                    TaskHandler.removeFile(key, r.codec);
                    r.isDownloaded = false;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            }
        realm.insertOrUpdate(t);
        realm.commitTransaction();
        return t;
    }
}
