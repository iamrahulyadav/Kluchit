package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.cybussolutions.kluchit.Adapters.Main_addapter;
import com.cybussolutions.kluchit.DataModels.Main_screen_pojo;
import com.cybussolutions.kluchit.Fragments.DrawerFragment;
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

public class Job_History extends AppCompatActivity {

    Tracker t;
    private Toolbar toolbar;
    TextView Email, name;
    ListView listView;
    private Main_addapter addapter;
    private ArrayList<Main_screen_pojo> listJobs = new ArrayList<>();
    ProgressDialog ringProgressDialog;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    String userId, user_cat;
    DrawerFragment drawerFragment = new DrawerFragment();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job__history);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("History");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);


        drawerFragment.setup((DrawerLayout) findViewById(R.id.drawerlayout), toolbar);


        ringProgressDialog = ProgressDialog.show(this,"", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userId = pref.getString("user_id", null);
        user_cat = pref.getString("user_cat", null);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Jsonrecieve();

        Email = (TextView) findViewById(R.id.total);
        name = (TextView) findViewById(R.id.userid);
        listView = (ListView) findViewById(R.id.listView2);
        addapter = new Main_addapter(getApplicationContext(), R.layout.singlerow, listJobs, this);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String job_id = ((TextView) view.findViewById(R.id.job_id)).getText().toString();
                String heading = ((TextView) view.findViewById(R.id.userid)).getText().toString();
                Intent intent = new Intent(Job_History.this,History_details.class);
                intent.putExtra("job_id",job_id);
                intent.putExtra("heading",heading);
                startActivity(intent);
            }
        });
        t = Analytics.getInstance(this).getDefaultTracker();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);
                    }
                }).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    protected void onStart() {
        super.onStart();

        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    protected void onResume() {
        super.onResume();
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void Jsonrecieve() {


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_ALL_JOBS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ringProgressDialog.dismiss();

                        parseJSONResponce(response);

                        listView.setAdapter(addapter);

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        ringProgressDialog.dismiss();
                        if (error instanceof NoConnectionError) {
                            Intent intent = new Intent(Job_History.this, NoInternet.class);
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
                params.put("user_catagory", user_cat);
                params.put("user_id", userId);
                params.put("screen_flag", "3");
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

            if (catagory == "false") {

                Intent intent = new Intent(Job_History.this,No_jobs.class);
                intent.putExtra("message","YOU HAVE NOT DONE ANY JOB WITH KLUCHIT");
                startActivity(intent);
            }

            JSONArray Array = new JSONArray(catagory);


            for (int i = 0; i < Array.length(); i++) {

                JSONObject Information = Array.getJSONObject(i);

                String job_id = Information.getString("job_id");
                String name = Information.getString("job_heading");
                String headin = Information.getString("job_start_date");
                String discription = Information.getString("job_description");


                Main_screen_pojo data = new Main_screen_pojo();
                data.setMaintxt(name);
                data.setCatagory(headin);
                data.setDiscription(discription);
                data.setJob_id(job_id);


                listJobs.add(data);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
