package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.interfaces.ApiPostTaskListener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class GenericPostTask extends AsyncTask<Void, Integer, String> {
    public final static int POST_LOGIN = 11;
    public final static int POST_USERS = 21;
    public final static int POST_SUGGESTION = 31;
    public final static int POST_PLAYLIST = 41;
    public final static int POST_MESSAGE = 51;

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

    private String normalRequest() {
        HttpURLConnection httpConnection = null;
        String result="";
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
            if (responseCode != HttpURLConnection.HTTP_OK &&
                    responseCode != HttpURLConnection.HTTP_ACCEPTED &&
                    responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException("HTTP POST " +
                        responseCode +
                        ": " +
                        httpConnection.getResponseMessage());
            }
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            result = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            mThrownException = e;
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return result;
    }

    private String secureRequest() {
        HttpsURLConnection httpsConnection = null;
        String result="";
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
            if (responseCode != HttpsURLConnection.HTTP_OK &&
                    responseCode != HttpsURLConnection.HTTP_ACCEPTED &&
                    responseCode != HttpsURLConnection.HTTP_CREATED) {
                throw new IOException("HTTP POST " +
                        responseCode +
                        ": " +
                        httpsConnection.getResponseMessage());
            }
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            result = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            mThrownException = e;
        } finally {
            if (httpsConnection != null) {
                httpsConnection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecute: get track api request succeded");
        if (mListener != null) {
            mListener.onRequestComplete(mRequestType, mThrownException, result);
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        if (mUrl.getProtocol().equals("http")) {
            return normalRequest();
        } else {
            return secureRequest();
        }
    }
}
