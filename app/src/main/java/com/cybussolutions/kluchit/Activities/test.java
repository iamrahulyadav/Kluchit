package com.cybussolutions.kluchit.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cybussolutions.kluchit.Network.UploaderService;
import com.cybussolutions.kluchit.R;

public class test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


            Intent mServiceIntent = new Intent(this, UploaderService.class);
                startService(mServiceIntent);
    }
}
