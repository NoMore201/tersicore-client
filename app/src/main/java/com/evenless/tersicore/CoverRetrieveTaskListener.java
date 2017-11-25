package com.evenless.tersicore;

import com.evenless.tersicore.model.Track;

public interface CoverRetrieveTaskListener {
    void onCoverRetrieveComplete(Track track, byte[] cover);
}
