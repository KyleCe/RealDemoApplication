package com.ce.game.realdemoapplication.test;

import android.app.Activity;
import android.os.Bundle;

import com.ce.game.realdemoapplication.MyApplication;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Kyle on 17/07/2017
 */

public abstract class BaseActivity extends Activity {
    public final String TAG;
    protected MyApplication mApp;
    protected ScheduledExecutorService mExecutor;

    public BaseActivity() {
        TAG = this.getClass().getSimpleName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (MyApplication) getApplication();
        mExecutor = mApp.getmExecutor();
    }
}
