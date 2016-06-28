package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.cybussolutions.kluchit.DataModels.Main_screen_pojo;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Job_detail extends AppCompatActivity {

    ProgressDialog ringProgressDialog;
    String userId,job_id;
    private Toolbar toolbar;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    private ArrayList<Job_details_pojo> listJobs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        ringProgressDialog = ProgressDialog.show(this, "Please wait ...", "Loading ...", true);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.show();


        Intent intent = getIntent();
        job_id = intent.getStringExtra("job_id");


        Jsonrecieve();



        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
        toolbar.setTitle("Job Details");


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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


    public void Jsonrecieve() {


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_JOB_DETAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ringProgressDialog.dismiss();

                        Toast.makeText(Job_detail.this,response, Toast.LENGTH_SHORT).show();
                        parseJSONResponce(response);

                      //  listView.setAdapter(addapter);

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
                params.put("job_id",job_id );
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

                data.setDiscription(discription);
                data.setEnd_date(end_date);
                data.setHeading(name);
                data.setStatus(job_status);
                data.setVenue(venue);
                data.setStart_date(start_date);

                listJobs.add(data);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
