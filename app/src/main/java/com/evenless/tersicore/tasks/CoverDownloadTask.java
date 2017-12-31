package com.evenless.tersicore.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.evenless.tersicore.CoverDownloadTaskListener;
import com.evenless.tersicore.DataBackend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.evenless.tersicore.MyListAdapter.ARTIST_STATE;

public class CoverDownloadTask extends AsyncTask<Void, Integer, Bitmap> {

    private URL mUrl;
    private CoverDownloadTaskListener mListener;
    private int mState;
    private String mQuery;

    public CoverDownloadTask(URL myurl, int state, String query, CoverDownloadTaskListener listener){
        mUrl = myurl;
        mState = state;
        mListener = listener;
        mQuery = query;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap temp = null;
        try {
            temp = BitmapFactory.decodeStream((InputStream) mUrl.getContent());
        } catch (IOException e) {
            Log.e("CoverDownloadTask", e.getMessage());
        }
        return temp;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        mListener.OnCoverDownloaded(result, mState, mQuery);
    }
}
