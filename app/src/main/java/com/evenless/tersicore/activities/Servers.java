package com.evenless.tersicore.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evenless.tersicore.AlertDialogTrack;
import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.R;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.interfaces.ApiPostTaskListener;
import com.evenless.tersicore.interfaces.ApiRequestTaskListener;
import com.evenless.tersicore.interfaces.ServerStatusTaskListener;
import com.evenless.tersicore.model.Track;
import com.evenless.tersicore.model.User;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Servers extends AppCompatActivity
implements ServerStatusTaskListener, ApiPostTaskListener, ApiRequestTaskListener {

    private ApiPostTaskListener ctx = this;
    private DialogInterface pending = null;
    private String server = null;
    private EditText pendingInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Servers");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        createList();


        findViewById(R.id.floatingAdd).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        server=null;
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setMessage("Insert Server URL");
                        final EditText input = new EditText(v.getContext());
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        final AlertDialog sd = builder.show();
                        sd.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    URL toValidate = new URL(input.getText().toString());
                                    TaskHandler.isServerRunning((ServerStatusTaskListener) ctx, toValidate);
                                    sd.dismiss();
                                } catch (MalformedURLException e) {
                                    input.setError("URL should have the form protoc://host:port");
                                }
                            }
                        });
                    }
                }
        );
    }

    private void createList() {
        final ArrayList<String> servers = new ArrayList<>();
        servers.addAll(PreferencesHandler.getServer(this));
        ListView lsv = findViewById(R.id.listServers);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                servers) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                text1.setText(servers.get(position));
                text2.setText("Click to Delete");
                return view;
            }
        };
        lsv.setAdapter(arrayAdapter);
        // register onClickListener to handle click events on each item
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, final View v, final int position, long arg3) {
                if (servers.size() < 2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("You can't delete your last server!!");
                    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    final String s = servers.get(position);
                    // Use the Builder class for convenient dialog construction
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Are you sure you want to delete " + s + " ?")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    PreferencesHandler.deleteServer(v.getContext(), s);
                                    createList();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.show();
                }
            }
        });
    }

    @Override
    public void onServerStatusCheck(final URL originalUrl, boolean running) {
        if(running){
            server=originalUrl.toExternalForm();
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Insert Password for username " + PreferencesHandler.getUsername(this));
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog sd = builder.show();
            sd.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        User temp = new User();
                        temp.id = PreferencesHandler.getUsername(builder.getContext());
                        temp.password=input.getText().toString();
                        TaskHandler.Login(temp, originalUrl.toExternalForm(), ctx);
                        pending=sd;
                        pendingInput=input;
                    } catch (MalformedURLException e) {
                        input.setError("There was an error in the login phase");
                    }
                }
            });

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Tersicore is not Running in " + originalUrl.toExternalForm());
            builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

    }

    @Override
    public void onRequestComplete(int requestType, Exception e, String result) {
        if(e==null) {
            DataBackend.setToken(server, result);
            try {
                TaskHandler.getTracks(this, server);
            } catch (MalformedURLException e1) {
                Toast.makeText(this, "There were errors in downloading from this server: try to delete and add again!", Toast.LENGTH_LONG).show();
            }
            pending.dismiss();
            createList();
        } else {
            pendingInput.setError("Login Failed!");
        }
    }

    @Override
    public void onRequestComplete(String response, Exception e, String token) {
        if(e!=null){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (response != null) {
            String s = DataBackend.getServer(token);
            Track[] listTracks = new Gson().fromJson(response, Track[].class);
            DataBackend.insertTracks(new ArrayList<>(Arrays.asList(listTracks)), s);
        }
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
}
