package com.cybussolutions.kluchit.Network;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Adapters.AndroidMultiPartEntity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Abdul on 21/08/2016.
 */
public class UploaderService extends Service {

    private static final String TAG = "UploaderService";

    private boolean isRunning  = false;

    final StringRequest request = new StringRequest(Request.Method.POST, "http://demo.cybussolutions.com/kluchitrm/common_controller/imageEntryDatabase",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Toast.makeText(getApplicationContext(),response, Toast.LENGTH_SHORT).show();
                    // ringProgressDialog.dismiss();



                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {


            if(error instanceof NoConnectionError) {

                Toast.makeText(getApplicationContext(), "No internet Connection, Try Again!", Toast.LENGTH_SHORT).show();

            }

            else
            {
                Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        }
    }) {
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            Map<String, String> params = new HashMap<>();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
            params.put("images",filename);

            SharedPreferences pref=getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            String job_id=pref.getString("job_id",null);
            String person_id=pref.getString("user_id",null);


            params.put("job_id",job_id);
            params.put("uploaded_by",person_id);
            params.put("date_added", timeStamp);//done
            params.put("date_modified", timeStamp);//done
            return params;

        }
    };


    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

        isRunning = true;
    }

    String filename=null;
    int flag=0;
    long totalSize=0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        SharedPreferences queue=getApplication().getSharedPreferences("Queue",Context.MODE_PRIVATE);
        filename=(String) queue.getString("filename",null);
        flag=(int) queue.getInt("flag",0);
        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR


        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {

        isRunning = false;

        Log.i(TAG, "Service onDestroy");
    }




    void send_image_db_request()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }


    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero

            //SocialSharing.this.setRequestedOrientation(SocialSharing.this.getResources().getConfiguration().orientation);

            //progressBar.setProgress(0);
            //txtPercentage.setText("Press Button to start uploading...");

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            //progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            //progressBar.setProgress(progress[0]);

            // updating percentage value
          //  txtPercentage.setText(String.valueOf(progress[0]) + "%");
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

                File sourceFile=null;
                if (filename!=null)
                    sourceFile = new File(filename);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("www.androidhive.info"));
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
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);
            //SocialSharing.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            super.onPostExecute(result);
        }

    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();



        if (flag==1)
            send_image_db_request();


    }




}