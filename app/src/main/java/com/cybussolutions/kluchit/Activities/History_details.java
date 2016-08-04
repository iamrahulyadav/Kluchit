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
import com.cybussolutions.kluchit.Adapters.history_adapter;
import com.cybussolutions.kluchit.DataModels.History_model;
import com.cybussolutions.kluchit.DataModels.Main_screen_pojo;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class History_details extends AppCompatActivity {

    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    private Toolbar toolbar;
    ListView listView;
    String userId,job_id,description;
    TextView status,amount;
    ProgressDialog ringProgressDialog;
    private history_adapter history_adapter1;
    private ArrayList<History_model> listhistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);


        final Intent intent = getIntent();
        job_id = intent.getStringExtra("job_id");
        description = intent.getStringExtra("heading");

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(description);

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        ringProgressDialog = ProgressDialog.show(this,"", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();

        listView = (ListView) findViewById(R.id.history_list);
        amount = (TextView) findViewById(R.id.amount);
        status = (TextView) findViewById(R.id.status);

        history_adapter1 = new history_adapter(getApplicationContext(), R.layout.singlerow, listhistory, this);

        Jsonrecieve();





        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userId = pref.getString("user_id", null);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void Jsonrecieve() {


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_QUESANDANS_HISTORY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        parseJSONResponce(response);
                        listView.setAdapter(history_adapter1);
                        JsonRecieveAmount();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        ringProgressDialog.dismiss();
                        if (error instanceof NoConnectionError) {
                            Intent intent = new Intent(History_details.this, NoInternet.class);
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
                params.put("jobid", job_id);
                params.put("userid", userId);

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
    public void JsonRecieveAmount() {


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.USER_AMOUNT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ringProgressDialog.dismiss();
                        String catagory;
                        JSONObject object = null;
                        try {
                            object = new JSONObject(response);
                            catagory = object.getString("result");
                            if(catagory.equals("false"))
                            {

                                status.setText("Not Verified");
                                amount.setText("0000");
                            }

                            else{
                                JSONArray Array = new JSONArray(catagory);

                                for (int i = 0; i < Array.length(); i++) {

                                    JSONObject Information = Array.getJSONObject(i);

                                    String amountreieve = Information.getString("total");
                                    String statusrecieve = Information.getString("paid");
                                    amount.setText(amountreieve);
                                    status.setText(statusrecieve);


                                    if (statusrecieve.equals("1")) {
                                        status.setText("PAID");
                                    } else  {
                                        status.setText("UN-PAID");
                                    }

                                }
                            }



                            //Toast.makeText(History_details.this,catagory, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }





                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        if (error instanceof NoConnectionError) {
                            Intent intent = new Intent(History_details.this, NoInternet.class);
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
                params.put("jobid", job_id);
                params.put("userid", userId);

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

                String questxt = Information.getString("qs_text");
                String quesdis = Information.getString("description");
                String quesans = Information.getString("question_answer");


                History_model data = new History_model();

                 data.setDescription(quesdis);
                data.setQuestionanswer(quesans);
                data.setQuestiontxt(questxt);

                listhistory.add(data);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
