package com.evenless.tersicore.tasks;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.evenless.tersicore.CoverRetrieveTaskListener;
import com.evenless.tersicore.model.Track;

import java.net.URL;
import java.util.HashMap;

public class CoverRetrieveTask extends AsyncTask<Void, Integer, byte[]> {
    private final static String TAG = "CoverRetrieveTask";

    private CoverRetrieveTaskListener mListener;
    private Track mTrack;
    private String mUrl;
    private int id;

    public CoverRetrieveTask(CoverRetrieveTaskListener listener,
                             Track track,
                             String url,
                             int imageId) {
        mListener = listener;
        mTrack = track;
        mUrl = url;
        id = imageId;
    }

    @Override
    protected byte[] doInBackground(Void... params) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(mUrl, new HashMap<String, String>());
        } catch (Exception e) {
            Log.e("CoverRetrieveTask", e.getMessage());
        }

        return mmr.getEmbeddedPicture();
    }

    @Override
    protected void onPostExecute(byte[] image) {
        super.onPostExecute(image);
        Log.d(TAG, "onPostExecute: get cover api request succeded");
        mListener.onCoverRetrieveComplete(mTrack, image, id);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
