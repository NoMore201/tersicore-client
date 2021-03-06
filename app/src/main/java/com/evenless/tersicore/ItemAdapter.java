package com.evenless.tersicore;

/*
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.activities.MainActivity;
import com.evenless.tersicore.model.Cover;
import com.evenless.tersicore.model.Track;
import com.woxthebox.draglistview.DragItemAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Long.parseLong;

public class ItemAdapter extends DragItemAdapter<Pair<Long, Track>, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public ItemAdapter(List<Track> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        List<Pair<Long, Track>> asd = new ArrayList<Pair<Long, Track>>();
        for(int i=0; i<list.size(); i++)
            asd.add(new Pair<>((long) i, list.get(i)));
        setItemList(asd);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Track tr = mItemList.get(position).second;
        String text = tr.toString();
        holder.mText.setText(text);
        byte[] tempcover=tr.getCover();
        if(tempcover!=null)
            holder.mImage.setImageBitmap(BitmapFactory.decodeByteArray(
                    tempcover, 0,
                    tempcover.length));
        else {
            String art;
            if(tr.album_artist!=null)
                art=tr.album_artist;
            else
                art=tr.artist;
            Cover asd = DataBackend.getCover(art, tr.album);
            if(asd!=null)
                holder.mImage.setImageBitmap(BitmapFactory.decodeByteArray(asd.cover,0,asd.cover.length));
            else
                holder.mImage.setImageResource(R.drawable.nocover);
        }
        holder.itemView.setTag(mItemList.get(position));
        holder.mDuration.setText(MainActivity.parseDuration((long) tr.duration*1000));
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText;
        ImageView mImage;
        TextView mDuration;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.text);
            mImage = itemView.findViewById(R.id.imagecover);
            mDuration = (TextView) itemView.findViewById(R.id.duration);
        }

        @Override
        public void onItemClicked(View view) {
        }

        @Override
        public boolean onItemLongClicked(View view) {
            return true;
        }
    }
}