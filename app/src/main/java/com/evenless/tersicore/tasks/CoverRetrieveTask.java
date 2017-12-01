package com.evenless.tersicore.tasks;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.CoverRetrieveTaskListener;
import com.evenless.tersicore.model.Track;

import java.util.HashMap;

public class CoverRetrieveTask extends AsyncTask<String, Integer, byte[]> {
    private final static String TAG = "CoverRetrieveTask";

    private CoverRetrieveTaskListener mListener;
    private Track mTrack;

    public CoverRetrieveTask(CoverRetrieveTaskListener listener, Track track) {
        mListener = listener;
        mTrack = track;
    }

    @Override
    protected byte[] doInBackground(String... urls) {
        byte [] data = null;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(urls[0], new HashMap<String, String>());
            data = mmr.getEmbeddedPicture();
        } catch (Exception e) {
            Log.e("CoverRetrieveTask", e.getMessage());
        } finally {
            return data;
        }
    }

    @Override
    protected void onPostExecute(byte[] image) {
        super.onPostExecute(image);
        Log.d(TAG, "onPostExecute: get cover api request succeded");
        mListener.onCoverRetrieveComplete(mTrack, image);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
