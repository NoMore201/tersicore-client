package com.evenless.tersicore.tasks;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.interfaces.CoverRetrieveTaskListener;
import com.evenless.tersicore.model.Track;

import java.util.HashMap;
import java.util.Map;

public class CoverRetrieveTask extends AsyncTask<Void, Integer, byte[]> {
    private final static String TAG = "CoverRetrieveTask";

    private CoverRetrieveTaskListener mListener;
    private Track mTrack;
    private String mUrl;
    private String mToken;
    private int id;
    private Exception mThrownException;

    public CoverRetrieveTask(CoverRetrieveTaskListener listener,
                             Track track,
                             String url,
                             String token,
                             int imageId) {
        mListener = listener;
        mTrack = track;
        mUrl = url;
        mToken = token;
        id = imageId;
    }

    @Override
    protected byte[] doInBackground(Void... params) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            Map<String,String> headers = new HashMap<String,String>();
            headers.put("AUTH", mToken);
            mmr.setDataSource(mUrl, headers);
        } catch (Exception e) {
            Log.e("CoverRetrieveTask", e.getMessage());
            mThrownException = e;
        }

        return mmr.getEmbeddedPicture();
    }

    @Override
    protected void onPostExecute(byte[] image) {
        super.onPostExecute(image);
        Log.d(TAG, "onPostExecute: get cover api request succeded");
        mListener.onCoverRetrieveComplete(mTrack, image, id, mThrownException);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
