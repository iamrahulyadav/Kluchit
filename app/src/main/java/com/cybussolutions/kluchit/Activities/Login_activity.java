package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

public class Login_activity extends AppCompatActivity{

    private int cou=0;
    Tracker t;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000 ;
    private String email;
    AutoCompleteTextView userName;
    EditText userPassword;

    private CallbackManager callbackManager;
    private LoginButton loginButton;

    TextInputLayout User,Password;

    Button login;

    String user,pass,username,useremail,userimage,house;

    CheckBox checkBox;

    ProgressDialog ringProgressDialog;

    ScrollView layout_interact;

    int wait_for=1000;

    private Animation move;
    private Animation mr_move;
    private Animation lmove;
    private Animation ml_move;

    public static final String PREFS_NAME = "AOP_PREFS";
    public static final String PREFS_KEY = "AOP_PREFS_String";


    String [] arr;

    private ArrayAdapter<String> adapter ;


    AssetFileDescriptor afd;

    String fb_email,fb_name,fb_userid;
    Bitmap fb_image;


    String postuser = EndPoints.BASE_URL + "/common_controller/reg_k";

    String upload = EndPoints.BASE_URL + "upload_profile.php";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);


        LoginManager.getInstance().logOut();


        ((EditText)findViewById(R.id.userpass1)).setTransformationMethod(new PasswordTransformationMethod());



        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String user_session= pref.getString("user_session", null);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(user_session != null)
        {
            Intent intent= new Intent(Login_activity.this, MainActivity.class);
            startActivity(intent);
            finish();
            //mp.start();
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();



        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));


        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                ringProgressDialog = ProgressDialog.show(Login_activity.this,"", "Loading ...", true);
                ringProgressDialog.setCancelable(false);
                ringProgressDialog.show();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            final String email ;
                            if (object.has("email"))
                            {
                                email = object.getString("email");
                            }
                            else
                            {
                                email="";
                            }

                            final String name ;
                            if (object.has("name"))
                            {
                                name = object.getString("name");
                            }
                            else
                            {
                                name="";
                            }
                            final String id;
                            if (object.has("id"))
                            {
                                id = object.getString("id");
                            }
                            else
                            {
                                id="";
                            }

                            fb_email=email;
                            fb_name=name;
                            fb_userid=id;


                            Toast.makeText(getApplicationContext(),email+" Facebook Login Successful",Toast.LENGTH_LONG).show();

                            check_old_or_new_user_server();
                            //here now hit server to find out new or old sign_up


                        } catch (JSONException e) {

                            ringProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Error retreiving user details, Try Again!",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"No Internet Connection!",Toast.LENGTH_LONG).show();

            }

            @Override
            public void onError(FacebookException e) {

                Toast.makeText(getApplicationContext(),"Some other error occured check your facebook app and try again!",Toast.LENGTH_LONG).show();
            }
        });



        User = (TextInputLayout) findViewById(R.id.userid);
        Password = (TextInputLayout) findViewById(R.id.userpass);
        login = (Button) findViewById(R.id.login);
        userName = (AutoCompleteTextView) findViewById(R.id.userid1);
        userPassword = (EditText) findViewById(R.id.userpass1);
        checkBox = (CheckBox) findViewById(R.id.checkBox);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( userName.getText().toString().isEmpty() || userPassword.getText().toString().isEmpty())
                {
                    if (userName.getText().toString().isEmpty())
                    userName.setError("Please enter Username");
                    if (userPassword.getText().toString().isEmpty());
                    userPassword.setError("Please enter Password");
                }
                else
                {
                    getdata();
                    Jsonsend();
                   // login.callOnClick();
                }
            }
        });
        t= Analytics.getInstance(this).getDefaultTracker();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );


        start_animations();


        arr=loadArray("arr",this);



        // For auto complete text

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, arr);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.userid1);
        textView.setAdapter(adapter);


        afd = getResources().openRawResourceFd(R.raw.bkt);
        getWindow().setFormat(PixelFormat.UNKNOWN);


        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    public boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public String[] updateArray(String add,String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        arr=new String[size+1];
        String array[] = new String[size];
        int i;
        for(i=0;i<size;i++) {
            array[i] = prefs.getString(arrayName + "_" + i, null);
            arr[i]=array[i];
        }
        arr[i]=add;
        saveArray(arr,"arr",this);
        return arr;

    }

    void start_animations()
    {
        move=AnimationUtils.loadAnimation(this, R.anim.move);
        mr_move=AnimationUtils.loadAnimation(this, R.anim.middle_right);
        lmove=AnimationUtils.loadAnimation(this, R.anim.move_left);
        ml_move=AnimationUtils.loadAnimation(this, R.anim.middle_left);


        findViewById(R.id.userid1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Tada).duration(1500).playOn(findViewById(R.id.userid));
            }
        });

        findViewById(R.id.userpass1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Tada).duration(1500).playOn(findViewById(R.id.userpass));
            }
        });


        layout_interact = (ScrollView) findViewById(R.id.sc);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
        fadeIn.setDuration(wait_for);
        layout_interact.startAnimation(fadeIn);



        move.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        findViewById(R.id.imageView).startAnimation(mr_move);

                    }
                }, 10/* 1sec delay */);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mr_move.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        findViewById(R.id.imageView).startAnimation(lmove);
                    }
                }, 10/* 1sec delay */);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lmove.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        findViewById(R.id.imageView).startAnimation(ml_move);
                    }
                }, 10/* 1sec delay */);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.imageView).startAnimation(move);
            }
        }, wait_for/* 1sec delay */);

    }



    @Override
    protected void onStart()
    {
        super.onStart();

        t.send(new HitBuilders.ScreenViewBuilder().build());

        t.send(new HitBuilders.ScreenViewBuilder().setNewSession().build());
    }


    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    void getdata()
    {

        user = userName.getText().toString();
        pass = userPassword.getText().toString();
        boolean flag=false;
        for (int i=0;i<arr.length;i++)
        {
            if (arr[i].equals(user)) {
                flag = true;
                break;
            }
        }
        if (flag==false) {
            arr = updateArray(user, "arr", this);
            adapter.add((String) arr[arr.length - 1]);
            adapter.notifyDataSetChanged();
        }

    }


    public void Jsonsend()
    {
        ringProgressDialog = ProgressDialog.show(this, "Please wait ...",	"Checking Credentials ...", true);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.show();


        StringRequest request = new StringRequest(Request.Method.POST, EndPoints.LOGIN,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {

                        ringProgressDialog.dismiss();
                        if(response.equals(""))
                        {
                            Toast.makeText(Login_activity.this, "Incorrect user name or password ",Toast.LENGTH_SHORT).show();
                            YoYo.with(Techniques.Tada).duration(1500).playOn(findViewById(R.id.userid));
                            YoYo.with(Techniques.Tada).duration(1500).playOn(findViewById(R.id.userpass));
                        }

                        else
                        {


                            String userid;

                            try {

                                JSONObject object = new JSONObject(response);


                                userid = object.getString("id");
                                username = object.getString("first_name");
                                username += " "+object.getString("last_name");
                                useremail = object.getString("email");
                                userimage = object.getString("user_image");
                                house = object.getString("house");


                                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();

                                editor.putString("user_id", userid);// Saving string

                                editor.putString("user_name",username);
                                editor.putString("user_email",useremail);
                                editor.putString("user_image", userimage);
                                editor.putString("fb_login","0");
                                editor.putString("house",house);

                                if(checkBox.isChecked())
                                {
                                    editor.putString("user_session", "logeed_in");
                                }

                                editor.commit();



                                Intent intent= new Intent(Login_activity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {

                ringProgressDialog.dismiss();
                Toast.makeText(getApplication(),"No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {

                Map<String,String> params = new HashMap<>();
                params.put("username",user);
                params.put("password",pass);
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


    protected void onResume()
    {
        super.onResume();
        start_animations();
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }


    protected void onPause(){
        super.onPause();
    }


    void image_download_fb_function()
    {
        ImageRequest ir = new ImageRequest("http://graph.facebook.com/"+fb_userid+"/picture?type=large", new Response.Listener<Bitmap>() {

            @Override
            public void onResponse(Bitmap response) {

                int abc=0;
//                                    String fb_email,fb_name,fb_image,fb_userid;

                if (response==null)
                    fb_image=BitmapFactory.decodeResource(getResources(),R.drawable.logomain);
                else
                    fb_image=response;



                SharedPreferences pref1 = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = pref1.edit();
                editor1.putString("user_name", fb_name);
                editor1.putString("user_image",fb_email+".jpeg");

                // Saving string
                editor1.commit();


             /*   sr.setRetryPolicy(new DefaultRetryPolicy(
                        MY_SOCKET_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(sr);

                                    /*Intent intent = new Intent(Login_activity.this, FBregistration.class);
                                    intent.putExtra("email",email);
                                    intent.putExtra("name",name);
                                    intent.putExtra("image",response);
                                    intent.putExtra("bool","1");
                                    LoginManager.getInstance().logOut();

                                    ringProgressDialog.dismiss();
                                    startActivity(intent);
                                    */

                image_upload_server_function();

            }
        },0, 0, null,  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LoginManager.getInstance().logOut();
                ringProgressDialog.dismiss();
                Toast.makeText(Login_activity.this,error.toString(),Toast.LENGTH_LONG).show();
            }
        });

                           /* ir.setRetryPolicy(new DefaultRetryPolicy(
                                    MY_SOCKET_TIMEOUT_MS,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(ir);

    }


    void check_old_or_new_user_server()
    {
        final StringRequest sr = new StringRequest(Request.Method.POST, postuser, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(Login_activity.this,response,Toast.LENGTH_LONG).show();

                    LoginManager.getInstance().logOut();

                    ringProgressDialog.dismiss();


                SharedPreferences m_pref=getApplicationContext().getSharedPreferences("MyPref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1=m_pref.edit();
                if (checkBox.isChecked()) {
                    editor1.putString("user_session", "logeed_in");
                }

                editor1.putString("user_image",fb_email+".jpeg");
                editor1.putString("user_name",fb_name);
                editor1.commit();


                if (!response.contains("old")) {


                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("user_id", response);
                    editor.putString("user_name",fb_name);
                    editor.commit();

                    new AlertDialog.Builder(Login_activity.this)
                            .setTitle("Signup Confirmation Dialog:")
                            .setMessage("You have successfully registered with (Email: " + fb_email + "). Thank You!")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    image_download_fb_function();
                                    Intent intent = new Intent(Login_activity.this, MainActivity.class);
                                    startActivity(intent);

                                }
                            }).setCancelable(false)
                            .create().show();

                }
                else
                {

                    StringTokenizer st = new StringTokenizer(response);
                    int i = 0;
                    String u=null;
                    while (st.hasMoreTokens()) {
                        if (i == 0) {
                            u = st.nextToken();
                            break;
                        }
                        i++;
                    }

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("user_id", u);
                    editor.putString("user_name",fb_name);
                    editor.commit();


                    Intent intent = new Intent(Login_activity.this, MainActivity.class);
                    startActivity(intent);

                }



                    //call intent for already registered
            }
                    //ringProgressDialog.dismiss();
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ringProgressDialog.dismiss();
                LoginManager.getInstance().logOut();
                Toast.makeText(getApplicationContext(),"Something went Wrong! Slow Internet Connection",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();


                params.put("username", fb_userid);//done
                params.put("password", "facebook");//done
                params.put("is_active", "1");//done
                params.put("email", fb_email.toLowerCase());//done

                String timeStamp = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())).toString();
                params.put("date_added", timeStamp);//done
                params.put("filename", fb_email.toLowerCase() + ".jpeg");//done


                String first = "", last = "";
                StringTokenizer st = new StringTokenizer(fb_name);
                int i = 0, count = 0;
                while (st.hasMoreTokens()) {
                    if (i == 0)
                        first = st.nextToken();
                    else {
                        if (count > 0)
                            last += " ";
                        last += st.nextToken();
                        count++;
                    }
                    i++;
                }

                params.put("first_name", first);//done
                params.put("last_name", last);//done

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };//post user


              sr.setRetryPolicy(new DefaultRetryPolicy(
                        MY_SOCKET_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(sr);
    }


    void image_upload_server_function()
    {
        StringRequest upload_image_request = new StringRequest(Request.Method.POST, upload,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s.contains("Uploaded"))
                        {
                            ringProgressDialog.dismiss();
                            //Intent intent=new Intent(Login_activity.this,MainActivity.class);
                            //startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog

                        //Showing toast
                        //result+=volleyError.toString();
                        ringProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                fb_image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                final String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                String image = encodedImage;

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("userImage", image);
                params.put("filename",fb_email);

                //returning parameters
                return params;
            }
        };

        upload_image_request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(upload_image_request);                //Intent intent=new Intent(Login_activity.this,MainActivity.class);
        //startActivity(intent);

    }
}


