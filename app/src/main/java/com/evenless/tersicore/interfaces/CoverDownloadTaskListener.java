package com.evenless.tersicore.interfaces;

import android.graphics.Bitmap;

/**
 * Created by McPhi on 10/12/2017.
 */

public interface CoverDownloadTaskListener {
    void OnCoverDownloaded(Bitmap result, int mState, String key);
}
