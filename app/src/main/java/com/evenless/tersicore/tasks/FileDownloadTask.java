package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.evenless.tersicore.interfaces.FileDownloadTaskListener;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileDownloadTask extends AsyncTask<Void, Integer, Boolean> {

    private URL mUrl;
    private FileDownloadTaskListener mListener;
    private String mQuery;
    private String mFormat;
    private String idm;

    public FileDownloadTask(URL myurl, String query, String format, FileDownloadTaskListener listener, String id){
        mUrl = myurl;
        mListener = listener;
        mQuery = query;
        mFormat=format;
        idm=id;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            InputStream is = mUrl.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;
            File s = new File(Environment.getExternalStorageDirectory() + "/TersicoreMusic");
            if(!s.exists())
                s.mkdir();

            FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/TersicoreMusic/" + mQuery + "." + mFormat));
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

        } catch (IOException e) {
            Log.e("FileDownloadTask", e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(aBoolean)
            mListener.OnFileDownloaded(mQuery, idm);
        else
            mListener.OnFileDownloaded(null, null);
    }
}
