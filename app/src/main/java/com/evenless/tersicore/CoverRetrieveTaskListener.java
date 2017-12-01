package com.evenless.tersicore;

import android.media.MediaMetadataRetriever;

import com.evenless.tersicore.model.Track;

public interface CoverRetrieveTaskListener {
    void onCoverRetrieveComplete(Track track, MediaMetadataRetriever cover);
}
