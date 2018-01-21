package com.evenless.tersicore;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.evenless.tersicore.activities.MainActivity;
import com.evenless.tersicore.activities.SearchActivity;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Playlist;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by McPhi on 29/12/2017.
 */

public class EmailSingleAdapter extends ArrayAdapter<EmailType> {

        Context context;
        int layoutResourceId;
        List<EmailType> data;

        //For use if we will do a "Sent Message" page
        boolean send;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtSong;
        ImageView info;
        ImageView avatar;
    }


    public EmailSingleAdapter(Context context, int layoutResourceId, List<EmailType> data, boolean s) {
            super(context, layoutResourceId,R.id.user, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
            send=s;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            EmailSingleAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

            final View result;

            EmailType a = data.get(position);

            if (convertView == null) {
                viewHolder = new EmailSingleAdapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.email_list, parent, false);
                viewHolder.txtName = convertView.findViewById(R.id.user);
                viewHolder.txtSong = convertView.findViewById(R.id.singasong);
                viewHolder.info = convertView.findViewById(R.id.onlineImgMail);
                viewHolder.avatar = convertView.findViewById(R.id.accountimg);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (EmailSingleAdapter.ViewHolder) convertView.getTag();
            }

            if(send)
                viewHolder.txtName.setText(a.recipient);
            else
                viewHolder.txtName.setText(a.sender);

            viewHolder.txtSong.setText(a.object);

            if(!a.isRead)
                viewHolder.info.setVisibility(View.VISIBLE);
            else
                viewHolder.info.setVisibility(View.GONE);

            Log.i("SingleEmailAdapter", a.isRead + "");
            User u = SearchActivity.getUser(a.sender);
            if(u!=null && u.avatar!=null && u.avatar.length()!=0) {
                byte[] avatars = u.getAvatar();
                if (avatars != null && avatars.length != 0)
                    viewHolder.avatar.setImageBitmap(BitmapFactory.decodeByteArray(avatars, 0, avatars.length));
            }

            return convertView;
        }

}
