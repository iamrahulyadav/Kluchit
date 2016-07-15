package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;

public class Job_discription extends AppCompatActivity {

    Tracker t;

    TextView discription, head, specialization;

    Button accept, reject, busy;

    String heading, spcl, discript, job_id;

    private Toolbar toolbar;

    private static final int MY_SOCKET_TIMEOUT_MS = 10000;

    String cat_type,userId;

    ProgressDialog ringProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job_discription);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Kluchit");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        discription = (TextView) findViewById(R.id.discription);
        head = (TextView) findViewById(R.id.heading);
        specialization = (TextView) findViewById(R.id.specailatiy);
        accept = (Button) findViewById(R.id.reply);
        reject = (Button) findViewById(R.id.reject);
        busy = (Button) findViewById(R.id.busy);

        Intent intent = getIntent();
        heading = intent.getStringExtra("name");
        spcl = intent.getStringExtra("specialization");
        discript = intent.getStringExtra("discription");
        job_id = intent.getStringExtra("job_id");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        cat_type = pref.getString("user_cat", null);
        editor.putString("job_id", job_id);
        userId = pref.getString("user_id", null);


        discription.setText(discript);
        specialization.setText(spcl);
        head.setText(heading);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Jsonsend("1");

            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Jsonsend("0");
                finish();
                Intent intent = new Intent(Job_discription.this,MainActivity.class);
                startActivity(intent);

            }
        });

        busy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Jsonsend("2");
                finish();
                finish();
                Intent intent = new Intent(Job_discription.this,MainActivity.class);
                startActivity(intent);

            }
        });
        t= Analytics.getInstance(this).getDefaultTracker();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Job_discription.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        Intent intent = new Intent(Job_discription.this,MainActivity.class);
        startActivity(intent);
        return true;
    }


    public void Jsonsend(final String result) {


        ringProgressDialog = ProgressDialog.show(this,"", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.SEND_RESPONCE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (result == "1") {

                            Intent intent = new Intent(Job_discription.this, Questions_Activity.class);
                            intent.putExtra("user_catagory", cat_type);
                            intent.putExtra("is_accept", result);
                            intent.putExtra("ques_type", "0");
                            intent.putExtra("job_id", job_id);
                            intent.putExtra("screen","1");

                            startActivity(intent);
                            finish();
                            ringProgressDialog.dismiss();

                        } else {
                            Toast.makeText(Job_discription.this,response, Toast.LENGTH_SHORT).show();
                            Toast.makeText(Job_discription.this, "Result has been Sent to the Administration", Toast.LENGTH_SHORT).show();

                            finish();
                           // ringProgressDialog.dismiss();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ringProgressDialog.dismiss();

                if(error instanceof NoConnectionError) {
                    Intent intent = new Intent(Job_discription.this,NoInternet.class);
                    startActivity(intent);

                }

                else
                {
                    Toast.makeText(getApplication(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("user_catagory", cat_type);
                params.put("is_accept", result);
                params.put("ques_type", "0");
                params.put("jobid",job_id);
                params.put("user_id",userId);
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


    @Override
    protected void onStart()
    {
        super.onStart();

        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    protected void onResume()
    {
        super.onResume();
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
