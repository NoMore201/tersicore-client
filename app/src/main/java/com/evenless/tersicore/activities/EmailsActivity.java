package com.evenless.tersicore.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.EmailSingleAdapter;
import com.evenless.tersicore.MediaPlayerService;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.ApiRequestExtraTaskListener;
import com.evenless.tersicore.interfaces.MediaPlayerServiceListener;
import com.evenless.tersicore.PlayerInterface;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.Track;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by McPhi on 10/12/2017.
 */

public class EmailsActivity extends AppCompatActivity
        implements ApiRequestExtraTaskListener {

    private static final String TAG = "EmailsActivity";
    private List<EmailType> listEmails = new ArrayList<>();
    private Context ctx = this;
    private int counter = 0;

    @Override
    protected void onStart() {
        super.onStart();
        for(String ss : PreferencesHandler.getServer(ctx))
            try {
                TaskHandler.getMessages(this, ss);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mails);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Emails");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final SwipeRefreshLayout swip = findViewById(R.id.swiperefresh);
        swip.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                counter=0;
                for(String ss : PreferencesHandler.getServer(ctx))
                    try {
                        counter++;
                        TaskHandler.getMessages((ApiRequestExtraTaskListener) ctx, ss);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        counter--;
                    }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateList();
    }

    private void updateList() {
        ListView lsv = findViewById(R.id.mails);
        EmailSingleAdapter arrayAdapter = new EmailSingleAdapter(
                this,
                R.id.user,
                listEmails, false);

        lsv.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Intent asd = new Intent(v.getContext(), SingleEmailActivity.class);
                asd.putExtra("EXTRA_EMAIL_ID", listEmails.get(position).id);
                startActivity(asd);
            }
        });
    }

    @Override
    public void onRequestComplete(String response, Exception e, String token) {

    }

    @Override
    public void onLatestRequestComplete(String response, Exception e) {

    }

    @Override
    public void onPlaylistSingleRequestComplete(String result, Exception e) {

    }

    @Override
    public void onPlaylistsRequestComplete(String result, Exception e) {

    }

    @Override
    public void onSuggestionsRequestComplete(String result, Exception e) {

    }

    @Override
    public void onMessagesRequestComplete(String response, Exception e) {
        boolean aaa = false;
        counter--;
        if(e!=null)
            e.printStackTrace();
        else{
            EmailType[] allMessages = new Gson().fromJson(response, EmailType[].class);
            for(EmailType m : allMessages)
                if(m.recipient.equals(PreferencesHandler.getUsername(ctx))) {
                    EmailType duo = DataBackend.getMessage(m.id);
                    if(duo==null) {
                        DataBackend.insertMessage(m);
                        listEmails.add(m);
                        aaa=true;
                    }
                    else if(!listEmails.contains(duo)) {
                        aaa = true;
                        listEmails.add(duo);
                    }
                }
        }

        if(aaa) {
            Collections.sort(listEmails, new Comparator<EmailType>() {
                @Override
                public int compare(EmailType o1, EmailType o2) {
                    if(o1.isRead && !o2.isRead)
                        return 1;
                    else if (!o1.isRead && o2.isRead)
                        return -1;
                    else {
                        SimpleDateFormat format =
                                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                        try {
                            Date one = format.parse(o1.date);
                            Date two = format.parse(o2.date);
                            if(one.after(two))
                                return -1;
                            else if(one.before(two))
                                return 1;
                            else
                                return 0;
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                            return 0;
                        }
                    }
                }
            });
            updateList();
        }

        if(counter==0){
            SwipeRefreshLayout swip = findViewById(R.id.swiperefresh);
            swip.setRefreshing(false);
        }
    }

    @Override
    public void onUsersRequestComplete(String response, Exception e, String token) {

    }
}
