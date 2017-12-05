package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.evenless.tersicore.ApiRequestTaskListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;



public class ApiRequestTask extends AsyncTask<Void, Integer, String> {
    private final static String TAG = "ApiRequestTask";

    protected ApiRequestTaskListener mListener;
    protected URL mUrl;

    public ApiRequestTask (ApiRequestTaskListener listener,
                           URL url) {
        mListener = listener;
        mUrl = url;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            Log.d(TAG, "onPostExecute: get track api request succeded");
            notifyResult(result);
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpsURLConnection connection = null;
        String result = null;
        StringBuilder sb;
        try {
            connection = (HttpsURLConnection) mUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            result = sb.toString();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    protected void notifyResult(String result) {
        mListener.onRequestComplete(result);
    }
}
