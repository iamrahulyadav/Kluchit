package com.cybussolutions.kluchit.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.HitBuilders;

public class Splash_Activity extends AppCompatActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2500;
    com.google.android.gms.analytics.Tracker tr;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Splash_Activity.this, Login_activity.class);
                Splash_Activity.this.startActivity(mainIntent);
                Splash_Activity.this.finish();}
        }, SPLASH_DISPLAY_LENGTH);

        tr = Analytics.getInstance(this).getDefaultTracker();

    }

    @Override
    protected void onStart() {
        super.onStart();

        tr.send(new HitBuilders.ScreenViewBuilder().build());
    }
    @Override
    protected void onResume() {
        super.onResume();
        tr.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

