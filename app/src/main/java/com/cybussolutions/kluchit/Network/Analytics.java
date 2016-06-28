package com.cybussolutions.kluchit.Network; /**
 * Created by Aaybee on 6/22/2016.
 */

import android.app.Application;
import android.content.Context;

import com.cybussolutions.kluchit.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class Analytics extends Application {

    private static Analytics mInstance;
    private static Tracker mTracker;
    private static Context c;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    private Analytics(Context c)
    {
        this.c=c;
    }

    public static synchronized Analytics getInstance(Context c)
    {
        if (mInstance == null) {
            mInstance = new Analytics(c);
        }
        return mInstance;
    }

    public synchronized Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(c);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
