package com.evenless.tersicore.tasks;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import com.evenless.tersicore.CoverRetrieveTaskListener;
import com.evenless.tersicore.model.Track;

public class CoverRetrieveTask extends AsyncTask<String, Integer, byte[]> {

    private CoverRetrieveTaskListener mListener;
    private Track mTrack;

    public CoverRetrieveTask(CoverRetrieveTaskListener listener, Track track) {
        mListener = listener;
        mTrack = track;
    }

    @Override
    protected byte[] doInBackground(String... urls) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(urls[0]);
        return mmr.getEmbeddedPicture();
    }

    @Override
    protected void onPostExecute(byte[] image) {
        super.onPostExecute(image);
        mListener.onCoverRetrieveComplete(mTrack, image);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
