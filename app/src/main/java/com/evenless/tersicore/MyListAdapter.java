package com.evenless.tersicore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.model.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by McPhi on 05/12/2017.
 */

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>
implements ApiRequestTaskListener{
    private ArrayList<String> mDataset;
    private ArrayList<Track> mTrackSet;
    private Map<String, Bitmap> mImages;
    public static final int ARTIST_STATE = 0;
    public static final int ALBUMS_STATE = 1;
    private final String[] listtype = {"ARTISTS", "ALBUMS"};
    private int listtypeNumber;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(LinearLayout v) {
            super(v);
            mTextView = v.findViewById(R.id.covertitle);
            mImageView = v.findViewById(R.id.coverimg);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyListAdapter(ArrayList myDataset, Map<String, Bitmap> mImg, int state) {
        if(state==ARTIST_STATE)
            mDataset = myDataset;
        else if(state==ALBUMS_STATE)
            mTrackSet = myDataset;

        mImages=mImg;
        listtypeNumber=state;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layoutlist, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.i("ListAdapter", "Binding position " + position);
        String temp;
        Bitmap tempImg = null;
        if(listtypeNumber ==ARTIST_STATE) {
            temp = mDataset.get(position);
            tempImg = mImages.get(temp);
            getArtistImage(temp, ARTIST_STATE);
            if(tempImg==null)
                getArtistImage(temp, ALBUMS_STATE);
        }
        else if(listtypeNumber==ALBUMS_STATE) {
            temp = mTrackSet.get(position).album;
            tempImg = mImages.get(temp + mTrackSet.get(position).artist);
            if(tempImg==null)
                getAlbumCover(mTrackSet.get(position), ALBUMS_STATE);
        }
        else
            temp = "";

        if(tempImg!=null)
            holder.mImageView.setImageBitmap(tempImg);
        else
            holder.mImageView.setImageResource(R.drawable.nocover);

        holder.mTextView.setText(temp);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(listtypeNumber==ARTIST_STATE)
            return mDataset.size();
        else if (listtypeNumber==ALBUMS_STATE)
            return mTrackSet.size();
        else
            return 0;
    }

    private void getArtistImage(String s, int id) {
        if(!mImages.containsKey(s)){
            try {
                TaskHandler.getArtistImageFromApi(this, s, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getAlbumCover(Track tr, int id){
        if(!mImages.containsKey(tr.album + tr.artist)){
            try {
                TaskHandler.getAlbumImageFromApi(this, tr.artist, tr.album, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestComplete(String response) {
        //Nothing
    }

    @Override
    public void onApiRequestError(Exception e) {
        //Nothing
    }

    private int findAlbumByArtistAndName(String name, String artist){
        for (int i=0; i<mTrackSet.size(); i++){
            if(mTrackSet.get(i).album.equals(name) && mTrackSet.get(i).artist.equals(artist))
                return i;
        }
        return -1;
    }

    @Override
    public void onImgRequestComplete(String result, int id) {
        try {
            JSONObject tempJson = new JSONObject(result);
            if(tempJson.has("artist")) {
                JSONArray tmp = tempJson.getJSONObject("artist").getJSONArray("image");
                final String link = tmp.getJSONObject(2).getString("#text");
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(link).getContent());
                if(bitmap!=null) {
                    mImages.put(tempJson.getJSONObject("artist").getString("name"), bitmap);
                    int tempid = mDataset.indexOf(tempJson.getJSONObject("artist").getString("name"));
                    if (tempid != -1)
                        this.notifyItemChanged(tempid);
                }
            } else {
                JSONArray tmp = tempJson.getJSONObject("album").getJSONArray("image");
                final String link = tmp.getJSONObject(2).getString("#text");
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(link).getContent());
                if(bitmap!=null) {
                    String key = tempJson.getJSONObject("album").getString("name") + tempJson.getJSONObject("album").getString("artist");
                    mImages.put(key, bitmap);
                    int tempid = findAlbumByArtistAndName(tempJson.getJSONObject("album").getString("name"), tempJson.getJSONObject("album").getString("artist"));
                    if (tempid != -1)
                        this.notifyItemChanged(tempid);
                }
            }
        } catch (JSONException e) {
            Log.e("Main3Activity", e.getMessage());
        } catch (MalformedURLException e) {
            Log.e("Main3Activity", e.getMessage());
        } catch (IOException e) {
            Log.e("Main3Activity", e.getMessage());
        } catch (Exception e) {
            Log.e("Main3Activity", e.getMessage());
        }
    }
}