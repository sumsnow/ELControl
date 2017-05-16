package com.easeic.elcontrol;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by sam on 2016/4/2.
 */
@ReportsCrashes(mailTo = "sumsnow.ye@163.com;sam88.ye@gmail.com", mode = ReportingInteractionMode.SILENT)

public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();

        ACRA.init(this);

        MyApplication.context = getApplicationContext();

        Foreground.init(this);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }


}