package com.evenless.tersicore.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.evenless.tersicore.DataBackend;
import com.evenless.tersicore.R;
import com.evenless.tersicore.model.EmailType;
import com.evenless.tersicore.model.User;

public class SingleEmailActivity extends AppCompatActivity {

    EmailType mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_email);
        String mailid = getIntent().getStringExtra("EXTRA_EMAIL_ID");
        mail = DataBackend.getMessage(mailid);
        DataBackend.setMessageAsRead(mail);
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
                asd.setImageBitmap(BitmapFactory.decodeByteArray(ava, 0, ava.length));
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
        }
    }
}
