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

public class SingleEmailActivity extends AppCompatActivity {

    EmailType mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_email);
        String mailid = getIntent().getStringExtra("EXTRA_MAIL_ID");
        //Get Mail
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
        text1.setText(mail.from);
        text2.setText(mail.object);
        text3.setText(mail.msg);
        ImageButton rep = findViewById(R.id.reply);
        ImageButton play = findViewById(R.id.playsong);
        play.setVisibility(View.GONE);
        rep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent asd = new Intent(v.getContext(), SendMail.class);
                asd.putExtra("EXTRA_CONTACT_NAME", mail.from);
                startActivity(asd);
            }
        });
    }
}
