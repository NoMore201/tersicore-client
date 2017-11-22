package com.evenless.tersicore;

import android.media.MediaPlayer;

import com.evenless.tersicore.exceptions.InvalidUrlException;
import com.evenless.tersicore.model.Track;

/**
 * Common interface to handle MediaPlayerService events
 */

public interface MediaPlayerServiceListener {
    void onNewTrackPlaying(Track newTrack);
    void onPlaylistComplete();
    void onPlaybackError(Exception exception);
}
