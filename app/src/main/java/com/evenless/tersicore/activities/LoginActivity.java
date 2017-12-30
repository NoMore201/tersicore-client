package com.evenless.tersicore.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.evenless.tersicore.R;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.ServerStatusTaskListener;
import com.evenless.tersicore.TaskHandler;

import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements ServerStatusTaskListener {
    private final static String TAG = "LoginActivity";

    private boolean mContentSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String server = PreferencesHandler.getServer(this);
        if (server != null) {
            /*
            Logica sbagliata: Tersicore deve funzionare anche offline
            try {
                URL url = new URL(server);
                TaskHandler.isServerRunning(this, url);

            } catch (MalformedURLException e) {
                Log.e(TAG, "onCreate: server url is invalid, resetting...", e);
            }*/
            goToNextActivity();
        } else {
            setContentView(R.layout.activity_login);
            mContentSet = true;
        }
    }

    public void onClick(View view) {
        findViewById(R.id.buttonBegin).setEnabled(false);
        findViewById(R.id.loginInputText).setEnabled(false);
        EditText mEdit = findViewById(R.id.loginInputText);
        String address = mEdit.getText().toString();
        if (!address.contentEquals("")) {
            try {
                URL toValidate = new URL(address);
                TaskHandler.isServerRunning(this, toValidate);
            } catch (MalformedURLException e) {
                mEdit.setError("URL should have the form protoc://host:port");
            }
        }
    }

    @Override
    public void onServerStatusCheck(URL originalUrl, boolean running) {
        if (running) {
            PreferencesHandler.setServer(this, originalUrl.toExternalForm());
            goToNextActivity();
        } else {
            if (!mContentSet) {
                setContentView(R.layout.activity_login);
                mContentSet = true;
            } else {
                findViewById(R.id.buttonBegin).setEnabled(true);
                findViewById(R.id.loginInputText).setEnabled(true);
            }
            ((EditText)findViewById(R.id.loginInputText))
                    .setError("no server running at " + originalUrl.toExternalForm());
        }
    }

    private void goToNextActivity() {
        Intent asd = new Intent(this, SearchActivity.class);
        startActivity(asd);
        finish();
    }

}
