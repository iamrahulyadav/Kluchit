package com.afollestad.materialcamera.internal;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.AndroidMultiPartEntity;
import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.util.CameraUtil;
import com.afollestad.materialcamera.util.Degrees;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_BACK;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_FRONT;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_UNKNOWN;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CameraFragment extends BaseCameraFragment implements View.OnClickListener {


    boolean is_offline()
    {
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String offline=pref.getString("offline",null);
        if (offline=="0")
        {
            return false;
        }
        return true;
    }



    public int getScreenOrientation()
    {
        Display getOrient = getActivity().getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }


    boolean already_uploaded;

    String[] web = {
            "Top Left",
            "Top Right",
            "Bottom Left",
            "Bottom Right",
            "Middle"

    } ;
    int[] imageId = {
            R.drawable.tl,
            R.drawable.tr,
            R.drawable.bl,
            R.drawable.br,
            R.drawable.mid
    };


    boolean flagger,flagger_;

    View veuw;
    CameraPreview mPreviewView;
    RelativeLayout mPreviewFrame;
    static int i=0;
    private Camera.Size mVideoSize;
    private Camera mCamera;
    private Point mWindowSize;
    private int mDisplayOrientation;
    private boolean mIsAutoFocusing;
    protected static final int MEDIA_TYPE_IMAGE = 0;
    ImageButton btn;
    String filename;
    ProgressBar progressBar;
    private TextView txtPercentage;
    private Button btnUpload;
    long totalSize = 0;
    private static final String TAG = CameraFragment.class.getSimpleName();
    String filepath;
    static int p=3;
    GridView grid;
    static int image_pos;

    public class CustomGrid extends BaseAdapter {
        private Context mContext;
        private final String[] web;
        private final int[] Imageid;

        public CustomGrid(Context c,String[] web,int[] Imageid ) {
            mContext = c;
            this.Imageid = Imageid;
            this.web = web;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 5;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {

                grid = new View(mContext);
                grid = inflater.inflate(R.layout.icon, null);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
                imageView.setImageResource(Imageid[position]);
            } else {
                grid = (View) convertView;
            }

            return grid;
        }
    }


    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Kluchit/photos");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                //Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp =
                new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            return new File(dir.getPath() + File.separator + "IMG_"
                    + timeStamp + ".jpg");
        } else {
            return null;
        }
    }

    void picturePreview(Bitmap bmap)
    {
    }

