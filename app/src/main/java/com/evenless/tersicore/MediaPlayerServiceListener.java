package com.evenless.tersicore;

import com.evenless.tersicore.model.Track;

/**
 * Common interface to handle MediaPlayerService events
 */

public interface MediaPlayerServiceListener {
    void onNewTrackPlaying(Track newTrack);
    void onPlaylistComplete();
    void onCoverFetched(Track track, int id);
    void onPlaybackError(Exception exception);
    void onPlaybackProgressUpdate(int currentMilliseconds);
    void onPreparedPlayback();
}
