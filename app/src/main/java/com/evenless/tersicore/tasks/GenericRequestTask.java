package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class GenericRequestTask extends AsyncTask<Void, Integer, String> {
    private final static String TAG = "GenericRequestTask";

    protected URL mUrl;
    protected Exception mThrownException;
    protected String mToken;

    protected GenericRequestTask(URL url, String token) {
        mUrl = url;
        mToken = token;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecute: get track api request succeded");
        notifyResult(result, mThrownException);
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection httpConnection = null;
        HttpsURLConnection httpsConnection = null;
        String result = null;
        StringBuilder sb;
        if (mUrl.getProtocol().equals("http")) {
            try {
                httpConnection = (HttpURLConnection) mUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setDoInput(true);
                if (mToken != null) {
                    httpConnection.setRequestProperty("AUTH", mToken);
                }
                httpConnection.connect();
                int responseCode = httpConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                result = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: unknown host", e);
                mThrownException = e;
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
            } else {
                try {
                    httpsConnection = (HttpsURLConnection) mUrl.openConnection();
                    httpsConnection.setRequestMethod("GET");
                    httpsConnection.setDoInput(true);
                    if (mToken != null) {
                        httpsConnection.setRequestProperty("AUTH", mToken);
                    }
                    httpsConnection.connect();
                    int responseCode = httpsConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP error code: " + responseCode);
                    }
                    BufferedReader br =
                            new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
                    sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    result = sb.toString();
                    br.close();
                } catch (Exception e) {
                    Log.e(TAG, "doInBackground: unknown host", e);
                    mThrownException = e;
                } finally {
                    if (httpsConnection != null) {
                        httpsConnection.disconnect();
                    }
                }
            }
        return result;
    }

    /**
     * Helper function used by subclasses to process the result of the request
     * and possible errors
     * @param result string returned by the API
     * @param e exception thrown
     */
    protected void notifyResult(String result, Exception e) {}
}
