package com.evenless.tersicore.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.evenless.tersicore.R;
import com.evenless.tersicore.PreferencesHandler;

public class Main2Activity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context ctx = this;

        if(PreferencesHandler.getServer(ctx) != null){
            goToNextActivity();
        } else {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);

            Button mButton = findViewById(R.id.buttonBegin);

            mButton.setOnClickListener(
                    new View.OnClickListener()
                    {
                        public void onClick(View view)
                        {
                            EditText mEdit = findViewById(R.id.editText2);
                            String IP = mEdit.getText().toString();
                            if(true) {
                                //PreferencesHandler.setServer(ctx, IP);
                                goToNextActivity();
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
                                alertDialog.setTitle("IP Not Valid");
                                alertDialog.setMessage("Tersicore has not been found in this server");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }
                    });
        }


    }

    private void goToNextActivity() {
        Intent asd = new Intent(this, MainActivity.class);
        startActivity(asd);
    }

}
