package com.ce.game.realdemoapplication.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.ViewGroup;

import com.ce.game.realdemoapplication.R;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BTaskActivity extends BaseActivity {
    Future mFuture;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        final ViewGroup parent = (ViewGroup) findViewById(R.id.parent);
        mFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
//                parent.setVisibility(View.GONE);
//                System.out.println(TAG + TAG + "  running" + parent);
//                System.out.println(TAG + TAG + "  running" + parent.getContext());
            }
        }, 0, 1, TimeUnit.SECONDS);

        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                System.out.println(TAG + TAG + "  running" + parent);
//                mHandler.postDelayed(this, 0);
            }
        }, 200);
        startActivity(new Intent(this, GuideActivity.class));
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    void startAsyncTask() {
        // This async task is an anonymous class and therefore has a hidden reference to the outer
        // class MainActivity. If the activity gets destroyed before the task finishes (e.g. rotation),
        // the activity instance will leak.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Do some slow work in background
                SystemClock.sleep(10000);
                return null;
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFuture.cancel(true);
        mHandler.removeCallbacksAndMessages(null);
    }

}
