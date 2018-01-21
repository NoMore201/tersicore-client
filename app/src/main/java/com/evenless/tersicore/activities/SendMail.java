package com.evenless.tersicore.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.ApiPostTaskListener;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackSuggestion;
import com.evenless.tersicore.model.User;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SendMail extends AppCompatActivity
    implements ApiPostTaskListener{

    Album suggestion;
    TrackSuggestion suggestionTrack;
    int counter=0;
    int succedeed=0;
    private User u;
    private ApiPostTaskListener ctx = this;
    private final String[] selectOption = {
            "Album",
            "Track"
    };
    private ArrayList<TrackSuggestion> trackSugg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);
        String mailid = getIntent().getStringExtra("EXTRA_CONTACT_NAME");
        String tracksuggested = getIntent().getStringExtra("EXTRA_UUID");
        if(tracksuggested!=null){
            Track temp = DataBackend.getTrack(tracksuggested);
            suggestionTrack = new TrackSuggestion();
            suggestionTrack.title = temp.title;
            suggestionTrack.uuid = temp.uuid;
            suggestionTrack.album = temp.album;
            suggestionTrack.artist = temp.artist;
            suggestionTrack.username = PreferencesHandler.getUsername(this);
            TextView asd = findViewById(R.id.songsend);
            asd.setText("Track Suggested: " + suggestionTrack.toString());
            findViewById(R.id.addsong).setVisibility(View.GONE);
            findViewById(R.id.removesong).setVisibility(View.VISIBLE);
        }
        u = SearchActivity.getUser(mailid);
        TextView username = findViewById(R.id.username);
        username.setText(u.id);
        ImageView imgv = findViewById(R.id.myimg);
        if(u.avatar!=null && u.avatar.length()!=0) {
            byte[] av = u.getAvatar();
            imgv.setImageBitmap(BitmapFactory.decodeByteArray(av, 0, av.length));
        }
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle("Send Email");
        ImageButton add = findViewById(R.id.addsong);
        ImageButton rem = findViewById(R.id.removesong);
        ImageButton send = findViewById(R.id.send);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackSugg=DataBackend.getTracksCopied();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailType email = new EmailType();
                email.recipient = u.id;
                EditText subj = findViewById(R.id.subject);
                email.object = subj.getText().toString();
                EditText body = findViewById(R.id.msg);
                email.msg = body.getText().toString();
                email.sender = PreferencesHandler.getUsername((Context) ctx);
                if(suggestion!=null) {
                    email.album = suggestion.name;
                    email.artist = suggestion.artist;
                } else if(suggestionTrack!=null)
                    email.songuuid = suggestionTrack.uuid;

                email.date = new Date().toString();
                counter=u.servers.size();
                for(String s : u.servers){
                    try {
                        TaskHandler.sendMessage(s, ctx, email);
                    } catch (MalformedURLException e) {
                        counter--;
                        e.printStackTrace();
                    }
                }
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context ctx = v.getContext();
                final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setItems(selectOption, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==1 || which==0) {
                                    AlertDialog.Builder build2 = new AlertDialog.Builder(ctx);
                                    final AutoCompleteTextView albums = new AutoCompleteTextView(ctx);
                                    switch (which) {
                                        // Album
                                        case 0:
                                            build2.setTitle("Search Album");
                                            ArrayAdapter<Album> adapter = new ArrayAdapter<>(ctx,
                                                    android.R.layout.simple_dropdown_item_1line, DataBackend.getAlbums());
                                            albums.setAdapter(adapter);
                                            break;
                                        // Track
                                        case 1:
                                            while(trackSugg.size()==0)
                                                try {
                                                    wait(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            build2.setTitle("Search Track");
                                            ArrayAdapter<TrackSuggestion> tadapter = new ArrayAdapter<>(ctx,
                                                    android.R.layout.simple_dropdown_item_1line, trackSugg);
                                            albums.setAdapter(tadapter);
                                            break;
                                    }
                                    final int selected = which;
                                    albums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                                                long id) {
                                            if (selected == 0) {
                                                suggestion = (Album) parent.getItemAtPosition(pos);
                                            } else {
                                                suggestionTrack = (TrackSuggestion) parent.getItemAtPosition(pos);
                                            }
                                        }
                                    });
                                    build2.setView(albums);
                                    // Set up the buttons
                                    build2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            TextView asd = findViewById(R.id.songsend);
                                            if (suggestion != null) {
                                                asd.setText("Album Suggested: " + suggestion.toString());
                                                findViewById(R.id.addsong).setVisibility(View.GONE);
                                                findViewById(R.id.removesong).setVisibility(View.VISIBLE);
                                            } else if (suggestionTrack != null) {
                                                asd.setText("Track Suggested: " + suggestionTrack.toString());
                                                findViewById(R.id.addsong).setVisibility(View.GONE);
                                                findViewById(R.id.removesong).setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                    build2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    build2.show();
                                }
                            }
                        });
                builder.show();
            }
        });
        rem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestionTrack=null;
                suggestion=null;
                findViewById(R.id.addsong).setVisibility(View.VISIBLE);
                findViewById(R.id.removesong).setVisibility(View.GONE);
                TextView asd = findViewById(R.id.songsend);
                asd.setText(R.string.no_suggestion_added);
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onRequestComplete(int requestType, Exception e, String result) {
        if(e!=null) {
            counter--;
            e.printStackTrace();
        }
        else
            succedeed++;

        if(counter==0)
            Toast.makeText((Context)ctx,
                    "There were errors sending message to your friend in all servers",
                    Toast.LENGTH_LONG).show();
        else if((counter-succedeed)==0){
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) ctx);
            builder.setMessage("Message Sent!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            builder.show();
        }

    }
}