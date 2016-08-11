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
import android.widget.ListView;
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
import com.cybussolutions.kluchit.Adapters.Question_adapter;
import com.cybussolutions.kluchit.DataModels.Questions;
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


public class Questions_Activity extends AppCompatActivity
{


    ListView listView;

    String catagory,q_Type,is_Accept,job_id,status;

    private Question_adapter addapter ;

    private Toolbar toolbar;

    String userid;


    Button sendReply;

    private static final int MY_SOCKET_TIMEOUT_MS = 10000 ;

    private ArrayList<Questions> list_Questions = new ArrayList<>();

    HashMap<String, String> value = null;

    ProgressDialog ringProgressDialog;

    String screen;



    Tracker t;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Questions");

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // geting user id from pref .
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userid = pref.getString("user_id", null);


        ringProgressDialog = ProgressDialog.show(this,"", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();

        final Intent intent = getIntent();
        catagory = intent.getStringExtra("user_catagory");
        is_Accept = intent.getStringExtra("is_accept");
        q_Type = intent.getStringExtra("ques_type");
        job_id = intent.getStringExtra("job_id");
        screen = intent.getStringExtra("screen");

        if (screen.equals("1"))
        {
            jsonSendPre(is_Accept);
        }
        else
        {
            jsonSendPost();
        }





        sendReply = (Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.question_list);

        addapter = new Question_adapter(getApplicationContext(), R.layout.ques_single_row, list_Questions, this);

        sendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                value = addapter.getvalue();


                if (list_Questions.size() == value.size()) {
                    jsonSendAnswers();
                    if(status.equals("POST"))
                    {
                        Closejob();
                    }
                   // Toast.makeText(Questions_Activity.this, value.toString(), Toast.LENGTH_SHORT).show();
                    finish();



                    Intent intent1 = new Intent(Questions_Activity.this, MainActivity.class);
                    startActivity(intent1);

                } else {
                    Toast.makeText(Questions_Activity.this, "answer all questions", Toast.LENGTH_SHORT).show();
                }

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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();
        Intent intent = new Intent(Questions_Activity.this,MainActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
       Intent intent = new Intent(Questions_Activity.this,MainActivity.class);
        startActivity(intent);
    }


    // for pre questions
    public void jsonSendPre(final String resutl)
    {

        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_PRE_QUESTIONS,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {

                        list_Questions = parseJSONResponce(response);
                        listView.setAdapter(addapter);
                        ringProgressDialog.dismiss();
                        status = "PRE";


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                ringProgressDialog.dismiss();
                if(error instanceof NoConnectionError) {
                    Intent intent = new Intent(Questions_Activity.this,NoInternet.class);
                    startActivity(intent);
                }

                else
                {
                    Toast.makeText(getApplication(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {

                Map<String,String> params = new HashMap<>();
                params.put("user_catagory",catagory);
                params.put("is_accept",resutl);
                params.put("ques_type",q_Type);
                params.put("job_id",job_id);
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


    // for post questions
    public void Closejob(){

        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.CLOSE_JOB,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ringProgressDialog.dismiss();

                        Toast.makeText(Questions_Activity.this,response, Toast.LENGTH_SHORT).show();
                        parseJSONResponce(response);


                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            Intent intent = new Intent(Questions_Activity.this, NoInternet.class);
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
                params.put("user_id",userid);
                // 1 for open 0 for close
                params.put("is_open","1");
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
    public void jsonSendPost()
    {

        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_POST_QUESTIONS,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {


                        list_Questions = parseJSONResponce(response);
                        listView.setAdapter(addapter);
                        ringProgressDialog.dismiss();

                        status = "POST";


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {

                if(error instanceof NoConnectionError) {
                    ringProgressDialog.dismiss();
                    Intent intent = new Intent(Questions_Activity.this,NoInternet.class);
                    startActivity(intent);
                }

                else
                {
                    Toast.makeText(getApplication(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    ringProgressDialog.dismiss();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {

                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                catagory = pref.getString("user_cat", null);
                Map<String,String> params = new HashMap<>();
                params.put("user_catagory",catagory);
                params.put("ques_type","1");
                params.put("job_id",job_id);
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



    private ArrayList<Questions> parseJSONResponce(String responce)  {

        JSONObject object;
        try
        {
            object = new JSONObject(responce);

            catagory = object.getString("result");

            JSONArray Array = new JSONArray(catagory);



            for (int i = 0; i < Array.length(); i++)
            {

                JSONObject Information=Array.getJSONObject(i);
                String q_id = Information.getString("id");
                String q_txt =Information.getString("qs_text");
                String cat_type =Information.getString("qs_category");


                Questions questions = new Questions();

                questions.setQ_Id(q_id);
                questions.setQ_Txt(q_txt);
                questions.setCat_type(cat_type);

                list_Questions.add(questions);



            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


            return list_Questions;
    }

    public void jsonSendAnswers()
    {


        StringRequest request = new StringRequest(Request.Method.POST, EndPoints.SEND_ANSWERS,
                new Response.Listener<String>()
                {
                   @Override
                    public void onResponse(String response)
                    {
                       // Toast.makeText(Questions_Activity.this,response, Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error instanceof NoConnectionError) {
                    Intent intent = new Intent(Questions_Activity.this,NoInternet.class);
                    startActivity(intent);

                }
               // Toast.makeText(Questions_Activity.this,error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {

                Map<String,String> params = new HashMap<>();
                params.put("answers",value.toString());
                params.put("job_id",job_id);
                params.put("user_id",userid);

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



