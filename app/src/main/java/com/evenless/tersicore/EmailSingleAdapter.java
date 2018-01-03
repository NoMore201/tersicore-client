package com.evenless.tersicore;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by McPhi on 29/12/2017.
 */

public class EmailSingleAdapter extends ArrayAdapter<EmailType> {

        Context context;
        int layoutResourceId;
        List<EmailType> data;

        public EmailSingleAdapter(Context context, int layoutResourceId, List<EmailType> data) {
            super(context, layoutResourceId,R.id.user, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text1 = view.findViewById(R.id.user);
            TextView text2 = view.findViewById(R.id.singasong);
            ImageView img = view.findViewById(R.id.accountimg);
            text1.setText(data.get(position).username);
            text2.setText(data.get(position).object);
            return view;
        }

}
