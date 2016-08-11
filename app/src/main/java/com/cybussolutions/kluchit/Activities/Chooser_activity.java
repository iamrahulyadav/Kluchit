package com.cybussolutions.kluchit.Activities;

import android.Manifest;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
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
    private final static int CAMERA_RQ = 6969;
    String filepath;


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


        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        if (pref!=null) {
            editor.putString("offline", "0");// Saving string
            editor.commit();
        }




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


                editor.putString("offline", "1");// Saving string
                editor.commit();


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


    void add_watermark()
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        //if (!preferences.contains("watermark")) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("watermark", "1");
        editor.commit();

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/Pictures/Kluchit";
        File file = new File(extStorageDirectory, "watermark.PNG");
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // }
    }

    void add_watermark_vid()
    {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_vid);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/Pictures/Kluchit";
        File file = new File(extStorageDirectory, "watermark_vid.PNG");
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String readableFileSize(long size) {
        if (size <= 0) return size + " B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private String fileSize(File file) {
        return readableFileSize(file.length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ) {
            if (resultCode == RESULT_OK) {


                add_watermark();
                add_watermark_vid();


                String imagepath = data.getData().getPath();
                final File file = new File(data.getData().getPath());

                GeneralUtils.checkForPermissionsMAndAbove(Chooser_activity.this, true);
                LoadJNI vk = new LoadJNI();
                try {
                    String workFolder = getApplicationContext().getFilesDir().getAbsolutePath();
                    String filename=getFileName(data.getData());
                    String[] complexCommand=null;


                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    String left=pref.getString("logo_pos",null);

                    //                    complexCommand = new String[] {"ffmpeg", "-y", "-i", imagepath, "-strict", "experimental", "-vf", "movie=/sdcard/Pictures/Kluchit/watermark.PNG [watermark]; [in][watermark] overlay=main_w-overlay_w-10:10 [out]", "-s", "320x240", "-r", "30", "-b", "15496k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050", "/sdcard/Pictures/Kluchit/" + filename};

                    if (left=="0")
                        complexCommand = new String[] {"ffmpeg", "-y", "-i", imagepath, "-strict", "experimental", "-vf", "movie=/sdcard/Pictures/Kluchit/watermark_vid.PNG , scale=80:80 [watermark]; [in][watermark] overlay=10:10 [out]", "-s", "1024x768", "-r", "30", "-b", "15496k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050", "/sdcard/Pictures/Kluchit/" + filename};
                    else if (left=="1")
                        complexCommand = new String[] {"ffmpeg", "-y", "-i", imagepath, "-strict", "experimental", "-vf", "movie=/sdcard/Pictures/Kluchit/watermark_vid.PNG , scale=80:80 [watermark]; [in][watermark] overlay=main_w-overlay_w-10:10 [out]", "-s", "1024x768", "-r", "30", "-b", "15496k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050", "/sdcard/Pictures/Kluchit/" + filename};
                    else if (left=="2")
                        complexCommand = new String[] {"ffmpeg", "-y", "-i", imagepath, "-strict", "experimental", "-vf", "movie=/sdcard/Pictures/Kluchit/watermark_vid.PNG , scale=80:80 [watermark]; [in][watermark] overlay=10:main_h-overlay_h-10 [out]", "-s", "1024x768", "-r", "30", "-b", "15496k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050", "/sdcard/Pictures/Kluchit/" + filename};
                    else if (left=="3")
                        complexCommand = new String[] {"ffmpeg", "-y", "-i", imagepath, "-strict", "experimental", "-vf", "movie=/sdcard/Pictures/Kluchit/watermark_vid.PNG , scale=80:80 [watermark]; [in][watermark] overlay=main_w-overlay_w-10:main_h-overlay_h-10 [out]", "-s", "1024x768", "-r", "30", "-b", "15496k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050", "/sdcard/Pictures/Kluchit/" + filename};
                    else
                        complexCommand = new String[] {"ffmpeg", "-y", "-i", imagepath, "-strict", "experimental", "-vf", "movie=/sdcard/Pictures/Kluchit/watermark_vid.PNG , scale=80:80 [watermark]; [in][watermark] overlay=main_w/2-overlay_w/2:main_h/2-overlay_h/2 [out]", "-s", "1024x768", "-r", "30", "-b", "15496k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050", "/sdcard/Pictures/Kluchit/" + filename};


                    vk.run(complexCommand, workFolder, getApplicationContext());
                    Toast.makeText(this, String.format("Saved to: %s, size: %s",
                            file.getAbsolutePath(), fileSize(file)), Toast.LENGTH_LONG).show();

                    filepath = "/sdcard/Pictures/Kluchit/" + getFileName(data.getData());


                } catch (Throwable e) {
                    Log.e("test", "vk run exception.", e);
                }


            }
            else if (resultCode == RESULT_CANCELED)
            {
                File saveDir = null;


                //FragmentTransaction ft = getFragmentManager().beginTransaction();
                //ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Only use external storage directory if permission is granted, otherwise cache directory is used by default
                    saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Kluchit/videos");
                    saveDir.mkdirs();
                }




                new MaterialCamera(this)
                        .saveDir(saveDir)
                        .showPortraitWarning(true)
                        .allowRetry(true)
                        .defaultToFrontFacing(true)
                        .start(CAMERA_RQ);

            }
            else if (resultCode==RESULT_FIRST_USER)
            {

            }

            else if (data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}