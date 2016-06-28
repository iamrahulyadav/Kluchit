package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Adapters.Category_Job;
import com.cybussolutions.kluchit.Adapters.Category_adapter;
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Select_category extends AppCompatActivity {


    Tracker t;
    ProgressDialog ringProgressDialog;
    private String s_category = new String();
    int count;
    ListView listView;
    private Toolbar toolbar;
    private Category_adapter category_adapter;
    private String jsonResponse;
    private Analytics myApp;



    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            setContentView(R.layout.activity_select_category);

            toolbar = (Toolbar) findViewById(R.id.app_bar);
            toolbar.setTitle("Kluchit");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

         //   final Category_Job[] nameArray ={new Category_Job("Photographer",this), new Category_Job("Videographer",this), new Category_Job("Content Writery",this),new Category_Job( "Hair Stylist",this)};


        ringProgressDialog = ProgressDialog.show(this, "Please wait ...", "Loading ...", true);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.show();


        listView = (ListView) findViewById(R.id.listView3);

           // category_adapter = new Category_adapter(Select_category.this, nameArray);
          //  listView.setAdapter(category_adapter);

        t= Analytics.getInstance(this).getDefaultTracker();


        jsonResponse="";
        json();//json
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);

            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            count=0;
            for (int i=0;i<listView.getCount();i++)
            {
                if (category_adapter.values.containsKey(i)) {
                    if (count>0)
                    {
                        s_category+=",";
                    }
                    s_category += category_adapter.values.get(i);
                    count++;
                }
            }
           // Toast.makeText(getBaseContext(),s_category,Toast.LENGTH_LONG).show();
            Intent intent = this.getIntent();
            intent.putExtra("chosen",s_category);
            if (count>0) {
                this.setResult(RESULT_OK, intent);
            }
            else
            {
                this.setResult(RESULT_CANCELED, intent);
            }
            finish();
            return true;
        }


    @Override
    public void onBackPressed()
    {
        count=0;
        for (int i=0;i<listView.getCount();i++)
        {
            if (category_adapter.values.containsKey(i)) {
                if (count>0)
                {
                    s_category+=",";
                }
                s_category += category_adapter.values.get(i);
                count++;
            }
        }
        // Toast.makeText(getBaseContext(),s_category,Toast.LENGTH_LONG).show();
        Intent intent = this.getIntent();
        intent.putExtra("chosen",s_category);
        if (count>0) {
            this.setResult(RESULT_OK, intent);
        }
        else
        {
            this.setResult(RESULT_CANCELED, intent);
        }
        super.onBackPressed();
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

    public void json()
    {
        String urlJsonObj= EndPoints.BASE_URL+"common_controller/getAllUserCategories";
        // Request a string response
        JsonObjectRequest request = new JsonObjectRequest(urlJsonObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // Result handling
                       // System.out.println(response.toString());

                        ringProgressDialog.dismiss();

                        try {
                                JSONArray jo = (JSONArray) response.get("result");
                                Category_Job [] arr=new Category_Job[jo.length()];
                                for (int j=0;j<jo.length();j++) {
                                    JSONObject obj=jo.getJSONObject(j);
                                    String id=obj.get("id").toString();
                                    String cat_type=obj.get("cat_type").toString();
                                    arr[j]=new Category_Job(cat_type,getApplicationContext(),id);
                                   // arr.
                                }
                            category_adapter=new Category_adapter(Select_category.this,arr);
                            listView.setAdapter(category_adapter);

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        //    Toast.makeText(Select_category.this, e.toString(), Toast.LENGTH_LONG).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ringProgressDialog.dismiss();
                // Error handling
                System.out.println("Something went wrong!");
                error.printStackTrace();
             //   Toast.makeText(Select_category.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });


        Volley.newRequestQueue(this).add(request);

 //       Volley.newRequestQueue(this).add(jsonRequest);
    }
}
