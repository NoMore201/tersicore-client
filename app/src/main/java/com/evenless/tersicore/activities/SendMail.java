package com.evenless.tersicore.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.evenless.tersicore.R;
import com.evenless.tersicore.model.EmailType;

public class SendMail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_email);
        String mailid = getIntent().getStringExtra("EXTRA_CONTACT_NAME");
        //Get Mail
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle("Send Email");
    }
}