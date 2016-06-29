package com.cybussolutions.kluchit.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;


public class FBregistration extends AppCompatActivity {
//rihana
//sadjaskljdadfadasdsad
    Tracker t;
    private Analytics myApp;
    private Toolbar toolbar;
    TextInputLayout user_emial, Password;
    EditText userEmail, userPassword, usercat;
    Button submit;

    RadioGroup rgroup;

    String user, pass, email, category, jobtype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyfacebookPref", MODE_PRIVATE);
        String fb_registraion = pref.getString("fb_registraion", null);


        if (fb_registraion != null) {
            Intent intent = new Intent(FBregistration.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbregistration);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Kluchit");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
        user_emial = (TextInputLayout) findViewById(R.id.user_emailfb);
        Password = (TextInputLayout) findViewById(R.id.user_passwordfb);
        userEmail = (EditText) findViewById(R.id.emialfb);
        usercat = (EditText) findViewById(R.id.editText);
        userPassword = (EditText) findViewById(R.id.passwordfb);
        submit = (Button) findViewById(R.id.submit);


        userEmail.setFocusable(false);

        final Intent intent = getIntent();
        user = intent.getStringExtra("name");


        rgroup = (RadioGroup) findViewById(R.id.radio_Group);

        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.in_house) {
                    jobtype = "inhouse";

                } else if (checkedId == R.id.Out_House) {
                    jobtype = "outhouse";
                }
                ((RadioButton) findViewById(R.id.in_house)).setError(null);

            }
        });


        usercat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(FBregistration.this, Select_category.class);
                startActivityForResult(intent1, 0);


            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getdata();

                if (((EditText) findViewById(R.id.emialfb)).getText().toString().equals("") || ((EditText) (findViewById(R.id.passwordfb))).getText().toString().equals("") || ((EditText) findViewById(R.id.userid)).getText().toString().equals("") || ((EditText) findViewById(R.id.editText)).getText().toString().equals("") || ((EditText) findViewById(R.id.userid)).getText().toString().equals("") || (((RadioGroup) findViewById(R.id.radio_Group)).getCheckedRadioButtonId() != ((RadioButton) findViewById(R.id.in_house)).getId() && ((RadioGroup) findViewById(R.id.radio_Group)).getCheckedRadioButtonId() != ((RadioButton) findViewById(R.id.Out_House)).getId())) {
                    if (((EditText) findViewById(R.id.emialfb)).getText().toString().equals("")) {
                        ((EditText) findViewById(R.id.emialfb)).setError("Please Write Your Email");
                    }
                    if (((EditText) (findViewById(R.id.passwordfb))).getText().toString().equals("")) {
                        ((EditText) findViewById(R.id.passwordfb)).setError("Please Set Your Password");
                    }
                    if (((EditText) findViewById(R.id.userid)).getText().toString().equals("")) {
                        ((EditText) findViewById(R.id.userid)).setError("Please Set Your Name");
                    }
                    if (((EditText) findViewById(R.id.editText)).getText().toString().equals("")) {
                        ((EditText) findViewById(R.id.editText)).setError("Please Select At least one Category!");

                    }
                    if ((((RadioGroup) findViewById(R.id.radio_Group)).getCheckedRadioButtonId() != ((RadioButton) findViewById(R.id.in_house)).getId() && ((RadioGroup) findViewById(R.id.radio_Group)).getCheckedRadioButtonId() != ((RadioButton) findViewById(R.id.Out_House)).getId())) {
                        ((RadioButton) findViewById(R.id.in_house)).setError("Please select a category!");
                    }
                } else {

                    // Toast.makeText(FBregistration.this, "Send Json ", Toast.LENGTH_SHORT).show();

                    String upload = EndPoints.BASE_URL + "upload_profile.php";

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    Bundle bundle = getIntent().getExtras();

                    if (bundle.getString("bool").contains("1")) {
                        ((Bitmap) intent.getParcelableExtra("image")).compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        final String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);


                        StringRequest stringRequest = new StringRequest(Request.Method.POST, upload,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String s) {
                                        //Disimissing the progress dialog
                                        //Showing toast message of the response
                                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        //Dismissing the progress dialog

                                        //Showing toast
                                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                //Converting Bitmap to String
                                String image = encodedImage;

                                //Creating parameters
                                Map<String, String> params = new Hashtable<String, String>();

                                //Adding parameters
                                params.put("userImage", image);

                                //returning parameters
                                return params;
                            }
                        };


                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        requestQueue.add(stringRequest);

                    } else {


                        StringRequest stringRequest = new StringRequest(Request.Method.POST, upload,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String s) {
                                        //Disimissing the progress dialog
                                        //Showing toast message of the response
                                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        //Dismissing the progress dialog

                                        //Showing toast
                                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                //Converting Bitmap to String


                                ImageView img = (ImageView) findViewById(R.id.imageView5);
                                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                                Bitmap bmap = drawable.getBitmap();

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                                byte[] imageBytes = baos.toByteArray();
                                // final String encodedImage =


                                String image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                                //Creating parameters
                                Map<String, String> params = new Hashtable<String, String>();

                                //Adding parameters
                                params.put("image", image);

                                //returning parameters
                                return params;
                            }
                        };


                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        requestQueue.add(stringRequest);
                    }





                    /*StringRequest sr = new StringRequest(Request.Method.POST,"http://abdullahmanzoordar.net23.net//reg_k.php", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!response.toString().contains("not"))
                                Toast.makeText(getApplicationContext(),"Registered",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getApplicationContext(),"Not Registered",Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),"error in sending request",Toast.LENGTH_LONG).show();
                        }
                    }){
                        @Override
                        protected Map<String,String> getParams(){
                            Map<String,String> params = new HashMap<String, String>();
                            String user_,pass_,employed_,categories_;
                            user_=((EditText)findViewById(R.id.emialfb)).getText().toString();
                            pass_=((EditText)findViewById(R.id.passwordfb)).getText().toString();
                            categories_=((EditText)findViewById(R.id.editText)).getText().toString();
                            params.put("username",user_);
                            params.put("password",pass_);

                            if (((RadioGroup) findViewById(R.id.radio_Group)).getCheckedRadioButtonId()==((RadioButton) findViewById(R.id.in_house)).getId()) {
                                params.put("employed_from","in_house");
                            }
                            else
                            {
                                params.put("employed_from","out_house");
                            }
                            params.put("categories",categories_);

                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("Content-Type","application/x-www-form-urlencoded");
                            return params;
                        }
                    };
                    Volley.newRequestQueue(getApplicationContext()).add(sr);
                    */

                }
            }
        });

        t = Analytics.getInstance(this).getDefaultTracker();
        userEmail.setText(intent.getStringExtra("email"));


        Bundle bundle = getIntent().getExtras();

        if (bundle.getString("bool").contains("1"))
            ((ImageView) findViewById(R.id.imageView5)).setImageBitmap((Bitmap) intent.getParcelableExtra("image"));
        else {
            ((ImageView) findViewById(R.id.imageView5)).setImageResource(R.drawable.applogo);
        }
        ((EditText) findViewById(R.id.userid)).setText(intent.getStringExtra("name"));
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
        LoginManager.getInstance().logOut();
        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        LoginManager.getInstance().logOut();
    }

    void getdata() {

        email = userEmail.getText().toString();
        pass = userPassword.getText().toString();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String arr = data.getStringExtra("chosen");
        TextView txt = (TextView) findViewById(R.id.editText);
        txt.setText(arr);

        if (!((EditText) findViewById(R.id.editText)).equals("")) {
            ((EditText) findViewById(R.id.editText)).setError(null);
        }


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


    // user.equals("")||pass.equals("")||email.equals("")||category.equals("Select Catefory")||jobtype.equals("")

}
