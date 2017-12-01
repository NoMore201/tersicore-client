package com.evenless.tersicore.tasks;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.evenless.tersicore.CoverRetrieveTaskListener;
import com.evenless.tersicore.model.Track;

import java.util.HashMap;

public class CoverRetrieveTask extends AsyncTask<String, Integer, MediaMetadataRetriever> {
    private final static String TAG = "CoverRetrieveTask";

    private CoverRetrieveTaskListener mListener;
    private Track mTrack;

    public CoverRetrieveTask(CoverRetrieveTaskListener listener, Track track) {
        mListener = listener;
        mTrack = track;
    }

    @Override
    protected MediaMetadataRetriever doInBackground(String... urls) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();;
        try {
            mmr.setDataSource(urls[0], new HashMap<String, String>());
        } catch (Exception e) {
            Log.e("CoverRetrieveTask", e.getMessage());
        }

        return mmr;

    }

    @Override
    protected void onPostExecute(MediaMetadataRetriever image) {
        super.onPostExecute(image);
        Log.d(TAG, "onPostExecute: get cover api request succeded");
        mListener.onCoverRetrieveComplete(mTrack, image);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
