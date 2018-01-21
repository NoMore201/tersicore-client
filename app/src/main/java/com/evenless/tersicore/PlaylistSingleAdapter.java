package com.evenless.tersicore;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by McPhi on 29/12/2017.
 */

public class PlaylistSingleAdapter extends ArrayAdapter<Playlist> {

        Context context;
        int layoutResourceId;
        List<Playlist> data;

        public PlaylistSingleAdapter(Context context, int layoutResourceId, List<Playlist> data) {
            super(context, layoutResourceId,R.id.playtitle, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text1 = view.findViewById(R.id.playtitle);
            TextView text2 = view.findViewById(R.id.playartists);
            TextView text3 = view.findViewById(R.id.uploadedby);
            text1.setText(data.get(position).name);
            String feature = "Featuring ";
            ArrayList<String> artists = new ArrayList<>();
            for (Track t : data.get(position).getTrackObjects()) {
                if(artists.size()>3){
                    feature = feature + "...";
                    break;
                } else if(!artists.contains(t.artist)) {
                    artists.add(t.artist);
                    feature = feature + t.artist + ", ";
                }
            }
            if(feature.endsWith(", "))
                text2.setText(feature.substring(0, feature.lastIndexOf(", ")));
            else
                text2.setText(feature);
            text3.setText("Uploaded by: " + data.get(position).uploader);
            ToggleButton tb = view.findViewById(R.id.likeButt);
            tb.setChecked(data.get(position).favorite);
            return view;
        }

}
