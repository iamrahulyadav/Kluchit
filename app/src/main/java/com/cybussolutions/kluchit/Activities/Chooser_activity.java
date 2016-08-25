package com.cybussolutions.kluchit.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cybussolutions.kluchit.R;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by Abdullah Manzoor Dar on 8/11/2016.
 */
public class Chooser_activity extends Activity {

    ImageButton camera,app;
    boolean continue_=false;
    ProgressDialog ringProgressDialog;


    boolean checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            // Place your dialog code here to display the dialog

            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
            return true;
        }
        return false;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser);




        camera= (ImageButton) findViewById(R.id.camera);
        app= (ImageButton) findViewById(R.id.app);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continue_=true;
                Intent intent=new Intent(Chooser_activity.this,JobOnDemand.class);
                startActivity(intent);

            }
        });

        app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continue_=true;
                Intent intent=new Intent(Chooser_activity.this,Splash_Activity.class);
                startActivity(intent);
                finish();
            }
        });


        boolean runner=false;
        if (checkFirstRun())
        {
            ringProgressDialog = ProgressDialog.show(this, "", "Welcome User! We are setting up things for you...", true);
            ringProgressDialog.setCancelable(false);
            ringProgressDialog.show();
            runner=true;
        }


        final boolean finalRunner = runner;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (continue_==false)//default action after 10 secs
                {
                    if (finalRunner ==true)
                        ringProgressDialog.dismiss();
                    continue_=true;
                    Intent intent=new Intent(Chooser_activity.this,Splash_Activity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 10000);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }


}