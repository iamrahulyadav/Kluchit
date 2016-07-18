package com.cybussolutions.kluchit.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
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

    String user,pass,username,useremail;

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


    private MediaPlayer mp;
  //  private SurfaceView mPreview;
  //  private SurfaceHolder holder;


    AssetFileDescriptor afd;

    String fb_email,fb_name,fb_userid;
    Bitmap fb_image;


    String postuser = EndPoints.BASE_URL + "/common_controller/saveNewUserBySocial";

    final StringRequest sr = new StringRequest(Request.Method.POST, postuser, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Toast.makeText(Login_activity.this,response,Toast.LENGTH_LONG).show();
            if (response.toString().contains("Already")) {
                ringProgressDialog.dismiss();
                LoginManager.getInstance().logOut();


                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user_id", fb_userid);// Saving string
                editor.commit();

                Intent intent=new Intent(Login_activity.this,MainActivity.class);
                startActivity(intent);

                //call intent for already registered
            }
            else if (response.toString().contains("Taken"))
            {
                ringProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Fill out correct details and try again!", Toast.LENGTH_LONG).show();
                //call intent for username taken (which would not be in our case)
                //((TextInputLayout)findViewById(R.id.user_namefb)).setError(response+" Please choose another username!");
            }
            else {
                ringProgressDialog.dismiss();
                LoginManager.getInstance().logOut();
                new AlertDialog.Builder(Login_activity.this)
                        .setTitle("Signup Confirmation Dialog:")
                        .setMessage("You have successfully registered with (Email: " + fb_email + "). Thank You!")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //getting started page
                            }
                        }).setCancelable(false)
                        .create().show();
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            ringProgressDialog.dismiss();
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

            String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())).toString();
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);
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
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));


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

                            Toast.makeText(getApplicationContext(),email+" Facebook Login Successful",Toast.LENGTH_LONG).show();
                            ImageRequest ir = new ImageRequest("http://graph.facebook.com/"+id+"/picture?type=large", new Response.Listener<Bitmap>() {

                                @Override
                                public void onResponse(Bitmap response) {

                                    int abc=0;
//                                    String fb_email,fb_name,fb_image,fb_userid;
                                    fb_email=email;
                                    fb_name=name;
                                    fb_userid=id;
                                    fb_image=response;



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



                                }
                            },0, 0, null,  new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Intent intent = new Intent(Login_activity.this, FBregistration.class);
                                    intent.putExtra("email",email);
                                    intent.putExtra("name",name);
                                    //intent.putExtra("image",R.drawable.person);
                                    intent.putExtra("bool","00");
                                    LoginManager.getInstance().logOut();


                                    ringProgressDialog.dismiss();
                                    startActivity(intent);
                                }
                            });


                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            requestQueue.add(ir);


                        } catch (JSONException e) {

                            ringProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Error retreiving user details, Try Again!",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
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
       // mPreview = (SurfaceView)findViewById(R.id.surface);
       // holder = mPreview.getHolder();
        //holder.setFixedSize(800, 480);

       // holder.addCallback(this);
       // holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
       // mp = new MediaPlayer();
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
       /* ml_move.setAnimationListener(new Animation.AnimationListener() {
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
                        findViewById(R.id.imageView).startAnimation(move);
                    }
                }, 10);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });*/


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
                                useremail = object.getString("email");


                                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("user_id", userid);// Saving string

                                editor.putString("user_name",username);
                                editor.putString("user_email",useremail);

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



        //mp.stop();
        //mp=
        //mp.setDisplay(holder);
        //mp.start();
        //cou=0;
        //onStart();
    }


    protected void onPause(){
        super.onPause();
       // mp.release();
    }
   /* @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //int x=0;
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = new MediaPlayer();
            }
        }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(),"abc",Toast.LENGTH_LONG).show();
            }


        try {
            mp.setDisplay(holder);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.setLooping(true);
            mp.setVolume(0, 0);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // Do something. For example: playButton.setEnabled(true);
                mp.start();
                cou++;
            }
        });
        mp.prepareAsync();

}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Toast.makeText(getApplicationContext(),"Destroyed",Toast.LENGTH_LONG).show();
    }
*/
}


