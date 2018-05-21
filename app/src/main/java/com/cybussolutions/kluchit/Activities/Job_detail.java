package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.DataModels.Job_details_pojo;
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    String j_o_d;




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
        closejob = (Button) findViewById(R.id.submit);


        closejob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new AlertDialog.Builder(Job_detail.this)
                        .setTitle("Close Job?")
                        .setMessage("Are you sure you want to Close this Job? ")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {

                                if (j_o_d.equals("1"))
                                {
                                    SharedPreferences pref = getSharedPreferences("JobOnDemand", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor=pref.edit();
                                    editor.clear();
                                    editor.commit();

                                    Closejob();

                                    Intent intent1 = new Intent(Job_detail.this,User_profile.class);
                                    startActivity(intent1);

                                    finish();
                                }

                                else {
                                    Intent intent1 = new Intent(Job_detail.this, Questions_Activity.class);
                                    intent1.putExtra("screen", "Post");
                                    intent1.putExtra("job_id", job_id);
                                    startActivity(intent1);
                                    finish();
                                }

                            }
                        }).create().show();


            }
        });


        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        t = Analytics.getInstance(this).getDefaultTracker();

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

                String job_id = Information.getString("id");
                String name = Information.getString("job_heading");
                String discription = Information.getString("job_description");
                String venue = Information.getString("job_venue");
                String start_date = Information.getString("job_start_date");
                j_o_d = Information.getString("job_on_demand");
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

    @Override
    public void onClick(View view) {


        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("job_id", job_id);
        editor.commit();


        /*new MaterialCamera(this)
                .saveDir(saveDir)
                .showPortraitWarning(true)
                .allowRetry(true)
                .defaultToFrontFacing(true)
               .start(CAMERA_RQ);*/
        Intent intent=new Intent(Job_detail.this,SocialSharingCusDef.class);
        startActivity(intent);



    }



}


