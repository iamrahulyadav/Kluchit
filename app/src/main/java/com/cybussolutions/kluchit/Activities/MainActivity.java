package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.cybussolutions.kluchit.PushNotification.GCMRegistrationIntentService;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private static final int MY_SOCKET_TIMEOUT_MS = 10000;

    DrawerFragment drawerFragment = new DrawerFragment();

    ListView listView;

    private Main_addapter addapter;

    private Toolbar toolbar;

    String userId, user_cat;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private ArrayList<Main_screen_pojo> listJobs = new ArrayList<>();

    ProgressDialog ringProgressDialog;

    Tracker t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Intent intent=getIntent();
        if (intent.hasExtra("check"))
        {
            String fb_userid=intent.getStringExtra("fb_userid");
        }



        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userId = pref.getString("user_id", null);


        Jsonsend();



        ringProgressDialog = ProgressDialog.show(this,"", "Loading ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.show();

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Kluchit");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        drawerFragment.setup((DrawerLayout) findViewById(R.id.drawerlayout), toolbar);

        listView = (ListView) findViewById(R.id.list_view);
        addapter = new Main_addapter(getApplicationContext(), R.layout.singlerow, listJobs, this);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String heading = ((TextView) view.findViewById(R.id.userid)).getText().toString();
                String discription = ((TextView) view.findViewById(R.id.department)).getText().toString();
                String date = ((TextView) view.findViewById(R.id.specialization)).getText().toString();
                String job_id = ((TextView) view.findViewById(R.id.job_id)).getText().toString();

                Intent intent1 = new Intent(MainActivity.this, Job_discription.class);
                intent1.putExtra("name", heading);
                intent1.putExtra("specialization", discription);
                intent1.putExtra("discription", date);
                intent1.putExtra("job_id", job_id);
                finish();
                startActivity(intent1);

            }
        });


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check type of intent filter
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    //Registration success
                    String token = intent.getStringExtra("token");
                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    //Registration error
                    Toast.makeText(getApplicationContext(), "GCM registration error!!!", Toast.LENGTH_LONG).show();
                } else {
                    //Tobe define
                }
            }
        };


        Intent itent = new Intent(this, GCMRegistrationIntentService.class);
        startService(itent);



        t= Analytics.getInstance(this).getDefaultTracker();


    }

  /*  private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(MainActivity.this, "Image saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();


                Bitmap photo = (Bitmap) data.getExtras().get("data");

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri(getApplicationContext(), photo);

                // CALL THIS METHOD TO GET THE ACTUAL PATH
                File finalFile = new File(getRealPathFromURI(tempUri));

                System.out.println(tempUri);


                String type = "image/*";
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "/"+timeStamp+".jpg";
                String mediaPath = Environment.getExternalStorageDirectory() + filename;

                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);

                // Set the MIME type
                share.setType(type);

                // Create the URI from the media
                File media = new File(mediaPath);
                Uri uri = Uri.fromFile(media);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, uri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));


            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
*/

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
    protected void onResume() {
        super.onResume();

        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));

        t.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    protected void onPause() {


        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
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
                        if(error instanceof NoConnectionError) {
                            Intent intent = new Intent(MainActivity.this,NoInternet.class);
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
                    params.put("screen_flag","1");
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


    public void Jsonsend() {


        final StringRequest request = new StringRequest(Request.Method.POST, EndPoints.GET_CATAGORY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        String res = response;


                        String catagory;


                        try {


                            JSONObject object = new JSONObject(response);


                            catagory = object.getString("result");

                            JSONArray Array = new JSONArray(catagory);

                            String Cat = "";

                            for (int i = 0; i < Array.length(); i++) {


                                JSONObject cat = Array.getJSONObject(i);

                                Cat = Cat + cat.getString("cat_type") + ",";
                                user_cat = Cat;


                            }

                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("user_cat", Cat);  // Saving string
                            editor.commit();

                            Jsonrecieve();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if(error instanceof NoConnectionError) {
                          Intent intent = new Intent(MainActivity.this,NoInternet.class);
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
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
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


    //////////// Getting Value from Json .

    private void parseJSONResponce(String responce) {




            String res = responce;


            String catagory;






            try {


                JSONObject object = new JSONObject(res);


                catagory = object.getString("result");

                if (catagory == "false") {

                    Intent intent = new Intent(MainActivity.this,No_jobs.class);
                    intent.putExtra("message","NO JOBS AVAILABLE FOR YOUR CATEGORY AT THIS MOMMENT");
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


    @Override
    protected void onStart()
    {
        super.onStart();

        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
