package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.ApiRequestTaskListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;



public class GenericRequestTask extends AsyncTask<Void, Integer, String> {
    private final static String TAG = "GenericRequestTask";

    protected URL mUrl;

    public GenericRequestTask(URL url) {
        mUrl = url;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecute: get track api request succeded");
        notifyResult(result, null);
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
                } finally {
                    if (httpsConnection != null) {
                        httpsConnection.disconnect();
                    }
                }
            }
        return result;
    }

    protected void notifyResult(String result, Exception e) {
    }
}
