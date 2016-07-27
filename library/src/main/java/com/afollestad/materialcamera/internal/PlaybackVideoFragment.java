package com.afollestad.materialcamera.internal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialcamera.AndroidMultiPartEntity;
import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.util.CameraUtil;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Aidan Follestad (afollestad)
 */
public class PlaybackVideoFragment extends Fragment implements CameraUriInterface, EasyVideoCallback {

    private EasyVideoPlayer mPlayer;
    private String mOutputUri;
    private BaseCaptureInterface mInterface;
    private static final String TAG = PlaybackVideoFragment.class.getSimpleName();
    ProgressBar progressBar;
    TextView txtPercentage;
    long totalSize=0;

    private Handler mCountdownHandler;
    private final Runnable mCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null) {
                long diff = mInterface.getRecordingEnd() - System.currentTimeMillis();
                if (diff <= 0) {
                    useVideo();
                    return;
                }
                mPlayer.setBottomLabelText(String.format("-%s", CameraUtil.getDurationString(diff)));
                if (mCountdownHandler != null)
                    mCountdownHandler.postDelayed(mCountdownRunnable, 200);
            }
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mInterface = (BaseCaptureInterface) activity;
    }

    public static PlaybackVideoFragment newInstance(String outputUri, boolean allowRetry, int primaryColor) {
        PlaybackVideoFragment fragment = new PlaybackVideoFragment();
        fragment.setRetainInstance(true);
        Bundle args = new Bundle();
        args.putString("output_uri", outputUri);
        args.putBoolean(CameraIntentKey.ALLOW_RETRY, allowRetry);
        args.putInt(CameraIntentKey.PRIMARY_COLOR, primaryColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer.reset();
            mPlayer = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mcam_fragment_videoplayback, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlayer = (EasyVideoPlayer) view.findViewById(R.id.playbackView);
        mPlayer.setCallback(this);
        mPlayer.setThemeColor(getArguments().getInt(CameraIntentKey.PRIMARY_COLOR));

        mPlayer.setSubmitTextRes(mInterface.labelUseVideo());
        mPlayer.setRetryTextRes(mInterface.labelRetry());
        mPlayer.setPlayDrawableRes(mInterface.iconPlay());
        mPlayer.setPauseDrawableRes(mInterface.iconPause());

        if (getArguments().getBoolean(CameraIntentKey.ALLOW_RETRY, true))
            mPlayer.setLeftAction(EasyVideoPlayer.LEFT_ACTION_RETRY);
        mPlayer.setRightAction(EasyVideoPlayer.RIGHT_ACTION_SUBMIT);

        mOutputUri = getArguments().getString("output_uri");

        if (mInterface.hasLengthLimit() && mInterface.continueTimerInPlayback()) {
            final long diff = mInterface.getRecordingEnd() - System.currentTimeMillis();
            mPlayer.setBottomLabelText(String.format("-%s", CameraUtil.getDurationString(diff)));
            startCountdownTimer();
        }

        mPlayer.setSource(Uri.parse(mOutputUri));


        progressBar=(ProgressBar)view.findViewById(R.id.two).findViewById(R.id.progressBar);
        txtPercentage=(TextView)view.findViewById(R.id.two).findViewById(R.id.txtPercentage);


        view.findViewById(R.id.two).findViewById(R.id.btnUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadFileToServer().execute();
            }
        });

        progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);



    }

    private void startCountdownTimer() {
        if (mCountdownHandler == null)
            mCountdownHandler = new Handler();
        else mCountdownHandler.removeCallbacks(mCountdownRunnable);
        mCountdownHandler.post(mCountdownRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCountdownHandler != null) {
            mCountdownHandler.removeCallbacks(mCountdownRunnable);
            mCountdownHandler = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void useVideo() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if (mInterface != null)
            mInterface.useVideo(mOutputUri);
    }

    @Override
    public String getOutputUri() {
        return getArguments().getString("output_uri");
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
    }

    @Override
    public void onBuffering(int percent) {
    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.mcam_error)
                .content(e.getMessage())
                .positiveText(android.R.string.ok)
                .show();
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
        if (mInterface != null)
            mInterface.onRetry(mOutputUri);
    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {
        useVideo();
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


                String m= mOutputUri.substring(5,mOutputUri.length());
                File sourcefile=new File(m);
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
                        // do nothing
                    }
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

        getView().findViewById(R.id.two).setVisibility(View.INVISIBLE);

    }
}