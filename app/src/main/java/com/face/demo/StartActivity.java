package com.face.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.face.demo.facefunc.FaceLivenessComposeActivity;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button btnPublic = findViewById(R.id.bt_public);
        btnPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, FaceLivenessComposeActivity.class);
                startActivity(intent);
            }
        });
        Button btnPrivate = findViewById(R.id.bt_private);
        btnPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainPrivateActivity.class);
                startActivity(intent);
            }
        });
    }

 
}