package com.evenless.tersicore.tasks;

import android.os.AsyncTask;

import com.evenless.tersicore.ApiRequestTaskListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ApiRequestTask extends AsyncTask<URL, Integer, String> {

    private ApiRequestTaskListener mListener;

    public ApiRequestTask (ApiRequestTaskListener listener) {
        mListener = listener;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mListener.onRequestComplete(result);
    }

    @Override
    protected String doInBackground(URL... urls) {
        HttpURLConnection connection = null;
        String result = null;
        StringBuilder sb;
        try {
            connection = (HttpURLConnection) urls[0].openConnection();
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
}
