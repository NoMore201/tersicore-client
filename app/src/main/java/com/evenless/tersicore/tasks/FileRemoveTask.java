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
        boolean yes = true;
        if(mQuery.equals("ALL")){
            File ass = new File(Environment.getExternalStorageDirectory() + "/TersicoreMusic");
            for(File f : ass.listFiles())
                if(!f.delete())
                    yes=false;
        }
        else{
            File asd = new File(Environment.getExternalStorageDirectory() + "/TersicoreMusic/" + mQuery + "." + mFormat);
            yes = asd.delete();
        }
        return yes;
    }
}
