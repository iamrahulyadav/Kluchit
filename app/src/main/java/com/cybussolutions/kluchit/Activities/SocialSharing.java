package com.cybussolutions.kluchit.Activities;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cybussolutions.kluchit.Adapters.AndroidMultiPartEntity;
import com.cybussolutions.kluchit.Network.EndPoints;
import com.cybussolutions.kluchit.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Abdullah Manzoor Dar on 7/13/2016.
 */
public class SocialSharing extends FragmentActivity {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    static int t=0;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private Uri fileUri; // file url to store image/video

    private ImageView imgPreview;
    private VideoView videoPreview;
    private Button btnCapturePicture, btnRecordVideo;
    ProgressDialog ringProgressDialog;
    TextView textview;
    private ProgressBar progressBar;
    private TextView txtPercentage;
    private Button btnUpload;
    long totalSize = 0;
    private String filePath = null;
    private static final String TAG = SocialSharing.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_sharing);

        imgPreview = (ImageView) findViewById(R.id.img);
        videoPreview = (VideoView) findViewById(R.id.vid);
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);
        textview= (TextView) findViewById(R.id.nopic);

        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        /**
         * Capture image button click event
         */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });

        /**
         * Record video button click event
         */
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // record video
                recordVideo();
            }
        });

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }

        Button share=(Button) findViewById(R.id.share);




        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (t == MEDIA_TYPE_IMAGE) {

                    //For sharing pictures on social media
                    String pathofBmp = fileUri.getPath();
                    Uri bmpUri = Uri.parse(pathofBmp);
                    final Intent emailIntent1 = new Intent(Intent.ACTION_SEND);
                    emailIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent1.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    emailIntent1.setType("image/png");
                    startActivity(Intent.createChooser(emailIntent1, "Share Image to"));

                    //upload_insta();


                    // upload_photo_video(true);





                } else if (t == MEDIA_TYPE_VIDEO) {

                    videoPreview.stopPlayback();

                    //For sharing video on social media
                    String path = fileUri.getPath();
                    Uri vuri = Uri.parse(path);
                    final Intent emailIntent1 = new Intent(Intent.ACTION_SEND);
                    emailIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent1.putExtra(Intent.EXTRA_STREAM, vuri);
                    emailIntent1.setType("video/*");
                    startActivity(Intent.createChooser(emailIntent1, "Share Video to"));

                    //upload_photo_video(false);


                } else {
                    Toast.makeText(SocialSharing.this, "Please Capture an image or video to share", Toast.LENGTH_LONG).show();
                }



            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
                new UploadFileToServer().execute();

            }
        });


        btnUpload.setVisibility(View.INVISIBLE);
        share.setVisibility(View.INVISIBLE);



        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(getFragmentManager().findFragmentById(R.id.two));
        ft.commit();


       // textview.setVisibility(View.VISIBLE);

    }

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Recording video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Button btn=(Button)findViewById(R.id.share);
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


                previewCapturedImage();
                btn.setVisibility(View.VISIBLE);
                btnUpload.setVisibility(View.VISIBLE);
                filePath = fileUri.getPath();


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).show(getFragmentManager().findFragmentById(R.id.two)).commit();
                    }
                }, 2000);




            } else if (resultCode == RESULT_CANCELED) {

                if (textview.getVisibility() == View.INVISIBLE)
                    textview.setVisibility(View.VISIBLE);

                btn.setVisibility(View.INVISIBLE);
                btnUpload.setVisibility(View.INVISIBLE);
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();
                    }
                }, 500);

            } else {

                if (textview.getVisibility() == View.INVISIBLE)
                    textview.setVisibility(View.VISIBLE);

                btn.setVisibility(View.INVISIBLE);
                btnUpload.setVisibility(View.INVISIBLE);
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();



                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();
                    }
                }, 500);


            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // video successfully recorded
                // preview the recorded video
                btn.setVisibility(View.VISIBLE);
                btnUpload.setVisibility(View.VISIBLE);
                previewVideo();
                filePath = fileUri.getPath();


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).show(getFragmentManager().findFragmentById(R.id.two)).commit();
                    }
                }, 2000);


            } else if (resultCode == RESULT_CANCELED) {

                if (textview.getVisibility() == View.INVISIBLE)
                    textview.setVisibility(View.VISIBLE);

                btn.setVisibility(View.INVISIBLE);
                btnUpload.setVisibility(View.INVISIBLE);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();
                    }
                }, 500);

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                if (textview.getVisibility() == View.INVISIBLE)
                    textview.setVisibility(View.VISIBLE);


                btn.setVisibility(View.INVISIBLE);
                btnUpload.setVisibility(View.INVISIBLE);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();
                    }
                }, 500);

                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();

            }
        }
    }

    /**
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {
            // hide video preview
            if (textview.getVisibility() == View.VISIBLE)
                textview.setVisibility(View.INVISIBLE);


            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

            //img_bit=rotatedBitmap;


            imgPreview.setImageBitmap(rotatedBitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Previewing recorded video
     */
    private void previewVideo() {
        try {
            // hide image preview

          //  textview.setVisibility(textview.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (textview.getVisibility() == View.VISIBLE)
                textview.setVisibility(View.INVISIBLE);


            imgPreview.setVisibility(View.GONE);

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(fileUri.getPath());
            // start playing
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            t=MEDIA_TYPE_IMAGE;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            t=MEDIA_TYPE_VIDEO;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            t=0;
            return null;
        }

        return mediaFile;
    }


    void upload_insta()
    {
        String upload= EndPoints.BASE_URL+"upload_insta.php";


        StringRequest stringRequest = new StringRequest(Request.Method.POST, upload,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        ringProgressDialog.dismiss();
                        Toast.makeText(SocialSharing.this,s,Toast.LENGTH_LONG).show();

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

                // bimatp factory
                BitmapFactory.Options options = new BitmapFactory.Options();

                // downsizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 16;


                Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

                String encodedImage=getStringImage(rotatedBitmap);
                //Adding parameters
                params.put("userImage", encodedImage);
                String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())).toString();

                params.put("filename",timeStamp+".jpeg");

                //returning parameters
                return params;
            }
        };


        ringProgressDialog = ProgressDialog.show(SocialSharing.this, "Please wait ...",	"Checking Credentials ...", true);
        //ringProgressDialog.setCancelable(true);
        ringProgressDialog.show();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://demo.cybussolutions.com/kluchitrm/vidupload.php");

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("www.androidhive.info"));
                entity.addPart("email", new StringBody("abc@gmail.com"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit).hide(getFragmentManager().findFragmentById(R.id.two)).commit();
            }
        }, 500);

    }
}
