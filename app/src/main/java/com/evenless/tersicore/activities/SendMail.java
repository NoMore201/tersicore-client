package com.evenless.tersicore.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.Album;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.TrackSuggestion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SendMail extends AppCompatActivity {

    Album suggestion;
    TrackSuggestion suggestionTrack;
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
        //Get Mail
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
                EditText username = findViewById(R.id.username);
                email.to = username.getText().toString();
                if(usernameExists()) {
                    EditText subj = findViewById(R.id.subject);
                    email.object = subj.getText().toString();
                    EditText body = findViewById(R.id.msg);
                    email.msg = body.getText().toString();
                    if(suggestion!=null) {
                        email.album = suggestion.name;
                        email.artist = suggestion.artist;
                    } else if(suggestionTrack!=null)
                        email.songuuid = suggestionTrack.uuid;
                    email.date = new Date().toString();
                    //Send Email
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

    private boolean usernameExists() {
        //is in list
        return true;
    }
}