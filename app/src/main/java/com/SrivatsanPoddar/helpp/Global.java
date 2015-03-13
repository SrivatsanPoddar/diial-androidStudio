package com.SrivatsanPoddar.helpp;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import com.google.gson.Gson;

import android.app.Application;

//Reports crashes and sends template email
@ReportsCrashes(formKey = "",
        resToastText = R.string.crash_toast_text,
        mode = ReportingInteractionMode.TOAST, mailTo = "ppod1991@gmail.com")

//Override default Application class
public class Global extends Application
{
    protected static final String TAG = "Global";
    private Gson gson = new Gson();
    
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);
    }
}