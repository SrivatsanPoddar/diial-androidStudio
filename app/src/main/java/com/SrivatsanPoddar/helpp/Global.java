package com.SrivatsanPoddar.helpp;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import com.google.gson.Gson;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

import android.app.Application;
//
//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.Tracker;
//import com.google.android.gms.analytics.Logger.LogLevel;
import android.util.Log;

@ReportsCrashes(formKey = "",
        resToastText = R.string.crash_toast_text,
        mode = ReportingInteractionMode.TOAST, mailTo = "support@getTaste.co")
public class Global extends Application
{

//    Tracker t;
    
    protected static final String TAG = "Global";
    private Gson gson = new Gson();
    
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);

        // EmailIntentSender e = new EmailIntentSender
    }
   
    

//    synchronized Tracker getTracker()
//    {
//        if (t == null)
//        {
//            t = GoogleAnalytics.getInstance(this).newTracker(
//                    R.xml.global_tracker);
//            GoogleAnalytics.getInstance(this).getLogger()
//                    .setLogLevel(LogLevel.VERBOSE);
//        }
//        return t;
//    }

}