Bitmap resize_insta(Bitmap yourBitmap) {

    return Bitmap.createScaledBitmap(yourBitmap, yourBitmap.getWidth(), yourBitmap.getHeight(), true);
}


    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /*public static Bitmap mark (Bitmap src, String watermark, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, w-(w/2), h-(h/2), paint);

        return result;
    }*/

    final StringRequest request = new StringRequest(Request.Method.POST, "http://demo.cybussolutions.com/kluchitrm/common_controller/imageEntryDatabase",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Toast.makeText(getActivity().getApplicationContext(),response, Toast.LENGTH_SHORT).show();
                    // ringProgressDialog.dismiss();



                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {


            if(error instanceof NoConnectionError) {

                Toast.makeText(getActivity().getApplicationContext(), "No internet Connection, Try Again!", Toast.LENGTH_SHORT).show();

            }

            else
            {
                Toast.makeText(getActivity().getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        }
    }) {
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            Map<String, String> params = new HashMap<>();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
            params.put("images",filename);

            SharedPreferences pref=getActivity().getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            String job_id=pref.getString("job_id",null);
            String person_id=pref.getString("user_id",null);


            SharedPreferences.Editor editor=pref.edit();
            editor.remove("job_id");
            editor.commit();

            params.put("job_id",job_id);
            params.put("uploaded_by",person_id);
            params.put("date_added", timeStamp);//done
            params.put("date_modified", timeStamp);//done
            return params;

        }
    };


    void send_image_db_request()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(request);
    }



    public static Bitmap addWatermark(Resources res, Bitmap source) {
        int w, h;
        Canvas c;
        Paint paint;
        Bitmap bmp, watermark;

        Matrix matrix;
        float scale;
        RectF r;

        w = source.getWidth();
        h = source.getHeight();

        // Create the new bitmap
        bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);

        // Copy the original bitmap into the new one
        c = new Canvas(bmp);
        c.drawBitmap(source, 0, 0, paint);

        // Load the watermark
        final int[] imageId = {
                R.drawable.log_one_a,
                R.drawable.log_two_a,
                R.drawable.log_three_a,
                R.drawable.log_four_a,
                R.drawable.log_five_a,
                R.drawable.log_six_a
        };


        watermark = BitmapFactory.decodeResource(res, imageId[image_pos]);
        // Scale the watermark to be approximately 10% of the source image height
        scale = (float) (((float) h * 0.1) / (float) watermark.getHeight());

        // Create the matrix
        matrix = new Matrix();
        matrix.postScale(scale, scale);
        // Determine the post-scaled size of the watermark
        r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
        matrix.mapRect(r);




        if (p==0)//wrong
            // Move the watermark to the top left corner
            matrix.postTranslate(0,0);
        else if (p==1)
            // Move the watermark to the top right corner
            matrix.postTranslate(w-r.width(),0);
        else if (p==2)//wrong
            // Move the watermark to the bottom left corner
            matrix.postTranslate(0, h - r.height());
        else if (p==3)
            // Move the watermark to the bottom right corner
            matrix.postTranslate(w - r.width(), h - r.height());
        else
            // Move the watermark to the middle
            matrix.postTranslate(w/2 - r.width()/2,h/2 - r.height()/2);







        // Draw the watermark
        c.drawBitmap(watermark, matrix, paint);
        // Free up the bitmap memory
        watermark.recycle();

        return bmp;
    }


    private void takePhoto() {
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                filepath=null;
                File picFile = getOutputMediaFile(0);
                if (picFile == null) {
                  //  Log.e(TAG, "Couldn't create media file; check storage permissions?");
                    return;
                }

                mCamera.stopPreview();
               /* new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 2000);*/
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(getCurrentCameraId(), cameraInfo);

                int orientation=cameraInfo.orientation;


                if (getScreenOrientation()== Configuration.ORIENTATION_PORTRAIT)
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);


                    Bitmap bmap2=RotateBitmap(bitmap,orientation);
                    Resources res = getActivity().getResources();
                    bmap2=addWatermark(res,bmap2);


                    ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
                    bmap2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
                    byte[] b = byteArrayBitmapStream.toByteArray();

                    try {
                        filepath=picFile.getPath();
                        filename=picFile.getName();
                        FileOutputStream fos = new FileOutputStream(picFile);
                        fos.write(b);
                        fos.close();
                        Toast.makeText(getActivity(),"Picture saved to "+picFile.getPath(),Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        //  Log.e(TAG, "File not found: " + e.getMessage());
                        e.getStackTrace();
                    } catch (IOException e) {
                        //  Log.e(TAG, "I/O error writing file: " + e.getMessage());
                        e.getStackTrace();
                    }

                }
                else
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);


                    Bitmap bmap2=RotateBitmap(bitmap,orientation+90);
                    Resources res = getActivity().getResources();
                    bmap2=addWatermark(res,bmap2);


                    ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
                    bmap2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
                    byte[] b = byteArrayBitmapStream.toByteArray();

                    try {
                        filepath=picFile.getPath();
                        filename=picFile.getName();
                        FileOutputStream fos = new FileOutputStream(picFile);
                        fos.write(b);
                        fos.close();
                        Toast.makeText(getActivity(),"Picture saved to "+picFile.getPath(),Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        //  Log.e(TAG, "File not found: " + e.getMessage());
                        e.getStackTrace();
                    } catch (IOException e) {
                        //  Log.e(TAG, "I/O error writing file: " + e.getMessage());
                        e.getStackTrace();
                    }
                }
            }
        };
        mCamera.takePicture(null, null, pictureCB);

    }

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    private static Camera.Size chooseVideoSize(BaseCaptureInterface ci, List<Camera.Size> choices) {
        Camera.Size backupSize = null;
        for (Camera.Size size : choices) {
            if (size.height <= ci.videoPreferredHeight()) {
                if (size.width == size.height * ci.videoPreferredAspect())
                    return size;
                if (ci.videoPreferredHeight() >= size.height)
                    backupSize = size;
            }
        }
        if (backupSize != null) return backupSize;
        LOG(CameraFragment.class, "Couldn't find any suitable video size");
        return choices.get(choices.size() - 1);
    }

    private static Camera.Size chooseOptimalSize(List<Camera.Size> choices, int width, int height, Camera.Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Camera.Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.width;
        int h = aspectRatio.height;
        for (Camera.Size option : choices) {
            if (option.height == width * h / w &&
                    option.width >= width && option.height >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            LOG(CameraFragment.class, "Couldn't find any suitable preview size");
            return aspectRatio;
        }
    }


    void flasher()
    {
        if (i%2==0) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            veuw.findViewById(R.id.flash).setBackgroundResource(R.drawable.flash_on);
        }
        else
        {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            veuw.findViewById(R.id.flash).setBackgroundResource(R.drawable.flash_off);
        }
        i++;
    }

    private void createInstagramIntent(String type, String mediaPath) throws IOException {

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType(type);


        Bitmap resized=resize_insta(BitmapFactory.decodeFile(mediaPath));

        File f = new File("/storage/emulated/0/Pictures/Kluchit/photos/", "insta.jpeg");
        f.createNewFile();

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();



        // Create the URI from the media
        File media = new File("/storage/emulated/0/Pictures/Kluchit/photos/", "insta.jpeg");
        Uri uri = Uri.fromFile(media);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }




    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewFrame = (RelativeLayout) view.findViewById(R.id.rootFrame);
        mPreviewFrame.setOnClickListener(this);
        veuw=view;
        already_uploaded=false;
        filename=null;
        filepath=null;
        flagger=false;
        flagger_=false;
        image_pos=0;
        view.findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i%2==0) {
                    veuw.findViewById(R.id.flash).setBackgroundResource(R.drawable.flash_on);
                }
                else
                {
                    veuw.findViewById(R.id.flash).setBackgroundResource(R.drawable.flash_off);
                }
                i++;
            }
        });





        view.findViewById(R.id.controlsFrame).setBackgroundResource(R.color.colorAccent);


        view.findViewById(R.id.takePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (flagger==false) {
                    boolean flag = false;
                    final Camera.Parameters param = mCamera.getParameters();
                    if (getCurrentCameraPosition() == CAMERA_POSITION_BACK) {
                        if (i % 2 == 0) {
                            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        } else {
                            param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            flag = true;
                        }
                        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        mCamera.setParameters(param);
                    }
                    mCamera.startPreview();

                    takePhoto();
                    if (flag == true) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                mCamera.setParameters(param);
                                mCamera.startPreview();
                                flagger_=true;


                                Bitmap bmp = BitmapFactory.decodeFile(filepath);
                                Drawable drawable = new BitmapDrawable(getResources(), bmp);
                                mPreviewView.setBackground(drawable);


                                //mCamera.startPreview();
                            }
                        }, 2000);
                    }
                    if (!is_offline())
                        mPreviewFrame.findViewById(R.id.two).setVisibility(View.VISIBLE);
                    flagger=true;

                    //view.findViewById(R.id.flash).callOnClick();

                }
                else
                {
                    if (flagger_==true)
                    {
                        mPreviewFrame.findViewById(R.id.two).setVisibility(View.INVISIBLE);
                        mPreviewView.setBackground(null);
                        flagger_=false;
                        already_uploaded=false;
                    }
                    else {
                        mCamera.startPreview();
                        mPreviewFrame.findViewById(R.id.two).setVisibility(View.INVISIBLE);
                        flagger = false;
                    }
                }
            }
        });


        mPreviewFrame.findViewById(R.id.two).setVisibility(View.INVISIBLE);

        mPreviewFrame.findViewById(R.id.two).findViewById(R.id.btnUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (already_uploaded==false) {
                    new UploadFileToServer().execute();

                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                else
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                    builder.setMessage("Video Already Uploaded to Server").setTitle("Attention!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                    android.app.AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });


        mPreviewFrame.findViewById(R.id.two).findViewById(R.id.cross).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPreviewFrame.findViewById(R.id.two).setVisibility(View.INVISIBLE);

            }
        });


        progressBar=(ProgressBar)mPreviewFrame.findViewById(R.id.two).findViewById(R.id.progressBar);
        txtPercentage = (TextView) mPreviewFrame.findViewById(R.id.two).findViewById(R.id.txtPercentage);
        btnUpload = (Button) mPreviewFrame.findViewById(R.id.two).findViewById(R.id.btnUpload);


        progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);

        if (i%2==0)
        veuw.findViewById(R.id.flash).setBackgroundResource(R.drawable.flash_off);
        else
            veuw.findViewById(R.id.flash).setBackgroundResource(R.drawable.flash_on);



        mPreviewFrame.findViewById(R.id.two).findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String path = filepath;
                try {
                    createInstagramIntent("image/*",path);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });




        grid=(GridView)mPreviewFrame.findViewById(R.id.filters).findViewById(R.id.gridView1);
        CustomGrid adapter = new CustomGrid(getActivity().getApplicationContext(), web, imageId);
        grid.setAdapter(adapter);


        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString("logo_pos", String.valueOf(3));
        // Saving string
        editor.commit();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                //working
                if (position==0) {
                    p = 0;
                    getActivity().findViewById(R.id.t_left).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.t_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.middle).setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),"top left",Toast.LENGTH_LONG).show();

                }
                else if (position==1) {
                    p = 1;
                    getActivity().findViewById(R.id.t_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.t_right).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.b_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.middle).setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),"top right",Toast.LENGTH_LONG).show();
                }
                else if (position==2) {
                    p = 2;
                    getActivity().findViewById(R.id.t_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.t_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_left).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.b_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.middle).setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),"bottom left",Toast.LENGTH_LONG).show();
                }
                else if (position==3) {
                    p = 3;
                    getActivity().findViewById(R.id.t_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.t_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_right).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.middle).setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),"bottom right",Toast.LENGTH_LONG).show();
                }
                else {
                    p = 4;
                    getActivity().findViewById(R.id.t_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.t_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_left).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.b_right).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(),"middle",Toast.LENGTH_LONG).show();
                }
                mPreviewFrame.findViewById(R.id.filters).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.hidelogopositionbar).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.showlogopositionbar).setVisibility(View.VISIBLE);

                editor.putString("logo_pos", String.valueOf(position));
                // Saving string
                editor.commit();

            }
        });


        view.findViewById(R.id.b_left).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.t_left).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.t_right).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.middle).setVisibility(View.INVISIBLE);


        mPreviewFrame.findViewById(R.id.filters).setVisibility(View.INVISIBLE);


        mPreviewFrame.findViewById(R.id.hidelogopositionbar).setVisibility(View.INVISIBLE);


        mPreviewFrame.findViewById(R.id.logo_window).setVisibility(View.INVISIBLE);


        mPreviewFrame.findViewById(R.id.right).setVisibility(View.INVISIBLE);



        mPreviewFrame.findViewById(R.id.showlogopositionbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreviewFrame.findViewById(R.id.filters).setVisibility(View.VISIBLE);
                mPreviewFrame.findViewById(R.id.hidelogopositionbar).setVisibility(View.VISIBLE);
                mPreviewFrame.findViewById(R.id.showlogopositionbar).setVisibility(View.INVISIBLE);
            }
        });

        mPreviewFrame.findViewById(R.id.hidelogopositionbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreviewFrame.findViewById(R.id.filters).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.hidelogopositionbar).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.showlogopositionbar).setVisibility(View.VISIBLE);
            }
        });

        mPreviewFrame.findViewById(R.id.filters).setBackgroundResource(R.color.colorAccent);


        mPreviewFrame.findViewById(R.id.logo_window).setBackgroundResource(R.color.colorAccent);




        mPreviewFrame.findViewById(R.id.left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreviewFrame.findViewById(R.id.logo_window).setVisibility(View.VISIBLE);
                mPreviewFrame.findViewById(R.id.right).setVisibility(View.VISIBLE);
                mPreviewFrame.findViewById(R.id.left).setVisibility(View.INVISIBLE);
            }
        });

        mPreviewFrame.findViewById(R.id.right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreviewFrame.findViewById(R.id.logo_window).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.right).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.left).setVisibility(View.VISIBLE);
            }
        });



        ListView list=(ListView)(mPreviewFrame.findViewById(R.id.logo_window).findViewById(R.id.list));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int[] imageId = {
                        R.drawable.log_one_a,
                        R.drawable.log_two_a,
                        R.drawable.log_three_a,
                        R.drawable.log_four_a,
                        R.drawable.log_five_a,
                        R.drawable.log_six_a
                };
                mPreviewFrame.findViewById(R.id.t_right).setBackgroundResource(imageId[i]);
                mPreviewFrame.findViewById(R.id.t_left).setBackgroundResource(imageId[i]);
                mPreviewFrame.findViewById(R.id.b_left).setBackgroundResource(imageId[i]);
                mPreviewFrame.findViewById(R.id.b_right).setBackgroundResource(imageId[i]);
                mPreviewFrame.findViewById(R.id.middle).setBackgroundResource(imageId[i]);
                image_pos=i;
                mPreviewFrame.findViewById(R.id.logo_window).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.right).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.left).setVisibility(View.VISIBLE);


            }
        });


    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mPreviewView.getHolder().getSurface().release();
        } catch (Throwable ignored) {
        }
        mPreviewFrame = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    public void onPause() {
        if (mCamera != null) mCamera.lock();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rootFrame) {
            if (mCamera == null || mIsAutoFocusing) return;
            try {
                mIsAutoFocusing = true;
                mCamera.cancelAutoFocus();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        mIsAutoFocusing = false;
                        if (!success)
                            Toast.makeText(getActivity(), "Unable to auto-focus!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            super.onClick(view);
        }
    }

    @Override
    public void openCamera() {
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) return;
        try {
            final int mBackCameraId = mInterface.getBackCamera() != null ? (Integer) mInterface.getBackCamera() : -1;
            final int mFrontCameraId = mInterface.getFrontCamera() != null ? (Integer) mInterface.getFrontCamera() : -1;
            if (mBackCameraId == -1 || mFrontCameraId == -1) {
                int numberOfCameras = Camera.getNumberOfCameras();
                if (numberOfCameras == 0) {
                    throwError(new Exception("No cameras are available on this device."));
                    return;
                }

                for (int i = 0; i < numberOfCameras; i++) {
                    //noinspection ConstantConditions
                    if (mFrontCameraId != -1 && mBackCameraId != -1) break;
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mFrontCameraId == -1) {
                        mInterface.setFrontCamera(i);
                    } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && mBackCameraId == -1) {
                        mInterface.setBackCamera(i);
                    }
                }
            }
            if (getCurrentCameraPosition() == CAMERA_POSITION_UNKNOWN) {
                if (getArguments().getBoolean(CameraIntentKey.DEFAULT_TO_FRONT_FACING, false)) {
                    // Check front facing first
                    if (mInterface.getFrontCamera() != null && (Integer) mInterface.getFrontCamera() != -1) {
                        mButtonFacing.setImageResource(mInterface.iconRearCamera());
                        mInterface.setCameraPosition(CAMERA_POSITION_FRONT);
                    } else {
                        mButtonFacing.setImageResource(mInterface.iconFrontCamera());
                        if (mInterface.getBackCamera() != null && (Integer) mInterface.getBackCamera() != -1)
                            mInterface.setCameraPosition(CAMERA_POSITION_BACK);
                        else mInterface.setCameraPosition(CAMERA_POSITION_UNKNOWN);
                    }
                } else {
                    // Check back facing first
                    if (mInterface.getBackCamera() != null && (Integer) mInterface.getBackCamera() != -1) {
                        mButtonFacing.setImageResource(mInterface.iconFrontCamera());
                        mInterface.setCameraPosition(CAMERA_POSITION_BACK);
                    } else {
                        mButtonFacing.setImageResource(mInterface.iconRearCamera());
                        if (mInterface.getFrontCamera() != null && (Integer) mInterface.getFrontCamera() != -1)
                            mInterface.setCameraPosition(CAMERA_POSITION_FRONT);
                        else mInterface.setCameraPosition(CAMERA_POSITION_UNKNOWN);
                    }
                }
            }

            if (mWindowSize == null)
                mWindowSize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
            final int toOpen = getCurrentCameraId();
            mCamera = Camera.open(toOpen == -1 ? 0 : toOpen);
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
            if (videoSizes == null || videoSizes.size() == 0)
                videoSizes = parameters.getSupportedPreviewSizes();
            mVideoSize = chooseVideoSize((BaseCaptureActivity) activity, videoSizes);
            Camera.Size previewSize = chooseOptimalSize(parameters.getSupportedPreviewSizes(),
                    mWindowSize.x, mWindowSize.y, mVideoSize);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                parameters.setRecordingHint(true);
            setCameraDisplayOrientation(parameters);
            mCamera.setParameters(parameters);
            createPreview();
            mMediaRecorder = new MediaRecorder();
        } catch (IllegalStateException e) {
            throwError(new Exception("Cannot access the camera.", e));
        } catch (RuntimeException e2) {
            throwError(new Exception("Cannot access the camera, you may need to restart your device.", e2));
        }
    }

    @SuppressWarnings("WrongConstant")
    private void setCameraDisplayOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(getCurrentCameraId(), info);
        final int deviceOrientation = Degrees.getDisplayRotation(getActivity());
        mDisplayOrientation = Degrees.getDisplayOrientation(
                info.orientation, deviceOrientation, info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        Log.d("CameraFragment", String.format("Orientations: Sensor = %d˚, Device = %d˚, Display = %d˚",
                info.orientation, deviceOrientation, mDisplayOrientation));

        int previewOrientation;
        if (CameraUtil.isArcWelder()) {
            previewOrientation = 0;
        } else {
            previewOrientation = mDisplayOrientation;
            if (Degrees.isPortrait(deviceOrientation) && getCurrentCameraPosition() == CAMERA_POSITION_FRONT)
                previewOrientation = Degrees.mirror(mDisplayOrientation);
        }
        parameters.setRotation(previewOrientation);
        mCamera.setDisplayOrientation(previewOrientation);
    }

    private void createPreview() {//here
        Activity activity = getActivity();
        if (activity == null) return;
        if (mWindowSize == null)
            mWindowSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
        mPreviewView = new CameraPreview(getActivity(), mCamera);
        if (mPreviewFrame.getChildCount() > 0 && mPreviewFrame.getChildAt(0) instanceof CameraPreview)
            mPreviewFrame.removeViewAt(0);
        mPreviewFrame.addView(mPreviewView, 0);
        mPreviewView.setAspectRatio(mWindowSize.x, mWindowSize.y);
    }

    @Override
    public void closeCamera() {
        try {
            if (mCamera != null) {
                try {
                    mCamera.lock();
                } catch (Throwable ignored) {
                }
                mCamera.release();
                mCamera = null;
            }
        } catch (IllegalStateException e) {
            throwError(new Exception("Illegal state while trying to close camera.", e));
        }
    }

    private boolean prepareMediaRecorder() {
        try {
            final Activity activity = getActivity();
            if (null == activity) return false;
            final BaseCaptureInterface captureInterface = (BaseCaptureInterface) activity;
            Camera.Parameters param=mCamera.getParameters();
            if (getCurrentCameraPosition()==CAMERA_POSITION_BACK) {
                if (i % 2 == 0) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                mCamera.setParameters(param);
            }
            setCameraDisplayOrientation(param);
            mMediaRecorder = new MediaRecorder();
            mCamera.stopPreview();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

            boolean canUseAudio = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                canUseAudio = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

            if (canUseAudio) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            } else {
                Toast.makeText(getActivity(), R.string.mcam_no_audio_access, Toast.LENGTH_LONG).show();
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

            final CamcorderProfile profile = CamcorderProfile.get(getCurrentCameraId(), mInterface.qualityProfile());
            mMediaRecorder.setOutputFormat(profile.fileFormat);
            mMediaRecorder.setVideoFrameRate(mInterface.videoFrameRate(profile.videoFrameRate));
            mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);
            mMediaRecorder.setVideoEncodingBitRate(mInterface.videoEncodingBitRate(profile.videoBitRate));
            mMediaRecorder.setVideoEncoder(profile.videoCodec);

            if (canUseAudio) {
                mMediaRecorder.setAudioEncodingBitRate(mInterface.audioEncodingBitRate(profile.audioBitRate));
                mMediaRecorder.setAudioChannels(profile.audioChannels);
                mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
                mMediaRecorder.setAudioEncoder(profile.audioCodec);
            }

            Uri uri = Uri.fromFile(getOutputMediaFile());
            mOutputUri = uri.toString();
            mMediaRecorder.setOutputFile(uri.getPath());

            if (captureInterface.maxAllowedFileSize() > 0) {
                mMediaRecorder.setMaxFileSize(captureInterface.maxAllowedFileSize());
                mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                            Toast.makeText(getActivity(), R.string.mcam_file_size_limit_reached, Toast.LENGTH_SHORT).show();
                            stopRecordingVideo(false);
                        }
                    }
                });
            }

            mMediaRecorder.setOrientationHint(mDisplayOrientation);
            mMediaRecorder.setPreviewDisplay(mPreviewView.getHolder().getSurface());

            try {
                mMediaRecorder.prepare();
                return true;
            } catch (Throwable e) {
                throwError(new Exception("Failed to prepare the media recorder: " + e.getMessage(), e));
                return false;
            }
        } catch (Throwable t) {
            try {
                mCamera.lock();
            } catch (IllegalStateException e) {
                throwError(new Exception("Failed to re-lock camera: " + e.getMessage(), e));
                return false;
            }
            t.printStackTrace();
            throwError(new Exception("Failed to begin recording: " + t.getMessage(), t));
            return false;
        }
    }

    @Override
    public boolean startRecordingVideo() {
        super.startRecordingVideo();
        if (prepareMediaRecorder()) {
            try {
                // UI
               // flasher();
                mButtonVideo.setImageResource(mInterface.iconStop());
                if (!CameraUtil.isArcWelder())
                    mButtonFacing.setVisibility(View.GONE);

                // Only start counter if count down wasn't already started
                if (!mInterface.hasLengthLimit()) {
                    mInterface.setRecordingStart(System.currentTimeMillis());
                    startCounter();
                }

                // Start recording

                mMediaRecorder.start();

                //changing
                mPreviewFrame.findViewById(R.id.logo_window).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.right).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.left).setVisibility(View.INVISIBLE);


                mPreviewFrame.findViewById(R.id.filters).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.hidelogopositionbar).setVisibility(View.INVISIBLE);
                mPreviewFrame.findViewById(R.id.showlogopositionbar).setVisibility(View.INVISIBLE);


                mButtonVideo.setEnabled(false);
                mButtonVideo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButtonVideo.setEnabled(true);
                    }
                }, 200);

                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                mInterface.setRecordingStart(-1);
                stopRecordingVideo(false);
                throwError(new Exception("Failed to start recording: " + t.getMessage(), t));
            }
        }
        return false;
    }

    @Override
    public void stopRecordingVideo(final boolean reachedZero) {
        super.stopRecordingVideo(reachedZero);

        if (mInterface.hasLengthLimit() && mInterface.shouldAutoSubmit() &&
                (mInterface.getRecordingStart() < 0 || mMediaRecorder == null)) {
            //flasher();
            stopCounter();
            if (mCamera != null) {
                try {
                    mCamera.lock();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            releaseRecorder();
            closeCamera();
            mButtonFacing.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mInterface.onShowPreview(mOutputUri, reachedZero);
                }
            }, 100);
            return;
        }

        if (mCamera != null)
            mCamera.lock();
        releaseRecorder();
        closeCamera();

        if (!mInterface.didRecord())
            mOutputUri = null;

        mButtonVideo.setImageResource(mInterface.iconRecord());
        if (!CameraUtil.isArcWelder())
            mButtonFacing.setVisibility(View.VISIBLE);
        if (mInterface.getRecordingStart() > -1 && getActivity() != null)
            mInterface.onShowPreview(mOutputUri, reachedZero);

        stopCounter();
    }

    static class CompareSizesByArea implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
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

                File sourcefile=new File(filepath);
                FileBody bdy=new FileBody(sourcefile);
                // Adding file data to http body
                entity.addPart("image",bdy );

                // Extra parameters if you want to pass to server
                entity.addPart("website", new StringBody("www.androidhive.info"));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        already_uploaded=true;

                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                progressBar.setProgress(0);
                txtPercentage.setText("Press button to start uploading...");

            }
        }, 500);


        send_image_db_request();

        //mPreviewFrame.findViewById(R.id.two).setVisibility(View.INVISIBLE);

    }
}