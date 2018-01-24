package com.evenless.tersicore.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.MyUsersListAdapter;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackSuggestion;
import com.evenless.tersicore.model.User;

import java.net.MalformedURLException;

public class SingleEmailActivity extends AppCompatActivity {

    EmailType mail;
    Album suggestion;
    Track suggestionTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_email);
        String mailid = getIntent().getStringExtra("EXTRA_EMAIL_ID");
        mail = DataBackend.getMessage(mailid);
        if(mail==null)
            onBackPressed();
        else {
            Toolbar toolbar = findViewById(R.id.toolbar2);
            toolbar.setTitle("View Email");
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            ImageView asd = findViewById(R.id.myimg);
            TextView text1 = findViewById(R.id.myname);
            TextView text2 = findViewById(R.id.subject);
            TextView text3 = findViewById(R.id.msg);
            final User sender = SearchActivity.getUser(mail.sender);
            byte [] ava = sender.getAvatar();
            if(ava!=null && ava.length!=0)
                asd.setImageBitmap(
                        MyUsersListAdapter.getRoundedShape(BitmapFactory.decodeByteArray(ava, 0, ava.length)));
            text1.setText(mail.sender);
            text2.setText(mail.object);
            text3.setText(mail.msg);
            ImageButton rep = findViewById(R.id.reply);
            ImageButton play = findViewById(R.id.playsong);
            play.setVisibility(View.GONE);
            rep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent asd = new Intent(v.getContext(), SendMail.class);
                    asd.putExtra("EXTRA_CONTACT_NAME", sender.id);
                    startActivity(asd);
                }
            });
            TextView sg = findViewById(R.id.songsend);
            if(mail.songuuid!=null){
                suggestionTrack = DataBackend.getTrack(mail.songuuid);
                if(suggestionTrack!=null){

                    sg.setText("Track Suggested: " + suggestionTrack.toString());
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent pp = new Intent(v.getContext(), MainActivity.class);
                            pp.putExtra("EXTRA_UUID", suggestionTrack.uuid);
                            startActivity(pp);
                        }
                    });
                } else {
                    sg.setText("Suggested Track isn't in your database");
                }
            } else if (mail.album!=null){
                sg.setText("Album Suggested: " + mail.artist + " - " + mail.album);
                suggestion=new Album(mail.album, mail.artist);
                play.setVisibility(View.VISIBLE);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent asd = new Intent(v.getContext(), SingleAlbumActivity.class);
                        asd.putExtra("EXTRA_ARTIST", mail.artist);
                        asd.putExtra("EXTRA_ALBUM", mail.album);
                        v.getContext().startActivity(asd);
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mail.isRead) {
            DataBackend.setMessageAsRead(mail);
            EmailType re = new EmailType();
            re.id=mail.id;
            re.isRead=true;
            for (String ss : PreferencesHandler.getServer(this))
                try {
                    TaskHandler.sendMessage(ss, null, re);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
        }
    }
}
