package com.cybussolutions.kluchit.Activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Adapters.AndroidMultiPartEntity;
import com.cybussolutions.kluchit.DataModels.Job_details_pojo;
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Job_detail extends AppCompatActivity implements View.OnClickListener {

    Tracker t;
    ProgressDialog ringProgressDialog;
    String userId, job_id;
    private Toolbar toolbar;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    private ArrayList<Job_details_pojo> listJobs = new ArrayList<>();
    TextView title, jobdescription, start, end, venuejob, isopen;
    Button closejob;
    private final static int CAMERA_RQ = 6969;
    private final static int PERMISSION_RQ = 84;
    long totalSize;
    ProgressBar progressBar;
    TextView txtPercentage;
    String filepath;
    boolean already_uploaded;

    Bitmap resize_insta(Bitmap yourBitmap) {

        return Bitmap.createScaledBitmap(yourBitmap, 800, 800, true);
    }


    private void createInstagramIntent(String type, String mediaPath) throws IOException {

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType(type);

        // Create the URI from the media
        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);


        ringProgressDialog = ProgressDialog.show(this, "", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userId = pref.getString("user_id", null);

        final Intent intent = getIntent();
        job_id = intent.getStringExtra("job_id");

        Jsonrecieve();

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
        toolbar.setTitle("Job Details");

        title = (TextView) findViewById(R.id.SetTitleHere);
        jobdescription = (TextView) findViewById(R.id.discription);
        start = (TextView) findViewById(R.id.start_dob);
        end = (TextView) findViewById(R.id.end_dob);
        venuejob = (TextView) findViewById(R.id.venue);
        isopen = (TextView) findViewById(R.id.status);
        closejob = (Button) findViewById(R.id.close_job);


        closejob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new AlertDialog.Builder(Job_detail.this)
                        .setTitle("Close Job?")
                        .setMessage("Are you sure you want to Close this Job? ")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent1 = new Intent(Job_detail.this, Questions_Activity.class);
                                intent1.putExtra("screen", "Post");
                                intent1.putExtra("job_id", job_id);
                                startActivity(intent1);
                                finish();
                            }
                        }).create().show();


            }
        });


        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        t = Analytics.getInstance(this).getDefaultTracker();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
        }


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);



        findViewById(R.id.btnUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (already_uploaded==false) {
                    new UploadFileToServer().execute();

                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                else
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Job_detail.this);
                    builder.setMessage("Video Already Uploaded to Server").setTitle("Attention!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                    android.app.AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });


        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createInstagramIntent("video/*",filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.cross).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }


    public void Closejob() {

        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.CLOSE_JOB,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ringProgressDialog.dismiss();

                        Toast.makeText(Job_detail.this, response, Toast.LENGTH_SHORT).show();
                        parseJSONResponce(response);


                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            Intent intent = new Intent(Job_detail.this, NoInternet.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(getApplication(), error.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                })


        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("job_id", job_id);
                params.put("user_id", userId);
                // 1 for open 0 for close
                params.put("is_open", "1");
                return params;

            }
        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);


    }

    public void Jsonrecieve() {


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_JOB_DETAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ringProgressDialog.dismiss();

                        parseJSONResponce(response);


                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            Intent intent = new Intent(Job_detail.this, NoInternet.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(getApplication(), error.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                })


        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("job_id", job_id);
                return params;

            }
        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);


    }

    // parsing josn responce
    private void parseJSONResponce(String responce) {


        String res = responce;

        String catagory;

        try {

            JSONObject object = new JSONObject(res);


            catagory = object.getString("result");


            JSONArray Array = new JSONArray(catagory);


            for (int i = 0; i < Array.length(); i++) {

                JSONObject Information = Array.getJSONObject(i);

                String job_id = Information.getString("job_id");
                String name = Information.getString("job_heading");
                String discription = Information.getString("job_description");
                String venue = Information.getString("job_venue");
                String start_date = Information.getString("job_start_date");
                String end_date = Information.getString("job_end_date");
                String job_status = Information.getString("job_status");

                Job_details_pojo data = new Job_details_pojo();


                data.setEnd_date(end_date);
                data.setHeading(name);
                data.setStatus(job_status);
                data.setVenue(venue);
                data.setStart_date(start_date);

                title.setText(name);
                jobdescription.setText(discription);
                start.setText(start_date);
                end.setText(end_date);
                venuejob.setText(venue);
                if (job_status.equals("1")) {
                    isopen.setText("open");

                } else {
                    isopen.setText("closed");

                }

                listJobs.add(data);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onClick(View view) {
        File saveDir = null;
        already_uploaded=false;


        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Only use external storage directory if permission is granted, otherwise cache directory is used by default
            saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Kluchit/videos");
            saveDir.mkdirs();
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("job_id", job_id);
        editor.commit();


        new MaterialCamera(this)
                .saveDir(saveDir)
                .showPortraitWarning(true)
                .allowRetry(true)
                .defaultToFrontFacing(true)
                .start(CAMERA_RQ);
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

                GeneralUtils.checkForPermissionsMAndAbove(Job_detail.this, true);
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


                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).show(getFragmentManager().findFragmentById(R.id.two)).commit();
                    filepath = "/sdcard/Pictures/Kluchit/" + getFileName(data.getData());


                } catch (Throwable e) {
                    Log.e("test", "vk run exception.", e);
                }


            }
            else if (resultCode == RESULT_CANCELED)
            {
                File saveDir = null;
                already_uploaded=false;


                //FragmentTransaction ft = getFragmentManager().beginTransaction();
                //ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Only use external storage directory if permission is granted, otherwise cache directory is used by default
                    saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Kluchit/videos");
                    saveDir.mkdirs();
                }

                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("job_id", job_id);
                editor.commit();



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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // Sample was denied WRITE_EXTERNAL_STORAGE permission
            Toast.makeText(this, "Videos will be saved in a cache directory instead of an external storage directory since permission was denied.", Toast.LENGTH_LONG).show();
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


    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {

            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }


        @SuppressWarnings("deprecation")
        private String uploadFile() {

            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://demo.cybussolutions.com/kluchitrm/vidupload.php");

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourcefile = new File(filepath);
                FileBody bdy = new FileBody(sourcefile);
                // Adding file data to http body
                entity.addPart("image", bdy);

                // Extra parameters if you want to pass to server
                entity.addPart("website", new StringBody("www.androidhive.info"));
                entity.addPart("email", new StringBody("abc@gmail.com"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("TAG", "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }


    private void showAlert(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        already_uploaded=true;

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                progressBar.setProgress(0);
                txtPercentage.setText("Press button to start uploading...");

            }
        }, 500);


    }
}
