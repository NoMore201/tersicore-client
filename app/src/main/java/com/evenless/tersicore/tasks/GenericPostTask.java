package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.interfaces.ApiPostTaskListener;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class GenericPostTask extends AsyncTask<Void, Integer, Void> {
    public final static int POST_LOGIN = 1;
    public final static int POST_MESSAGE = 2;
    public final static int POST_SUGGESTION = 3;
    public final static int POST_PLAYLIST = 4;

    private final static String TAG = "GenericGetTask";

    private Exception mThrownException;
    private String mToken;
    private URL mUrl;
    private String mData;
    private int mRequestType;
    private ApiPostTaskListener mListener;


    public GenericPostTask(URL url, int requestType,
                              String token, String data,
                              ApiPostTaskListener listener) {
        mUrl = url;
        mRequestType = requestType;
        mToken = token;
        mData = data;
        mListener = listener;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecute: get track api request succeded");
        if (mListener != null) {
            mListener.onRequestComplete(mRequestType, mThrownException);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        HttpURLConnection httpConnection = null;
        HttpsURLConnection httpsConnection = null;
        if (mUrl.getProtocol().equals("http")) {
            try {
                httpConnection = (HttpURLConnection) mUrl.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoInput(true);
                httpConnection.setDoOutput(true);
                httpConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                if (mToken != null) {
                    httpConnection.setRequestProperty("AUTH", mToken);
                }
                BufferedOutputStream bs = new BufferedOutputStream(httpConnection.getOutputStream());
                bs.write(mData.getBytes("UTF-8"));
                bs.flush();
                httpConnection.connect();
                int responseCode = httpConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException(responseCode + ": " + httpConnection.getResponseMessage());
                }
            } catch (Exception e) {
                mThrownException = e;
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
        } else {
            try {
                httpsConnection = (HttpsURLConnection) mUrl.openConnection();
                httpsConnection.setRequestMethod("POST");
                httpsConnection.setDoInput(true);
                httpsConnection.setDoOutput(true);
                httpsConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                if (mToken != null) {
                    httpsConnection.setRequestProperty("AUTH", mToken);
                }
                BufferedOutputStream bs = new BufferedOutputStream(httpsConnection.getOutputStream());
                bs.write(mData.getBytes("UTF-8"));
                bs.flush();
                httpsConnection.connect();
                int responseCode = httpsConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK && responseCode!=HttpURLConnection.HTTP_ACCEPTED
                        && responseCode!=HttpURLConnection.HTTP_CREATED) {
                    throw new IOException(responseCode + ": " + httpsConnection.getResponseMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: unknown host", e);
                mThrownException = e;
            } finally {
                if (httpsConnection != null) {
                    httpsConnection.disconnect();
                }
            }
        }
        return null;
    }
}
