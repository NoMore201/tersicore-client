package com.evenless.tersicore.tasks;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;

public class FileRemoveTask extends AsyncTask<Void, Integer, Boolean> {

    private String mQuery;
    private String mFormat;

    public FileRemoveTask(String query, String format){
        mQuery = query;
        mFormat=format;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File asd = new File(Environment.getExternalStorageDirectory() + "/Music/" + mQuery + "." + mFormat);
        return asd.delete();
    }
}
