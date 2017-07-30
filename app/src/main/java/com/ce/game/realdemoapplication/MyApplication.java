package com.ce.game.realdemoapplication;

import android.app.Application;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by ChenGong on 17/07/2017
 */

public class MyApplication extends Application {
    ScheduledExecutorService mExecutor;
    public static MyApplication instance;

    public ScheduledExecutorService getmExecutor() {
        return mExecutor;
    }

    public static RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutor = new ScheduledThreadPoolExecutor(2);
        instance = this;
//        enabledStrictMode();
        refWatcher = LeakCanary.install(MyApplication.this);
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
    }

    public void mustDie(Object object) {
        refWatcher.watch(object);
    }


    private static void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build());
    }
}
