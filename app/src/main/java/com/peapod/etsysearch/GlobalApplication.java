package com.peapod.etsysearch;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Extended application for crash reporting
 */

@ReportsCrashes(formKey = "",
        resToastText = R.string.crash_toast_text,
        mode = ReportingInteractionMode.TOAST, mailTo = "ppod1991@gmail.com")
public class GlobalApplication extends Application {

    public void onCreate() {
        super.onCreate();
        ACRA.init(this); //Crash reporting
    }
}