package com.evenless.tersicore.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.CoverDownloadTaskListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by McPhi on 10/12/2017.
 */

public class CoverDownloadTask extends AsyncTask<Void, Integer, Bitmap> {

    private URL url;
    private CoverDownloadTaskListener mListener;
    private int mState;
    private String mQuery;

    public CoverDownloadTask(URL myurl, int state, String query, CoverDownloadTaskListener listener){
        url=myurl;
        mState=state;
        mListener=listener;
        mQuery = query;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap temp = null;
        try {
            temp = BitmapFactory.decodeStream((InputStream) url.getContent());
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
