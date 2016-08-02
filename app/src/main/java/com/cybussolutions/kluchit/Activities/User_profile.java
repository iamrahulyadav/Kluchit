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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Adapters.Main_addapter;
import com.cybussolutions.kluchit.DataModels.Main_screen_pojo;
import com.cybussolutions.kluchit.Fragments.DrawerFragment;
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.Network.Volley_singelton;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User_profile extends AppCompatActivity {

    Tracker t;
    private Toolbar toolbar;
    TextView total,name,current;
    ListView listView;
    private Main_addapter addapter;
    private ArrayList<Main_screen_pojo> listJobs = new ArrayList<>();
    ProgressDialog ringProgressDialog;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    String userId, user_cat,username,useremail;
    DrawerFragment drawerFragment = new DrawerFragment();
    private Volley_singelton volley_singelton;
    private ImageLoader imageLoader;
    ImageView pp;
    String profile;

        // test comment


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Profile");

        ringProgressDialog = ProgressDialog.show(this,"", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();


        volley_singelton=Volley_singelton.getInstance();
        imageLoader=volley_singelton.getLoader();

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        drawerFragment.setup((DrawerLayout) findViewById(R.id.drawerlayout), toolbar);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userId = pref.getString("user_id", null);
        user_cat=pref.getString("user_cat",null);
        username = pref.getString("user_name",null);
        useremail = pref.getString("user_email",null);
        profile = pref.getString("user_image",null);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Jsonrecieve();
        Jsonrecievejobinfo();

        total = (TextView) findViewById(R.id.total);
        name = (TextView) findViewById(R.id.userid);
        current = (TextView) findViewById(R.id.current);
        listView = (ListView) findViewById(R.id.list_profile);
        pp = (ImageView) findViewById(R.id.profile);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String job_id = ((TextView) view.findViewById(R.id.job_id)).getText().toString();
                Intent intent = new Intent(User_profile.this, Job_detail.class);
                intent.putExtra("job_id", job_id);
                startActivity(intent);
            }
        });

        String url = EndPoints.FB_PROFILE_PIC_PATH+profile;
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
               pp.setImageBitmap(response.getBitmap());

            }

            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("IMAGE ERROR","LOADING IMAGE ERROR");



            }
        });



        name.setText(username);


        addapter = new Main_addapter(getApplicationContext(), R.layout.singlerow, listJobs, this);

        t= Analytics.getInstance(this).getDefaultTracker();

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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();
        return true;
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
                    public void onErrorResponse(VolleyError error)
                    {
                        ringProgressDialog.dismiss();

                        if(error instanceof NoConnectionError) {
                            Intent intent = new Intent(User_profile.this,NoInternet.class);
                            startActivity(intent);

                        }

                        else
                        {
                            Toast.makeText(getApplication(), error.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                })


        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("user_catagory", user_cat);
                params.put("user_id",userId);
                params.put("screen_flag","2");
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

    public void Jsonrecievejobinfo() {



        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.USER_JOB_INFO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String catagory;
                        JSONObject object = null;

                        try {
                            object = new JSONObject(response);
                            catagory = object.getString("result");

                            JSONObject obj = new JSONObject(catagory);
                            String currentvalue  = obj.getString("current");
                            String totalvalue = obj.getString("total");

                            total.setText(totalvalue);
                            current.setText(currentvalue);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }




                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if(error instanceof NoConnectionError) {
                            Intent intent = new Intent(User_profile.this,NoInternet.class);
                            startActivity(intent);

                        }

                        else
                        {
                            Toast.makeText(getApplication(), error.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                })


        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
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

    private void parseJSONResponce(String responce) {




        String res = responce;
        
        String catagory;






        try {


            JSONObject object = new JSONObject(res);


            catagory = object.getString("result");

            if (catagory == "false") {

                Intent intent = new Intent(User_profile.this,No_jobs.class);
                intent.putExtra("message","NO CURRENTY HAVE NO JOBS AVAILABLE");
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




        }catch(JSONException e){
            e.printStackTrace();
        }



    }


}
