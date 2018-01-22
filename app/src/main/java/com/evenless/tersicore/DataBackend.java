package com.evenless.tersicore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Favorites;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Tokens;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackResources;
import com.evenless.tersicore.model.TrackSuggestion;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmException;

public class DataBackend {

    private static final String TAG = "DataBackend";

    /**
     * Save tracks into the database
     * @param tracks list of track informations to save
     */
    public static void insertTracks(List<Track> tracks, String server) {
        List<Track> second = new ArrayList<>();
        Realm realm = getInstance();
        realm.beginTransaction();
        for(Track t : tracks){
            Track old = getTrack(t.uuid);
            for(TrackResources r : t.resources)
                r.server=server;
            if(old!=null){
                for(TrackResources r : t.resources)
                    if(!old.resources.contains(r))
                        old.resources.add(r);
                second.add(old);
            } else
                second.add(t);
        }
        realm.copyToRealmOrUpdate(second);
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
            ArrayList<Track> asd = findAllOffline();
            for (Track t : asd)
                if(t.artist!=null && !result.contains(t.artist))
                    result.add(t.artist);
        } else {
            RealmResults<Track> unique;
            unique = getInstance().where(Track.class)
                    .distinct("artist");
            for (Track t : unique) {
                if (t.artist != null && !containsIgnoreCase(t.artist, result)) {
                    result.add(t.artist);
                }
            }
        }
        return result;
    }
    private static boolean containsIgnoreCase(String artist, ArrayList<String> result) {
        for(String s : result)
            if(artist.equalsIgnoreCase(s))
                return true;
        return false;
    }

    private static ArrayList<Track> findAllOffline() {
        RealmResults<Track> tracks = getInstance().where(Track.class).findAll();
        return findAllOffline(tracks);
    }

    private static ArrayList<Track> findAllOffline(RealmResults<Track> tracks) {
        ArrayList <Track> offlineTracks = new ArrayList<>();
        for (Track t : tracks)
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
            return findAllOffline();
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
            ArrayList<Track> asd = findAllOffline();
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
                if(t.album!=null) {
                    Album temp=null;
                    if (t.album_artist != null) {
                        temp= new Album(t.album, t.album_artist);
                    } else
                        temp= new Album(t.album, t.artist);
                    if(!result.contains(temp))
                        result.add(temp);
                }
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
            ArrayList<Track> asd = findAllOffline();
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
                if(t.album!=null) {
                    Album temp = null;
                    if (t.album_artist != null && t.album_artist.equalsIgnoreCase(artist)) {
                        temp = new Album(t.album, t.album_artist);
                        if(!result.contains(temp))
                            result.add(temp);
                    }
                    else if (t.artist != null && t.artist.equalsIgnoreCase(artist)) {
                        temp = new Album(t.album, t.artist);
                        if(!result.contains(temp))
                            result.add(temp);
                    }
                }
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
        RealmResults<Track> result = getInstance().where(Track.class).equalTo("artist", artist, Case.INSENSITIVE).findAll();
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
                .equalTo("artist", artist, Case.INSENSITIVE).or().equalTo("album_artist", artist, Case.INSENSITIVE)
                .endGroup().equalTo("album", album, Case.INSENSITIVE)
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
                .equalTo("artist", artist, Case.INSENSITIVE)
                .equalTo("album", album, Case.INSENSITIVE)
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

    public static List<Playlist> getPlaylists(String username) {
        return sortByUploader(getInstance().where(Playlist.class)
                .findAllSorted("favorite", Sort.DESCENDING), username);
    }

    private static List<Playlist> sortByUploader(RealmResults<Playlist> favorite, final String u) {
        List<Playlist> favorites = new ArrayList<>();
        List<Playlist> remains = new ArrayList<>();
        for(Playlist p : favorite)
            if(p.uploader.equals(u))
                favorites.add(p);
            else
                remains.add(p);
        favorites.addAll(remains);
        return favorites;
    }

    public static Playlist getPlaylist(String pid) {
        return getInstance().where(Playlist.class).equalTo("id", pid).findFirst();
    }

    //Not Working
    public static List<Track> modifyPlaylistPosition(int fromPosition, int toPosition, String id) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Playlist p = realm.where(Playlist.class)
                .equalTo("id", id)
                .findFirst();
        List<String> listTracks = p.tracks;
        String temp = listTracks.get(fromPosition);
        listTracks.remove(temp);
        listTracks.add(toPosition, temp);
        realm.commitTransaction();
        return p.getTrackObjects();
    }

    public static List<Track> deleteFromPlaylist(Track it, String id) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Playlist p = realm.where(Playlist.class)
                .equalTo("id", id)
                .findFirst();
        List<Track> listTracks = p.getTrackObjects();
        p.tracks.remove(listTracks.indexOf(it));
        realm.commitTransaction();
        return p.getTrackObjects();
    }

    public static Playlist createNewPlaylist(String name, List<Track> toPlay, String username) {
        Playlist p = new Playlist(name, username);
        for (Track t: toPlay) {
            p.tracks.add(t.uuid);
        }
        insertPlaylist(p);
        return p;
    }

    public static Playlist addToPlaylist(Playlist playlist, List<Track> toPlay) {
        Realm realm = getInstance();
        realm.beginTransaction();
        List<String> toAdd = new ArrayList<>();
        for(Track t : toPlay)
            toAdd.add(t.uuid);
        playlist.tracks.addAll(toAdd);
        realm.insertOrUpdate(playlist);
        realm.commitTransaction();
        return playlist;
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
            temp.add(new TrackSuggestion(t.uuid, t.album, getTrackArtist(t), t.title, null));
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
        TaskHandler.removeAllFiles();
        realm.delete(TrackResources.class);
        realm.delete(Track.class);
        realm.commitTransaction();
    }

    public static Track insertOfflineTrack(String key, String id) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Track t = getTrack(id);
        for (TrackResources r : t.resources)
            if(r.uuid.equals(key)) {
                r.isDownloaded = true;
                realm.insertOrUpdate(r);
            }
        realm.commitTransaction();
        return t;
    }

    public static Track removeOfflineTrack(Track trr, String key) {
        Realm realm = getInstance();
        realm.beginTransaction();
        for (TrackResources r : trr.resources)
            if(r.uuid.compareTo(key)==0) {
                try {
                    TaskHandler.removeFile(key, r.codec);
                    r.isDownloaded = false;
                    realm.insertOrUpdate(r);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            }
        realm.commitTransaction();
        return trr;
    }

    public static List<Playlist> getMyPlaylists(String username) {
        return getInstance().where(Playlist.class)
                .equalTo("uploader", username)
                .findAllSorted("favorite", Sort.DESCENDING);
    }

    public static EmailType getMessage(String id) {
        return getInstance().where(EmailType.class)
                .equalTo("id", id).findFirst();
    }

    public static EmailType setMessageAsRead(EmailType mail) {
        Realm realm = getInstance();
        realm.beginTransaction();
        mail.isRead=true;
        realm.copyToRealmOrUpdate(mail);
        realm.commitTransaction();
        return mail;
    }

    public static void insertMessage(EmailType m) {
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(m);
        realm.commitTransaction();
    }

    public static String getToken(String server) {
        Tokens t = getInstance().where(Tokens.class).equalTo("server", server).findFirst();
        if(t!=null)
            return t.token;
        else
            return null;
    }

    public static String getServer(String token) {
        Tokens t = getInstance().where(Tokens.class).equalTo("token", token).findFirst();
        if(t!=null)
            return t.server;
        else
            return null;
    }

    public static List<String> getServers(){
        List<String> aa = new ArrayList<>();
        for(Tokens t : getInstance().where(Tokens.class).findAll())
            aa.add(t.server);
        return aa;
    }

    public static void setToken(String server, String result) {
        Tokens t = new Tokens();
        t.token=result;
        t.server=server;
        Realm realm = getInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(t);
        realm.commitTransaction();
    }

    public static void deleteToken(String server) {
        Realm realm = getInstance();
        realm.beginTransaction();
        Tokens toDel = realm.where(Tokens.class).equalTo("server", server).findFirst();
        if(toDel!=null)
            toDel.deleteFromRealm();
        realm.commitTransaction();
    }

    public static void deletePlaylist(Playlist pid) {
        Realm realm = getInstance();
        realm.beginTransaction();
        pid.deleteFromRealm();
        realm.commitTransaction();
    }

    public static void removeOfflineTracks() {
        Realm realm = getInstance();
        realm.beginTransaction();
        for(TrackResources t : realm.where(TrackResources.class).findAll())
            if(t.isDownloaded){
                t.isDownloaded=false;
                realm.copyToRealmOrUpdate(t);
            }
        realm.commitTransaction();
    }
}
