package com.evenless.tersicore;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.evenless.tersicore.activities.SendMail;
import com.evenless.tersicore.activities.SingleEmailActivity;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by McPhi on 29/12/2017.
 */

public class MyUsersListAdapter extends ArrayAdapter<User> {

    private ArrayList<User> data;
    private Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtSong;
        ImageView info;
        ImageView avatar;
        ImageButton mailto;
    }

    public MyUsersListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<User> objects) {
        super(context, R.layout.frienditem, objects);
        data=objects;
        mContext = context;
    }

    @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            final View result;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.frienditem, parent, false);
                viewHolder.txtName = convertView.findViewById(R.id.user);
                viewHolder.txtSong = convertView.findViewById(R.id.singasong);
                viewHolder.info = convertView.findViewById(R.id.onlineimg);
                viewHolder.avatar = convertView.findViewById(R.id.accountimg);
                viewHolder.mailto = convertView.findViewById(R.id.mailto);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final User a = data.get(position);

            viewHolder.txtName.setText(a.id);
            if(a.last_track!=null)
                viewHolder.txtSong.setText(a.last_track);
            else
                viewHolder.txtSong.setText("");

            if(a.online)
                viewHolder.info.setVisibility(View.VISIBLE);
            else
                viewHolder.info.setVisibility(View.GONE);

            byte[] avatars = a.getAvatar();
            if(avatars!=null && avatars.length!=0)
                viewHolder.avatar.setImageBitmap(BitmapFactory.decodeByteArray(avatars, 0, avatars.length));
            viewHolder.mailto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent asd = new Intent(v.getContext(), SendMail.class);
                    asd.putExtra("EXTRA_CONTACT_NAME", a.id);
                    v.getContext().startActivity(asd);
                }
            });
            return convertView;
        }

}
