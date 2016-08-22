package com.cybussolutions.kluchit.Activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cybussolutions.kluchit.Network.Analytics;
import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Aaybee on 6/30/2016.
 */
public class No_Internet_Category extends Activity {
    private Toolbar toolbar;
    Tracker t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_category);


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("No Internet");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));


        t= Analytics.getInstance(this).getDefaultTracker();
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }
}
