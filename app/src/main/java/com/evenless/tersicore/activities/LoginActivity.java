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
            try {
                URL url = new URL(server);
                TaskHandler.isServerRunning(this, url);
            } catch (MalformedURLException e) {
                Log.e(TAG, "onCreate: server url is invalid, resetting...", e);
            }
        } else {
            setContentView(R.layout.activity_login);
            mContentSet = true;
        }
    }

    public void onClick(View view) {
        EditText mEdit = findViewById(R.id.editText2);
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
            Log.d(TAG, "onServerStatusCheck: " + originalUrl.toExternalForm());
            PreferencesHandler.setServer(this, originalUrl.toExternalForm());
            goToNextActivity();
        } else {
            if (!mContentSet) {
                setContentView(R.layout.activity_login);
                mContentSet = true;
            }
            Log.d(TAG, "onServerStatusCheck: no server!");
            EditText text = findViewById(R.id.editText2);
            text.setError("no server running at " + originalUrl.toExternalForm());
        }

    }

    private void goToNextActivity() {
        Intent asd = new Intent(this, Main3Activity.class);
        startActivity(asd);
        finish();
    }

}
