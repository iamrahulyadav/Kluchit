package com.cybussolutions.kluchit.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.afollestad.materialcamera.MaterialCamera;
import com.cybussolutions.kluchit.R;

import java.io.File;

/**
 * Created by Abdullah Manzoor Dar on 8/11/2016.
 */
public class Chooser_activity extends Activity {

    ImageButton camera,app;
    boolean continue_=false;
    ProgressDialog ringProgressDialog;
    private final static int CAMERA_RQ = 6969;


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


                File saveDir = null;

                //FragmentTransaction ft = getFragmentManager().beginTransaction();
                //ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();

                if (ContextCompat.checkSelfPermission(Chooser_activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Only use external storage directory if permission is granted, otherwise cache directory is used by default
                    saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Kluchit/videos");
                    saveDir.mkdirs();
                }



                new MaterialCamera(Chooser_activity.this)
                        .saveDir(saveDir)
                        .showPortraitWarning(true)
                        .allowRetry(true)
                        .defaultToFrontFacing(true)
                        .start(CAMERA_RQ);

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