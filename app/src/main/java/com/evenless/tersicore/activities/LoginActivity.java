package com.evenless.tersicore.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.R;
import com.evenless.tersicore.PreferencesHandler;
import com.evenless.tersicore.interfaces.ApiPostTaskListener;
import com.evenless.tersicore.interfaces.ServerStatusTaskListener;
import com.evenless.tersicore.TaskHandler;
import com.evenless.tersicore.model.User;

import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import io.realm.annotations.PrimaryKey;

public class LoginActivity extends AppCompatActivity implements ServerStatusTaskListener, ApiPostTaskListener {
    private final static String TAG = "LoginActivity";

    private boolean mContentSet = false;
    private String server="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> server = PreferencesHandler.getServer(this);
        if (server.size() != 0) {
            goToNextActivity();
        } else {
            setContentView(R.layout.activity_login);
            mContentSet = true;
        }
    }

    public void onClick(View view) {
        findViewById(R.id.buttonBegin).setEnabled(false);
        findViewById(R.id.loginInputText).setEnabled(false);
        findViewById(R.id.loginPassword).setEnabled(false);
        findViewById(R.id.loginUsername).setEnabled(false);
        EditText mEdit = findViewById(R.id.loginInputText);
        String address = mEdit.getText().toString();
        if (!address.contentEquals("")) {
            try {
                URL toValidate = new URL(address);
                TaskHandler.isServerRunning(this, toValidate);
            } catch (MalformedURLException e) {
                findViewById(R.id.buttonBegin).setEnabled(true);
                findViewById(R.id.loginInputText).setEnabled(true);
                findViewById(R.id.loginPassword).setEnabled(true);
                findViewById(R.id.loginUsername).setEnabled(true);
                mEdit.setError("URL should have the form protoc://host:port");
            }
        }
    }

    @Override
    public void onServerStatusCheck(URL originalUrl, boolean running) {
        if (running) {
            User temp = new User();
            TextView tt = findViewById(R.id.loginUsername);
            TextView pp = findViewById(R.id.loginPassword);
            temp.id = tt.getText().toString();
            temp.password = pp.getText().toString();
            try {
                TaskHandler.Login(temp, originalUrl.toExternalForm(), this);
                server=originalUrl.toExternalForm();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                onServerStatusCheck(originalUrl, false);
            }
        } else {
            if (!mContentSet) {
                setContentView(R.layout.activity_login);
                mContentSet = true;
            } else {
                findViewById(R.id.buttonBegin).setEnabled(true);
                findViewById(R.id.loginInputText).setEnabled(true);
                findViewById(R.id.loginPassword).setEnabled(true);
                findViewById(R.id.loginUsername).setEnabled(true);
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

    @Override
    public void onRequestComplete(int requestType, Exception e, String result) {
        TextView us = findViewById(R.id.loginUsername);
        if(e==null) {
            PreferencesHandler.setServer(this, server);
            PreferencesHandler.setUser(this, us.getText().toString());
            DataBackend.setToken(server, result);
            goToNextActivity();
        } else {
            if (!mContentSet) {
                setContentView(R.layout.activity_login);
                mContentSet = true;
            } else {
                findViewById(R.id.buttonBegin).setEnabled(true);
                findViewById(R.id.loginInputText).setEnabled(true);
                findViewById(R.id.loginPassword).setEnabled(true);
                us.setEnabled(true);
            }
            ((EditText)findViewById(R.id.loginUsername))
                    .setError("Login Failed!");
        }
    }
}
