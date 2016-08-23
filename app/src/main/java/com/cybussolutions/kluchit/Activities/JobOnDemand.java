package com.cybussolutions.kluchit.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Abdul on 22/08/2016.
 *
 * use EditText.setFocusable(false) to disable editing
 EditText.setFocusableInTouchMode(true) to enable editing;


 */
public class JobOnDemand extends Activity {


    Button btn;
    TextView s_date,e_date;
    String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
    ProgressDialog ringProgressDialog;
    EditText job_title;
    EditText job_description;
    EditText venue;
    Button submit;
    boolean created;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_on_demand);
        btn=(Button)findViewById(R.id.btn);
        s_date=(TextView)findViewById(R.id.start_dob);
        e_date=(TextView)findViewById(R.id.end_dob);


        btn.setVisibility(View.INVISIBLE);
        s_date.setText(timeStamp);
        e_date.setText("When you wish to!");
        job_title=(EditText) findViewById(R.id.SetTitleHere);
        job_description=(EditText) findViewById(R.id.description);
        venue=(EditText)findViewById(R.id.venue);
        submit=(Button)findViewById(R.id.submit);
        created=false;

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (created==false) {
                    ringProgressDialog = ProgressDialog.show(JobOnDemand.this, "Please wait ...", "Creating job ...", true);
                    ringProgressDialog.setCancelable(false);
                    ringProgressDialog.show();
                    if (check_compulsory_fields_for_submission())
                        json_send();
                    else
                        ringProgressDialog.dismiss();
                }
                else
                {
                    new AlertDialog.Builder(JobOnDemand.this)
                            .setTitle("End Job Confirmation Dialog:")
                            .setMessage("Are you sure about closing this job?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setCancelable(false)
                            .create().show();
                }
            }
        });
    }

    boolean check_compulsory_fields_for_submission()
    {

        if (job_title.getText().toString().isEmpty())
        {
            job_title.setError("Please mention job title");
            return false;
        }
        if (venue.getText().toString().isEmpty())
        {
            venue.setError("Please enter a venue");
            return false;
        }
        if (job_description.getText().toString().isEmpty())
        {
            job_description.setError("Please enter description");
            return false;
        }
        return true;
    }



    void json_send()
    {
        String upload_job= EndPoints.DEMAND_JOB;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, upload_job,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        ringProgressDialog.dismiss();
                        Toast.makeText(JobOnDemand.this,s,Toast.LENGTH_LONG).show();
                        btn.setVisibility(View.VISIBLE);
                        submit.setBackgroundResource(R.drawable.closejob);
                        created=true;


                        job_title.setFocusable(false);
                        job_description.setFocusable(false);
                        venue.setFocusable(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog

                        //Showing toast
                        //result+=volleyError.toString();
                        ringProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String


                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();


                String name=job_title.getText().toString();
                params.put("name",name);
                params.put("start_date",timeStamp);
                params.put("end_date",timeStamp);
                params.put("venue",venue.getText().toString());
                params.put("description",job_description.getText().toString());

                String date_modified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                params.put("date_modified",date_modified);



                /*params.put("job_status","1");
                params.put("date_added",timeStamp);
                params.put("date_modified",timeStamp);*/


                //returning parameters
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
}
