package com.evenless.tersicore;

import com.evenless.tersicore.model.Track;

/**
 * Common interface to handle MediaPlayerService events
 */

public interface MediaPlayerServiceListener {
    void onNewTrackPlaying(Track newTrack);
    void onPlaylistComplete();
    void onCoverFetched(Track track);
    void onPlaybackError(Exception exception);
    void onPlaybackProgressUpdate(Track track, int currentMilliseconds);
}
